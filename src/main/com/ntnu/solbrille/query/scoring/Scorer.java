package com.ntnu.solbrille.query.scoring;

import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryResult;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */

public interface Scorer {

	public float getScore(QueryResult result, QueryRequest request);
}
