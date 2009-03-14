package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexEntry;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DictionaryEntry implements IndexEntry {

    private static final byte HAS_ODD = 0x1;
    private static final byte HAS_EVEN = 0x2;

    private static final IndexEntryDescriptor<InvertedListPointer> INV_LIST_PTR_DESC = new InvertedListPointer.InvertedListPointerDescriptor();

    public static class DictionaryEntryDescriptor implements IndexEntryDescriptor<DictionaryEntry> {

        public DictionaryEntry readIndexEntryDescriptor(ByteBuffer buffer) {
            byte flag = buffer.get();
            InvertedListPointer evenPointer = null;
            if ((flag & HAS_EVEN) > 0) {
                evenPointer = INV_LIST_PTR_DESC.readIndexEntryDescriptor(buffer);
            }
            InvertedListPointer oddPointer = null;
            if ((flag & HAS_EVEN) > 0) {
                oddPointer = INV_LIST_PTR_DESC.readIndexEntryDescriptor(buffer);
            }
            return new DictionaryEntry(evenPointer, oddPointer);
        }
    }

    // Double set of inverted list pointers for consistent index view.
    private InvertedListPointer evenPointer;
    private InvertedListPointer oddPointer;

    public DictionaryEntry(InvertedListPointer evenPointer, InvertedListPointer oddPointer) {
        this.evenPointer = evenPointer;
        this.oddPointer = oddPointer;
    }

    public InvertedListPointer getEvenPointer() {
        return evenPointer;
    }

    public InvertedListPointer getOddPointer() {
        return oddPointer;
    }

    public void setEvenPointer(InvertedListPointer evenPointer) {
        this.evenPointer = evenPointer;
    }

    public void setOddPointer(InvertedListPointer oddPointer) {
        this.oddPointer = oddPointer;
    }

    public int getSeralizedLength() {
        int size = Constants.BYTE_SIZE;
        if (evenPointer != null) {
            size += evenPointer.getSeralizedLength();
        }
        if (oddPointer != null) {
            size += oddPointer.getSeralizedLength();
        }
        return size;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        byte flag = 0;
        if (evenPointer != null) {
            flag |= HAS_EVEN;
        }
        if (oddPointer != null) {
            flag |= HAS_ODD;
        }
        buffer.put(flag);
        if ((flag & HAS_EVEN) > 0) {
            evenPointer.serializeToByteBuffer(buffer);
        }
        if ((flag & HAS_ODD) > 0) {
            oddPointer.serializeToByteBuffer(buffer);
        }
    }
}
