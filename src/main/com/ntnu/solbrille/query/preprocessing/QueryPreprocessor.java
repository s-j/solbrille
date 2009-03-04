package com.ntnu.solbrille.query.preprocessing;

import com.ntnu.solbrille.query.UnPreparedQuery;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public interface QueryPreprocessor {
	public void preprocess(UnPreparedQuery query);
}
