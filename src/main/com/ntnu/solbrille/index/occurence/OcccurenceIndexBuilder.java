package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.iterators.AnnotatingIterator;
import com.ntnu.solbrille.utils.iterators.DuplicateCollectingIterator;
import com.ntnu.solbrille.utils.iterators.IteratorMerger;

import java.io.IOException;
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
public class OcccurenceIndexBuilder {

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
            Pair<Pair<DictionaryTerm, InvertedListPointer>, DiskInvertedList>... sources)
            throws IOException, InterruptedException {
        Iterator<DocumentOccurence>[] documentIterators = new Iterator[sources.length];
        int position = 0;
        for (Pair<Pair<DictionaryTerm, InvertedListPointer>, DiskInvertedList> source : sources) {
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

    private static Iterator<Pair<DictionaryTerm, InvertedListPointer>> mergeInvertedLists(
            final InvertedListBuilder output,
            InvertedList... inputs) throws IOException, InterruptedException {
        Iterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>[] listIterators = new Iterator[inputs.length];
        int position = 0;
        for (InvertedList list : inputs) {
            listIterators[position++] = new AnnotatingIterator<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>(list.getTermIterator(), list);
        }
        Iterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> mergedTermIterators
                = new IteratorMerger<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>(TERM_MERGE_COMP, listIterators);
        final Iterator<Collection<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>> collectedTerms
                = new DuplicateCollectingIterator<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>>(TERM_MERGE_COMP, mergedTermIterators);
        return new Iterator<Pair<DictionaryTerm, InvertedListPointer>>() {

            public boolean hasNext() {
                return collectedTerms.hasNext();
            }

            public Pair<DictionaryTerm, InvertedListPointer> next() {
                Collection<Pair<Pair<DictionaryTerm, InvertedListPointer>, InvertedList>> terms = collectedTerms.next();
                DictionaryTerm term = terms.iterator().next().getFirst().getFirst(); // always at least one term
                try {
                    InvertedListPointer pointer = writeToInvertedList(term, output,
                            (Pair<Pair<DictionaryTerm, InvertedListPointer>, DiskInvertedList>[]) terms.toArray(new Iterator[terms.size()]));
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
        };
    }

    private final AtomicReference<IndexPhaseState> activeIndexPhase
            = new AtomicReference<IndexPhaseState>(new IndexPhaseState());

    private final OccurenceIndex index;

    public OcccurenceIndexBuilder(OccurenceIndex index) {
        this.index = index;
    }

    private static class MemoryInvertedList implements InvertedList {
        private NavigableMap<DictionaryTerm, List<DocumentOccurence>> invertedList;

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

    private void updateIndex() throws IOException, InterruptedException {
        IndexPhaseState state = activeIndexPhase.getAndSet(new IndexPhaseState());
        DiskInvertedList activeIndex = index.getActiveList();
        DiskInvertedList inactiveIndex = index.getInactiveList();

        // TODO: support merge in multiple passes (or not?)
        int position = 0;
        InvertedList[] phaseLists = new DiskInvertedList[2 + state.partialListsOnDisk.size()];
        phaseLists[position++] = activeIndex;
        phaseLists[position++] = new MemoryInvertedList(state.aggregateState);
        Iterator<Pair<DictionaryTerm, InvertedListPointer>> result
                = mergeInvertedLists(inactiveIndex.getOverwriteBuilder(), phaseLists);
        // TODO: update pointers in dictionary

    }

    private boolean shouldUpdateIndex() {
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    // TODO: implement disk flushing and merging
    private void flushToDisk() {
    }

    private boolean shouldFlushToDisk() { //
        return false;
    }

    public void addDocument(long documentId, Struct document) throws IOException, InterruptedException {
        Map<DictionaryTerm, DocumentOccurence> invertedDocument
                = invertDocument(documentId, document.getField("content").getValue());
        Map<DictionaryTerm, List<DocumentOccurence>> stateMap = activeIndexPhase.get().aggregateState;
        for (Map.Entry<DictionaryTerm, DocumentOccurence> entry : invertedDocument.entrySet()) {
            List<DocumentOccurence> occurences = stateMap.get(entry.getKey());
            if (occurences != null) {
                occurences.add(entry.getValue()); // always ascending document id's
            } else {
                stateMap.put(entry.getKey(), new ArrayList<DocumentOccurence>(Arrays.asList(entry.getValue())));
            }
        }
        if (shouldFlushToDisk()) {
            flushToDisk();
        }
        if (shouldUpdateIndex()) {
            updateIndex();
        }
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
            position += token.length();
        }

        return invertedDocument;
    }
}
