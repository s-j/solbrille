package com.ntnu.solbrille.utils.iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DuplicateCollectingIterator<T> implements Iterator<Collection<T>> {

    private final Comparator<T> comparator;
    private CachedIterator<T> wrapped;

    public DuplicateCollectingIterator(Iterator<T> wrapped) {
        this(null, wrapped);
    }

    public DuplicateCollectingIterator(Comparator<T> comparator, Iterator<T> wrapped) {
        this.comparator = comparator;
        this.wrapped = new CachedIteratorAdapter<T>(wrapped);
        if (this.wrapped.hasNext()) {
            this.wrapped.next();
        } else {
            this.wrapped = null;
        }
    }

    public boolean hasNext() {
        return wrapped != null;
    }

    public Collection<T> next() {
        Collection<T> group = new ArrayList<T>();
        T current = wrapped.getCurrent();
        group.add(current);
        while (wrapped.hasNext()) {
            wrapped.next();
            if (isEqual(wrapped.getCurrent(), current)) {
                group.add(wrapped.getCurrent());
            } else {
                break;
            }
        }
        if (isEqual(wrapped.getCurrent(), current)) {
            wrapped = null;
        }
        return group;
    }

    private boolean isEqual(T o1, T o2) {
        return comparator == null ? o1.equals(o2) : comparator.compare(o1, o2) == 0;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
