package com.ntnu.solbrille.query.matching;

import java.util.Iterator;

import com.ntnu.solbrille.index.occurence.InvertedListReader;
import com.ntnu.solbrille.query.PreparedQuery;
import com.ntnu.solbrille.query.UnprocessedQueryResult;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class Matcher implements Iterator<UnprocessedQueryResult>{
	private PreparedQuery query;
	private InvertedListReader reader;
	
	public Matcher(InvertedListReader reader){
		this.reader=reader;

	}
	
	public void load(PreparedQuery query){
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
