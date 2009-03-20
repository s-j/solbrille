package com.ntnu.solbrille.query.matching;

import com.ntnu.solbrille.index.document.DocumentStatisticsEntry;
import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
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
    private DocumentStatisticsIndex statistics;
    private QueryResult current;

    private DocumentOccurence currentDocument;

    private int requiredAndTerms;
    private final Heap<SkippableIterator<DocumentOccurence>> andTerms = new Heap();
    private final Heap<SkippableIterator<DocumentOccurence>> nandTerms = new Heap();
    private final Heap<SkippableIterator<DocumentOccurence>> orTerms = new Heap();
    private final Map<SkippableIterator<DocumentOccurence>, DictionaryTerm> iteratorToTerm = new IdentityHashMap();
    private final Map<Modifier, Heap<SkippableIterator<DocumentOccurence>>> modiferToHeap = new HashMap();

    public Matcher(OccurenceIndex index, DocumentStatisticsIndex statistics) {
        this.index = index;
        this.statistics = statistics;
        currentDocument = null;
        modiferToHeap.put(Modifier.OR, orTerms);
        modiferToHeap.put(Modifier.AND, andTerms);
        modiferToHeap.put(Modifier.NAND, nandTerms);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public QueryResult next() {
        DocumentStatisticsEntry dse = statistics.getDocumentStatistics(current.getDocumentId());
        current.setStatisticsEntry(dse);
        return current;
    }

    public boolean hasNext() {
        boolean match = false;
        QueryResult qr = null;
        DocumentOccurence currentOccurence = null;
        
        while (!match) {
            // Need all and term iterators present to do matching
            if (requiredAndTerms == andTerms.size() && requiredAndTerms > 0) {
                SkippableIterator<DocumentOccurence> head = andTerms.peek();
                qr = new QueryResult(head.getCurrent().getDocumentId());
                qr.addOccurences(iteratorToTerm.get(head), head.getCurrent());
                currentOccurence = head.getCurrent();
                if (head.hasNext()) { // update head
                    head.next();
                    andTerms.headChanged();
                } else { // remove iterator
                    ((Closeable) andTerms.poll()).close();
                }

                head = andTerms.isEmpty() ? null : andTerms.peek();
                while (head != null && qr.getTerms().size() < requiredAndTerms) {
                    if (head.getCurrent().getDocumentId() != qr.getDocumentId()) {
                        qr = new QueryResult(head.getCurrent().getDocumentId());
                        currentOccurence = head.getCurrent();
                    }
                    qr.addOccurences(iteratorToTerm.get(head), head.getCurrent());

                    if (head.hasNext()) {
                        head.next();
                        andTerms.headChanged();
                    } else {
                        ((Closeable) andTerms.poll()).close();
                    }
                    head = andTerms.isEmpty() ? null : andTerms.peek();
                }

                if (qr.getTerms().size() < requiredAndTerms) {
                    qr = null; // no match
                }
            }

            if (requiredAndTerms > 0 && qr == null) { // no match
                return false;
            }

            if (orTerms.size() > 0) {  
                SkippableIterator<DocumentOccurence> head = orTerms.peek();
                if (qr == null) {
                    qr = new QueryResult(head.getCurrent().getDocumentId());
                    qr.addOccurences(iteratorToTerm.get(head), head.getCurrent());
                    currentOccurence = head.getCurrent();

                    if (head.hasNext()) {
                        head.next();
                        orTerms.headChanged();
                    } else {
                        ((Closeable) orTerms.poll()).close();
                    }
                }

                head = orTerms.peek();
                while (head != null && head.getCurrent().getDocumentId() <= qr.getDocumentId()) {
                    head.skipTo(currentOccurence);
                    orTerms.headChanged();

                    if (head.getCurrent().getDocumentId() == qr.getDocumentId()) {
                        qr.addOccurences(iteratorToTerm.get(head), head.getCurrent());

                        if (!head.hasNext()) {
                            ((Closeable) orTerms.poll()).close();
                        }
                    }

                    head = orTerms.peek();
                }
            }

            if (nandTerms.size() > 0 && qr != null) {
                SkippableIterator<DocumentOccurence> head = nandTerms.peek();
                while (head != null && head.getCurrent().getDocumentId() <= qr.getDocumentId()) {
                    head.skipTo(currentOccurence);
                    nandTerms.headChanged();
                    if (head.getCurrent().getDocumentId() == qr.getDocumentId()) {
                        qr = null;
                        if (!head.hasNext()) {
                            ((Closeable) nandTerms.poll()).close();
                        }
                        break;
                    }

                    head = nandTerms.peek();
                }
            }

            if (qr != null) {
                match = true;
                current = qr;
                qr = null;
            }

            if (match == false && orTerms.isEmpty() && andTerms.isEmpty()) {
                return match;
            }

        }
        return match;
    }

    private void skipPastCurrent() {

        for (SkippableIterator<DocumentOccurence> si : IteratorUtils.chainedIterable(andTerms, orTerms, nandTerms)) {
            si.skipPast(currentDocument);
        }
    }

    private boolean hasMore(Heap<SkippableIterator<DocumentOccurence>> iterators) {
        for (SkippableIterator<DocumentOccurence> si : iterators) {
            if (!(si.getCurrent() != null)) {
                return false;
            }
        }

        return true;
    }

    public void addSource(QueryProcessingComponent source) {
        throw new UnsupportedOperationException();
    }

    public boolean loadQuery(QueryRequest query) {
        clean();
        this.query = query;
        for (DictionaryTerm dt : this.query.getTerms()) {
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
                SkippableIterator<DocumentOccurence> si = result.getIterator();
                if (si.hasNext()) {
                    si.next();
                } else {
                    continue;
                }
                query.setDocumentCount(dt, result.getDocumentCount());
                iteratorToTerm.put(si, dt);
                modiferToHeap.get(mod).add(si);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        requiredAndTerms = andTerms.size();
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

    private void cleanUpIterator(SkippableIterator<DocumentOccurence> iter, Modifier mod) {
        if (!iter.hasNext()) {
            switch (mod) {
                case OR:
                    orTerms.remove(iter);
                    break;
                case NAND:
                    nandTerms.remove(iter);
                    break;
            }

        }
    }

    public void cleanUpAndIterators() {
        for (SkippableIterator<DocumentOccurence> si : andTerms) {
            if (!si.hasNext()) {
                andTerms.clear();
                orTerms.clear();
                break;
            }
        }
    }

    private DocumentOccurence maximumCurrentDocument(Iterable<SkippableIterator<DocumentOccurence>> iterators) {
        DocumentOccurence maximum = new DocumentOccurence(-1);
        for (SkippableIterator<DocumentOccurence> si : iterators) {
            if (si.getCurrent().compareTo(maximum) > 0)
                maximum = si.getCurrent();
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
        return next();
    }
}
