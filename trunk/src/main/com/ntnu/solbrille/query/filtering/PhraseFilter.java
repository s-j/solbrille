package com.ntnu.solbrille.query.filtering;

import com.ntnu.solbrille.query.QueryResult;
import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryRequest.Modifier;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.DocumentOccurence;
import com.ntnu.solbrille.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class PhraseFilter implements Filter{
    QueryRequest query;

    @Override
    public boolean loadQuery(QueryRequest request) {
        this.query = request;
        return true;
    }

    @Override
    public boolean filter(QueryResult result) {
        System.out.println("dung!");
        for (Pair<Modifier, ArrayList<DictionaryTerm>> phrase : query.getPhrases()){
            Modifier mod = phrase.getFirst();
            ArrayList<DictionaryTerm> terms = phrase.getSecond();

            boolean occurs = false;
            boolean end = false;

            ArrayList<List<Integer>> lists = new ArrayList<List<Integer>>();
            int[] ptrs = new int[terms.size()];

            int i=0;
            for (DictionaryTerm term : terms){
              DocumentOccurence occ = result.getOccurences(term);
                if (occ == null){
                    occurs = false;
                    end = true;
                    break;
              }
              List<Integer> pos = occ.getPositionList();
              lists.add(pos);
              ptrs[i++] = 0;
            }

            while(!end && !occurs){
               if (ptrs[0] > lists.get(0).size()){
                   end = true;
                   break;
               }
               int curr = lists.get(0).get(ptrs[0]);

               boolean endround = false;
               for (int j = 1; j < lists.size(); j++){
                   int currj = lists.get(j).get(ptrs[j]);

                   while (currj < curr + j){
                       if (ptrs[j] >= lists.get(j).size()){
                           end = true;
                           endround = true;
                           break;
                       }
                       currj  = lists.get(j).get(ptrs[j]);
                   }

                   if ( currj > curr + j){
                       endround = true;
                       break;
                   }
               }
               if (!endround) occurs = true;
            }

            if ((mod == Modifier.AND && !occurs) || (mod == Modifier.NAND && occurs)) return false;

        }
        return true;
    }
}
