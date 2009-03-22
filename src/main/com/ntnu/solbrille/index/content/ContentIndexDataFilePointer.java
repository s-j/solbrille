package com.ntnu.solbrille.index.content;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexKeyEntry;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class ContentIndexDataFilePointer implements IndexKeyEntry<ContentIndexDataFilePointer> {
    public static class ContentIndexDataFilePointerDescriptor implements IndexEntryDescriptor<ContentIndexDataFilePointer> {

        @Override
        public ContentIndexDataFilePointer readIndexEntry(ByteBuffer buffer) {
            return new ContentIndexDataFilePointer(buffer.getLong(), buffer.getInt());
        }

    }

    private long blockOffset;

    private int byteOffset;

    public ContentIndexDataFilePointer(long blockOffset, int byteOffset) {
        this.blockOffset = blockOffset;
        this.byteOffset = byteOffset;
    }

    public long getBlockOffset() {
        return blockOffset;
    }

    public void setBlockOffset(long blockOffset) {
        this.blockOffset = blockOffset;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public void setByteOffset(int byteOffset) {
        this.byteOffset = byteOffset;
    }

    @Override
    public int getSeralizedLength() {
        return Constants.LONG_SIZE + Constants.INT_SIZE;
    }

    @Override
    public void serializeToByteBuffer(ByteBuffer buffer) {
        buffer.putLong(blockOffset);
        buffer.putInt(byteOffset);
    }

    @Override
    public int compareTo(ContentIndexDataFilePointer o) {
        int cmp = Long.valueOf(blockOffset).compareTo(o.blockOffset);
        cmp = cmp == 0 ? Integer.valueOf(byteOffset).compareTo(o.byteOffset) : cmp;
        return cmp;
    }
}
