package com.ntnu.solbrille.query.processor;

import com.ntnu.solbrille.query.PreparedQuery;
import com.ntnu.solbrille.query.ProcessedQueryResult;
import com.ntnu.solbrille.query.UnPreparedQuery;
import com.ntnu.solbrille.query.UnprocessedQueryResult;
import com.ntnu.solbrille.query.filtering.Filter;
import com.ntnu.solbrille.query.matching.Matcher;
import com.ntnu.solbrille.query.preprocessing.QueryPreprocessor;
import com.ntnu.solbrille.query.scoring.Scorer;
import com.sun.tools.javac.util.List;

public class QueryProcessor {
	List<QueryPreprocessor> preprocessors;
	Matcher queryMatcher;
	List<Scorer> resultScorers;
	List<Filter> resultFilters;
	
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
	
}
