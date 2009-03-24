package com.ntnu.solbrille.utils.iterators;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class CastingIteratorToEnumerator<T> implements Iterator<T> {


    private final Enumeration wrapped;

    public CastingIteratorToEnumerator(Enumeration wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean hasNext() {
        return wrapped.hasMoreElements();
    }

    @Override
    public T next() {
        return (T) wrapped.nextElement();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
