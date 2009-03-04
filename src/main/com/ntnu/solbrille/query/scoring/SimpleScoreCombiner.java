package com.ntnu.solbrille.query.scoring;

import java.util.ArrayList;

import com.ntnu.solbrille.query.*;
import com.ntnu.solbrille.query.matching.Matcher;
import com.ntnu.solbrille.utils.Pair;


public class SimpleScoreCombiner implements ScoreCombiner{
	private Matcher src;
	
	private ArrayList<Pair<Scorer, Float>> scorers = new ArrayList<Pair<Scorer, Float>>();
    
	private boolean normalize = false;
	private float totalWeight = 0.0f;
	
	public SimpleScoreCombiner(Matcher src, boolean normalize){
		this.src = src;
		this.normalize = normalize;
	}
	
	public void addScorer(Scorer scorer, float weight){
		scorers.add(new Pair<Scorer, Float>(scorer, weight));
		totalWeight += weight;
	}
	

	@Override
	public boolean hasNext() {
		return src.hasNext();
	}

	@Override
	public ProcessedQueryResult next() {
		assert src.hasNext();
		
		UnprocessedQueryResult unpresult = src.next();
		ProcessedQueryResult procresult = null;
		
		for (Pair<Scorer,Float> scorerpair : scorers) {
			ProcessedQueryResult partresult = scorerpair.getFirst().getScore(unpresult);
			if (procresult == null) procresult = partresult;
			else procresult.setScore( procresult.getScore() + partresult.getScore() * scorerpair.getSecond());
		}
		if (normalize) procresult.setScore(procresult.getScore()/totalWeight);
		
		return procresult;
	}

	@Override
	public void remove() {
	     throw new UnsupportedOperationException();	
	}
	
}
