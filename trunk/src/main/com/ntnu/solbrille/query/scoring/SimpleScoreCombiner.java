package com.ntnu.solbrille.query.scoring;

import java.util.ArrayList;

import com.ntnu.solbrille.query.*;
import com.ntnu.solbrille.query.matching.Matcher;
import com.ntnu.solbrille.query.preprocessing.QueryProcessingComponent;
import com.ntnu.solbrille.utils.Pair;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class SimpleScoreCombiner implements ScoreCombiner{
	private QueryProcessingComponent src = null;
	private ArrayList<Pair<Scorer, Float>> scorers = new ArrayList<Pair<Scorer, Float>>();
    private QueryRequest query = null;
	private boolean normalize = false;
	private float totalWeight = 0.0f;
	
	public SimpleScoreCombiner(boolean normalize){
		this.normalize = normalize;
	}
	
	public void addScorer(Scorer scorer, float weight){
		scorers.add(new Pair<Scorer, Float>(scorer, weight));
		totalWeight += weight;
	}
	

	@Override
	public boolean hasNext() {
		assert src != null;
		return src.hasNext();
	}

	@Override
	public QueryResult next() {
		assert src != null;
		assert src.hasNext();
		
		QueryResult next = src.next();
		float accscore = (float) 0.0;
		
		for (Pair<Scorer,Float> scorerpair : scorers) {
			accscore += scorerpair.getFirst().getScore(next, query) * scorerpair.getSecond();
		}
		
		next.setScore(normalize ? accscore/totalWeight : accscore);
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
