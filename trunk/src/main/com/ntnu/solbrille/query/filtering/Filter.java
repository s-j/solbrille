package com.ntnu.solbrille.query.filtering;

import com.ntnu.solbrille.query.ProcessedQueryResult;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public interface Filter {
	
	public boolean filter(ProcessedQueryResult result);
}
