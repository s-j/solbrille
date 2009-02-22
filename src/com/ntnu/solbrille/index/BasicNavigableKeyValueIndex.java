package com.ntnu.solbrille.index;

import com.ntnu.solbrille.buffering.BufferPool;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Basic implementation of a in memory navigable key value index. Based on the concurrent skip list map from the JDK.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class BasicNavigableKeyValueIndex<K extends IndexKeyEntry<K>, V extends IndexEntry>
        extends ConcurrentSkipListMap<K, V>
        implements NavigableKeyValueIndex<K, V> {

    private final IndexEntry.IndexEntryDescriptor<K> keyDescriptor;
    private final IndexEntry.IndexEntryDescriptor<V> valueDescriptor;

    public BasicNavigableKeyValueIndex(IndexEntry.IndexEntryDescriptor<K> keyDescriptor, IndexEntry.IndexEntryDescriptor<V> valueDescriptor) {
        this.keyDescriptor = keyDescriptor;
        this.valueDescriptor = valueDescriptor;
    }

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        MapIndexHelper.initializeFromFile(this, keyDescriptor, valueDescriptor, bufferPool, fileNumber, blockOffset);
    }

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        MapIndexHelper.initializeFromFile(this, keyDescriptor, valueDescriptor, bufferPool, fileNumber, blockOffset, byteOffset);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        MapIndexHelper.dumpToFile(this, bufferPool, fileNumber, blockOffset);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        MapIndexHelper.dumpToFile(this, bufferPool, fileNumber, blockOffset, byteOffset);
    }
}
