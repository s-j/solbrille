package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexKeyEntry;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentIdEntry implements IndexKeyEntry<DocumentIdEntry> {


    public static class DocumentIdEntryDescriptor implements IndexEntryDescriptor<DocumentIdEntry> {

        public DocumentIdEntry readIndexEntryDescriptor(ByteBuffer buffer) {
            return new DocumentIdEntry(buffer.getLong());
        }
    }

    private long documentId;

    public DocumentIdEntry(long documentId) {
        this.documentId = documentId;
    }

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }

    public int getSeralizedLength() {
        return Constants.LONG_SIZE;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        buffer.putLong(documentId);
    }

    public int compareTo(DocumentIdEntry o) {
        return Long.valueOf(documentId).compareTo(o.documentId);
    }
}
