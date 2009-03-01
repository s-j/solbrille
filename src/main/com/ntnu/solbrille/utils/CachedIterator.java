package com.ntnu.solbrille.utils;

import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class CachedIterator<T extends Comparable<T>> implements Iterator<T>, Comparable<CachedIterator<T>> {

    private Iterator<T> wrapped;
    private T current;

    public CachedIterator(Iterator<T> wrapped) {
        this.wrapped = wrapped;
    }

    public boolean hasNext() {
        return wrapped.hasNext();
    }

    public T getCurrent() {
        return current;
    }

    public T next() {
        current = wrapped.next();
        return current;
    }

    public void remove() {
        wrapped.remove();
        current = null;
    }

    public int compareTo(CachedIterator<T> o) {
        if (current == null) {
            return o.current == null ? 0 : -1;
        }
        return o.current == null ? 1 : current.compareTo(o.current);
    }
}
