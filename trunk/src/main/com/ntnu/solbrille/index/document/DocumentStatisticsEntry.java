package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexEntry;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentStatisticsEntry implements IndexEntry {

    public static class DocumentStatisticEntryDescriptor implements IndexEntryDescriptor<DocumentStatisticsEntry> {

        public DocumentStatisticsEntry readIndexEntryDescriptor(ByteBuffer buffer) {
            return new DocumentStatisticsEntry(buffer.getLong());
        }
    }

    private long documentLength;

    public DocumentStatisticsEntry(long documentLength) {
        this.documentLength = documentLength;
    }

    public long getDocumentLength() {
        return documentLength;
    }

    public void setDocumentLength(long documentLength) {
        this.documentLength = documentLength;
    }

    public int getSeralizedLength() {
        return Constants.LONG_SIZE;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        buffer.putLong(documentLength);
    }
}
