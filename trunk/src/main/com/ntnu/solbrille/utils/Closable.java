package com.ntnu.solbrille.utils;

/**
 * Interface for classes which require cleanup/resource release etc.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public interface Closable {
    void close();
}
