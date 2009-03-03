package com.ntnu.solbrille.query.scoring;

import com.ntnu.solbrille.query.ProcessedQueryResult;
import com.ntnu.solbrille.query.UnprocessedQueryResult;

public interface Scorer {
	public ProcessedQueryResult getScore(UnprocessedQueryResult result);
}
