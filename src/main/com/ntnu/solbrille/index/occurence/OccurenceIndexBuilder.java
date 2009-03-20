package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.index.document.DocumentIndexBuilder;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.iterators.AbstractWrappingIterator;
import com.ntnu.solbrille.utils.iterators.AnnotatingIterator;
import com.ntnu.solbrille.utils.iterators.DuplicateCollectingIterator;
import com.ntnu.solbrille.utils.iterators.IteratorMerger;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class OccurenceIndexBuilder extends AbstractLifecycleComponent {

    private static final class OccurenceIndexBuilderMutex {
    }

    private final OccurenceIndexBuilderMutex mutex = new OccurenceIndexBuilderMutex();

    private final OccurenceIndex index;
    private final DocumentIndexBuilder documentIndexBuilder;

    public OccurenceIndexBuilder(OccurenceIndex index, DocumentIndexBuilder documentIndexBuilder) {
        this.index = index;
        this.documentIndexBuilder = documentIndexBuilder;
    }

    /**
     * Feeds a document to the indexer. The document fed to this method might not appear in the index before
     * the {@link #updateIndex()} is called.
     *
     * @param documentId The document id to be added
     * @param document   The document content
     * @throws IOException          On IO error
     * @throws InterruptedException If submitting thread were interupted.
     */
    public void addDocument(long documentId, URI uri, String document) throws IOException, InterruptedException {
        Map<DictionaryTerm, DocumentOccurence> invertedDocument = invertDocument(documentId, document);
        synchronized (mutex) {
            Map<DictionaryTerm, List<DocumentOccurence>> stateMap = activeIndexPhase.get().aggregateState;
            for (Map.Entry<DictionaryTerm, DocumentOccurence> entry : invertedDocument.entrySet()) {
                List<DocumentOccurence> occurences = stateMap.get(entry.getKey());
                if (occurences != null) {
                    occurences.add(entry.getValue()); // always ascending document id's
                } else {
                    stateMap.put(entry.getKey(), new ArrayList<DocumentOccurence>(Arrays.asList(entry.getValue())));
                }
            }
        }
        if (shouldFlushToDisk()) {
            flushToDisk();
        }
        if (shouldUpdateIndex()) {
            updateIndex();
        }

        long totalTokens = 0;
        Pair<DictionaryTerm, Long> mostFrequentTerm = null;
        for (Map.Entry<DictionaryTerm, DocumentOccurence> termOcc : invertedDocument.entrySet()) {
            totalTokens += termOcc.getValue().getPositionList().size();
            if (mostFrequentTerm == null || termOcc.getValue().getPositionList().size() > mostFrequentTerm.getSecond()) {
                mostFrequentTerm = new Pair<DictionaryTerm, Long>(termOcc.getKey(), Long.valueOf(termOcc.getValue().getPositionList().size()));
            }
        }

        documentIndexBuilder.addDocument(
                documentId,
                uri,
                new InvertedDocumentInfo(document.length(), mostFrequentTerm, totalTokens, invertedDocument.size()));
    }

    public void updateIndex() throws IOException, InterruptedException {
        IndexPhaseState state;
        DiskInvertedList activeIndex;
        DiskInvertedList inactiveIndex;
        synchronized (mutex) {
            state = activeIndexPhase.getAndSet(new IndexPhaseState());
            activeIndex = index.getActiveList();
            inactiveIndex = index.getInactiveList();
        }
        // TODO: support merge in multiple passes (or not?)
        int position = 0;
        InvertedList[] phaseLists = new InvertedList[2 + state.partialListsOnDisk.size()];
        phaseLists[position++] = activeIndex;
        phaseLists[position++] = new MemoryInvertedList(state.aggregateState);
        for (InvertedList disk : state.partialListsOnDisk) {
            phaseLists[position++] = disk;
        }
        Iterator<Pair<DictionaryTerm, InvertedListPointer>> result
                = mergeInvertedLists(inactiveIndex.getOverwriteBuilder(), phaseLists);
        index.updateDictionaryEntries(result);
        documentIndexBuilder.updateIndex();
    }

    @Override
    public void start() {
        setIsRunning(true);
    }

    @Override
    public void stop() {
        setIsRunning(false);
    }

    private static class MemoryInvertedList implements InvertedList {
        private final NavigableMap<DictionaryTerm, List<DocumentOccurence>> invertedList;

        private MemoryInvertedList(NavigableMap<DictionaryTerm, List<DocumentOccurence>> invertedList) {
            this.invertedList = invertedList;
        }

        public Iterator<Pair<DictionaryTerm, InvertedListPointer>> getTermIterator() throws IOException, InterruptedException {
            return new AnnotatingIterator(invertedList.keySet().iterator(), new InvertedListPointer(0, 0));
        }

        public Iterator<DocumentOccurence> lookupTerm(DictionaryTerm term, InvertedListPointer pointer) throws IOException, InterruptedException {
            return invertedList.get(term).iterator();
        }
    }

    private static final class IndexPhaseState {
        private final NavigableMap<DictionaryTerm, List<DocumentOccurence>> aggregateState
                = new TreeMap<DictionaryTerm, List<DocumentOccurence>>();
        private final Collection<DiskInvertedList> partialListsOnDisk = new ArrayList<DiskInvertedList>();
    }


    private static final Comparator<DocumentOccurence> DOC_OCC_COMP = new Comparator<DocumentOccurence>() {
        public int compare(DocumentOccurence o1, DocumentOccurence o2) {
            return o1.getDocumentId() == o2.getDocumentId() ? 0 : 1; // only used for equality ok to break contract.
        }
    };


    private static InvertedListPointer writeToInvertedList(
            DictionaryTerm term,
            InvertedListBuilder output,
            Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>... sources)
            throws IOException, InterruptedException {
        Iterator<DocumentOccurence>[] documentIterators = new Iterator[sources.length];
        int position = 0;
        for (Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList> source : sources) {
            documentIterators[position++] = source.getSecond().lookupTerm(source.getFirst().getFirst(), source.getFirst().getSecond());
        }
        Iterator<DocumentOccurence> mergedDocuments = new IteratorMerger<DocumentOccurence>(documentIterators);
        Iterator<Collection<DocumentOccurence>> collectedDocuments = new DuplicateCollectingIterator<DocumentOccurence>(DOC_OCC_COMP, mergedDocuments);
        InvertedListPointer start = output.nextTerm(term);
        while (collectedDocuments.hasNext()) {
            position = 0;
            Collection<DocumentOccurence> docs = collectedDocuments.next();
            Iterator<Integer>[] positionLists = new Iterator[docs.size()];
            long docId = -1;
            for (DocumentOccurence docOcc : docs) {
                if (docId == -1) {
                    docId = docOcc.getDocumentId();
                } else {
                    assert docId == docOcc.getDocumentId();
                }
                positionLists[position++] = docOcc.getPositionList().iterator();
            }
            output.nextDocument(docId);
            Iterator<Integer> posList = new IteratorMerger<Integer>(positionLists);
            while (posList.hasNext()) {
                output.nextOccurence(posList.next());
            }
        }
        return start;
    }

    private static final Comparator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>
            TERM_MERGE_COMP = new Comparator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>() {
        public int compare(Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList> o1,
                           Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList> o2) {
            return o1.getFirst().getFirst().compareTo(o2.getFirst().getFirst());
        }
    };

    private static class LazyMergedInvertedListWriterIterator extends
            AbstractWrappingIterator<Pair<DictionaryTerm, InvertedListPointer>,
                    DuplicateCollectingIterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>> {
        private final InvertedListBuilder output;

        private LazyMergedInvertedListWriterIterator(
                DuplicateCollectingIterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> wrapped,
                InvertedListBuilder output) {
            super(wrapped);
            this.output = output;
        }

        public boolean hasNext() {
            return getWrapped().hasNext();
        }

        public Pair<DictionaryTerm, InvertedListPointer> next() {
            Collection<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> terms = getWrapped().next();
            DictionaryTerm term = terms.iterator().next().getFirst().getFirst(); // always at least one term
            try {
                InvertedListPointer pointer = writeToInvertedList(term, output, terms.toArray(new Pair[terms.size()]));
                return new Pair<DictionaryTerm, InvertedListPointer>(term, pointer);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            output.close();
            super.close();
        }
    }

    /**
     * Merges a number of inverted lists and puts the output in another. This method is lazy, teh reslting
     * iterator will run the computation when consumed.
     *
     * @param output The inverted lists to write the results to.
     * @param inputs The inverted lists to be merged.
     * @return A iterator of the dictionary terms and the location of the term in the <code>output</code>
     *         parameter. This iterator need to be consumed for teh file to be written.
     * @throws IOException          On error writing to output.
     * @throws InterruptedException If the calling thread is blocked.
     */
    private Iterator<Pair<DictionaryTerm, InvertedListPointer>> mergeInvertedLists(
            InvertedListBuilder output,
            InvertedList... inputs) throws IOException, InterruptedException {
        AnnotatingIterator<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>[] listIterators = new AnnotatingIterator[inputs.length];
        int position = 0;
        for (InvertedList list : inputs) {
            listIterators[position++] = new AnnotatingIterator<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>(list.getTermIterator(), list);
        }
        IteratorMerger<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> mergedTermIterators
                = new IteratorMerger<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>(TERM_MERGE_COMP, listIterators);
        DuplicateCollectingIterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> collectedTerms
                = new DuplicateCollectingIterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>(TERM_MERGE_COMP, mergedTermIterators);
        return new LazyMergedInvertedListWriterIterator(collectedTerms, output);
    }

    private final AtomicReference<IndexPhaseState> activeIndexPhase
            = new AtomicReference<IndexPhaseState>(new IndexPhaseState());

    private boolean shouldUpdateIndex() {
        return false;
    }

    // TODO: implement disk flushing and merging
    private void flushToDisk() {
    }

    private boolean shouldFlushToDisk() { //
        return false;
    }

    private static Map<DictionaryTerm, DocumentOccurence> invertDocument(long documentId, String content) {
        int position = 0;
        Map<DictionaryTerm, DocumentOccurence> invertedDocument = new HashMap<DictionaryTerm, DocumentOccurence>();
        StringTokenizer tokenizer = new StringTokenizer(content);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            DictionaryTerm term = new DictionaryTerm(token);
            DocumentOccurence occurence = invertedDocument.get(term);
            if (occurence != null) {
                occurence.addPosition(position);
            } else {
                occurence = new DocumentOccurence(documentId);
                occurence.addPosition(position);
                invertedDocument.put(term, occurence);
            }
            position++;
        }

        return invertedDocument;
    }
}
