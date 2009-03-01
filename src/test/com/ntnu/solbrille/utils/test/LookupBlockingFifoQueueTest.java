package com.ntnu.solbrille.utils.test;

import com.ntnu.solbrille.utils.LookupBlockingFifoQueue;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class LookupBlockingFifoQueueTest extends TestCase {

    public void testQueuing() throws InterruptedException {
        LookupBlockingFifoQueue<Integer> queue = new LookupBlockingFifoQueue<Integer>(new Object());
        for (int i : new int[]{1, 2, 3, 4, 5}) {
            queue.offer(i);
        }
        for (int i : new int[]{2, 3, 1, 5, 4}) {
            assertTrue(queue.contains(i));
        }
        assertEquals(5, queue.size());
        List<Integer> c = new ArrayList<Integer>();
        assertEquals(5, queue.drainTo(c));
        assertEquals(0, queue.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(Integer.valueOf(i + 1), c.get(i));
        }

        for (int i : new int[]{1, 2, 3, 4, 5}) {
            queue.offer(i);
        }
        for (int i = 0; i < 5; i++) {
            assertEquals(Integer.valueOf(i + 1), queue.take());
        }
    }


}
