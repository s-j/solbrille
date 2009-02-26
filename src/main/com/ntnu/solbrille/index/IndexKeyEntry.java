package com.ntnu.solbrille.index;

/**
 * Interface for key entries in a index. In addition to the functioanlity
 * specified in {@link IndexEntry} keys need to
 * be comparabale.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public interface IndexKeyEntry<T> extends IndexEntry, Comparable<T> {
}
