package com.ntnu.solbrille.utils.iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DuplicateCollectingIterator<T> implements Iterator<Collection<T>> {

    private final Comparator<T> comparator;
    private final CachedIteratorAdapter<T> wrapped;

    private T current;

    public DuplicateCollectingIterator(Iterator<T> wrapped) {
        this(null, wrapped);
    }

    public DuplicateCollectingIterator(Comparator<T> comparator, Iterator<T> wrapped) {
        this.comparator = comparator;
        this.wrapped = new CachedIteratorAdapter<T>(wrapped);
        if (this.wrapped.hasNext()) {
            this.wrapped.next();
        }
    }

    public boolean hasNext() {
        return wrapped.hasNext();
    }

    public Collection<T> next() {
        List<T> group = new ArrayList<T>();
        current = wrapped.getCurrent();
        group.add(current);
        while (wrapped.hasNext()) {
            wrapped.next();
            if (isEqual(wrapped.getCurrent(), current)) {
                group.add(wrapped.getCurrent());
            } else {
                break;
            }
        }
        return group;
    }

    private boolean isEqual(T o1, T o2) {
        if (comparator == null) {
            return o1.equals(o2);
        } else {
            return comparator.compare(o1, o2) == 0;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
