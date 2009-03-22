package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;
import com.ntnu.solbrille.index.Index;
import com.ntnu.solbrille.utils.Pair;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DiskInvertedList implements Index, InvertedList {

    private BufferPool bufferPool;
    private int fileNumber;
    private long blockOffset;

    private InvertedListReader reader;

    private AtomicInteger indexPhase = new AtomicInteger(0);

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        initializeFromFile(bufferPool, fileNumber, blockOffset, 0);
    }

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        this.bufferPool = bufferPool;
        this.fileNumber = fileNumber;
        this.blockOffset = blockOffset;
        this.reader = new InvertedListReader(bufferPool, fileNumber, blockOffset + 1);
        Buffer metaBuffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        metaBuffer.getReadLock().lock();
        try {
            indexPhase.set(metaBuffer.getByteBuffer().getInt());
        }
        finally {
            metaBuffer.getReadLock().unlock();
            bufferPool.unPinBuffer(metaBuffer);
        }
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        writeToFile(bufferPool, fileNumber, blockOffset, 0);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        Buffer metaBuffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        metaBuffer.getWriteLock().lock();
        try {
            int phase = indexPhase.get();
            metaBuffer.getByteBuffer().putInt(phase);
            metaBuffer.setIsDirty(true);
        }
        finally {
            metaBuffer.getWriteLock().unlock();
            bufferPool.unPinBuffer(metaBuffer);
        }
    }

    public Pair<Long, Integer> getOnDiskSize() {
        return null;
    }

    int getIndexPhase() {
        return indexPhase.get();
    }

    void setIndexPhase(int indexPhase) {
        this.indexPhase.set(indexPhase);
    }

    @Override
    public Pair<Iterator<DocumentOccurence>, Long> lookupTerm(DictionaryTerm term, InvertedListPointer pointer) throws IOException, InterruptedException {
        TermIterator iter = reader.iterateTerm(term, pointer);
        return new Pair<Iterator<DocumentOccurence>, Long>(iter, iter.getNumberOfDocuments());
    }

    @Override
    public Iterator<Pair<DictionaryTerm, InvertedListPointer>> getTermIterator() throws IOException, InterruptedException {
        return reader.getFileIterator();
    }

    /**
     * Gets a inverted list builder which overwrites this file.
     *
     * @return Inverted list builder.
     * @throws java.io.IOException
     */
    InvertedListBuilder getOverwriteBuilder() throws IOException, InterruptedException {
        return new InvertedListBuilder(bufferPool, fileNumber, blockOffset + 1);
    }
}
