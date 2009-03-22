package com.ntnu.solbrille.index.content;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;
import com.ntnu.solbrille.utils.Closeable;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
class ContentIndexDataFileWriter implements Closeable {

    private final BufferPool pool;
    private final int fileNumber;
    private ContentIndexDataFilePointer freeSpaceStart;

    private Buffer currentBuffer;
    private ByteBuffer currentByteBuffer;

    ContentIndexDataFileWriter(int fileNumber, ContentIndexDataFilePointer freeSpaceStart, BufferPool pool) throws IOException, InterruptedException {
        this.fileNumber = fileNumber;
        this.freeSpaceStart = freeSpaceStart;
        this.pool = pool;
        currentBuffer = pool.pinBuffer(new FileBlockPointer(fileNumber, freeSpaceStart.getBlockOffset()));
        currentBuffer.setIsDirty(true);
        currentByteBuffer = currentBuffer.getByteBuffer();
        currentByteBuffer.position(freeSpaceStart.getByteOffset());
    }

    ContentIndexDataFilePointer writeContent(Iterable<String> tokens) throws IOException, InterruptedException {
        currentBuffer.getWriteLock().lock();
        if (currentByteBuffer.remaining() < Constants.LONG_SIZE) {
            currentBuffer.getWriteLock().unlock();
            moveNextBuffer();
            currentBuffer.getWriteLock().lock();
        }

        assert currentByteBuffer.remaining() >= Constants.LONG_SIZE;
        FileBlockPointer firstBufferPointer = currentBuffer.getBlockPointer();
        int startPosition = currentByteBuffer.position();
        ContentIndexDataFilePointer pointer = new ContentIndexDataFilePointer(
                firstBufferPointer.getBlockNumber(), startPosition);
        currentByteBuffer.position(startPosition + Constants.LONG_SIZE);
        long count = 0;
        for (String token : tokens) {
            count++;
            writeToken(token);
        }
        freeSpaceStart = new ContentIndexDataFilePointer(
                currentBuffer.getBlockPointer().getBlockNumber(),
                currentByteBuffer.position());
        currentBuffer.getWriteLock().unlock(); // intermediate buffers locked and unlocked in writeToken.
        Buffer firstBuffer = pool.pinBuffer(firstBufferPointer);
        firstBuffer.getWriteLock().lock();
        try {
            firstBuffer.setIsDirty(true);
            ByteBuffer buf = firstBuffer.getByteBuffer();
            buf.position(startPosition);
            buf.putLong(count);
        }
        finally {
            firstBuffer.getWriteLock().unlock();
            pool.unPinBuffer(firstBuffer);
        }
        return pointer;
    }

    ContentIndexDataFilePointer getFreeSpaceStart() {
        return freeSpaceStart;
    }

    private void writeToken(String token) throws IOException, InterruptedException {
        int remaining = currentByteBuffer.remaining();
        if (remaining < Constants.INT_SIZE) {
            currentBuffer.getWriteLock().unlock();
            moveNextBuffer();
            currentBuffer.getWriteLock().lock();
            remaining = currentByteBuffer.remaining();
        }
        char[] chars = token.toCharArray();
        currentByteBuffer.putInt(chars.length);
        remaining -= Constants.INT_SIZE;
        for (char c : chars) {
            if (remaining < Constants.CHAR_SIZE) {
                currentBuffer.getWriteLock().unlock();
                moveNextBuffer();
                currentBuffer.getWriteLock().lock();
                remaining = currentByteBuffer.remaining();
            }
            currentByteBuffer.putChar(c);
            remaining -= Constants.CHAR_SIZE;
        }
    }

    private void moveNextBuffer() throws IOException, InterruptedException {
        FileBlockPointer next = currentBuffer.getBlockPointer().next();
        pool.unPinBuffer(currentBuffer);
        currentBuffer = pool.pinBuffer(next);
        currentBuffer.setIsDirty(true);
        currentByteBuffer = currentBuffer.getByteBuffer();
    }

    @Override
    public void close() {
        if (currentBuffer.getWriteLock().isHeldByCurrentThread()) {
            currentBuffer.getWriteLock().unlock();
        }
        pool.unPinBuffer(currentBuffer);
    }
}
