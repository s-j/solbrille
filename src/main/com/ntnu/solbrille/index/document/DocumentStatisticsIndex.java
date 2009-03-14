package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.BasicNavigableKeyValueIndex;
import com.ntnu.solbrille.index.NavigableKeyValueIndex;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentStatisticsIndex extends AbstractLifecycleComponent {

    private final OccurenceIndex occurenceIndex;

    private final BufferPool bufferPool;
    private final int systemInfoFileNumber;
    private final int idMappingFileNumber;
    private final int documentStatisticFileNumber;

    private final SystemWideInfoIndex infoIndex = new SystemWideInfoIndex();

    private final NavigableKeyValueIndex<DocumentUriEntry, DocumentIdEntry> idMapping
            = new BasicNavigableKeyValueIndex<DocumentUriEntry, DocumentIdEntry>(
            new DocumentUriEntry.DocumentUriEntryDescriptor(),
            new DocumentIdEntry.DocumentIdEntryDescriptor());

    private final NavigableKeyValueIndex<DocumentIdEntry, DocumentStatisticsEntry> statistics
            = new BasicNavigableKeyValueIndex<DocumentIdEntry, DocumentStatisticsEntry>(
            new DocumentIdEntry.DocumentIdEntryDescriptor(),
            new DocumentStatisticsEntry.DocumentStatisticEntryDescriptor());


    public DocumentStatisticsIndex(
            BufferPool bufferPool,
            OccurenceIndex occurenceIndex,
            int systemInfoFileNumber,
            int idMappingFileNumber,
            int documentStatisticFileNumber) {
        this.bufferPool = bufferPool;
        this.occurenceIndex = occurenceIndex;
        this.systemInfoFileNumber = systemInfoFileNumber;
        this.idMappingFileNumber = idMappingFileNumber;
        this.documentStatisticFileNumber = documentStatisticFileNumber;
    }

    public void start() {
        try {
            infoIndex.initializeFromFile(bufferPool, systemInfoFileNumber, 0);
            idMapping.initializeFromFile(bufferPool, idMappingFileNumber, 0);
            statistics.initializeFromFile(bufferPool, documentStatisticFileNumber, 0);
            setIsRunning(true);
        } catch (IOException e) {
            setFailCause(e);
        } catch (InterruptedException e) {
            setFailCause(e);
        }
    }

    public void stop() {
        setIsRunning(false);
        try {
            infoIndex.writeToFile(bufferPool, systemInfoFileNumber, 0);
            idMapping.writeToFile(bufferPool, idMappingFileNumber, 0);
            statistics.writeToFile(bufferPool, documentStatisticFileNumber, 0);
        } catch (IOException e) {
            setFailCause(e);
        } catch (InterruptedException e) {
            setFailCause(e);
        }
    }

    /**
     * Gets the statistics for the document accociated with the supplied Id.
     *
     * @param documentId The document id to be looked up.
     * @return The statistcs entry for the document.
     */
    public DocumentStatisticsEntry getDocumentStatistics(long documentId) {
        return statistics.get(new DocumentIdEntry(documentId));
    }

    /**
     * Gets the document id for the document with the supplied URI.
     *
     * @param documentUri The document URI to lookup.
     * @return The document URI for the supplied document or -1 if none exists.
     */
    public long getDocumentIdFor(URI documentUri) {
        DocumentIdEntry docuementIdEntry = idMapping.get(new DocumentUriEntry(documentUri));
        if (docuementIdEntry != null) {
            return docuementIdEntry.getDocumentId();
        }
        return -1;
    }

    public long getNextDocumentId() {
        return infoIndex.getNextDocumentId();
    }

    /**
     * Gets the total number of documents.
     *
     * @return The total number of documents.
     */
    public long getTotalNumberOfDocuments() {
        return infoIndex.getTotalNumberOfDocuments();
    }

    /**
     * Gets the total number of tokens indexed.
     *
     * @return The total number of tokens indexed.
     */
    public long getTotalNumberOfTokens() {
        return infoIndex.getTotalNumberOfTokens();
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
    public long getTotalSize() {
        return infoIndex.getTotalDocumentLength();
    }


    /**
     * Gets the average document size for this collection in bytes
     *
     * @return The average document size in bytes
     */
    public long getAvgSize() {
        return getTotalSize() / getTotalNumberOfDocuments();
    }

    public void registerDocuemntIndexed(String uri, long documentId, long numberOfTokens, long documentLength) throws URISyntaxException {
        idMapping.put(new DocumentUriEntry(new URI(uri)), new DocumentIdEntry(documentId));
        statistics.put(new DocumentIdEntry(documentId), new DocumentStatisticsEntry(documentLength, numberOfTokens));
        infoIndex.registerDocumentIndexed(documentLength, numberOfTokens);
    }
}
