package com.ntnu.solbrille.query.processing;

import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.preprocessing.QueryPreprocessor;
import com.ntnu.solbrille.query.preprocessing.QueryProcessingComponent;
import com.ntnu.solbrille.utils.Heap;
import com.sun.tools.javac.util.List;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class QueryProcessor {
	private List<QueryPreprocessor> preprocessors;
	private QueryProcessingComponent src;
	
	public QueryProcessor(QueryProcessingComponent source){
		src = source;
	}
	
	public QueryRequest prepareQuery(String strquery){
		QueryRequest query = new QueryRequest(strquery);
		for (QueryPreprocessor preprocessor : preprocessors) {
			preprocessor.preprocess(query);
		}
		return query;
	}
	
	public void init(){
		//do some init
	}
	
	public QueryResult[] processQuery(String strquery, int start, int end){
		QueryRequest query = prepareQuery(strquery);
		assert src.loadQuery(query);
		
		int rescnt = start - end;
		assert rescnt > 0;
		
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
