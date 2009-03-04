package com.ntnu.solbrille.query.scoring;

import java.util.ArrayList;

import com.ntnu.solbrille.feeder.processors.DocumentProcessor;
import com.ntnu.solbrille.query.*;
import com.ntnu.solbrille.query.matching.Matcher;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.iterators.IteratorMerger;
import com.sun.tools.javac.util.List;


public class SingleScoreCombiner implements ScoreCombiner{
	private Matcher src;
	private Scorer scorer;

	public SingleScoreCombiner(Matcher src, Scorer scorer){
		this.src = src;
		this.scorer = scorer;
	}

	@Override
	public boolean hasNext() {
		return src.hasNext();
	}

	@Override
	public ProcessedQueryResult next() {
		assert src.hasNext();
		UnprocessedQueryResult unpresult = src.next();
		ProcessedQueryResult procresult = scorer.getScore(unpresult);
		return procresult;
	}

	@Override
	public void remove() {
	     throw new UnsupportedOperationException();	
	}
	
}
