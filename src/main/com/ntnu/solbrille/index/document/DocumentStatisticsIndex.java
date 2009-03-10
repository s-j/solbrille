package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.index.BasicNavigableKeyValueIndex;
import com.ntnu.solbrille.index.NavigableKeyValueIndex;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentStatisticsIndex {

    private final OccurenceIndex occurenceIndex;

    private final NavigableKeyValueIndex<DocumentUriEntry, DocumentIdEntry> idMapping
            = new BasicNavigableKeyValueIndex<DocumentUriEntry, DocumentIdEntry>(
            new DocumentUriEntry.DocumentUriEntryDescriptor(),
            new DocumentIdEntry.DocumentIdEntryDescriptor());

    private final NavigableKeyValueIndex<DocumentIdEntry, DocumentStatisticsEntry> statistics
            = new BasicNavigableKeyValueIndex<DocumentIdEntry, DocumentStatisticsEntry>(
            new DocumentIdEntry.DocumentIdEntryDescriptor(),
            new DocumentStatisticsEntry.DocumentStatisticEntryDescriptor());


    public DocumentStatisticsIndex(OccurenceIndex occurenceIndex) {
        this.occurenceIndex = occurenceIndex;
    }

    /**
     * Gets the statistics for the document accociated with the supplied Id.
     *
     * @param documentId The document id to be looked up.
     * @return The statistcs entry for the document.
     */
    public DocumentStatisticsEntry getDocumentStatistics(long documentId) {
        return new DocumentStatisticsEntry(100);
    }

    /**
     * Gets the total number of documents.
     *
     * @return The total number of documents.
     */
    public long getTotalNumberOfDocuments() {
        return 100;
    }

    /**
     * Gets the total number of tokens indexed.
     *
     * @return The total number of tokens indexed.
     */
    public long getTotalNumberOfTokens() {
        return 1000;
    }

    /**
     * Gets the total number of distinct terms in the dictionary dictionary.
     *
     * @return The total number of distinct terms in the dictionary.
     */
    public long getTotalNumberOfDictionaryTerms() {
        return occurenceIndex.getDictionaryTermCount();
    }
    

    /**
     * Gets the total size of the document collection in bytes
     *
     * @return The total size of the document collection in bytes
     */
    public long getTotalSize(){
    	return (long) 10E+12;
    }
    

    /**
     * Gets the average document size for this collection in bytes
     *
     * @return The average document size in bytes
     */
    public long getAvgSize(){
    	return getTotalSize() / getTotalNumberOfDocuments();
    }
}
