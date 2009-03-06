package com.ntnu.solbrille.utils.iterators;

import com.ntnu.solbrille.utils.Pair;

import java.util.Iterator;

/**
 * A itterator which wraps the elements in a wrapped iterator in a pair associated with some tag.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class AnnotatingIterator<TSource, TAnnotation> extends AbstractWrappingIterator<Pair<TSource, TAnnotation>, Iterator<TSource>> {

    private final TAnnotation tag;

    public AnnotatingIterator(Iterator<TSource> wrapped, TAnnotation tag) {
        super(wrapped);
        this.tag = tag;
    }

    public boolean hasNext() {
        return getWrapped().hasNext();
    }

    public Pair<TSource, TAnnotation> next() {
        return new Pair<TSource, TAnnotation>(getWrapped().next(), tag);
    }

    public void remove() {
        getWrapped().remove();
    }
}
