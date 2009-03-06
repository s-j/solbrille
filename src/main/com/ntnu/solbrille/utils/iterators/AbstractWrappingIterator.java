package com.ntnu.solbrille.utils.iterators;

import com.ntnu.solbrille.utils.Closable;

import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public abstract class AbstractWrappingIterator<T, TIter extends Iterator<?>> implements Closable, Iterator<T> {
    private TIter wrapped;

    protected AbstractWrappingIterator(TIter wrapped) {
        this.wrapped = wrapped;
    }

    public void close() {
        if (wrapped instanceof Closable) {
            ((Closable) wrapped).close();
        }
    }

    protected TIter getWrapped() {
        return wrapped;
    }

    protected void setWrapped(TIter wrapped) {
        this.wrapped = wrapped;
    }
}
