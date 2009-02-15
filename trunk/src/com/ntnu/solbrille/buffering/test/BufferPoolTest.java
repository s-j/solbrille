package com.ntnu.solbrille.buffering.test;

import com.ntnu.solbrille.buffering.Buffer;
import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.buffering.FileBlockPointer;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class BufferPoolTest extends TestCase {

    public void testBuffers() throws IOException, InterruptedException, ExecutionException {
        ExecutorService es = new ThreadPoolExecutor(1, 4, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

        File file = new File("test.bin");
        file.createNewFile();
        FileChannel channel = new RandomAccessFile(file, "rw").getChannel();
        try {
            BufferPool pool = new BufferPool(10, 16 * 1024);
            final int fileNumber = pool.registerFile(channel, file);
            Collection<Buffer> buffers = new ArrayList<Buffer>(10);
            for (int i = 0; i < 10; i++) {
                buffers.add(pool.pinBuffer(new FileBlockPointer(fileNumber, i)));
            }
            assertEquals(10, buffers.size());
            final BufferPool localPool = pool;
            Future<Buffer> tryPinExtra = es.submit(new Callable<Buffer>() {
                public Buffer call() throws IOException, InterruptedException {
                    return localPool.pinBuffer(new FileBlockPointer(fileNumber, 10));
                }
            });
            Thread.sleep(100); // make sure buffer had been returned if one were available
            assertFalse(tryPinExtra.isDone());

            for (Buffer buf : buffers) { // unpin the ten first buffers pinned
                pool.unPinBuffer(buf);
            }

            Thread.sleep(100); // make sure buffer 10 have been made avilable

            assertTrue(tryPinExtra.isDone());
            Buffer buff10 = tryPinExtra.get();

            assertEquals(buff10.getBlockPointer(), new FileBlockPointer(fileNumber, 10));

            for (int i = 0; i < 100; i++) {
                assertTrue(buff10.getByteBuffer().remaining() > 4);
                buff10.getByteBuffer().putInt(i);
            }
            buff10.setIsDirty(true);
            pool.unPinBuffer(buff10);
            pool.stopPool();

            BufferPool pool1 = new BufferPool(1, 16 * 1024);
            int fileNumber2 = pool1.registerFile(channel, file);
            Buffer buffer = pool1.pinBuffer(new FileBlockPointer(fileNumber2, 10));

            for (int i = 0; i < 100; i++) {
                assertEquals(i, buffer.getByteBuffer().getInt());
            }
            pool1.unPinBuffer(buffer);
            pool1.stopPool();
        }
        finally {
            channel.close();
            file.delete();
        }

        es.shutdown();
    }

    // TODO: Test deletion, concurrency

}
