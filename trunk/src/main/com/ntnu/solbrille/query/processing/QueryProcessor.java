package com.ntnu.solbrille.query.processing;

import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.preprocessing.QueryPreprocessor;
import com.ntnu.solbrille.utils.Heap;
import com.sun.tools.javac.util.List;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class QueryProcessor {
	private QueryPreprocessor preprocessor;
	private QueryProcessingComponent src;
	
	public QueryProcessor(QueryProcessingComponent source, QueryPreprocessor preprocessor){
		src = source;
		this.preprocessor = preprocessor;
	}
	
	public QueryResult[] processQuery(String strquery, int start, int end){
		QueryRequest query = preprocessor.preprocess(strquery);
		int rescnt = start - end;
		
		if (!src.loadQuery(query) || (rescnt <= 0)) return new QueryResult[0];
		
		Heap<QueryResult> results = new Heap<QueryResult>();
		
		for (int i=0; i<end && src.hasNext(); i++){
			QueryResult next = src.next();
			if (results.size() < rescnt) 
				results.add(next);
			else {
				QueryResult least = results.remove();
				results.add( least.compareTo(next) >= 0 ? least : next);
			}
		}
		
		QueryResult[] res = (QueryResult[]) results.toArray();
		
		if (start > res.length) return null;
		else if (end > res.length) rescnt = start - res.length;
		
		QueryResult[] ret = new QueryResult[rescnt]; 
		for (int i=0; i<rescnt; i++) ret[i] = res[end - i - 1];
		
		return ret;
	}
	
}
