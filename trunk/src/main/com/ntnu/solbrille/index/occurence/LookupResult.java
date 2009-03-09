package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.utils.Closeable;
import com.ntnu.solbrille.utils.iterators.SkippableIterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class LookupResult implements Closeable {

    private long documentCount;
    private SkippableIterator<DocumentOccurence> iterator;

    LookupResult(long documentCount, SkippableIterator<DocumentOccurence> iterator) {
        this.documentCount = documentCount;
        this.iterator = iterator;
    }

    public long getDocumentCount() {
        return documentCount;
    }

    public SkippableIterator<DocumentOccurence> getIterator() {
        return iterator;
    }

    public void close() {
        if (iterator instanceof Closeable) {
            ((Closeable) iterator).close();
        }
    }
}
