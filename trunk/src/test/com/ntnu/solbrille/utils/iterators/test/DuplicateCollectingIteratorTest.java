package com.ntnu.solbrille.utils.iterators.test;

import com.ntnu.solbrille.utils.iterators.DuplicateCollectingIterator;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DuplicateCollectingIteratorTest extends TestCase {


    private static final Integer[] data = {1, 2, 2, 2, 3, 4, 5, 6, 6, 6, 7, 7};
    private static final int[] counts = {0, 1, 3, 1, 1, 1, 3, 2};

    private static final Integer[] single_data = {0};
    private static final int[] single_counts = {1};

    public void testDuplicateCollection() {
        DuplicateCollectingIterator<Integer> dupeCollect = new DuplicateCollectingIterator<Integer>(Arrays.asList(data).iterator());
        while (dupeCollect.hasNext()) {
            Collection<Integer> dupes = dupeCollect.next();
            assertEquals(counts[dupes.iterator().next()], dupes.size());
        }
        assertFalse(dupeCollect.hasNext());

        dupeCollect = new DuplicateCollectingIterator<Integer>(Arrays.asList(single_data).iterator());
        while (dupeCollect.hasNext()) {
            Collection<Integer> dupes = dupeCollect.next();
            assertEquals(single_counts[dupes.iterator().next()], dupes.size());
        }
        assertFalse(dupeCollect.hasNext());
    }
}
