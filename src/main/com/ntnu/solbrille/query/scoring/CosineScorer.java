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
	
	public static float k1 = 1.5f;
	public static float k2 = 10.0f;
	public static float b = 0.75f;
	
	private long N;
	
	public CosineScorer(DocumentStatisticsIndex statistics, OccurenceIndex index){
		this.statistics = statistics;
		this.index = index;
	}
	
	@Override
	public float getScore(QueryResult result, QueryRequest request) {
		//TODO: avoid recalculating sumwtq, maxtd, etc.
		N = statistics.getTotalNumberOfDocuments();
		long maxtq = 0, tmptq;
		long maxtd = 0, tmptd;
		for (DictionaryTerm term : result.getTerms() ) {
			tmptd = result.getOccurences(term).getPositionList().size();
			tmptq = request.getQueryOccurenceCount(term);
			if (maxtq < tmptq) maxtq = tmptq;
			if (maxtd < tmptd) maxtd = tmptd;//FIXM bugbug: det skal v¾re max for alle ord i orgboken, ikke bare de som er med i sp¿rringen
		}
		
		double sumwtdq = 0.0;
		double sumwtq = 0.0;
		double sumwtd = 0.0;
		for (DictionaryTerm term : result.getTerms() ) {
			float tfid =  ((float)result.getOccurences(term).getPositionList().size()) / maxtd;
			long fq =  request.getQueryOccurenceCount(term);		//FIXME - number of occurrences in the query
			long df = request.getDocumentCount(term);
			double idf =  Math.log(((float)N)/df);
			double wtd = tfid * idf;
			double wtq = (0.5 + (0.5*fq)/maxtq)* idf;
			sumwtdq += wtq * wtd;
			sumwtq += wtq * wtq;
			sumwtd += wtd * wtd;
			System.out.println(maxtd);
		}
		float res = (float) (sumwtdq / ( Math.sqrt(sumwtq) * Math.sqrt(sumwtd)));
		//System.out.println(sumwtq);
		return res;	
	}
}
