package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexEntry;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class StaticInformationEntry implements IndexEntry {

    public static class StaticInformationEntryDescriptor implements IndexEntryDescriptor<StaticInformationEntry> {
        public StaticInformationEntry readIndexEntryDescriptor(ByteBuffer buffer) {
            StaticInformationEntry entry = new StaticInformationEntry();
            entry.documentIdCounter.set(buffer.getLong());
            entry.totalNumberOfTokens.set(buffer.getLong());
            entry.totalNumberOfDocuments.set(buffer.getLong());
            entry.totalDocumentLength.set(buffer.getLong());
            return entry;
        }
    }

    private final AtomicLong documentIdCounter = new AtomicLong();
    private final AtomicLong totalNumberOfTokens = new AtomicLong();
    private final AtomicLong totalNumberOfDocuments = new AtomicLong();
    private final AtomicLong totalDocumentLength = new AtomicLong();


    public int getSeralizedLength() {
        return 4 * Constants.LONG_SIZE;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        System.out.println("Write DocIdCounter: " + documentIdCounter);
        buffer.putLong(documentIdCounter.get());
        buffer.putLong(totalNumberOfTokens.get());
        buffer.putLong(totalNumberOfDocuments.get());
        buffer.putLong(totalDocumentLength.get());
    }

    public long getDocumentIdCounter() {
        return documentIdCounter.get();
    }

    public long getNextDocumentId() {
        return documentIdCounter.incrementAndGet();
    }

    public long getTotalDocumentLength() {
        return totalDocumentLength.get();
    }

    public long getTotalNumberOfDocuments() {
        return totalNumberOfDocuments.get();
    }

    public long getTotalNumberOfTokens() {
        return totalNumberOfTokens.get();
    }

    public void registerNewDocumentIndexed(long documentLength, long numberOfTokensIndexed) {
        totalNumberOfDocuments.incrementAndGet();
        totalDocumentLength.addAndGet(documentLength);
        totalNumberOfTokens.addAndGet(numberOfTokensIndexed);
    }
}
