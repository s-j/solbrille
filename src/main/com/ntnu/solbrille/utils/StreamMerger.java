package com.ntnu.solbrille.utils;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Merges multiple sorted collections.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class StreamMerger<T> implements Iterator<T> {

    private final Heap<CachedIterator<T>> heap;

    public StreamMerger(Iterable<T>... inputs) {
        this(null, inputs);
    }

    public StreamMerger(Comparator<CachedIterator<T>> comparator, Iterable<T>... inputs) {
        heap = new Heap<CachedIterator<T>>(comparator);
        for (Iterable<T> input : inputs) {
            CachedIterator<T> iterator = new CachedIterator<T>(input.iterator());
            if (iterator.hasNext()) {
                iterator.next();
                heap.add(iterator);
            }
        }
    }

    public StreamMerger(Iterator<T>... inputs) {
        this(null, inputs);
    }

    public StreamMerger(Comparator<CachedIterator<T>> comparator, Iterator<T>... inputs) {
        heap = new Heap<CachedIterator<T>>(comparator);
        for (Iterator<T> input : inputs) {
            CachedIterator<T> iterator = new CachedIterator<T>(input);
            if (iterator.hasNext()) {
                iterator.next();
                heap.add(iterator);
            }
        }
    }

    public boolean hasNext() {
        return !heap.isEmpty();
    }

    public T next() {
        CachedIterator<T> head = heap.peek();
        T next = head.getCurrent();
        if (head.hasNext()) {
            head.next();
            heap.headChanged();
        } else {
            heap.poll();
        }
        return next;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
