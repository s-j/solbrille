package com.ntnu.solbrille.utils.iterators;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public enum SkipType {

    SKIP_TO() {
        @Override
        public <T extends Comparable<T>> boolean shouldContinue(T target, T current) {
            return current.compareTo(target) < 0;
        }
    },
    SKIP_PAST() {
        @Override
        public <T extends Comparable<T>> boolean shouldContinue(T target, T current) {
            return current.compareTo(target) <= 0;
        }
    };

    public abstract <T extends Comparable<T>> boolean shouldContinue(T target, T current);


}
