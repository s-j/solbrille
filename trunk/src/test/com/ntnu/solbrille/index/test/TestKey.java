package com.ntnu.solbrille.index.test;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexKeyEntry;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class TestKey implements IndexKeyEntry<TestKey> {

    public static class TestKeyDescriptor implements IndexEntryDescriptor<TestKey> {

        public TestKey readIndexEntry(ByteBuffer buffer) {
            int value = buffer.getInt();
            return new TestKey(value);
        }
    }

    private int value;

    public TestKey(int value) {
        this.value = value;
    }

    public int getSeralizedLength() {
        return Constants.INT_SIZE;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        buffer.putInt(value);
    }

    public int compareTo(TestKey o) {
        return value - o.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestKey) {
            return value == ((TestKey) obj).value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(value).hashCode();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
