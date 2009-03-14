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
        try {
            indexPhase.set(metaBuffer.getByteBuffer().getInt());
        }
        finally {
            bufferPool.unPinBuffer(metaBuffer);
        }
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        writeToFile(bufferPool, fileNumber, blockOffset, 0);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        Buffer metaBuffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        try {
            int phase = indexPhase.get();
            metaBuffer.getByteBuffer().putInt(phase);
            metaBuffer.setIsDirty(true);
        }
        finally {
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

    public TermIterator lookupTerm(DictionaryTerm term, InvertedListPointer pointer) throws IOException, InterruptedException {
        return reader.iterateTerm(term, pointer);
    }

    public Iterator<Pair<DictionaryTerm, InvertedListPointer>> getTermIterator() throws IOException, InterruptedException {
        return reader.getFileIterator();
    }

    /**
     * Gets a inverted list builder which overwrites this file.
     *
     * @return Inverted list builder.
     */
    InvertedListBuilder getOverwriteBuilder() throws IOException, InterruptedException {
        return new InvertedListBuilder(bufferPool, fileNumber, blockOffset + 1);
    }
}
