package com.ntnu.solbrille.query.matching;

import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.DocumentOccurence;
import com.ntnu.solbrille.index.occurence.LookupResult;
import com.ntnu.solbrille.index.occurence.OccurenceIndex;
import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryRequest.Modifier;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.QueryTermOccurence;
import com.ntnu.solbrille.query.processing.QueryProcessingComponent;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.iterators.CachedIterator;
import com.ntnu.solbrille.utils.iterators.SkippableIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:janmaxim@idi.ntnu.no">Jan Maximilian Winther Kristiansen</a>
 * @version $Id $.
 */
public class Matcher implements QueryProcessingComponent, CachedIterator<QueryResult> {
    private QueryRequest query;
    private OccurenceIndex index;

    private long currentDocumentId;

    private HashMap<Modifier, List<SkippableIterator>> queryTermMap;

    public Matcher(OccurenceIndex index) {
        this.index = index;

        this.currentDocumentId = 0;

        this.queryTermMap = new HashMap<Modifier, List<SkippableIterator>>();
        this.queryTermMap.put(Modifier.AND, new ArrayList<SkippableIterator>());
        this.queryTermMap.put(Modifier.NAND, new ArrayList<SkippableIterator>());
        this.queryTermMap.put(Modifier.OR, new ArrayList<SkippableIterator>());
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public QueryResult next() {
        boolean andMatch = false;
        boolean nandMatch = false;
        boolean match = false;
        while (!match) {

            // Matches the AND-terms. If all iterators are at the same docId, we have a match.
            while (!andMatch) {

                // If an iterator is at the end and there is no match yet, it exists no matches.
                if (!hasNext()) {
                    match = false;
                    break;
                }

                if (!isEqual(queryTermMap.get(Modifier.AND))) {
                    // If not all iterators are at the same DocId, find the maximum DocID and skip to it.
                    long maximum = maximumCurrentDocId(queryTermMap.get(Modifier.AND));
                    for (SkippableIterator si : queryTermMap.get(Modifier.AND)) {
                        si.skipTo(maximum);
                    }
                } else {
                    // If all iterators are at the same DocId, there is an AND-match.
                    // I don't like this way of accessing the element (regarding the index), any suggestions?
                    currentDocumentId = ((DocumentOccurence) queryTermMap.get(Modifier.AND).get(0).getCurrent()).getDocumentId();
                    andMatch = true;
                    break;
                }
            }

            // TODO: OR matches - will this be like a "DON'T_CARE"-sitation?

            // Checks if the currently matched document has any NAND-terms in it, if there is an AND-match.
            for (SkippableIterator si : queryTermMap.get(Modifier.NAND)) {
                si.skipTo(currentDocumentId);
                if (((DocumentOccurence) si.getCurrent()).getDocumentId() == currentDocumentId) {
                    nandMatch = true;
                    break;
                }
            }


            // If there is a AND-match, and not a valid NAND-martch. The result is valid.
            if (andMatch && !nandMatch) {
                match = true;
                break;
            } else {
                // Skip to one value above currentDocumentId in order to prevent looping at the same spot
                // for AND-matches. Should probably be handled elsewhere?
                for (SkippableIterator si : queryTermMap.get(Modifier.AND)) {
                    si.skipPast(currentDocumentId);
                }

                andMatch = false;
                nandMatch = false;
            }
        }

        // Creates a query result as a return value. The return value is null if there is no matches.
        // TODO: Needs to be properly handled and populated. a match -> populated queryset with AND and OR matches. !match -> null.
        QueryResult qs = (match) ? new QueryResult(currentDocumentId) : null;

        return qs;
    }

    public boolean hasNext() {
        //TODO: Each AND-iterator needs to have a next.
        return false;
    }

    public void addSource(QueryProcessingComponent source) {
        throw new UnsupportedOperationException();
    }

    public boolean loadQuery(QueryRequest query) {
        this.query = query;

        for (DictionaryTerm dt : this.query.getTerms()) {
            // a Query has no way of accessing its Modifier?
            try {
                Modifier mod = Modifier.OR;
                QueryTermOccurence termOccs = query.getQueryOccurences(dt);
                while (termOccs.hasNext() && mod != Modifier.NAND) {
                    Pair<Integer, Modifier> occ = termOccs.next();
                    if (mod == Modifier.OR) {
                        mod = occ.getSecond();
                    }
                    if (mod == Modifier.AND && occ.getSecond() == Modifier.NAND) {
                        mod = occ.getSecond();
                    }
                }
                LookupResult result = index.lookup(dt);
                query.setDocumentCount(dt, result.getDocumentCount());
                queryTermMap.get(mod).add(result.getIterator());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        }

        return true;
    }

    private long maximumCurrentDocId(List<SkippableIterator> iteratorList) {
        long maximum = Long.MIN_VALUE;
        for (SkippableIterator si : iteratorList) {
            if (((DocumentOccurence) si.getCurrent()).getDocumentId() > maximum)
                maximum = ((DocumentOccurence) si.getCurrent()).getDocumentId();
        }

        return maximum;
    }

    private boolean isEqual(List<SkippableIterator> iteratorList) {
        if (iteratorList.size() < 1) return false;

        // I don't like this way of accessing the element (regarding hard-coded the index), any suggestions?
        long value = ((DocumentOccurence) iteratorList.get(0).getCurrent()).getDocumentId();
        for (SkippableIterator si : iteratorList) {
            if (((DocumentOccurence) si.getCurrent()).getDocumentId() != value) {
                return false;
            }
        }

        return true;
    }

    public QueryResult getCurrent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
