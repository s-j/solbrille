package com.ntnu.solbrille.index;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.utils.Pair;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation of a in memory key value index. Based on the concurrent hash map from the JDK.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class BasicKeyValueIndex<K extends IndexKeyEntry<K>, V extends IndexEntry>
        extends ConcurrentHashMap<K, V>
        implements KeyValueIndex<K, V> {

    private final IndexEntry.IndexEntryDescriptor<K> keyDescriptor;
    private final IndexEntry.IndexEntryDescriptor<V> valueDescriptor;

    public BasicKeyValueIndex(IndexEntry.IndexEntryDescriptor<K> keyDescriptor, IndexEntry.IndexEntryDescriptor<V> valueDescriptor) {
        this.keyDescriptor = keyDescriptor;
        this.valueDescriptor = valueDescriptor;
    }

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        clear();
        MapIndexHelper.initializeFromFile(this, keyDescriptor, valueDescriptor, bufferPool, fileNumber, blockOffset);
    }

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        clear();
        MapIndexHelper.initializeFromFile(this, keyDescriptor, valueDescriptor, bufferPool, fileNumber, blockOffset, byteOffset);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        MapIndexHelper.dumpToFile(this, bufferPool, fileNumber, blockOffset);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        MapIndexHelper.dumpToFile(this, bufferPool, fileNumber, blockOffset, byteOffset);
    }

    public Pair<Long, Integer> getOnDiskSize() {
        return null;
    }


}
