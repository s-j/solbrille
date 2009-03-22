package com.ntnu.solbrille.index.content;

import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;
import com.ntnu.solbrille.index.Index;
import com.ntnu.solbrille.utils.Closeable;
import com.ntnu.solbrille.utils.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class ContentIndexDataFile implements Index, Closeable {

    private static final class ContentIndexDataFileMutex {
    }

    private final Object mutex = new ContentIndexDataFileMutex();

    private static final ContentIndexDataFilePointer.ContentIndexDataFilePointerDescriptor POINTER_DESC
            = new ContentIndexDataFilePointer.ContentIndexDataFilePointerDescriptor();

    @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"})
    // pool is synchronized internally
    private BufferPool pool;
    private int fileNumber;

    private ContentIndexDataFileWriter writer;

    ContentIndexDataFileIterator getContent(ContentIndexDataFilePointer pointer, int offset, int length)
            throws IOException, InterruptedException {
        return new ContentIndexDataFileIterator(fileNumber, pool, pointer, offset, length);
    }

    ContentIndexDataFilePointer writeContent(Iterable<String> tokens) throws IOException, InterruptedException {
        synchronized (mutex) {
            // TODO: let multiple threads write at once by precomputing locations ?  
            return writer.writeContent(tokens);
        }
    }

    @Override
    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset)
            throws IOException, InterruptedException {
        initializeFromFile(bufferPool, fileNumber, blockOffset, 0);
    }

    @Override
    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset)
            throws IOException, InterruptedException {
        pool = bufferPool;
        this.fileNumber = fileNumber;
        ContentIndexDataFilePointer freeSpaceStart = new ContentIndexDataFilePointer(1, 0);
        Buffer firstBlock = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        firstBlock.getReadLock().lock();
        try {
            ByteBuffer byteBuffer = firstBlock.getByteBuffer();
            if (byteBuffer.get() == (byte) 1) {
                freeSpaceStart = POINTER_DESC.readIndexEntry(byteBuffer);
            }
        }
        finally {
            firstBlock.getReadLock().unlock();
            bufferPool.unPinBuffer(firstBlock);
        }
        synchronized (mutex) {
            writer = new ContentIndexDataFileWriter(fileNumber, freeSpaceStart, pool);
        }
    }

    @Override
    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset)
            throws IOException, InterruptedException {
        writeToFile(bufferPool, fileNumber, blockOffset, 0);
    }

    @Override
    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset)
            throws IOException, InterruptedException {
        Buffer firstBlock = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        firstBlock.getWriteLock().lock();
        try {
            firstBlock.setIsDirty(true);
            ByteBuffer byteBuffer = firstBlock.getByteBuffer();
            byteBuffer.put((byte) 1);
            synchronized (mutex) {
                writer.getFreeSpaceStart().serializeToByteBuffer(byteBuffer);
            }
        }
        finally {
            firstBlock.getWriteLock().unlock();
            bufferPool.unPinBuffer(firstBlock);
        }
    }

    @Override
    public Pair<Long, Integer> getOnDiskSize() {
        ContentIndexDataFilePointer pointer;
        synchronized (mutex) {
            pointer = writer.getFreeSpaceStart();
        }
        return new Pair<Long, Integer>(pointer.getBlockOffset(), pointer.getByteOffset());
    }

    @Override
    public void close() {
        writer.close();
    }
}
