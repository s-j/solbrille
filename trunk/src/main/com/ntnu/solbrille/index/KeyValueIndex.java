package com.ntnu.solbrille.index;

import java.util.Map;

/**
 * An interface that wraps <IndexKeyEntry, IndexEnty> as a Map and also is an index.
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public interface KeyValueIndex<K extends IndexKeyEntry<K>, V extends IndexEntry> extends Map<K, V>, Index {
}
