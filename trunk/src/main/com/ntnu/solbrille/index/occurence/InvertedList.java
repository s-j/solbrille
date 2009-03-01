package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.Index;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class InvertedList implements Index {

    private class DocumentIterator implements Iterator<DocumentOccurence> {

        public boolean hasNext() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public DocumentOccurence next() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void remove() {
            throw new NotImplementedException();
        }
    }

    public Iterator<DocumentOccurence> getDocumentIterator(InvertedListPointer pointer) {

        return null;
    }

    private BufferPool bufferPool;
    private int fileNumber;
    private long blockOffset;
    private int byteOffset;


    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        initializeFromFile(bufferPool, fileNumber, blockOffset, 0);
    }

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        this.bufferPool = bufferPool;
        this.fileNumber = fileNumber;
        this.blockOffset = blockOffset;
        this.byteOffset = byteOffset;
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        writeToFile(bufferPool, fileNumber, blockOffset, 0);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        //TODO: Write file to supplied file. 
    }
}
