package com.ntnu.solbrille.query.scoring;

import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;
import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryResult;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class CosineScorer implements Scorer{
	private DocumentStatisticsIndex statistics;
	private OccurenceIndex index;
	private QueryRequest query;
	
	public CosineScorer(DocumentStatisticsIndex statistics, OccurenceIndex index){
		this.statistics = statistics;
		this.index = index;
	}
	
    public boolean loadQuery(QueryRequest query){
    	this.query = query;
    	return true;
    }
	
	@Override
	public float getScore(QueryResult result) {
		//TODO: avoid recalculating sumwtq, maxtd, etc.
		long N = statistics.getTotalNumberOfDocuments();
		long maxtq = 0, tmptq;
		long maxtd = 0, tmptd;
		for (DictionaryTerm term : result.getTerms() ) {
			tmptd = result.getStatisticsEntry().getMostFrequentTerm().getSecond();
			tmptq = query.getQueryOccurenceCount(term);
			if (maxtq < tmptq) maxtq = tmptq;
			if (maxtd < tmptd) maxtd = tmptd;
        }
		
		double sumwtdq = 0.0;
		for (DictionaryTerm term : result.getTerms() ) {
			float tfid =  ((float)result.getOccurences(term).getPositionList().size()) / maxtd;
			long fq =  query.getQueryOccurenceCount(term);
			long df = query.getDocumentCount(term);
			double idf =  Math.log(((float)N)/df);
			double wtd = tfid * idf;
			double wtq = (0.5 + (0.5*fq)/maxtq)* idf;
			sumwtdq += wtq * wtd;
		}
		return (float) (sumwtdq/result.getStatisticsEntry().getTfIdfDocumentWeight());
	}
}
