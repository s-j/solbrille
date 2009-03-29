package com.ntnu.solbrille.query;

import com.ntnu.solbrille.index.document.DocumentStatisticsEntry;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.DocumentOccurence;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.query.clustering.ClusterList;

import java.util.HashMap;
import java.util.Set;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class QueryResult implements Comparable<QueryResult> {
    private final long documentId;
    private HashMap<DictionaryTerm, DocumentOccurence> positionLists = new HashMap<DictionaryTerm, DocumentOccurence>();
    private DocumentStatisticsEntry stats;
    private float score;

    private Pair<Integer, Integer> bestWindow;
    private ClusterList clusterList;

    public QueryResult(long documentId) {
        this.documentId = documentId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public int compareTo(QueryResult result) {
        return Float.valueOf(score).compareTo(result.score);
    }

    public long getDocumentId() {
        return documentId;
    }

    public Set<DictionaryTerm> getTerms() {
        return positionLists.keySet();
    }

    public DocumentOccurence getOccurences(DictionaryTerm term) {
        return positionLists.get(term);
    }

    public boolean hasOccurences(DictionaryTerm term) {
        return positionLists.containsKey(term);
    }

    public DocumentOccurence addOccurences(DictionaryTerm term, DocumentOccurence occs) {
        return positionLists.put(term, occs);
    }

    public DocumentOccurence removeOccurences(DictionaryTerm term) {
        return positionLists.remove(term);
    }

    public void setStatisticsEntry(DocumentStatisticsEntry stats) {
        this.stats = stats;
    }

    public DocumentStatisticsEntry getStatisticsEntry() {
        return stats;
    }

    public Pair<Integer, Integer> getBestWindow() {
        return bestWindow;
    }

    public void setBestWindow(Pair<Integer, Integer> bestWindow) {
        this.bestWindow = bestWindow;
    }

    public void setClusterList(ClusterList cl) {
        this.clusterList = cl;
    }

    public ClusterList getClusterList() {
        return clusterList;
    }
}

