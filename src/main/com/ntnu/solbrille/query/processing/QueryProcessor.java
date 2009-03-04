package com.ntnu.solbrille.query.processing;

import com.ntnu.solbrille.query.PreparedQuery;
import com.ntnu.solbrille.query.ProcessedQueryResult;
import com.ntnu.solbrille.query.UnPreparedQuery;
import com.ntnu.solbrille.query.UnprocessedQueryResult;
import com.ntnu.solbrille.query.filtering.Filter;
import com.ntnu.solbrille.query.filtering.Filters;
import com.ntnu.solbrille.query.matching.Matcher;
import com.ntnu.solbrille.query.preprocessing.QueryPreprocessor;
import com.ntnu.solbrille.query.scoring.ScoreCombiner;
import com.ntnu.solbrille.query.scoring.Scorer;
import com.ntnu.solbrille.utils.Heap;
import com.sun.tools.javac.util.List;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class QueryProcessor {
	private List<QueryPreprocessor> preprocessors;
	private Matcher queryMatcher;
	private ScoreCombiner scroreCombiner;
	private Filters filters;
	
	public QueryProcessor(Matcher matcher, ScoreCombiner scorecombiner, Filters filters){
		this.queryMatcher = matcher;
		this.scroreCombiner = scorecombiner;
		this.filters = filters;
	}
	
	public PreparedQuery prepareQuery(String query){
		UnPreparedQuery nquery = new UnPreparedQuery(query);
		for (QueryPreprocessor preprocessor : preprocessors) {
			preprocessor.preprocess(nquery);
		}
		PreparedQuery pquery = new PreparedQuery();
		for (String processedTerm : nquery.getTerms()){
			//TODO find corresponding DictionaryTerm
			pquery.addTerm(null, nquery.getModifier(processedTerm));
		}
		return pquery;
	}
	
	public void init(){
		//do some init
	}
	
	public ProcessedQueryResult[] processQuery(String strquery, int start, int end){
		PreparedQuery query = prepareQuery(strquery);
		
		int rescnt = start - end;
		assert rescnt > 0;
		
		Heap<ProcessedQueryResult> results = new Heap<ProcessedQueryResult>();
		
		for (int i=0; i<end && filters.hasNext(); i++){
			ProcessedQueryResult next = filters.next();
			if (results.size() < rescnt) 
				results.add(next);
			else {
				ProcessedQueryResult least = results.remove();
				results.add( least.compareTo(next) >= 0 ? least : next);
			}
		}
		
		ProcessedQueryResult[] res = (ProcessedQueryResult[]) results.toArray();
		
		if (start > res.length) return null;
		else if (end > res.length) rescnt = start - res.length;
		
		ProcessedQueryResult[] ret = new ProcessedQueryResult[rescnt]; 
		for (int i=0; i<rescnt; i++) ret[i] = res[end - i - 1];
		
		return ret;
	}
	
}
