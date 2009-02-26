package com.ntnu.solbrille.index;

import java.nio.ByteBuffer;

/**
 * Interface for entries in a index.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public interface IndexEntry {

    /**
     * A descriptor used by indices as factories when reading entries from
     * files.
     */
    interface IndexEntryDescriptor<T extends IndexEntry> {

        /**
         * Reads a index entry from the supplied byte buffer. The format of
         * the data in the byte buffer is equal to the format of <c>T</c>
         * that {@link com.ntnu.solbrille.index.IndexEntry#serializeToByteBuffer(ByteBuffer)}
         * produces.
         *
         * @param buffer The buffer to read the entry from.
         * @return The deserialized instance.
         */
        T readIndexEntryDescriptor(ByteBuffer buffer);
    }

    /**
     * Gets the number of bytes required to serialize this entry.
     *
     * @return Number of bytes required to serialize.
     */
    int getSeralizedLength();

    /**
     * Serialize this entry to a byte buffer.
     *
     * @param buffer The buffer to serialize this entry to.
     */
    void serializeToByteBuffer(ByteBuffer buffer);
}
