package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.BasicNavigableKeyValueIndex;
import com.ntnu.solbrille.index.NavigableKeyValueIndex;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;
import com.ntnu.solbrille.utils.Closeable;
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
public class OccurenceIndex extends AbstractLifecycleComponent {

    private final AtomicInteger indexPhase = new AtomicInteger();

    private final NavigableKeyValueIndex<DictionaryTerm, DictionaryEntry> dictionary
            = new BasicNavigableKeyValueIndex<DictionaryTerm, DictionaryEntry>(
            new DictionaryTerm.DictionaryTermDescriptor(),
            new DictionaryEntry.DictionaryEntryDescriptor());

    private final DiskInvertedList oddInvertedList = new DiskInvertedList();
    private final DiskInvertedList evenInvertedList = new DiskInvertedList();

    private final BufferPool bufferPool;
    private final int dictionaryFileNumber, oddInvertedListFileNumber, evenInvertedListFileNumber;

    public OccurenceIndex(
            BufferPool bufferPool, int dictionaryFileNumber, int oddInvertedListFileNumber, int evenInvertedListFileNumber) {
        this.bufferPool = bufferPool;
        this.dictionaryFileNumber = dictionaryFileNumber;
        this.oddInvertedListFileNumber = oddInvertedListFileNumber;
        this.evenInvertedListFileNumber = evenInvertedListFileNumber;
    }

    public void start() {
        try {
            System.out.println("Read dict!");
            dictionary.initializeFromFile(bufferPool, dictionaryFileNumber, 0);
            System.out.println("Dict done!");
            evenInvertedList.initializeFromFile(bufferPool, evenInvertedListFileNumber, 0);
            oddInvertedList.initializeFromFile(bufferPool, oddInvertedListFileNumber, 0);
            indexPhase.set(Math.max(evenInvertedList.getIndexPhase(), oddInvertedList.getIndexPhase()));
            System.out.println("Started with index phase: " + indexPhase.get());
            setIsRunning(true);
        } catch (IOException e) {
            setFailCause(e);
        } catch (InterruptedException e) {
            setFailCause(e);
        }
    }

    public void stop() {
        try {
            System.out.println("Write dict!");
            dictionary.writeToFile(bufferPool, dictionaryFileNumber, 0);
            System.out.println("Dict write done!");
            evenInvertedList.writeToFile(bufferPool, evenInvertedListFileNumber, 0);
            oddInvertedList.writeToFile(bufferPool, oddInvertedListFileNumber, 0);
            setIsRunning(true);
        } catch (IOException e) {
            setFailCause(e);
        } catch (InterruptedException e) {
            setFailCause(e);
        }
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
            Pair<Iterator<DocumentOccurence>, Long> lookup = getActiveList().lookupTerm(term, pointer);
            TermIterator occs = (TermIterator) lookup.getFirst();
            result = new LookupResult(occs.getNumberOfDocuments(), occs);
        }
        return result;
    }

    public long getDocumentCount(String term) throws IOException, InterruptedException {
        return getDocumentCount(new DictionaryTerm(term));
    }

    public long getDocumentCount(DictionaryTerm term) throws IOException, InterruptedException {
        LookupResult lr = lookup(term);
        long count = lr.getDocumentCount();
        lr.close();
        return count;
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
        if (updates instanceof Closeable) {
            ((Closeable) updates).close();
        }
        indexPhase.incrementAndGet();
        getActiveList().setIndexPhase(indexPhase.get());
        getInactiveList().setIndexPhase(indexPhase.get());

    }

    private DictionaryEntry buildDictionaryEntry(InvertedListPointer pointer) {
        return indexPhase.get() % 2 > 0 ? new DictionaryEntry(pointer, null) : new DictionaryEntry(null, pointer);
    }

    private void updateDictionaryEntry(DictionaryEntry entry, InvertedListPointer pointer) {
        if (indexPhase.get() % 2 > 0) {
            entry.setEvenPointer(pointer);
        } else {
            entry.setOddPointer(pointer);
        }
    }

    public Iterable<DictionaryTerm> getDictionaryTerms() {
        return dictionary.keySet();
    }
}
