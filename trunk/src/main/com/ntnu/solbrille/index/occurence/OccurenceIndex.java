package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.BasicNavigableKeyValueIndex;
import com.ntnu.solbrille.index.NavigableKeyValueIndex;

import java.io.IOException;
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

    private final InvertedList oddInvertedList = new InvertedList();
    private final InvertedList evenInvertedList = new InvertedList();

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

    InvertedList getActiveList() {
        if (indexPhase.get() % 2 > 0) {
            return oddInvertedList;
        } else {
            return evenInvertedList;
        }
    }

    InvertedList getInactiveList() {
        if (indexPhase.get() % 2 > 0) {
            return evenInvertedList;
        } else {
            return oddInvertedList;
        }
    }

    /**
     * Registers a dictionary entry. This must be done when the inverted list of the term has been built.
     * This way each dictionary entry is update once per indexing phase.
     *
     * @param term  The term to be updated/created.
     * @param entry The new entry.
     * @return True if the term is a new one. False if a old entry were updated.
     */
    public boolean registerDictionaryEntry(DictionaryTerm term, DictionaryEntry entry) {
        DictionaryEntry oldEntry = dictionary.get(term);
        if (oldEntry != null) {
            if (entry.getEvenPointer() == null) { // next phase is odd.
                oldEntry.setOddPointer(entry.getOddPointer());
            } else { // next phase is even
                oldEntry.setEvenPointer(entry.getEvenPointer());
            }
        } else {
            dictionary.put(term, entry);
            return true;
        }
        return false;
    }
}
