package com.ntnu.solbrille.query.matching;

import java.util.Iterator;

import com.ntnu.solbrille.index.occurence.InvertedListReader;
import com.ntnu.solbrille.query.PreparedQuery;
import com.ntnu.solbrille.query.UnprocessedQueryResult;

public class Matcher implements Iterator<UnprocessedQueryResult>{
	private PreparedQuery query;
	private InvertedListReader reader;
	
	public Matcher(InvertedListReader reader, PreparedQuery query){
		this.reader=reader;
		this.query=query;
	}
	
	public void remove() {
        throw new UnsupportedOperationException();
    }
	
    public UnprocessedQueryResult next(){
    	//FIXME
    	return null;
    }
	
    public boolean hasNext(){
    	//TODO
		return false;
	}
}
