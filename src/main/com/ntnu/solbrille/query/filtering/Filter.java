package com.ntnu.solbrille.query.filtering;

import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.QueryRequest;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public interface Filter {
	public boolean loadQuery(QueryRequest request);
	public boolean filter(QueryResult result);
}
