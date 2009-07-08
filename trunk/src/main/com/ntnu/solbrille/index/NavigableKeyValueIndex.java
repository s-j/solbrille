package com.ntnu.solbrille.index;

import java.util.NavigableMap;

/**
 * Interface to any navigable key value index structure. Defines a navigable map with
 * ability to initialize from a file, and to dump it's content to a file.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public interface NavigableKeyValueIndex<K extends IndexKeyEntry<K>, V extends IndexEntry>
        extends NavigableMap<K, V>, KeyValueIndex<K, V> {

}
