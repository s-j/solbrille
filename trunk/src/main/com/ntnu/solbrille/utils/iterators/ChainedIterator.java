package com.ntnu.solbrille.utils.iterators;

import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class ChainedIterator<T> implements Iterator<T> {

    private int current = 0;
    private Iterator[] iterators;

    public ChainedIterator(Iterator<T>... iterators) {
        this.iterators = iterators;
    }


    @Override
    public boolean hasNext() {
        if (current >= iterators.length) {
            return false;
        }
        if (iterators[current].hasNext()) {
            return true;
        }
        current++;
        return hasNext();
    }

    @Override
    public T next() {
        return ((Iterator<T>) iterators[current]).next();
    }

    @Override
    public void remove() {
        iterators[current].remove();
    }
}
