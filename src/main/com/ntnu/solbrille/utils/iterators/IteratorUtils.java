package com.ntnu.solbrille.utils.iterators;

import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class IteratorUtils {

    private static class ChainedIterable<T> implements Iterable<T> {
        private Iterable[] iterables;

        ChainedIterable(Iterable<T>... iterables) {
            this.iterables = iterables;
        }

        @Override
        public Iterator<T> iterator() {
            Iterator<T>[] iters = new Iterator[iterables.length];
            for (int i = 0; i < iterables.length; i++) {
                iters[i] = iterables[i].iterator();
            }
            return new ChainedIterator<T>(iters);
        }
    }

    public static <T> Iterable<T> chainedIterable(Iterable<T>... iterables) {
        return new ChainedIterable<T>(iterables);
    }
}
