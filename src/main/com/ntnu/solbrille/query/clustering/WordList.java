package com.ntnu.solbrille.query.clustering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class WordList {
    private HashMap<String,Integer> terms ;
    private List<String> invlist;

    public WordList() {
        terms = new HashMap<String,Integer>();
        invlist = new ArrayList<String>();
    }

    public int getValue(String s) {
        if(!terms.containsKey(s)) {
            terms.put(s,invlist.size());
            invlist.add(s);
            return invlist.size()-1;
        }
        return terms.get(s);
    }

    public String getString(int i) {
        return invlist.get(i);
    }
}