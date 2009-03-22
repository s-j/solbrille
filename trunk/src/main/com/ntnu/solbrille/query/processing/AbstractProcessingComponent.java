package com.ntnu.solbrille.query.processing;

import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryResult;

/**
 * Asbtract processing component.
 *
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public abstract class AbstractProcessingComponent implements QueryProcessingComponent {

    private QueryProcessingComponent src;
    private QueryRequest query;

    protected QueryProcessingComponent getSrc() {
        return src;
    }

    protected QueryRequest getQuery() {
        return query;
    }

    @Override
    public void addSource(QueryProcessingComponent source) {
        src = source;
    }

    @Override
    public boolean loadQuery(QueryRequest query) {
        this.query = query;
        if (src != null) {
            return src.loadQuery(query);
        }
        return true;
    }

    @Override
    public boolean hasNext() {
        return src.hasNext();
    }

    @Override
    public QueryResult next() {
        return src.next();
    }

    @Override
    public void remove() {
        src.remove();
    }
}
