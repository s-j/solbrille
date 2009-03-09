package com.ntnu.solbrille.system.test;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.occurence.DocumentOccurence;
import com.ntnu.solbrille.index.occurence.OcccurenceIndexBuilder;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;
import com.ntnu.solbrille.utils.iterators.SkippableIterator;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class IndexLookupTest extends TestCase {

    private String[] documents = new String[]{"ola", "jan max", "robin", "simon", "arne", "solbrille search engine", "max speed"};


    public void testIndexLookup() throws IOException, InterruptedException {
        BufferPool pool = new BufferPool(10, 128); // really small buffers, just to be evil
        File dictFile = new File("dictionary.bin");
        dictFile.createNewFile();
        FileChannel dictChannel = new RandomAccessFile(dictFile, "rw").getChannel();
        int dictFileNumber = pool.registerFile(dictChannel, dictFile);

        File inv1File = new File("inv1.bin");
        inv1File.createNewFile();
        FileChannel inv1Channel = new RandomAccessFile(inv1File, "rw").getChannel();
        int inv1FileNumber = pool.registerFile(inv1Channel, inv1File);

        File inv2File = new File("inv2.bin");
        inv2File.createNewFile();
        FileChannel inv2Channel = new RandomAccessFile(inv2File, "rw").getChannel();
        int inv2FileNumber = pool.registerFile(inv2Channel, inv2File);
        OccurenceIndex occurenceIndex = new OccurenceIndex(pool, dictFileNumber, inv1FileNumber, inv2FileNumber);
        try {
            OcccurenceIndexBuilder occIndexBuilder = new OcccurenceIndexBuilder(occurenceIndex);
            for (long i = 0; i < documents.length; i++) {
                occIndexBuilder.addDocument(i, documents[(int) i]);
            }
            assertFalse(occurenceIndex.lookup("nissefar").getIterator().hasNext());
            assertFalse(occurenceIndex.lookup("max").getIterator().hasNext()); // may change if we adopt dynamic flush strategy
            occIndexBuilder.flush();
            SkippableIterator<DocumentOccurence> maxLookup = occurenceIndex.lookup("max").getIterator();
            assertTrue(maxLookup.hasNext());
            assertEquals(1, maxLookup.next().getDocumentId());
            assertTrue(maxLookup.hasNext());
            assertEquals(6, maxLookup.next().getDocumentId());
        }
        finally {
            pool.stopPool();
            dictChannel.close();
            inv1Channel.close();
            inv2Channel.close();
            dictFile.delete();
            inv1File.delete();
            inv2File.delete();
        }
    }

}
