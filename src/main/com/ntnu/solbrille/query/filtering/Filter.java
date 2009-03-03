package com.ntnu.solbrille.query.filtering;

import com.ntnu.solbrille.query.ProcessedQueryResult;
public interface Filter {
	
	public boolean filter(ProcessedQueryResult result);
}
