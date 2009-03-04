package com.ntnu.solbrille.utils.iterators;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public interface SkippableIterator<T> extends CachedIterator<T> {
    public void skipTo(T target);

    public void skipPast(T target);
}
