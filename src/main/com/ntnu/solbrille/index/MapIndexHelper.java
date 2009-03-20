package com.ntnu.solbrille.index;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;
import com.ntnu.solbrille.utils.Pair;

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
public class MapIndexHelper {

    public static <K extends IndexKeyEntry, V extends IndexEntry> Pair<Long, Integer> initializeFromFile(
            Map<K, V> index,
            IndexEntry.IndexEntryDescriptor<K> keyDescriptor,
            IndexEntry.IndexEntryDescriptor<V> valueDescriptor,
            BufferPool bufferPool,
            int fileNumber,
            long blockOffset) throws IOException, InterruptedException {
        return initializeFromFile(index, keyDescriptor, valueDescriptor, bufferPool, fileNumber, blockOffset, 0);
    }

    public static <K extends IndexKeyEntry, V extends IndexEntry> Pair<Long, Integer> initializeFromFile(
            Map<K, V> index,
            IndexEntry.IndexEntryDescriptor<K> keyDescriptor,
            IndexEntry.IndexEntryDescriptor<V> valueDescriptor,
            BufferPool bufferPool,
            int fileNumber,
            long blockOffset,
            int byteOffset)
            throws IOException, InterruptedException {

        Buffer buffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        assert buffer.getByteBuffer().capacity() > byteOffset + Constants.INT_SIZE;
        buffer.getByteBuffer().position(byteOffset);
        buffer.getReadLock().lock();
        ByteBuffer byteBuffer;
        try {
            byteBuffer = buffer.getByteBuffer();
            int remaining = byteBuffer.getInt();
            while (remaining > 0) {
                int blockEntries = byteBuffer.getInt();
                for (int i = 0; i < blockEntries; i++) {
                    K key = keyDescriptor.readIndexEntry(byteBuffer);
                    V value = valueDescriptor.readIndexEntry(byteBuffer);
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
        FileBlockPointer lastBuffer = buffer.getBlockPointer();
        bufferPool.unPinBuffer(buffer);
        return new Pair<Long, Integer>(lastBuffer.getBlockNumber(), byteBuffer.position());
    }

    public static <K extends IndexKeyEntry, V extends IndexEntry> Pair<Long, Integer> dumpToFile(
            Map<K, V> index,
            BufferPool bufferPool,
            int fileNumber,
            long blockOffset) throws IOException, InterruptedException {
        return dumpToFile(index, bufferPool, fileNumber, blockOffset, 0);
    }

    public static <K extends IndexKeyEntry, V extends IndexEntry> Pair<Long, Integer> dumpToFile(
            Map<K, V> index,
            BufferPool bufferPool,
            int fileNumber,
            long blockOffset,
            int byteOffset) throws IOException, InterruptedException {
        Buffer firstBuffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        assert firstBuffer.getByteBuffer().capacity() > byteOffset + Constants.INT_SIZE;
        firstBuffer.getByteBuffer().position(byteOffset);
        firstBuffer.getWriteLock().lock();
        firstBuffer.setIsDirty(true);
        int lastByteOffset = byteOffset;
        long currentBlock;
        try {
            ByteBuffer byteBuffer = firstBuffer.getByteBuffer();
            firstBuffer.setIsDirty(true);
            byteBuffer.putInt(0); // placeholder for afterwards when we know how big the file is.
            Iterator<Map.Entry<K, V>> entryIterator = index.entrySet().iterator();
            currentBlock = blockOffset;
            Buffer buffer = bufferPool.pinBuffer(firstBuffer.getBlockPointer());
            buffer.getWriteLock().lock();
            int written = 0;
            if (entryIterator.hasNext()) {
                Map.Entry<K, V> entry = entryIterator.next();
                int size = entry.getKey().getSeralizedLength() + entry.getValue().getSeralizedLength();
                while (entry != null) { // need to write a new block
                    buffer.setIsDirty(true);
                    int blockStartPosition = byteBuffer.position();
                    byteBuffer.putInt(0); // place holder for block count.
                    int remainingCapacity = byteBuffer.remaining();
                    int blockCount = 0;
                    while (entry != null && size <= remainingCapacity) { // write records to block while remaining
                        entry.getKey().serializeToByteBuffer(byteBuffer);
                        entry.getValue().serializeToByteBuffer(byteBuffer);
                        remainingCapacity -= size;
                        written++;
                        blockCount++;
                        if (entryIterator.hasNext()) {
                            entry = entryIterator.next();
                            size = entry.getKey().getSeralizedLength() + entry.getValue().getSeralizedLength();
                        } else {
                            entry = null;
                        }
                    }
                    // write number of items in block.
                    lastByteOffset = byteBuffer.position();
                    byteBuffer.position(blockStartPosition);
                    byteBuffer.putInt(blockCount);
                    if (entry != null) { // do we need a new block
                        buffer.getWriteLock().unlock();
                        bufferPool.unPinBuffer(buffer);
                        buffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, ++currentBlock));
                        buffer.getWriteLock().lock();
                        byteBuffer = buffer.getByteBuffer();
                    }
                }
            }
            buffer.getWriteLock().unlock();
            bufferPool.unPinBuffer(buffer);
            ByteBuffer firstByteBuffer = firstBuffer.getByteBuffer();
            firstByteBuffer.position(byteOffset);
            firstByteBuffer.putInt(written);
        }
        finally {
            firstBuffer.getWriteLock().unlock();
        }
        bufferPool.unPinBuffer(firstBuffer);
        return new Pair<Long, Integer>(currentBlock, lastByteOffset);
    }
}
