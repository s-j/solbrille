package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.utils.Closeable;
import com.ntnu.solbrille.utils.iterators.SkipType;
import com.ntnu.solbrille.utils.iterators.SkippableIterator;

import java.io.IOException;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
class TermIterator implements SkippableIterator<DocumentOccurence>, Comparable<TermIterator>, Closeable {

    private final InvertedListReader.Reader reader;
    private final long numberOfDocuments;
    private DocumentOccurence current;

    TermIterator(InvertedListReader invertedListReader, DictionaryTerm term, InvertedListPointer termPointer) throws IOException, InterruptedException {
        reader = invertedListReader.getNewReader();
        reader.initializeOnTerm(term, termPointer);
        numberOfDocuments = reader.remainingDocumentsInCurrentTerm;
    }

    @Override
    public DocumentOccurence getCurrent() {
        if (current == null && hasNext()) {
            next();
        }
        return current;
    }

    public long getNumberOfDocuments() {
        return numberOfDocuments;
    }

    @Override
    public boolean hasNext() {
        return reader.remainingDocumentsInCurrentTerm > 0;
    }

    @Override
    public DocumentOccurence next() {
        assert hasNext();
        try {
            current = reader.readNextDocument();
            return current;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return null;
    }

    @Override
    public void skipTo(DocumentOccurence target) {
        skip(target, SkipType.SKIP_TO);
    }

    @Override
    public void skipPast(DocumentOccurence target) {
        skip(target, SkipType.SKIP_PAST);
    }

    private void skip(DocumentOccurence target, SkipType type) {
        while (hasNext() && type.shouldContinue(target, current)) {
            next();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        reader.close();
    }

    @Override
    public int compareTo(TermIterator o) {
        return getCurrent().compareTo(o.getCurrent());
    }
}
