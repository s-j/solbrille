package com.ntnu.solbrille.index.basicentrytypes;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexKeyEntry;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class StringEntryType implements IndexKeyEntry<StringEntryType> {
    private String string;

    public int getSeralizedLength() {
        return Constants.INT_SIZE + string.length() * Constants.CHAR_SIZE;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int compareTo(StringEntryType o) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
