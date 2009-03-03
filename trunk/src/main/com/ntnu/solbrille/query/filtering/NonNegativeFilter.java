package com.ntnu.solbrille.query.filtering;

import com.ntnu.solbrille.query.ProcessedQueryResult;

public class NonNegativeFilter implements Filter{

	@Override
	public boolean filter(ProcessedQueryResult result) {
		return result.getScore()>0;
	}
	
	
}
