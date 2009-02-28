package com.ntnu.solbrille.utils.test;

import com.ntnu.solbrille.utils.ResultSplicer;
import com.ntnu.solbrille.utils.Heap;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: janmaxim
 * Date: Feb 26, 2009
 * Time: 2:44:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResultSplicerTest extends TestCase {

    /**
     * Test to verify that ResultSplicer works. This is currently done with Integers.
     */
    public void testScanner()
    {
        ResultSplicer scanner = new ResultSplicer();

        ArrayList<Integer> resultSet1 = new ArrayList<Integer>();
        ArrayList<Integer> resultSet2 = new ArrayList<Integer>();
        ArrayList<Integer> resultSet3 = new ArrayList<Integer>();

        // The only match is document 8 which is present in all result sets
        resultSet1.add(new Integer(1));
        resultSet2.add(new Integer(1));
        resultSet1.add(new Integer(2));
        resultSet2.add(new Integer(3));
        resultSet3.add(new Integer(5));
        resultSet1.add(new Integer(4));
        resultSet2.add(new Integer(6));
        resultSet2.add(new Integer(7));
        resultSet1.add(new Integer(8));
        resultSet2.add(new Integer(8));
        resultSet3.add(new Integer(8));

        Heap testHeap = new Heap();
        testHeap.addAll(resultSet1);
        testHeap.addAll(resultSet2);
        testHeap.addAll(resultSet3);

        List resultSets = new ArrayList();
        resultSets.add(resultSet1);
        resultSets.add(resultSet2);
        resultSets.add(resultSet3);

        List matches = scanner.match(resultSets);

        assertNotNull(matches);

        for (Object o : matches) {
            assertEquals((Integer)o, new Integer(8));
        }
    }

}
