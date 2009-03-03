package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.utils.Pair;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public interface InvertedList {
    Iterator<DocumentOccurence> lookupTerm(DictionaryTerm term, InvertedListPointer pointer) throws IOException, InterruptedException;

    Iterator<Pair<DictionaryTerm, InvertedListPointer>> getTermIterator() throws IOException, InterruptedException;
}
