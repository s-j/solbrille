package com.ntnu.solbrille.index.test;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.BasicKeyValueIndex;
import com.ntnu.solbrille.index.KeyValueIndex;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class BasicKeyValueIndexTest extends TestCase {

    public void testBasicKeyValueIndexWriteToFile() throws IOException, InterruptedException {
        KeyValueIndex<TestKey, TestValue> index = new BasicKeyValueIndex<TestKey, TestValue>(
                new TestKey.TestKeyDescriptor(), new TestValue.TestValueDescriptor());

        for (int i = 0; i < 100; i++) {
            index.put(new TestKey(i), new TestValue("Entry " + i));
        }

        BufferPool pool = new BufferPool(10, 1 * 1024);
        File file = new File("test.bin");
        file.createNewFile();
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        pool.start();
        try {

            int fileNumber = pool.registerFile(channel, file);
            index.writeToFile(pool, fileNumber, 0);

            KeyValueIndex<TestKey, TestValue> index2 = new BasicKeyValueIndex<TestKey, TestValue>(
                    new TestKey.TestKeyDescriptor(), new TestValue.TestValueDescriptor());
            index2.initializeFromFile(pool, fileNumber, 0);
            for (Map.Entry<TestKey, TestValue> entry : index2.entrySet()) {
                assertTrue(index.containsKey(entry.getKey()));
                assertEquals(index.get(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<TestKey, TestValue> entry : index.entrySet()) {
                assertTrue(index2.containsKey(entry.getKey()));
                assertEquals(index2.get(entry.getKey()), entry.getValue());
            }
        }
        finally {
            pool.stop();
            channel.close();
            file.delete();
        }
    }

    public void testBasicKeyValueIndexWriteToFileWithDictionaryTerm() throws IOException, InterruptedException {
        KeyValueIndex<DictionaryTerm, TestValue> index = new BasicKeyValueIndex<DictionaryTerm, TestValue>(
                new DictionaryTerm.DictionaryTermDescriptor(), new TestValue.TestValueDescriptor());

        for (int i = 0; i < 100; i++) {
            index.put(new DictionaryTerm("term" + i), new TestValue("Entry " + i));
        }

        BufferPool pool = new BufferPool(10, 1 * 1024);
        File file = new File("test2.bin");
        file.createNewFile();
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        pool.start();
        try {

            int fileNumber = pool.registerFile(channel, file);
            index.writeToFile(pool, fileNumber, 0);

            KeyValueIndex<DictionaryTerm, TestValue> index2 = new BasicKeyValueIndex<DictionaryTerm, TestValue>(
                    new DictionaryTerm.DictionaryTermDescriptor(), new TestValue.TestValueDescriptor());
            index2.initializeFromFile(pool, fileNumber, 0);
            for (Map.Entry<DictionaryTerm, TestValue> entry : index2.entrySet()) {
                assertTrue(index.containsKey(entry.getKey()));
                assertEquals(index.get(entry.getKey()), entry.getValue());
            }
            for (Map.Entry<DictionaryTerm, TestValue> entry : index.entrySet()) {
                assertTrue(index2.containsKey(entry.getKey()));
                assertEquals(index2.get(entry.getKey()), entry.getValue());
            }
            System.out.println("Finished!");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            pool.stop();
            channel.close();
            file.delete();
        }
    }
}
