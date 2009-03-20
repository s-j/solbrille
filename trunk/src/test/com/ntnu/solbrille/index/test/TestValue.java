package com.ntnu.solbrille.index.test;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexEntry;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class TestValue implements IndexEntry {

    public static class TestValueDescriptor implements IndexEntryDescriptor<TestValue> {
        public TestValue readIndexEntry(ByteBuffer buffer) {
            int length = buffer.getInt();
            char[] charArray = new char[length];
            for (int i = 0; i < length; i++) {
                charArray[i] = buffer.getChar();
            }
            return new TestValue(new String(charArray));
        }
    }

    private String value;

    public TestValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestValue) {
            return value.equals(((TestValue) obj).value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getSeralizedLength() {
        return Constants.INT_SIZE + value.length() * Constants.CHAR_SIZE;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        buffer.putInt(value.length());
        for (char c : value.toCharArray()) {
            buffer.putChar(c);
        }
    }
}
