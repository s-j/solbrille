package com.ntnu.solbrille.utils;
/**
 * An implementation of PriorityQueue in case we might need specialized functionality. Used by <ResultSplicer>
 *
 * Created by IntelliJ IDEA.
 * User: janmaxim
 * Date: Feb 26, 2009
 * Time: 11:21:22 AM
 */

import java.util.Comparator;
import java.util.PriorityQueue;

public class Heap<T> extends PriorityQueue<T> {

    public Heap() {
    }

    public Heap(Comparator<T> comparator) {
        super(16, comparator);
    }

    public void headChanged() {
        offer(poll());
    }
}
