package com.ntnu.solbrille.query.preprocessing;

import com.ntnu.solbrille.query.QueryRequest;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public interface QueryPreprocessor {
	public void preprocess(QueryRequest query);
}
