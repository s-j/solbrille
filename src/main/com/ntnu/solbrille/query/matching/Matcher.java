package com.ntnu.solbrille.query.matching;

import com.ntnu.solbrille.index.occurence.OccurenceIndex;
import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.preprocessing.QueryProcessingComponent;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class Matcher implements QueryProcessingComponent{
	private QueryRequest query;
	private OccurenceIndex index;
	
	public Matcher(OccurenceIndex index){
		this.index=index;
	}
	
	public void remove() {
        throw new UnsupportedOperationException();
    }
	
    public QueryResult next(){
    	//FIXME
    	return null;
    }
	
    public boolean hasNext(){
    	//TODO
		return false;
	}

	@Override
	public void addSource(QueryProcessingComponent source) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean loadQuery(QueryRequest query) {
		this.query = query;
		return true;
	}
}
