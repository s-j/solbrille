package com.ntnu.solbrille.query.processing;

import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.preprocessing.QueryPreprocessor;

import java.util.Arrays;
import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class QueryProcessor {

    private static final class QueryProcessorMutex {
    }

    private final Object mutex = new QueryProcessorMutex();

    private QueryPreprocessor preprocessor;
    private QueryProcessingComponent src;

    public QueryProcessor(QueryProcessingComponent source, QueryPreprocessor preprocessor) {
        src = source;
        this.preprocessor = preprocessor;
    }

    public QueryResult[] processQuery(String strquery, int start, int end) {
        synchronized (mutex) {
            QueryRequest query = preprocessor.preprocess(strquery);
            int rescnt = end - start;

            if (!src.loadQuery(query) || (rescnt <= 0)) return new QueryResult[0];

            NavigableSet<QueryResult> results = new TreeSet<QueryResult>(Collections.reverseOrder());
            float best = Float.NEGATIVE_INFINITY;
            while (src.hasNext()) {
                QueryResult next = src.next();
                best = Math.max(best, next.getScore());
                if (results.size() > rescnt) {
                    QueryResult least = results.last();
                    if (least.compareTo(next) <= 0) {
                        results.remove(least);
                        results.add(next);
                    }
                } else {
                    results.add(next);
                }
            }
            System.out.println("Best: " + best);
            QueryResult[] res = results.toArray(new QueryResult[results.size()]);
            Arrays.sort(res, Collections.reverseOrder());
            if (start > res.length) return null;
            else if (end > res.length) {
                end = res.length;
                rescnt = end - start;
            }

            QueryResult[] ret = new QueryResult[rescnt];
            for (int i = 0; i < rescnt; i++) ret[i] = res[start + i];

            return ret;
        }
    }

}
