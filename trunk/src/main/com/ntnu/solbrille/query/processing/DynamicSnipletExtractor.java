package com.ntnu.solbrille.query.processing;

import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.utils.Pair;
import com.ntnu.solbrille.utils.iterators.IteratorMerger;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DynamicSnipletExtractor extends AbstractProcessingComponent {

    private final int snipletLength;

    public DynamicSnipletExtractor(int snipletLength) {
        this.snipletLength = snipletLength;
    }

    @Override
    public QueryResult next() {
        QueryResult result = getSrc().next();
        result.setBestWindow(getBestSnipletStart(result));
        return result;
    }

    private Pair<Integer, Integer> getBestSnipletStart(QueryResult result) {
        Iterator<Integer>[] inputs = new Iterator[result.getTerms().size()];
        int inpPos = 0;
        int totalOccs = 0;
        for (DictionaryTerm term : result.getTerms()) {
            List<Integer> occs = result.getOccurences(term).getPositionList();
            totalOccs += occs.size();
            inputs[inpPos++] = occs.iterator();
        }
        Iterator<Integer> merged = new IteratorMerger(inputs);
        int[] occurences = new int[totalOccs];
        int occNum = 0;
        while (merged.hasNext()) {
            occurences[occNum++] = merged.next();
        }
        int bestWindow = 0;
        int bestWindowValue = 1;
        while ((bestWindowValue + bestWindow) < occurences.length
                && (occurences[bestWindow + bestWindowValue] - occurences[bestWindow]) < snipletLength) {
            bestWindowValue++; // extend window as far as possible
        }
        int lastWindow = 0;
        int lastWindowEnd = bestWindowValue;
        for (int i = 1; i < occurences.length && lastWindowEnd < occurences.length; i++) {
            int windowEnd = lastWindowEnd;
            while (windowEnd < (occurences.length)
                    && (occurences[windowEnd] - occurences[i] < snipletLength)) {
                windowEnd++;
            }
            int windowValue = lastWindowEnd - lastWindow - 1 + (windowEnd - lastWindowEnd);
            if (windowValue > bestWindowValue) {
                bestWindowValue = windowValue;
                bestWindow = i;
            }
            lastWindow = i;
            lastWindowEnd = windowEnd;
        }
        return new Pair<Integer, Integer>(occurences[bestWindow] - 1, snipletLength);
    }
}
