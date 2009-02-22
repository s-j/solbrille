package com.ntnu.solbrille.index;

import com.ntnu.solbrille.buffering.BufferPool;

import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public interface KeyValueIndex<K extends IndexKeyEntry<K>, V extends IndexEntry> extends Map<K, V> {
    /**
     * Initialize the index from a file. This may for in-memory structures be to read the content of the
     * file into memory, or for disk based indexes to establish a point of refference in the underlying
     * file.
     * <p/>
     * The format file recieved of this method should be compatible with the data dumped by the
     * <c>dumpToFile</c> methods.
     *
     * @param bufferPool  The buffer pool managing the file.
     * @param fileNumber  The file number.
     * @param blockOffset The block offset into the file where the index is stored.
     * @throws IOException          On IO related errors.
     * @throws InterruptedException If the current thread is interupted.
     */
    void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset)
            throws IOException, InterruptedException;

    /**
     * Overload of the <c>initializeFromFile</c> method with byte offset into the first block of the file.
     *
     * @param bufferPool  The buffer pool managing the file.
     * @param fileNumber  The file number.
     * @param blockOffset The block offset into the file where the index is stored.
     * @param byteOffset  The number of bytes into the first block of the file the index starts.
     * @throws IOException          On IO related errors.
     * @throws InterruptedException If the current thread is interupted.
     */
    void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset)
            throws IOException, InterruptedException;

    /**
     * Writes this index structure to file, for in-memory indexes this will be to dump the content
     * of the index into the file. For disk based indexes it will suffice to make sure that all data
     * in the index is reflected on disk.
     *
     * @param bufferPool  The buffer pool managing the file.
     * @param fileNumber  The file number.
     * @param blockOffset The block offset into the file where the index should be stored.
     * @throws IOException          On IO related errors.
     * @throws InterruptedException If the current thread is interupted.
     */
    void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset)
            throws IOException, InterruptedException;

    /**
     * Overload of the <c>dumpToFile</c> method with byte offset into the first block of the file.
     *
     * @param bufferPool  The buffer pool managing the file.
     * @param fileNumber  The file number.
     * @param blockOffset The block offset into the file where the index should be stored.
     * @param byteOffset  The number of bytes into the first block of the file the index starts.
     * @throws IOException          On IO related errors.
     * @throws InterruptedException If the current thread is interupted.
     */
    void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset)
            throws IOException, InterruptedException;
}
