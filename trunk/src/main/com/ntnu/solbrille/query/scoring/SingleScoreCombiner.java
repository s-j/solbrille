package com.ntnu.solbrille.query.scoring;

import com.ntnu.solbrille.query.*;
import com.ntnu.solbrille.query.matching.Matcher;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
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
