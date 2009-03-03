package com.ntnu.solbrille.query.preprocessing;

import com.ntnu.solbrille.query.UnPreparedQuery;

public interface QueryPreprocessor {
	public void preprocess(UnPreparedQuery query);
}
