package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexEntry;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.utils.Pair;

import java.net.URI;
import java.nio.ByteBuffer;

/**
 * An entry representation used by {@link DocumentStatisticsIndex}
 * 
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 * @see DocumentStatisticsIndex
 */
public class DocumentStatisticsEntry implements IndexEntry {

    public static class DocumentStatisticEntryDescriptor implements IndexEntryDescriptor<DocumentStatisticsEntry> {

        private static final DocumentUriEntry.DocumentUriEntryDescriptor URI_DESC = new DocumentUriEntry.DocumentUriEntryDescriptor();
        private static final DictionaryTerm.DictionaryTermDescriptor TERM_DESC = new DictionaryTerm.DictionaryTermDescriptor();

        @Override
        public DocumentStatisticsEntry readIndexEntry(ByteBuffer buffer) {
            DocumentUriEntry uri = URI_DESC.readIndexEntry(buffer);
            Pair<DictionaryTerm, Long> mostFrequent = new Pair<DictionaryTerm, Long>(TERM_DESC.readIndexEntry(buffer), buffer.getLong());
            DocumentStatisticsEntry statisticsEntry = new DocumentStatisticsEntry(
                    buffer.getLong(), buffer.getLong(), buffer.getLong(), mostFrequent, uri.getDocumentUri());
            statisticsEntry.setTfIdfVectorLength(buffer.getFloat());
            return statisticsEntry;
        }
    }

    private final DocumentUriEntry uriEntry;

    private long documentLength;
    private long numberOfTokens;
    private long uniqueTerms;

    private float tfIdfWeight;

    private Pair<DictionaryTerm, Long> mostFrequentTerm;

    public DocumentStatisticsEntry(
            long documentLength,
            long numberOfTokens,
            long uniqueTerms,
            Pair<DictionaryTerm, Long> mostFrequentTerm,
            URI docUri) {
        this.documentLength = documentLength;
        this.numberOfTokens = numberOfTokens;
        this.uniqueTerms = uniqueTerms;
        this.mostFrequentTerm = mostFrequentTerm;
        uriEntry = new DocumentUriEntry(docUri);
    }

    public Pair<DictionaryTerm, Long> getMostFrequentTerm() {
        return mostFrequentTerm;
    }

    public void setMostFrequentTerm(Pair<DictionaryTerm, Long> mostFrequentTerm) {
        this.mostFrequentTerm = mostFrequentTerm;
    }

    public long getUniqueTerms() {
        return uniqueTerms;
    }

    public void setUniqueTerms(long uniqueTerms) {
        this.uniqueTerms = uniqueTerms;
    }

    public URI getURI() {
        return uriEntry.getDocumentUri();
    }

    public void setURI(URI uri) {
        uriEntry.setDocumentUri(uri);
    }

    public long getNumberOfTokens() {
        return numberOfTokens;
    }

    public void setNumberOfTokens(long numberOfTokens) {
        this.numberOfTokens = numberOfTokens;
    }

    public long getDocumentLength() {
        return documentLength;
    }

    public void setDocumentLength(long documentLength) {
        this.documentLength = documentLength;
    }

    public float getTfIdfVectorLength() {
        return tfIdfWeight;
    }

    public void setTfIdfVectorLength(float weight) {
        tfIdfWeight = weight;
    }

    @Override
    public int getSeralizedLength() {
        return 4 * Constants.LONG_SIZE + Constants.FLOAT_SIZE + mostFrequentTerm.getFirst().getSeralizedLength() + uriEntry.getSeralizedLength();
    }

    @Override
    public void serializeToByteBuffer(ByteBuffer buffer) {
        uriEntry.serializeToByteBuffer(buffer);
        mostFrequentTerm.getFirst().serializeToByteBuffer(buffer);
        buffer.putLong(mostFrequentTerm.getSecond());
        buffer.putLong(documentLength);
        buffer.putLong(numberOfTokens);
        buffer.putLong(uniqueTerms);
        buffer.putFloat(tfIdfWeight);
    }

    @Override
    public String toString() {
        return "(uri: " + uriEntry.getDocumentUri()
                + ", length: " + documentLength
                + ", TF*IDF weight: " + getTfIdfVectorLength()
                + ", Unique terms: " + uniqueTerms
                + ", Number of tokens: " + numberOfTokens
                + ", Most frequent term: " + mostFrequentTerm + " )";
    }
}
