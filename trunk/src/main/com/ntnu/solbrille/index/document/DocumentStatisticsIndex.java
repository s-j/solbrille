package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.buffering.BufferPool;
import com.ntnu.solbrille.index.BasicNavigableKeyValueIndex;
import com.ntnu.solbrille.index.NavigableKeyValueIndex;
import com.ntnu.solbrille.index.occurence.InvertedDocumentInfo;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * A life-cycle component that provides system wide info, document statistics and document ID to URI mappings 
 * 
 * It wraps {@link SystemWideInfoIndex} and two mappings, {@link DocumentUriEntry} to {@link DocumentIdEntry} and {@link DocumentIdEntry} to {@link DocumentStatisticsEntry}. 
 * It has also a reference to {@link OccurenceIndex}.
 * 
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 * @see OccurenceIndex
 * @see SystemWideInfoIndex
 * @see DocumentStatisticsEntry
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
            writeToDisk();
        } catch (IOException e) {
            setFailCause(e);
        } catch (InterruptedException e) {
            setFailCause(e);
        }
    }

    private void writeToDisk() throws IOException, InterruptedException {
        infoIndex.writeToFile(bufferPool, systemInfoFileNumber, 0);
        idMapping.writeToFile(bufferPool, idMappingFileNumber, 0);
        statistics.writeToFile(bufferPool, documentStatisticFileNumber, 0);
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
        return getTotalNumberOfDocuments() == 0 ? 0 : getTotalSize() / getTotalNumberOfDocuments();
    }

    void registerDocumentIndexed(long documentId, String uri, InvertedDocumentInfo documentInfo) throws URISyntaxException {
        URI docUri = new URI(uri);
        idMapping.put(new DocumentUriEntry(docUri), new DocumentIdEntry(documentId));
        statistics.put(
                new DocumentIdEntry(documentId),
                new DocumentStatisticsEntry(
                        documentInfo.getDocumentSize(),
                        documentInfo.getTotalTokens(),
                        documentInfo.getUniqueTerms(),
                        documentInfo.getMostFrequentTerm(),
                        docUri));
    }

    void updateGlobalStatistics(StaticInformationEntry delta, Map<Long, Float> tfIdfAccumulator) throws IOException, InterruptedException {
        infoIndex.registerDocumentsIndexed(
                delta.getTotalNumberOfDocuments(),
                delta.getTotalDocumentLength(),
                delta.getTotalNumberOfTokens());
        for (Map.Entry<Long, Float> docWeight : tfIdfAccumulator.entrySet()) {
            DocumentIdEntry idEntry = new DocumentIdEntry(docWeight.getKey());
            DocumentStatisticsEntry entry = statistics.get(idEntry);
            entry.setTfIdfVectorLength((float) Math.sqrt(docWeight.getValue()));
            // statistics.put(idEntry, entry); updated reference no need 
        }
        writeToDisk();
    }
}
