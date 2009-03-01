package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexKeyEntry;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DictionaryTerm implements IndexKeyEntry<DictionaryTerm> {

    public static class DictionaryTermDescriptor implements IndexEntryDescriptor<DictionaryTerm> {

        public DictionaryTerm readIndexEntryDescriptor(ByteBuffer buffer) {
            int length = buffer.getInt();
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                chars[i] = buffer.getChar();
            }
            return new DictionaryTerm(new String(chars));
        }

    }

    // TODO: replace with integer based byte array
    private String term;

    public DictionaryTerm(String term) {
        this.term = term;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getSeralizedLength() {
        return Constants.INT_SIZE + Constants.CHAR_SIZE * term.toCharArray().length;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        char[] chars = term.toCharArray();
        buffer.putInt(chars.length);
        for (int i = 0; i < chars.length; i++) {
            buffer.putChar(chars[i]);
        }
    }

    public int compareTo(DictionaryTerm o) {
        return term.compareTo(o.term);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DictionaryTerm) {
            return term.equals(((DictionaryTerm) obj).term);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }

    @Override
    public String toString() {
        return term;
    }
}
