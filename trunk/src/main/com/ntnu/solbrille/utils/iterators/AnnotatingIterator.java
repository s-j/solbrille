package com.ntnu.solbrille.utils.iterators;

import com.ntnu.solbrille.utils.Pair;

import java.util.Iterator;

/**
 * A itterator which wraps the elements in a wrapped iterator in a pair associated with some tag.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class AnnotatingIterator<TSource, TAnnotation> implements Iterator<Pair<TSource, TAnnotation>> {

    private final TAnnotation tag;
    private final Iterator<TSource> wrapped;

    public AnnotatingIterator(Iterator<TSource> wrapped, TAnnotation tag) {
        this.tag = tag;
        this.wrapped = wrapped;
    }

    public boolean hasNext() {
        return wrapped.hasNext();
    }

    public Pair<TSource, TAnnotation> next() {
        return new Pair<TSource, TAnnotation>(wrapped.next(), tag);
    }

    public void remove() {
        wrapped.remove();
    }
}
