package com.ntnu.solbrille.query.clustering;

import javax.swing.JTabbedPane;
import com.ntnu.solbrille.query.QueryResult;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class Cluster {

    List<QueryResult> results = new ArrayList<QueryResult>();
    private List<String> tags = new ArrayList<String>();
    private double score = 0.0;

    public int getSize() {
        return results.size();
    }

    public List<QueryResult> getResults() {
        return results;
    }

    public List<String> getTags() {
        return tags;
    }
    
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(score + " [");
        for(String tag:tags) {
            sb.append(tag + ",");
        }
        sb.append("]");

        sb.append("{");
        for(QueryResult result:results) {
            sb.append(result.getStatisticsEntry().getURI() + ",");
        }
        sb.append("}");
        return sb.toString();
    }
}
