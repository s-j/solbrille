package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexEntry;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class InvertedListPointer implements IndexEntry {
    public static final class InvertedListPointerDescriptor implements IndexEntryDescriptor<InvertedListPointer> {

        public InvertedListPointer readIndexEntryDescriptor(ByteBuffer buffer) {
            return new InvertedListPointer(buffer.getLong(), buffer.getInt());
        }

    }

    private long blockOffset;
    private int byteOffset;

    public InvertedListPointer(long blockOffset, int byteOffset) {
        this.blockOffset = blockOffset;
        this.byteOffset = byteOffset;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public void setByteOffset(int byteOffset) {
        this.byteOffset = byteOffset;
    }

    public long getBlockOffset() {
        return blockOffset;
    }

    public void setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
    }

    public int getSeralizedLength() {
        return Constants.LONG_SIZE + Constants.INT_SIZE;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        buffer.putLong(blockOffset);
        buffer.putInt(byteOffset);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof InvertedListPointer) {
            InvertedListPointer other = (InvertedListPointer) o;
            return other.blockOffset == blockOffset && other.byteOffset == byteOffset;
        }
        return false;
    }

    @Override
    public String toString() {
        return blockOffset + ":" + byteOffset;
    }
}
