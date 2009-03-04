package com.ntnu.solbrille.utils.iterators;

import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class SkipAdaptor<T extends Comparable<T>> extends CachedIteratorAdapter<T> implements SkippableIterator<T> {

    CachedIteratorAdapter<T> wrapped;

    public SkipAdaptor(Iterator<T> wrapped) {
        super(wrapped);
        this.wrapped = new CachedIteratorAdapter<T>(wrapped);
    }

    public void skipTo(T target) {
        skip(target, SkipType.SKIP_TO);
    }

    public void skipPast(T target) {
        skip(target, SkipType.SKIP_PAST);
    }

    private void skip(T target, SkipType skipType) {
        while (skipType.shouldContinue(target, wrapped.getCurrent()) && wrapped.hasNext()) {
            wrapped.next();
        }
    }

    public boolean hasNext() {
        return wrapped.hasNext();
    }

    public T next() {
        return wrapped.next();
    }

    public void remove() {
        wrapped.remove();
    }

    public int compareTo(CachedIterator<T> o) {
        return getCurrent().compareTo(o.getCurrent());
    }
}
