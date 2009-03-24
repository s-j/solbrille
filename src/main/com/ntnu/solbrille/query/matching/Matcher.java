package com.ntnu.solbrille.query.matching;

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
import com.ntnu.solbrille.utils.iterators.IteratorUtils;
import com.ntnu.solbrille.utils.iterators.SkippableIterator;

import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:janmaxim@idi.ntnu.no">Jan Maximilian Winther Kristiansen</a>
 * @version $Id $.
 */
public class Matcher implements QueryProcessingComponent {
    private QueryRequest query;
    private OccurenceIndex index;
    private DocumentStatisticsIndex statistics;
    private QueryResult current;

    private int requiredAndTerms;
    private final Heap<SkippableIterator<DocumentOccurence>> andTerms = new Heap();
    private final Heap<SkippableIterator<DocumentOccurence>> nandTerms = new Heap();
    private final Heap<SkippableIterator<DocumentOccurence>> orTerms = new Heap();
    private final Map<SkippableIterator<DocumentOccurence>, DictionaryTerm> iteratorToTerm = new IdentityHashMap();
    private final Map<Modifier, Heap<SkippableIterator<DocumentOccurence>>> modiferToHeap = new HashMap();

    public Matcher(OccurenceIndex index, DocumentStatisticsIndex statistics) {
        this.index = index;
        this.statistics = statistics;
        modiferToHeap.put(Modifier.OR, orTerms);
        modiferToHeap.put(Modifier.AND, andTerms);
        modiferToHeap.put(Modifier.NAND, nandTerms);
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public QueryResult next() {
        assert current != null;
        QueryResult result = current;
        current = null;
        return result;
    }

    public boolean hasNext() {
        QueryResult qr = null;
        DocumentOccurence currentOccurence = null;
        while (current == null) {
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
                        } else {
                            head.next();
                            orTerms.headChanged();
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
                    } else if (head.getCurrent().getDocumentId() < qr.getDocumentId() && !head.hasNext()) {
                        ((Closeable) nandTerms.poll()).close();
                    }

                    head = nandTerms.peek();
                }
            }

            if (qr != null) {
                current = qr;
                current.setStatisticsEntry(statistics.getDocumentStatistics(current.getDocumentId()));
            }

            if (current == null && orTerms.isEmpty() && andTerms.isEmpty()) {
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
                    if (mod == Modifier.PNAND) {//an ugly hack by Simon
                        mod = Modifier.OR;
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
}
