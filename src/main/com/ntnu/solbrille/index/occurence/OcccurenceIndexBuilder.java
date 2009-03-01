package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.buffering.FileBlockPointer;
import com.ntnu.solbrille.feeder.Struct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
        public final NavigableMap<DictionaryTerm, List<DocumentOccurence>> aggregateState
                = new TreeMap<DictionaryTerm, List<DocumentOccurence>>();
        public final NavigableMap<DictionaryTerm, List<FileBlockPointer>> partialListsOnDisk
                = new TreeMap<DictionaryTerm, List<FileBlockPointer>>();
    }

    private class InvertedListMerger {

    }

    private final AtomicReference<IndexPhaseState> activeIndexPhase
            = new AtomicReference<IndexPhaseState>(new IndexPhaseState());

    private final OccurenceIndex index;

    public OcccurenceIndexBuilder(OccurenceIndex index) {
        this.index = index;
    }

    private void updateIndex() {
        IndexPhaseState state = activeIndexPhase.getAndSet(new IndexPhaseState());
        InvertedList activeIndex = index.getActiveList();
        InvertedList inactiveIndex = index.getInactiveList();


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

    public void addDocument(long documentId, Struct document) {
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
