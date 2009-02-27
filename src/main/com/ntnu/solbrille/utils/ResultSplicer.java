package main.com.ntnu.solbrille.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: janmaxim
 * Date: Feb 26, 2009
 * Time: 12:05:56 PM
 */
public class ResultSplicer {


    /**
     * Matches the elements in the heap against each other. If the number of matched elements are
     * equal to the number of result sets, it returns an ArrayList<Object> consisting of the matched objects.
     * If there is no match, it returns an empty ArrayList<Object>.
     *
     * @param resultSets
     * @return ArrayList
     */
    // TODO: Not entirely finished.
    public List<Object> match(List<List> resultSets)
    {
        if (resultSets.size() == 1) {
            throw new IllegalArgumentException("Must have atleast two result sets to match");
        }

        int numberOfResultSets = resultSets.size();

        Heap resultHeap = new Heap();
        for (List c : resultSets) {
            resultHeap.addAll(c);
        }

        ArrayList matches = new ArrayList();

        while (!resultHeap.isEmpty()) {
            Object elem = resultHeap.poll();
            matches.add(elem);

            int numberOfMatches = 1;
            while (!resultHeap.isEmpty() && numberOfResultSets >= numberOfMatches) {
                // TODO: Extend this to be more general ( elem.compareTo(results.peek()) ) to compare docIds
                if (elem.equals(resultHeap.peek())) {
                    matches.add(resultHeap.poll());
                    numberOfMatches++;
                } else {
                    matches.clear();
                    numberOfMatches = 0;
                    break;
                }
            }

            if (matches.size() == numberOfResultSets) {
                break;
            } else if (resultHeap.isEmpty()) {
                matches.clear();
            }
        }

        return matches;
    }
}
