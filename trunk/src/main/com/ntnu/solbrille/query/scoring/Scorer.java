package com.ntnu.solbrille.query.scoring;

import com.ntnu.solbrille.query.ProcessedQueryResult;
import com.ntnu.solbrille.query.UnprocessedQueryResult;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public interface Scorer {
	public ProcessedQueryResult getScore(UnprocessedQueryResult result);
}
