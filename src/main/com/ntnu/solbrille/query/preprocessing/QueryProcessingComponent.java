package com.ntnu.solbrille.query.preprocessing;

import java.util.Iterator;

import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryResult;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public interface QueryProcessingComponent extends Iterator<QueryResult>{
	public void addSource(QueryProcessingComponent source);
	public boolean loadQuery(QueryRequest query);
}
