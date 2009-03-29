package com.ntnu.solbrille.index.content;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;
import com.ntnu.solbrille.utils.Closeable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class ContentIndexDataFileIterator implements Iterator<String>, Closeable {

    private final BufferPool pool;

    private Buffer currentBuffer;
    private ByteBuffer currentByteBuffer;

    private final long numberOfTokens;
    private long numberOfTokensLeft;

    public ContentIndexDataFileIterator(
            int fileNumber,
            BufferPool pool,
            ContentIndexDataFilePointer pointer,
            long offset, int length) throws IOException, InterruptedException {
        this.pool = pool;
        currentBuffer = pool.pinBuffer(new FileBlockPointer(fileNumber, pointer.getBlockOffset()));
        currentBuffer.getReadLock().lock();
        currentByteBuffer = currentBuffer.getByteBuffer();
        currentByteBuffer.position(pointer.getByteOffset());
        numberOfTokens = currentByteBuffer.getLong();
        numberOfTokensLeft = Math.min(numberOfTokens, offset + length);
        skipTokens(offset);
    }


    private void skipTokens(long offset) throws IOException, InterruptedException {
        while (hasNext() && offset > 0) { // TODO: optimize
            next();
            offset--;
        }
    }

    private void moveNextBuffer() throws IOException, InterruptedException {
        currentBuffer.getReadLock().unlock();
        FileBlockPointer next = currentBuffer.getBlockPointer().next();
        pool.unPinBuffer(currentBuffer);
        currentBuffer = pool.pinBuffer(next);
        currentByteBuffer = currentBuffer.getByteBuffer();
        currentBuffer.getReadLock().lock();
    }


    @Override
    public boolean hasNext() {
        return numberOfTokensLeft > 0;
    }

    @Override
    public String next() {
        numberOfTokensLeft--;
        try {
            int remaining = currentByteBuffer.remaining();
            if (remaining < Constants.INT_SIZE) {
                moveNextBuffer();
                remaining = currentByteBuffer.remaining();
            }
            int tokLength = currentByteBuffer.getInt();
            char[] token = new char[tokLength];
            remaining -= Constants.INT_SIZE;
            int pos = 0;
            while (pos < tokLength) {
                if (remaining < Constants.CHAR_SIZE) {
                    moveNextBuffer();
                    remaining = currentByteBuffer.remaining();
                }
                token[pos++] = currentByteBuffer.getChar();
                remaining -= Constants.CHAR_SIZE;
            }
            return new String(token);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        currentBuffer.getReadLock().unlock();
        pool.unPinBuffer(currentBuffer);
    }
}
