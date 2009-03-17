package com.ntnu.solbrille.query.scoring;

import com.ntnu.solbrille.index.document.DocumentStatisticsEntry;
import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;
import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryResult;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class OkapiScorer implements Scorer {
    private DocumentStatisticsIndex statistics;
    private OccurenceIndex index;

    public static float k1 = 1.5f;
    public static float k2 = 100.0f;
    public static float b = 0.75f;

    private long N, avdl;

    public OkapiScorer(DocumentStatisticsIndex statistics, OccurenceIndex index) {
        this.statistics = statistics;
        this.index = index;

        N = statistics.getTotalNumberOfDocuments();
        avdl = statistics.getAvgSize();
    }

    @Override
    public float getScore(QueryResult result, QueryRequest request) {
        // TODO Auto-generated method stub
        float res = 0.0f;
        DocumentStatisticsEntry entry = statistics.getDocumentStatistics(result.getDocumentId());

        long dl = entry.getDocumentLength();                         //FIXME - length of the document in bytes

        for (DictionaryTerm term : result.getTerms()) {
            long f = result.getOccurences(term).getPositionList().size();
            long df = request.getDocumentCount(term);                 //FIXME - number of docs contains a term
            long fq = request.getQueryOccurenceCount(term);        //FIXME - number of occurrences in the query
            res += Math.log(1 + (N - df + 0.5) / (df + 0.5)) * (((k1 + 1) * f) / (k1 * (1 - b + b * dl / avdl) + f)) * (((k2 + 1) * fq) / (k2 + fq));
        }

        return res;
    }

}
