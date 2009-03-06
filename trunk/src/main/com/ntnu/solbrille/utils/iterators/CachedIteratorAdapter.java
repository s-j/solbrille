package com.ntnu.solbrille.utils.iterators;

import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class CachedIteratorAdapter<T> extends AbstractWrappingIterator<T, Iterator<T>> implements Comparable<CachedIterator<T>>, CachedIterator<T> {

    private T current;

    public CachedIteratorAdapter(Iterator<T> wrapped) {
        super(wrapped);
    }

    public boolean hasNext() {
        return getWrapped().hasNext();
    }

    public T getCurrent() {
        return current;
    }

    public T next() {
        current = getWrapped().next();
        return current;
    }

    public void remove() {
        getWrapped().remove();
        current = null;
    }

    public int compareTo(CachedIterator<T> o) {
        if (current == null) {
            return o.getCurrent() == null ? 0 : -1;
        }
        if (current instanceof Comparable<?>) {
            return ((Comparable<T>) current).compareTo(o.getCurrent());
        }
        return o.getCurrent() == null ? current == null ? -1 : 0 : current == null ? 0 : 1;
    }
}
