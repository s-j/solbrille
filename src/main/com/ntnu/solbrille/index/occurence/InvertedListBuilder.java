package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 *          <p/>
 *          TODO: Gap encoded and compressed position lists.
 */
public class InvertedListBuilder {
    private BufferPool bufferPool;
    private int fileNumber;
    private Buffer currentBuffer;

    private ByteBuffer activeByteBuffer;
    private int blockLastElementStart;

    private long totalTerms = 0L;
    private long totalOccurences = 0L;
    private long firstBlockOffset;
    private int firstByteOffset;

    private DictionaryTerm firstTerm;
    private long firstTermBlockOffset;
    private int firstTermByteOffset;

    public InvertedListBuilder(BufferPool bufferPool, int fileNumber, long blockOffset) throws IOException, InterruptedException {
        this(bufferPool, fileNumber, blockOffset, 0);
    }

    public InvertedListBuilder(BufferPool bufferPool, int fileNumber, long blockOffset, int byteOffset) throws IOException, InterruptedException {
        this.bufferPool = bufferPool;
        this.fileNumber = fileNumber;
        currentBuffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, blockOffset));
        byteOffset = Math.max(byteOffset, Constants.INT_SIZE); // make room for last element start.
        firstBlockOffset = blockOffset;
        firstByteOffset = byteOffset;
        activeByteBuffer = currentBuffer.getByteBuffer();
        activeByteBuffer.position(byteOffset);
        blockLastElementStart = byteOffset;
        assert activeByteBuffer.capacity() > 3 * Constants.LONG_SIZE + Constants.INT_SIZE; // need to be able to store head in first block.
        activeByteBuffer.putLong(0L); // total terms placeholder
        activeByteBuffer.putLong(0L); // total occurenses placeholder
        activeByteBuffer.putLong(0L); // first term block placeholder
        activeByteBuffer.putInt(0); // first term byte placeholder
        currentBuffer.setIsDirty(true);
    }

    private DictionaryTerm currentTerm;
    private long currentTermStartBlock;
    private int currentTermStartByteOffset;
    private long documentsInTerm;
    private long occurensesInTerm;


    /**
     * Inserts a term and returns the starting address for the term.
     *
     * @param term Term to be added.
     * @return The address in the file for the term.
     * @throws IOException          If the there are some errors writing to the file.
     * @throws InterruptedException If the calling thread is interrupted.
     */
    public InvertedListPointer nextTerm(DictionaryTerm term) throws IOException, InterruptedException {
        int size = term.getSeralizedLength() + 3 * Constants.LONG_SIZE + Constants.INT_SIZE;
        if (activeByteBuffer.remaining() < size) {
            moveNextBlock();
        }
        finishCurrentTerm();
        // Prepare for next term.
        currentTermStartBlock = currentBuffer.getBlockPointer().getBlockNumber();
        currentTermStartByteOffset = activeByteBuffer.position();
        if (firstTerm == null) { // first term
            firstTerm = term;
            firstTermBlockOffset = currentTermStartBlock;
            firstTermByteOffset = currentTermStartByteOffset;
        }

        currentTerm = term;
        documentsInTerm = 0L;
        occurensesInTerm = 0L;
        blockLastElementStart = activeByteBuffer.position();
        term.serializeToByteBuffer(activeByteBuffer);
        totalTerms++;
        activeByteBuffer.putLong(0L); // num documents placeholder
        activeByteBuffer.putLong(0L); // num occurecnes of term placeholder
        activeByteBuffer.putLong(0L); // next term block placeholder
        activeByteBuffer.putInt(0);   // next term block offset placeholder
        currentBuffer.setIsDirty(true);
        return new InvertedListPointer(currentTermStartBlock, currentTermStartByteOffset);
    }

    private void finishCurrentTerm() throws IOException, InterruptedException {
        if (currentTerm != null) { // not first term
            finishCurrentDocument();
            Buffer termStartBlock = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, currentTermStartBlock));
            try {
                ByteBuffer byteBuffer = termStartBlock.getByteBuffer();
                byteBuffer.position(currentTermStartByteOffset + currentTerm.getSeralizedLength());
                byteBuffer.putLong(documentsInTerm);
                byteBuffer.putLong(occurensesInTerm);
                byteBuffer.putLong(currentBuffer.getBlockPointer().getBlockNumber());
                byteBuffer.putInt(activeByteBuffer.position());
                currentTerm = null;
            }
            finally {
                termStartBlock.setIsDirty(true);
                bufferPool.unPinBuffer(termStartBlock);
            }
        }
    }

    private long currentDocumentId = -1L;
    private long currentDocumentStartBlock;
    private int currentDocumentStartByteOffset;
    private long occurensesInDocument = 0L; // TBD: perhaps enough with integer

    public InvertedListPointer nextDocument(long documentId) throws IOException, InterruptedException {
        int size = 3 * Constants.LONG_SIZE + Constants.INT_SIZE;
        if (activeByteBuffer.remaining() < size) {
            moveNextBlock();
        }
        finishCurrentDocument();
        currentDocumentId = documentId;
        currentDocumentStartBlock = currentBuffer.getBlockPointer().getBlockNumber();
        currentDocumentStartByteOffset = activeByteBuffer.position();
        occurensesInDocument = 1L;
        blockLastElementStart = activeByteBuffer.position();
        activeByteBuffer.putLong(documentId);
        activeByteBuffer.putLong(0L); // occurenses in document placeholder
        activeByteBuffer.putLong(0L); // next document block placeholder
        activeByteBuffer.putInt(0);   // next document byte offset
        currentBuffer.setIsDirty(true);
        documentsInTerm++;
        return new InvertedListPointer(currentDocumentStartBlock, currentDocumentStartByteOffset);
    }

    private void finishCurrentDocument() throws IOException, InterruptedException {
        if (currentDocumentId != -1) {
            Buffer docStartBlock = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, currentDocumentStartBlock));
            try {
                ByteBuffer byteBuffer = docStartBlock.getByteBuffer();
                byteBuffer.position(currentDocumentStartByteOffset + Constants.LONG_SIZE);
                byteBuffer.putLong(occurensesInDocument);
                byteBuffer.putLong(currentBuffer.getBlockPointer().getBlockNumber());
                byteBuffer.putInt(activeByteBuffer.position());
                currentDocumentId = -1;
            }
            finally {
                docStartBlock.setIsDirty(true);
                bufferPool.unPinBuffer(docStartBlock);
            }
        }
    }

    public void nextOccurence(int position) throws IOException, InterruptedException {
        if (activeByteBuffer.remaining() < Constants.INT_SIZE) {
            moveNextBlock();
        }
        blockLastElementStart = activeByteBuffer.position();
        activeByteBuffer.putInt(position);
        currentBuffer.setIsDirty(true);
        occurensesInDocument++;
        occurensesInTerm++;
        totalOccurences++;
    }

    private void moveNextBlock() throws IOException, InterruptedException {
        activeByteBuffer.putInt(0, blockLastElementStart);
        currentBuffer.setIsDirty(true);
        FileBlockPointer next = currentBuffer.getBlockPointer().next();
        bufferPool.unPinBuffer(currentBuffer);
        currentBuffer = bufferPool.pinBuffer(next);
        currentBuffer.setIsDirty(true);
        activeByteBuffer = currentBuffer.getByteBuffer();
        activeByteBuffer.position(Constants.INT_SIZE);
    }

    public void finishFile() throws IOException, InterruptedException {
        finishCurrentDocument();
        finishCurrentTerm();
        activeByteBuffer.putInt(0, blockLastElementStart);
        currentBuffer.setIsDirty(true);
        activeByteBuffer = null;
        bufferPool.unPinBuffer(currentBuffer);
        currentBuffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, firstBlockOffset));
        activeByteBuffer = currentBuffer.getByteBuffer();
        activeByteBuffer.position(firstByteOffset);
        activeByteBuffer.putLong(totalTerms);
        activeByteBuffer.putLong(totalOccurences);
        activeByteBuffer.putLong(firstTermBlockOffset);
        activeByteBuffer.putInt(firstTermByteOffset);
        currentBuffer.setIsDirty(true);
        bufferPool.unPinBuffer(currentBuffer);
        currentBuffer = null;
        activeByteBuffer = null;
    }
}
