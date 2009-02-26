package com.ntnu.solbrille.index;

import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

/**
 * Helper class for serializaing in-memory indices backed by types of maps.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
class MapIndexHelper {

    private static final int INT_SIZE = 4;

    public static <K extends IndexKeyEntry, V extends IndexEntry> void initializeFromFile(
            Map<K, V> index,
            IndexEntry.IndexEntryDescriptor<K> keyDescriptor,
            IndexEntry.IndexEntryDescriptor<V> valueDescriptor,
            BufferPool bufferPool,
            int fileNumber,
            long blockOffset) throws IOException, InterruptedException {
        initializeFromFile(index, keyDescriptor, valueDescriptor, bufferPool, fileNumber, blockOffset, 0);
    }

    public static <K extends IndexKeyEntry, V extends IndexEntry> void initializeFromFile(
            Map<K, V> index,
            IndexEntry.IndexEntryDescriptor<K> keyDescriptor,
            IndexEntry.IndexEntryDescriptor<V> valueDescriptor,
            BufferPool bufferPool,
            int fileNumber,
            long blockOffset,
            int byteOffset)
            throws IOException, InterruptedException {

        Buffer buffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        assert buffer.getByteBuffer().capacity() > byteOffset + INT_SIZE;
        buffer.getByteBuffer().position(byteOffset);
        buffer.getReadLock().lock();
        try {
            ByteBuffer byteBuffer = buffer.getByteBuffer();
            int remaining = byteBuffer.getInt();
            while (remaining > 0) {
                int blockEntries = byteBuffer.getInt();
                for (int i = 0; i < blockEntries; i++) {
                    K key = keyDescriptor.readIndexEntryDescriptor(byteBuffer);
                    V value = valueDescriptor.readIndexEntryDescriptor(byteBuffer);
                    index.put(key, value);
                }
                remaining -= blockEntries;
                if (remaining > 0) { // unlock block, and load new one.
                    buffer.getReadLock().unlock();
                    bufferPool.unPinBuffer(buffer);
                    buffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, ++blockOffset));
                    buffer.getReadLock().lock();
                    byteBuffer = buffer.getByteBuffer();
                }
            }
        }
        finally {
            buffer.getReadLock().unlock();
        }
        bufferPool.unPinBuffer(buffer);
    }

    public static <K extends IndexKeyEntry, V extends IndexEntry> void dumpToFile(
            Map<K, V> index,
            BufferPool bufferPool,
            int fileNumber,
            long blockOffset) throws IOException, InterruptedException {
        dumpToFile(index, bufferPool, fileNumber, blockOffset, 0);
    }

    public static <K extends IndexKeyEntry, V extends IndexEntry> void dumpToFile(
            Map<K, V> index,
            BufferPool bufferPool,
            int fileNumber,
            long blockOffset,
            int byteOffset) throws IOException, InterruptedException {
        Buffer firstBuffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        assert firstBuffer.getByteBuffer().capacity() > byteOffset + INT_SIZE;
        firstBuffer.getByteBuffer().position(byteOffset);
        firstBuffer.getWriteLock().lock();
        try {
            ByteBuffer byteBuffer = firstBuffer.getByteBuffer();
            byteBuffer.putInt(0); // placeholder for afterwards when we know how big the file is.
            Iterator<Map.Entry<K, V>> entryIterator = index.entrySet().iterator();
            long currentBlock = blockOffset;
            Buffer buffer = bufferPool.pinBuffer(firstBuffer.getBlockPointer());
            int written = 0;
            if (entryIterator.hasNext()) {
                Map.Entry<K, V> entry = entryIterator.next();
                int size = entry.getKey().getSeralizedLength() + entry.getValue().getSeralizedLength();
                int remainingCapacity = byteBuffer.remaining();
                while (entry != null) { // need to write a new block
                    buffer.getWriteLock().lock();
                    try {
                        int blockStartPosition = byteBuffer.position();
                        byteBuffer.putInt(0); // place holder for block count.
                        int blockCount = 0;
                        while (entry != null && size < remainingCapacity) { // write records to block while remaining
                            entry.getKey().serializeToByteBuffer(byteBuffer);
                            entry.getValue().serializeToByteBuffer(byteBuffer);
                            remainingCapacity -= size;
                            blockCount++;
                            if (entryIterator.hasNext()) {
                                entry = entryIterator.next();
                                written++;
                                size = entry.getKey().getSeralizedLength() + entry.getValue().getSeralizedLength();
                            } else {
                                entry = null;
                            }
                        }
                        // write number of items in block. 
                        byteBuffer.position(blockStartPosition);
                        byteBuffer.putInt(blockCount);
                        if (entry != null) { // do we need a new block
                            bufferPool.unPinBuffer(buffer);
                            buffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, ++currentBlock));
                        }
                    }
                    finally {
                        buffer.getWriteLock().unlock();
                    }

                }
            }

            ByteBuffer firstByteBuffer = firstBuffer.getByteBuffer();
            firstByteBuffer.position(byteOffset);
            firstByteBuffer.putInt(written);
        }
        finally {
            firstBuffer.getWriteLock().unlock();
        }
        bufferPool.unPinBuffer(firstBuffer);
    }
}
