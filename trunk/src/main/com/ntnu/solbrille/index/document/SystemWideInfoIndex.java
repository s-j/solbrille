package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;
import com.ntnu.solbrille.index.Index;
import com.ntnu.solbrille.utils.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
class SystemWideInfoIndex implements Index {
    private static final StaticInformationEntry.StaticInformationEntryDescriptor INFO_ENTRY_DESC
            = new StaticInformationEntry.StaticInformationEntryDescriptor();

    private StaticInformationEntry data = new StaticInformationEntry();

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        initializeFromFile(bufferPool, fileNumber, blockOffset, 0);
    }

    public long getNextDocumentId() {
        return data.getNextDocumentId();
    }

    public long getTotalNumberOfDocuments() {
        return data.getTotalNumberOfDocuments();
    }

    public long getTotalDocumentLength() {
        return data.getTotalDocumentLength();
    }

    void registerDocumentIndexed(long documentLength, long numberOfTokens) {
        data.registerNewDocumentIndexed(documentLength, numberOfTokens);
    }

    public long getTotalNumberOfTokens() {
        return data.getTotalNumberOfTokens();
    }

    public void initializeFromFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        Buffer buffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        try {
            ByteBuffer byteBuffer = buffer.getByteBuffer();
            byteBuffer.position(byteOffset);
            boolean exists = byteBuffer.get() == (byte) 1;
            if (exists) {
                data = INFO_ENTRY_DESC.readIndexEntryDescriptor(byteBuffer);
            } else {
                data = new StaticInformationEntry();
            }
        }
        finally {
            bufferPool.unPinBuffer(buffer);
        }
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        writeToFile(bufferPool, fileNumber, blockOffset, 0);
    }

    public void writeToFile(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        if (data == null) {
            return;
        }
        Buffer buffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        try {
            ByteBuffer byteBuffer = buffer.getByteBuffer();
            byteBuffer.position(byteOffset);
            byteBuffer.put((byte) 1);
            data.serializeToByteBuffer(byteBuffer);
            buffer.setIsDirty(true);
        }
        finally {
            bufferPool.unPinBuffer(buffer);
        }
    }

    public Pair<Long, Integer> getOnDiskSize() {
        return new Pair<Long, Integer>(0L, Constants.BYTE_SIZE + data.getSeralizedLength());
    }
}
