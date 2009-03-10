package com.ntnu.solbrille.query;

import java.util.HashMap;
import java.util.Set;

import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.DocumentOccurence;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class QueryResult implements Comparable<QueryResult>{
    private final long documentId;
    private HashMap<DictionaryTerm, DocumentOccurence> positionLists = new HashMap<DictionaryTerm, DocumentOccurence>();
	private float score;
	
	public QueryResult(long documentId){
		this.documentId=documentId;
	}
	
	public float getScore(){
		return score;
	}
	
	public void setScore(float score){
		this.score = score;
	}
	
	public long getDocumentId(){
		return documentId;
	}
	
	public Set<DictionaryTerm> getTerms(){
		return positionLists.keySet();
	}
	
    public DocumentOccurence getOccurences(DictionaryTerm term){
    	return positionLists.get(term);
    }

    public boolean hasOccurences(DictionaryTerm term){
    	return positionLists.containsKey(term);
    }
    
    public DocumentOccurence addOccurences(DictionaryTerm term, DocumentOccurence occs){
    	return positionLists.put(term, occs);
    }
    
    public DocumentOccurence removeOccurences(DictionaryTerm term){
    	return positionLists.remove(term);
    }

	public int compareTo(QueryResult result){
		return Float.valueOf(score).compareTo(result.score);
	}
}

