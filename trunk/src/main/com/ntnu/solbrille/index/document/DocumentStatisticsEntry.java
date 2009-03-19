package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.Constants;
import com.ntnu.solbrille.index.IndexEntry;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.utils.Pair;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentStatisticsEntry implements IndexEntry {

    public static class DocumentStatisticEntryDescriptor implements IndexEntryDescriptor<DocumentStatisticsEntry> {

        public DocumentStatisticsEntry readIndexEntryDescriptor(ByteBuffer buffer) {
            return new DocumentStatisticsEntry(buffer.getLong(), buffer.getLong());
        }
    }

    private long documentLength;
    private long numberOfTokens;

    public DocumentStatisticsEntry(long documentLength, long numberOfTokens) {
        this.documentLength = documentLength;
        this.numberOfTokens = numberOfTokens;
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

    public Pair<DictionaryTerm, Long> getMostFrequentTerm(){
    	return new Pair(null, 10); //TODO
    }
    
    public long getNumberOfUniqueTerms(){
    	return 42; //TODO
    }
    
    public void setDocumentLength(long documentLength) {
        this.documentLength = documentLength;
    }

    public int getSeralizedLength() {
        return 2 * Constants.LONG_SIZE;
    }

    public void serializeToByteBuffer(ByteBuffer buffer) {
        buffer.putLong(documentLength);
        buffer.putLong(numberOfTokens);
    }
}
