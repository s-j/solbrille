package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexKeyEntry;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentUriEntry implements IndexKeyEntry<DocumentUriEntry> {

    public static class DocumentUriEntryDescriptor implements IndexEntryDescriptor<DocumentUriEntry> {

        public DocumentUriEntry readIndexEntryDescriptor(ByteBuffer buffer) {
            int length = buffer.getInt();
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                chars[i] = buffer.getChar();
            }
            try {
                return new DocumentUriEntry(new URI(new String(chars)));
            } catch (URISyntaxException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            return null;
        }
    }

    private URI documentUri;

    public DocumentUriEntry(URI documentUri) {
        this.documentUri = documentUri;
    }

    public URI getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(URI documentUri) {
        this.documentUri = documentUri;
    }

    public int getSeralizedLength() {
        return Constants.INT_SIZE + documentUri.toString().toCharArray().length * Constants.CHAR_SIZE;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        char[] chars = documentUri.toString().toCharArray();
        buffer.putInt(chars.length);
        for (char c : chars) {
            buffer.putChar(c);
        }
    }

    public int compareTo(DocumentUriEntry o) {
        return documentUri.compareTo(o.documentUri);
    }
}
