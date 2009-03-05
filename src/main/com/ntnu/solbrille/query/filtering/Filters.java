package com.ntnu.solbrille.query.filtering;

import java.util.ArrayList;
import java.util.Iterator;

import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.preprocessing.QueryProcessingComponent;
import com.ntnu.solbrille.query.scoring.ScoreCombiner;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */

public class Filters implements QueryProcessingComponent{
	private QueryResult current = null;
	private QueryProcessingComponent src = null;
	private QueryRequest query = null;
	private ArrayList<Filter> filters = new ArrayList<Filter>();
	
	public Filters(){
	}
	
	public void addFilter(Filter f){
		filters.add(f);
	}
	
	@Override
	public boolean hasNext() {
		assert src != null;
		if (filters.size() == 0) return src.hasNext();
		
		if (current != null) return true;
		else {
			while (src.hasNext()){
				current = src.next();
				boolean trip = true;
				for (Filter filter : filters) {
					trip &= filter.filter(current);
				}
				if (!trip) current = null;
				else return true; 
			}
		}
		return false;
	}

	@Override
	public QueryResult next() {
		assert src!= null;
		if (filters.size() == 0) return src.next();
		
		QueryResult ret = current;
		current = null;
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addSource(QueryProcessingComponent source) {
		src = source;
		
	}

	@Override
	public boolean loadQuery(QueryRequest query) {
		this.query = query;
		assert src!= null;
		return src.loadQuery(query);
	}
	

}
