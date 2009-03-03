package com.ntnu.solbrille.utils.iterators;

import com.ntnu.solbrille.utils.Heap;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Merges multiple sorted collections.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class IteratorMerger<T> implements Iterator<T> {

    private static final class CachedIteratorComparaer<T> implements Comparator<CachedIterator<T>> {

        private final Comparator<T> comp;

        private CachedIteratorComparaer(Comparator<T> comp) {
            if (comp != null) {
                this.comp = comp;
            } else {
                this.comp = new DefaultComparator<T>();
            }
        }

        public int compare(CachedIterator<T> o1, CachedIterator<T> o2) {
            return comp.compare(o1.getCurrent(), o2.getCurrent());
        }

        private class DefaultComparator<T> implements Comparator<T> {

            public int compare(T o1, T o2) {
                if (o1 instanceof Comparable<?>) {
                    return ((Comparable<T>) o1).compareTo((T) o2);
                }
                return 0;
            }
        }
    }

    private final Heap<CachedIterator<T>> heap;

    public IteratorMerger(Iterable<T>... inputs) {
        this(null, inputs);
    }

    public IteratorMerger(Comparator<T> comparator, Iterable<T>... inputs) {
        heap = new Heap<CachedIterator<T>>(new CachedIteratorComparaer<T>(comparator));
        for (Iterable<T> input : inputs) {
            CachedIterator<T> iterator = new CachedIterator<T>(input.iterator());
            if (iterator.hasNext()) {
                iterator.next();
                heap.add(iterator);
            }
        }
    }

    public IteratorMerger(Iterator<T>... inputs) {
        this(null, inputs);
    }

    public IteratorMerger(Comparator<T> comparator, Iterator<T>... inputs) {
        heap = new Heap<CachedIterator<T>>(new CachedIteratorComparaer<T>(comparator));
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
