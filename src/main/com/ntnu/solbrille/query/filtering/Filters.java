package com.ntnu.solbrille.query.filtering;

import java.util.ArrayList;
import java.util.Iterator;

import com.ntnu.solbrille.query.ProcessedQueryResult;
import com.ntnu.solbrille.query.scoring.ScoreCombiner;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class Filters implements Iterator<ProcessedQueryResult> {
	private ProcessedQueryResult current = null;
	private ScoreCombiner sc;
	private ArrayList<Filter> filters = new ArrayList<Filter>();
	
	public Filters(ScoreCombiner sc){
		this.sc = sc;
	}
	
	public void addFilter(Filter f){
		filters.add(f);
	}
	
	@Override
	public boolean hasNext() {
		if (filters.size() == 0) return sc.hasNext();
		
		if (current != null) return true;
		else {
			while (sc.hasNext()){
				current = sc.next();
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
	public ProcessedQueryResult next() {
		if (filters.size() == 0) return sc.next();
		
		ProcessedQueryResult ret = current;
		current = null;
		return ret;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	

}
