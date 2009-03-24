package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.index.document.DocumentIndexBuilder;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;
import com.ntnu.solbrille.utils.IntArray;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.iterators.AbstractWrappingIterator;
import com.ntnu.solbrille.utils.iterators.AnnotatingIterator;
import com.ntnu.solbrille.utils.iterators.DuplicateCollectingIterator;
import com.ntnu.solbrille.utils.iterators.IteratorMerger;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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
     * @param uri
     * @param documentId The document id to be added
     * @param document   The document content, as a map of terms, with a list of positions in the document
     * @throws IOException          On IO error
     * @throws InterruptedException If submitting thread were interupted.
     */
    public void addDocument(long documentId, URI uri, long documentLength, Map<String, ? extends List<Integer>> document) throws IOException, InterruptedException {
        Map<DictionaryTerm, DocumentOccurence> invertedDocument = createInvertedDocument(documentId, document);
        synchronized (mutex) {
            IndexPhaseState state = activeIndexPhase.get();
            state.size++;
            Map<DictionaryTerm, List<DocumentOccurence>> stateMap = state.aggregateState;
            for (Map.Entry<DictionaryTerm, DocumentOccurence> entry : invertedDocument.entrySet()) {
                List<DocumentOccurence> occurences = stateMap.get(entry.getKey());
                if (occurences != null) {
                    occurences.add(entry.getValue()); // always ascending document id's
                } else {
                    List<DocumentOccurence> occList = new ArrayList<DocumentOccurence>(1);
                    occList.add(entry.getValue());
                    stateMap.put(entry.getKey(), occList);
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
                new InvertedDocumentInfo(documentLength, mostFrequentTerm, totalTokens, invertedDocument.size()));
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
        long newTotalDocuments = documentIndexBuilder.getTotalNumberOfDocuments() + state.size;
        int position = 0;
        InvertedList[] phaseLists = new InvertedList[2 + state.partialListsOnDisk.size()];
        phaseLists[position++] = activeIndex;
        phaseLists[position++] = new MemoryInvertedList(state.aggregateState);
        for (InvertedList disk : state.partialListsOnDisk) {
            phaseLists[position++] = disk;
        }
        Map<Long, Float> tfIdfAccumulator = new HashMap<Long, Float>();
        Iterator<Pair<DictionaryTerm, InvertedListPointer>> result
                = mergeInvertedLists(inactiveIndex.getOverwriteBuilder(), tfIdfAccumulator, newTotalDocuments, phaseLists);
        index.updateDictionaryEntries(result);
        documentIndexBuilder.updateIndex(tfIdfAccumulator);
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

        public Pair<Iterator<DocumentOccurence>, Long> lookupTerm(DictionaryTerm term, InvertedListPointer pointer) throws IOException, InterruptedException {
            List<DocumentOccurence> list = invertedList.get(term);
            return new Pair<Iterator<DocumentOccurence>, Long>(list.iterator(), Long.valueOf(list.size()));
        }
    }

    private static final class IndexPhaseState {
        private long size = 0;
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
            long totalDocuments,
            Map<Long, Float> tfIdfAccumulator,
            Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>... sources)
            throws IOException, InterruptedException {
        Iterator<DocumentOccurence>[] documentIterators = new Iterator[sources.length];
        int position = 0;
        long termDocs = 0;
        for (Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList> source : sources) {
            Pair<Iterator<DocumentOccurence>, Long> result = source.getSecond().lookupTerm(source.getFirst().getFirst(), source.getFirst().getSecond());
            termDocs += result.getSecond();
            documentIterators[position++] = result.getFirst();
        }

        float idf = (float) totalDocuments / (float) termDocs;

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
            long termFrequency = 0;
            while (posList.hasNext()) {
                output.nextOccurence(posList.next());
                termFrequency++;
            }
            Float oldWeight = tfIdfAccumulator.get(docId);
            tfIdfAccumulator.put(docId, (oldWeight == null ? 0 : oldWeight) + (float) Math.pow(idf * termFrequency, 2.0));
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
        private final long newTotalDocuments;
        private final Map<Long, Float> tfIdfWeightAccumulator;

        private LazyMergedInvertedListWriterIterator(
                DuplicateCollectingIterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> wrapped,
                InvertedListBuilder output, long newTotalDocuments,
                Map<Long, Float> tfIdfWeightAccumulator) {
            super(wrapped);
            this.output = output;
            this.newTotalDocuments = newTotalDocuments;
            this.tfIdfWeightAccumulator = tfIdfWeightAccumulator;
        }

        public boolean hasNext() {
            return getWrapped().hasNext();
        }

        public Pair<DictionaryTerm, InvertedListPointer> next() {
            Collection<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> terms = getWrapped().next();
            DictionaryTerm term = terms.iterator().next().getFirst().getFirst(); // always at least one term
            try {
                InvertedListPointer pointer = writeToInvertedList(term, output, newTotalDocuments, tfIdfWeightAccumulator, terms.toArray(new Pair[terms.size()]));
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

    private Iterator<Pair<DictionaryTerm, InvertedListPointer>> mergeInvertedLists(
            InvertedListBuilder output,
            Map<Long, Float> tdIdfWeightAccumulator,
            long newTotalDocuments, InvertedList... inputs) throws IOException, InterruptedException {
        AnnotatingIterator<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>[] listIterators = new AnnotatingIterator[inputs.length];
        int position = 0;
        for (InvertedList list : inputs) {
            listIterators[position++] = new AnnotatingIterator<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>(list.getTermIterator(), list);
        }
        IteratorMerger<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> mergedTermIterators
                = new IteratorMerger<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>(TERM_MERGE_COMP, listIterators);
        DuplicateCollectingIterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> collectedTerms
                = new DuplicateCollectingIterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>(TERM_MERGE_COMP, mergedTermIterators);
        return new LazyMergedInvertedListWriterIterator(collectedTerms, output, newTotalDocuments, tdIdfWeightAccumulator);
    }

    private final AtomicReference<IndexPhaseState> activeIndexPhase
            = new AtomicReference<IndexPhaseState>(new IndexPhaseState());

    private boolean shouldUpdateIndex() {
        return false;
    }


    /**
     * Helper function to add a document as a String
     */
    public void addDocument(long documentId, URI uri, String document) throws IOException, InterruptedException {
        addDocument(documentId, uri, document.length(), invertDocument(document));

    }

    private Map<DictionaryTerm, DocumentOccurence> createInvertedDocument(long documentId, Map<String, ? extends List<Integer>> document) {
        Map<DictionaryTerm, DocumentOccurence> retMap = new HashMap<DictionaryTerm, DocumentOccurence>(document.size());
        for (Map.Entry<String, ? extends List<Integer>> entry : document.entrySet()) {
            DictionaryTerm term = new DictionaryTerm(entry.getKey());
            DocumentOccurence occ = new DocumentOccurence(documentId, entry.getValue());
            retMap.put(term, occ);
        }
        return retMap;
    }

    public static Map<String, List<Integer>> invertDocument(String document) {
        StringTokenizer st = new StringTokenizer(document);
        Map<String, List<Integer>> docMap = new HashMap<String, List<Integer>>();
        int pos = 0;
        while (st.hasMoreTokens()) {
            String term = st.nextToken();
            if (!docMap.containsKey(term)) {
                IntArray array = new IntArray(1);
                array.add(pos);
                docMap.put(term, array);
            } else {
                docMap.get(term).add(pos);
            }
            pos++;
        }
        return docMap;

    }


    // TODO: implement disk flushing and merging
    private void flushToDisk() {
    }

    private boolean shouldFlushToDisk() { //
        return false;
    }


}
