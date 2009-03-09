package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.BasicNavigableKeyValueIndex;
import com.ntnu.solbrille.index.NavigableKeyValueIndex;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.iterators.CachedIterator;
import com.ntnu.solbrille.utils.iterators.CachedIteratorAdapter;
import com.ntnu.solbrille.utils.iterators.SkipAdaptor;
import com.ntnu.solbrille.utils.iterators.VoidIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class OccurenceIndex {

    private final AtomicInteger indexPhase = new AtomicInteger();

    private final NavigableKeyValueIndex<DictionaryTerm, DictionaryEntry> dictionary
            = new BasicNavigableKeyValueIndex<DictionaryTerm, DictionaryEntry>(
            new DictionaryTerm.DictionaryTermDescriptor(),
            new DictionaryEntry.DictionaryEntryDescriptor());

    private final DiskInvertedList oddInvertedList = new DiskInvertedList();
    private final DiskInvertedList evenInvertedList = new DiskInvertedList();

    private final int dictionaryFileNumber, oddInvertedListFileNumber, evenInvertedListFileNumber;

    public OccurenceIndex(
            BufferPool bufferPool, int dictionaryFileNumber,
            int oddInvertedListFileNumber, int evenInvertedListFileNumber)
            throws IOException, InterruptedException {

        this.dictionaryFileNumber = dictionaryFileNumber;
        this.oddInvertedListFileNumber = oddInvertedListFileNumber;
        this.evenInvertedListFileNumber = evenInvertedListFileNumber;

        dictionary.initializeFromFile(bufferPool, dictionaryFileNumber, 0);
        evenInvertedList.initializeFromFile(bufferPool, evenInvertedListFileNumber, 0);
        oddInvertedList.initializeFromFile(bufferPool, oddInvertedListFileNumber, 0);
    }

    public LookupResult lookup(String term) throws IOException, InterruptedException {
        return lookup(new DictionaryTerm(term));
    }

    public LookupResult lookup(DictionaryTerm term) throws IOException, InterruptedException {
        InvertedListPointer pointer = dictionaryLookup(term);
        LookupResult result;
        if (pointer == null) {
            result = new LookupResult(0, new SkipAdaptor<DocumentOccurence>(new VoidIterator<DocumentOccurence>()));
        } else {
            TermIterator occs = getActiveList().lookupTerm(term, pointer);
            result = new LookupResult(occs.getNumberOfDocuments(), occs);
        }
        return result;
    }

    public long getDictionaryTermCount() {
        return dictionary.size();
    }

    private InvertedListPointer dictionaryLookup(DictionaryTerm term) {
        DictionaryEntry entry = dictionary.get(term);
        if (entry == null) {
            return null;
        }
        return indexPhase.get() % 2 > 0 ? entry.getOddPointer() : entry.getEvenPointer();
    }

    // package private methods used while updating the index.

    DiskInvertedList getActiveList() {
        if (indexPhase.get() % 2 > 0) {
            return oddInvertedList;
        } else {
            return evenInvertedList;
        }
    }

    DiskInvertedList getInactiveList() {
        if (indexPhase.get() % 2 > 0) {
            return evenInvertedList;
        } else {
            return oddInvertedList;
        }
    }

    /**
     * Merges the new pointers into the dictionary. Probably faster than updating
     * one term at the time.
     * <p/>
     * Maks the new index active when the merge is done.
     *
     * @param newPointers Iterator with the new inverted list pointers.
     */
    void updateDictionaryEntries(Iterator<Pair<DictionaryTerm, InvertedListPointer>> newPointers) {
        CachedIterator<Map.Entry<DictionaryTerm, DictionaryEntry>> entries
                = new CachedIteratorAdapter<Map.Entry<DictionaryTerm, DictionaryEntry>>(dictionary.entrySet().iterator());
        CachedIterator<Pair<DictionaryTerm, InvertedListPointer>> updates
                = new CachedIteratorAdapter<Pair<DictionaryTerm, InvertedListPointer>>(newPointers);
        while (entries.hasNext() && updates.hasNext()) {
            entries.next();
            DictionaryTerm updateTerm = updates.next().getFirst();
            while (entries.getCurrent().getKey().compareTo(updateTerm) < 0 && entries.hasNext()) {
                entries.next();
            }
            if (entries.getCurrent().getKey().equals(updateTerm)) { // do update
                updateDictionaryEntry(entries.getCurrent().getValue(), updates.getCurrent().getSecond());
            } else if (entries.getCurrent().getKey().compareTo(updateTerm) > 0) { // insert
                dictionary.put(updateTerm, buildDictionaryEntry(updates.getCurrent().getSecond()));
            }
        }
        while (updates.hasNext()) {
            DictionaryTerm term = updates.next().getFirst();
            dictionary.put(term, buildDictionaryEntry(updates.getCurrent().getSecond()));
        }

        indexPhase.incrementAndGet(); // next index phase
    }

    private DictionaryEntry buildDictionaryEntry(InvertedListPointer pointer) {
        if (indexPhase.get() % 2 > 0) {
            return new DictionaryEntry(pointer, null);
        } else {
            return new DictionaryEntry(null, pointer);
        }
    }

    private void updateDictionaryEntry(DictionaryEntry entry, InvertedListPointer pointer) {
        if (indexPhase.get() % 2 > 0) {
            entry.setEvenPointer(pointer);
        } else {
            entry.setOddPointer(pointer);
        }
    }
}
