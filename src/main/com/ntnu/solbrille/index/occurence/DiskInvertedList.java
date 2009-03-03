package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.Index;
import com.ntnu.solbrille.utils.Pair;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DiskInvertedList implements Index, InvertedList {

    private BufferPool bufferPool;
    private int fileNumber;
    private long blockOffset;

    private InvertedListReader reader;

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        initializeFromFile(bufferPool, fileNumber, blockOffset, 0);
    }

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        this.bufferPool = bufferPool;
        this.fileNumber = fileNumber;
        this.blockOffset = blockOffset;
        this.reader = new InvertedListReader(bufferPool, fileNumber, blockOffset);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        writeToFile(bufferPool, fileNumber, blockOffset, 0);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        //TODO: Write file to supplied file. 
    }

    public Iterator<DocumentOccurence> lookupTerm(DictionaryTerm term, InvertedListPointer pointer) throws IOException, InterruptedException {
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
        return new InvertedListBuilder(bufferPool, fileNumber, blockOffset);
    }
}
