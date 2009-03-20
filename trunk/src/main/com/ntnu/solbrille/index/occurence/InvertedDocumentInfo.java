package com.ntnu.solbrille.index.occurence;

import com.ntnu.solbrille.utils.Pair;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class InvertedDocumentInfo {

    private final Pair<DictionaryTerm, Long> mostFrequentTerm;
    private final long uniqueTerms;
    private final long totalTokens;
    private final long documentSize;

    public InvertedDocumentInfo(long documentSize, Pair<DictionaryTerm, Long> mostFrequentTerm, long totalTokens, long uniqueTerms) {
        this.documentSize = documentSize;
        this.mostFrequentTerm = mostFrequentTerm;
        this.totalTokens = totalTokens;
        this.uniqueTerms = uniqueTerms;
    }

    public long getDocumentSize() {
        return documentSize;
    }

    public Pair<DictionaryTerm, Long> getMostFrequentTerm() {
        return mostFrequentTerm;
    }

    public long getTotalTokens() {
        return totalTokens;
    }

    public long getUniqueTerms() {
        return uniqueTerms;
    }
}
