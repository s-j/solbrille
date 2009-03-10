package com.ntnu.solbrille.query.scoring;

import com.ntnu.solbrille.query.*;
import com.ntnu.solbrille.query.matching.Matcher;
import com.ntnu.solbrille.query.processing.QueryProcessingComponent;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class SingleScoreCombiner implements ScoreCombiner{
	private QueryProcessingComponent src;
	private Scorer scorer;
	private QueryRequest query = null;

	public SingleScoreCombiner(Scorer scorer){
		this.scorer = scorer;
	}

	@Override
	public boolean hasNext() {
		return src.hasNext();
	}

	@Override
	public QueryResult next() {
		assert src.hasNext();
		QueryResult next = src.next();
		next.setScore(scorer.getScore(next, query));
		return next;
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
		return src.loadQuery(query);
	}
}
