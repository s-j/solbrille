package com.ntnu.solbrille.query.clustering;

import org.carrot2.text.suffixtrees2.ISequence;
import com.ntnu.solbrille.utils.IntArray;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class WordSequence implements ISequence {
    private List<Integer> words;
    private WordList wl;


    public WordSequence(WordList wl) {
        words = new IntArray();
        this.wl = wl;
    }

    public int size() {
        return words.size();
    }

    public int objectAt(int i) {
        return words.get(i);
    }


    public void addWord(String term) {
        words.add(wl.getValue(term));
    }
}
