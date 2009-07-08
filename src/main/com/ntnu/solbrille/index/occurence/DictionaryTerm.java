package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexKeyEntry;

import java.nio.ByteBuffer;

/**
 * Dictionary Term representation for lexicon management, indexing and querying
 * 
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 * @see DictionaryEntry
 */
public class DictionaryTerm implements IndexKeyEntry<DictionaryTerm> {

    public static class DictionaryTermDescriptor implements IndexEntryDescriptor<DictionaryTerm> {

        @Override
        public DictionaryTerm readIndexEntry(ByteBuffer buffer) {
            int pos = buffer.position();
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

    @Override
    public int getSeralizedLength() {
        return Constants.INT_SIZE + Constants.CHAR_SIZE * term.toCharArray().length;
    }

    @Override
    public void serializeToByteBuffer(ByteBuffer buffer) {
        char[] chars = term.toCharArray();
        int startPos = buffer.position();
        buffer.putInt(chars.length);
        for (int i = 0; i < chars.length; i++) {
            buffer.putChar(chars[i]);
        }
        assert buffer.position() - startPos == getSeralizedLength();
    }

    @Override
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
