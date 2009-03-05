package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;
import com.ntnu.solbrille.utils.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class InvertedListReader {

    private static final DictionaryTerm.DictionaryTermDescriptor TERM_DESCRIPTOR = new DictionaryTerm.DictionaryTermDescriptor();

    private class Reader {
        private Buffer currentBuffer;
        private ByteBuffer currentByteBuffer;

        private int currentBlockLastElementStart;
        private DictionaryTerm currentTerm = null;

        private long remainingDocumentsInCurrentTerm;
        private long currentDocumentId = -1;
        private long remainingPositionsInCurrentDocument;
        private long nextDocumentBlock = -1;

        private int nextDocumentByteOffset = -1;

        public void initializeOnTerm(DictionaryTerm term, InvertedListPointer termPointer) throws IOException, InterruptedException {
            currentTerm = term;
            currentBuffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, termPointer.getBlockOffset()));
            currentByteBuffer = currentBuffer.getByteBuffer();
            currentBlockLastElementStart = currentByteBuffer.getInt();
            currentByteBuffer.position(termPointer.getByteOffset());
            if (Constants.DEBUG) { // check if we are pointing on the correct term
                currentTerm = TERM_DESCRIPTOR.readIndexEntryDescriptor(currentByteBuffer);
                assert currentTerm.equals(term);
            } else { // assume pointing on correct term.
                currentTerm = term;
                currentByteBuffer.position(currentByteBuffer.position() + term.getSeralizedLength());
            }
            remainingDocumentsInCurrentTerm = currentByteBuffer.getLong();
            currentByteBuffer.getLong(); // skip number of occurences
            currentByteBuffer.position(currentByteBuffer.position() + Constants.LONG_SIZE + Constants.INT_SIZE);
            if (currentByteBuffer.position() > currentBlockLastElementStart) {
                moveNextBlock();
            }
            nextDocumentBlock = currentBuffer.getBlockPointer().getBlockNumber();
            nextDocumentByteOffset = currentByteBuffer.position();
        }

        private void moveNextBlock() throws IOException, InterruptedException {
            FileBlockPointer next = currentBuffer.getBlockPointer().next();
            bufferPool.unPinBuffer(currentBuffer);
            currentBuffer = bufferPool.pinBuffer(next);
            currentByteBuffer = currentBuffer.getByteBuffer();
            currentBlockLastElementStart = currentByteBuffer.getInt();
        }

        // assumes positioned on next document start
        public DocumentOccurence readNextDocument() throws IOException, InterruptedException {
            if (currentByteBuffer.position() > currentBlockLastElementStart) {
                moveNextBlock();
            }
            assert currentBuffer.getBlockPointer().getBlockNumber() == nextDocumentBlock;
            assert currentByteBuffer.position() == nextDocumentByteOffset;
            currentDocumentId = currentByteBuffer.getLong();
            remainingPositionsInCurrentDocument = currentByteBuffer.getLong();
            nextDocumentBlock = currentByteBuffer.getLong();
            nextDocumentByteOffset = currentByteBuffer.getInt();
            //try {
            DocumentOccurence docOcc = new DocumentOccurence(currentDocumentId);

            while (remainingPositionsInCurrentDocument > 0) {
                docOcc.addPosition(readNextPosition());
            }
            remainingDocumentsInCurrentTerm--;
            return docOcc;
            /*}
            catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
            return null;*/
        }

        private int readNextPosition() throws IOException, InterruptedException {
            assert remainingPositionsInCurrentDocument > 0;
            if (currentByteBuffer.position() > currentBlockLastElementStart) {
                moveNextBlock();
            }
            remainingPositionsInCurrentDocument--;
            return currentByteBuffer.getInt();
        }

    }

    private class TermIterator implements Iterator<DocumentOccurence> {

        private final Reader reader = new Reader();

        private TermIterator(DictionaryTerm term, InvertedListPointer termPointer) throws IOException, InterruptedException {
            reader.initializeOnTerm(term, termPointer);
        }

        public boolean hasNext() {
            return reader.remainingDocumentsInCurrentTerm > 0;
        }

        public DocumentOccurence next() {
            assert hasNext();
            try {
                return reader.readNextDocument();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    private class InvertedListIterator implements Iterator<Pair<DictionaryTerm, InvertedListPointer>> {

        InvertedListPointer nextTerm;
        long numberOfTerms;

        /**
         * Loads a term from the inverted file. And returnes the address of the next term.
         *
         * @param pointer The location of the term.
         * @return Pair of the term and the pointer to the next term in the file.
         * @throws IOException          If there were som error while reading the inverted file.
         * @throws InterruptedException If the calling thread were interupted.
         */
        private Pair<DictionaryTerm, InvertedListPointer> getTermAndNextPointer(InvertedListPointer pointer) throws IOException, InterruptedException {
            Buffer buffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, pointer.getBlockOffset()));
            try {
                ByteBuffer byteBuffer = buffer.getByteBuffer();
                byteBuffer.position(pointer.getByteOffset());
                DictionaryTerm term = TERM_DESCRIPTOR.readIndexEntryDescriptor(byteBuffer);
                byteBuffer.position(byteBuffer.position() + 2 * Constants.LONG_SIZE);
                return new Pair<DictionaryTerm, InvertedListPointer>(term, new InvertedListPointer(byteBuffer.getLong(), byteBuffer.getInt()));
            }
            finally {
                bufferPool.unPinBuffer(buffer);
            }
        }

        private InvertedListIterator(InvertedListPointer nextTerm, long numberOfTerms) {
            this.nextTerm = nextTerm;
            this.numberOfTerms = numberOfTerms;
        }

        public boolean hasNext() {
            return numberOfTerms > 0 && nextTerm != null;
        }

        public Pair<DictionaryTerm, InvertedListPointer> next() {
            try {
                Pair<DictionaryTerm, InvertedListPointer> termAndNext = getTermAndNextPointer(nextTerm);
                Pair<DictionaryTerm, InvertedListPointer> returnValue =
                        new Pair<DictionaryTerm, InvertedListPointer>(termAndNext.getFirst(), nextTerm);
                nextTerm = termAndNext.getSecond();
                numberOfTerms--;
                return returnValue;
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(0);
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final BufferPool bufferPool;
    private final int fileNumber;
    private final long startBlock;

    public InvertedListReader(BufferPool bufferPool, int fileNumber, long startBlock) {
        this.bufferPool = bufferPool;
        this.fileNumber = fileNumber;
        this.startBlock = startBlock;
    }

    public Iterator<DocumentOccurence> iterateTerm(DictionaryTerm term, InvertedListPointer pointer)
            throws IOException, InterruptedException {
        return new TermIterator(term, pointer);
    }

    public Iterator<Pair<DictionaryTerm, InvertedListPointer>> getFileIterator() throws IOException, InterruptedException {
        Pair<InvertedListPointer, Long> first = getFirstTermPointerAndTermCount();
        return new InvertedListIterator(first.getFirst(), first.getSecond());
    }

    Pair<InvertedListPointer, Long> getFirstTermPointerAndTermCount() throws IOException, InterruptedException {
        Buffer buffer = bufferPool.pinBuffer(new FileBlockPointer(fileNumber, startBlock));
        try {
            ByteBuffer byteBuffer = buffer.getByteBuffer();
            byteBuffer.position(Constants.INT_SIZE); // last element start not needed
            long numberOfTerms = byteBuffer.getLong();
            byteBuffer.position(byteBuffer.position() + Constants.LONG_SIZE);
            InvertedListPointer pointer = new InvertedListPointer(byteBuffer.getLong(), byteBuffer.getInt());
            return new Pair<InvertedListPointer, Long>(pointer, numberOfTerms);
        }
        finally {
            bufferPool.unPinBuffer(buffer);
        }
    }


}
