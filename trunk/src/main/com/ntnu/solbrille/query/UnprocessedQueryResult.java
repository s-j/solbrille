package com.ntnu.solbrille.query;

import java.util.HashMap;

import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.utils.IntArray;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class UnprocessedQueryResult implements Comparable<UnprocessedQueryResult>{
    private final long documentId;
    private final HashMap<DictionaryTerm, IntArray> positionLists = new HashMap<DictionaryTerm, IntArray>();
	
    public UnprocessedQueryResult(long documentId){
    	this.documentId = documentId;
    }
    
    public void addOccurrences(DictionaryTerm term, IntArray occs){
    	positionLists.put(term, occs);
    }
    
    public long getDocumentId(){
    	return documentId;
    }
    
    public IntArray getOccurrences(DictionaryTerm term){
    	return positionLists.get(term);
    }

	@Override
	public int compareTo(UnprocessedQueryResult result) {
		if (documentId < result.documentId) return -1;
		else if (documentId == result.documentId) return 0;
		else return 1;
	}
}
