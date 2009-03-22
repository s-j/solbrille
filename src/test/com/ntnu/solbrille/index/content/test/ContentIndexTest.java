package com.ntnu.solbrille.index.content.test;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.content.ContentIndex;
import com.ntnu.solbrille.index.content.ContentIndexBuilder;
import com.ntnu.solbrille.index.content.ContentIndexDataFileIterator;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class ContentIndexTest extends TestCase {


    private static String[] documents = {
            "as bas kac .aascd adsf. ;Kjka, opqw.. -adsf",
            "¿ l k",
            "p",
            "¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaeh"
                    + "dasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf"
                    + "¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21"
                    + "^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfa"
                    + "k,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfa"
                    + "ehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfda"
                    + "sf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š"
                    + "21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asd"
                    + "fak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdk"
                    + "dasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf"
                    + "¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21"
                    + "^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfa"
                    + "k,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfa"
                    + "ehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfda"
                    + "sf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š"
                    + "21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asd"
                    + "fak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdk"
                    + "dasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf"
                    + "¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21"
                    + "^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfa"
                    + "k,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfa"
                    + "ehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfda"
                    + "sf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š"
                    + "21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asd"
                    + "fak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdk"
                    + "dasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf"
                    + "¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21"
                    + "^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfa"
                    + "k,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfa"
                    + "ehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfda"
                    + "sf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š"
                    + "21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asd"
                    + "fak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdk"
                    + "dasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf"
                    + "¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21"
                    + "^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfa"
                    + "k,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfa"
                    + "ehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfda"
                    + "sf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š"
                    + "21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asd"
                    + "fak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdk"
                    + "dasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf"
                    + "¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21"
                    + "^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfa"
                    + "k,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfa"
                    + "ehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfda"
                    + "sf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š"
                    + "21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asd"
                    + "fak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdk"
                    + "dasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf"
                    + "¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21"
                    + "^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfa"
                    + "k,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfa"
                    + "ehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfda"
                    + "sf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š"
                    + "21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asd"
                    + "fak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdk"
                    + "dasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf"
                    + "¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21"
                    + "^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfa"
                    + "k,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfa"
                    + "ehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfda"
                    + "sf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š"
                    + "21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asd"
                    + "fak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdk"
                    + "dasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf"
                    + "¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21"
                    + "^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfa"
                    + "k,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfa"
                    + "ehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfda"
                    + "sf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š"
                    + "21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asd"
                    + "fak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdk"
                    + "faehdasf¾¿asdfak,.-,.-,.@Š21^sadfasdfdasf¿slfkasdkfaehdasf"};
    private static int[][][] ranges = {
            {{2, 4}, {4, 7}},
            {},
            {},
            {}
    };

    public void testContentIndex() throws IOException, InterruptedException {
        BufferPool pool = new BufferPool(10, 1024); // realy small buffers just to be naughty :P
        pool.start();

        File indexFile = new File("index.bin");
        indexFile.createNewFile();
        FileChannel indexChannel = new RandomAccessFile(indexFile, "rw").getChannel();

        File dataFile = new File("data.bin");
        dataFile.createNewFile();
        FileChannel dataChannel = new RandomAccessFile(dataFile, "rw").getChannel();

        ContentIndex contentIndex = new ContentIndex(
                pool,
                pool.registerFile(indexChannel, indexFile),
                pool.registerFile(dataChannel, dataFile));
        contentIndex.start();
        ContentIndexBuilder builder = new ContentIndexBuilder(contentIndex);
        builder.start();
        try {
            List<String>[] tokenized = new List[documents.length];
            for (int i = 0; i < documents.length; i++) {
                tokenized[i] = Arrays.asList(documents[i].split(" "));
                Iterator<String> actual = tokenized[i].iterator();
                builder.addDocument((long) (i + 1), tokenized[i]);
                ContentIndexDataFileIterator lookup = contentIndex.getContent(i + 1, 0, Integer.MAX_VALUE);
                while (actual.hasNext()) {
                    assertTrue(lookup.hasNext());
                    String fromIndex = lookup.next();
                    String stored = actual.next();
                    assertEquals(fromIndex, stored);
                }
                assertFalse(lookup.hasNext());
                lookup.close();
            }

            int i = 0;
            for (int[][] tests : ranges) {
                for (int[] test : tests) {
                    ContentIndexDataFileIterator lookup = contentIndex.getContent(i + 1, test[0], test[1]);
                    List<String> actualData = tokenized[i].subList(test[0], Math.min(tokenized[i].size(), test[0] + test[1]));
                    Iterator<String> actual = actualData.iterator();
                    while (lookup.hasNext() && actual.hasNext()) {
                        assertEquals(lookup.next(), actual.next());
                    }
                    assertFalse(lookup.hasNext());
                    assertFalse(actual.hasNext());
                    lookup.close();
                }
                i++;
            }
        }
        finally {
            builder.stop();
            contentIndex.stop();
            pool.stop();
            dataChannel.close();
            indexChannel.close();
            dataFile.delete();
            indexFile.delete();
        }
    }

}
