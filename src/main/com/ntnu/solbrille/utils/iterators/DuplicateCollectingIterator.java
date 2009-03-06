package com.ntnu.solbrille.utils.iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DuplicateCollectingIterator<T> extends AbstractWrappingIterator<Collection<T>, CachedIterator<T>> {

    private final Comparator<T> comparator;

    public DuplicateCollectingIterator(Iterator<T> wrapped) {
        this(null, wrapped);
    }

    public DuplicateCollectingIterator(Comparator<T> comparator, Iterator<T> wrapped) {
        super(new CachedIteratorAdapter<T>(wrapped));
        this.comparator = comparator;
        if (getWrapped().hasNext()) {
            getWrapped().next();
        } else {
            setWrapped(null);
        }
    }

    public boolean hasNext() {
        return getWrapped() != null;
    }

    public Collection<T> next() {
        Collection<T> group = new ArrayList<T>();
        T current = getWrapped().getCurrent();
        group.add(current);
        while (getWrapped().hasNext()) {
            getWrapped().next();
            if (isEqual(getWrapped().getCurrent(), current)) {
                group.add(getWrapped().getCurrent());
            } else {
                break;
            }
        }
        if (isEqual(getWrapped().getCurrent(), current)) {
            setWrapped(null);
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
