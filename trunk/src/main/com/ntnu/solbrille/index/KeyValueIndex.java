package com.ntnu.solbrille.index;

import java.util.Map;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public interface KeyValueIndex<K extends IndexKeyEntry<K>, V extends IndexEntry> extends Map<K, V>, Index {
}
