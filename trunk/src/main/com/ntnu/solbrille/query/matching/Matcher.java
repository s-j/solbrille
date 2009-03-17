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
import com.ntnu.solbrille.utils.Closeable;
import com.ntnu.solbrille.utils.Heap;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.iterators.CachedIterator;
import com.ntnu.solbrille.utils.iterators.IteratorUtils;
import com.ntnu.solbrille.utils.iterators.SkippableIterator;

import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:janmaxim@idi.ntnu.no">Jan Maximilian Winther Kristiansen</a>
 * @version $Id $.
 */
public class Matcher implements QueryProcessingComponent, CachedIterator<QueryResult> {
    private QueryRequest query;
    private OccurenceIndex index;

    private long currentDocumentId;

    private final Heap<SkippableIterator<DocumentOccurence>> andTerms = new Heap();
    private final Heap<SkippableIterator<DocumentOccurence>> nandTerms = new Heap();
    private final Heap<SkippableIterator<DocumentOccurence>> orTerms = new Heap();
    private final Map<SkippableIterator<DocumentOccurence>, DictionaryTerm> iteratorToTerm = new IdentityHashMap();
    private final Map<Modifier, Heap<SkippableIterator<DocumentOccurence>>> modiferToHeap = new HashMap();

    private DictionaryTerm term1;
    private Iterator<DocumentOccurence> term1Results;

    public Matcher(OccurenceIndex index) {
        this.index = index;
        currentDocumentId = 0;
        modiferToHeap.put(Modifier.OR, orTerms);
        modiferToHeap.put(Modifier.AND, andTerms);
        modiferToHeap.put(Modifier.NAND, nandTerms);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public QueryResult next() {
        /*boolean andMatch = false;
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
*/
        DocumentOccurence docOcc = term1Results.next();
        QueryResult result = new QueryResult(docOcc.getDocumentId());
        result.addOccurences(term1, docOcc);
        return result;
    }

    public boolean hasNext() {
        //TODO: Each AND-iterator needs to have a next.
        return term1Results.hasNext();
    }

    public void addSource(QueryProcessingComponent source) {
        throw new UnsupportedOperationException();
    }

    public boolean loadQuery(QueryRequest query) {
        clean();
        this.query = query;
        term1Results = null;
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
                iteratorToTerm.put(result.getIterator(), dt);
                modiferToHeap.get(mod).add(result.getIterator());

                if (term1Results == null) {
                    term1 = dt;
                    term1Results = result.getIterator();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

        }

        return true;
    }

    private void clean() {
        for (SkippableIterator<DocumentOccurence> iter : IteratorUtils.chainedIterable(andTerms, orTerms, nandTerms)) {
            if (iter instanceof Closeable) ((Closeable) iter).close();
        }
        andTerms.clear();
        orTerms.clear();
        nandTerms.clear();
    }

    private long maximumCurrentDocId(Iterable<SkippableIterator<DocumentOccurence>> iterators) {
        long maximum = Long.MIN_VALUE;
        for (SkippableIterator si : iterators) {
            if (((DocumentOccurence) si.getCurrent()).getDocumentId() > maximum)
                maximum = ((DocumentOccurence) si.getCurrent()).getDocumentId();
        }
        return maximum;
    }

    private boolean isEqual(Iterable<SkippableIterator<DocumentOccurence>> iterators) {
        Iterator<SkippableIterator<DocumentOccurence>> iter = iterators.iterator();
        if (!iter.hasNext()) return false;
        long value = iter.next().getCurrent().getDocumentId();
        for (SkippableIterator<DocumentOccurence> si : iterators) {
            if (si.getCurrent().getDocumentId() != value) {
                return false;
            }
        }
        return true;
    }

    public QueryResult getCurrent() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
