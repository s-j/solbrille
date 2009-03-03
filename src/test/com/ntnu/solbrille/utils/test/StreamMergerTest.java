package com.ntnu.solbrille.utils.test;

import com.ntnu.solbrille.utils.StreamMerger;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class StreamMergerTest extends TestCase {

    public void testEmptyStreamMerger() {
        List<Integer> l1 = new ArrayList<Integer>();
        List<Integer> l2 = new ArrayList<Integer>();
        List<Integer> l3 = new ArrayList<Integer>();
        List<Integer> l4 = new ArrayList<Integer>();
        StreamMerger<Integer> merger = new StreamMerger<Integer>(l1, l2, l3, l4);
        assertFalse(merger.hasNext());
    }

    public void testSomeEmptyStreamMerger() {
        List<Integer> l1 = new ArrayList<Integer>();
        List<Integer> l2 = new ArrayList<Integer>();
        List<Integer> l3 = new ArrayList<Integer>();
        List<Integer> l4 = new ArrayList<Integer>();
        l2.add(7);
        l2.add(9);
        l1.add(2);
        l4.add(1);
        StreamMerger<Integer> merger = new StreamMerger<Integer>(l1, l2, l3, l4);
        assertTrue(merger.hasNext());
        assertEquals(1, (int) merger.next());
        assertTrue(merger.hasNext());
        assertEquals(2, (int) merger.next());
        assertTrue(merger.hasNext());
        assertEquals(7, (int) merger.next());
        assertTrue(merger.hasNext());
        assertEquals(9, (int) merger.next());
        assertFalse(merger.hasNext());
    }
}
