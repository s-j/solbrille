package com.ntnu.solbrille.query.preprocessing;

import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.query.QueryRequest;
import com.ntnu.solbrille.query.QueryRequest.Modifier;
import com.ntnu.solbrille.query.QueryTermOccurence;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class QueryPreprocessor {

    SnowballStemmer stemmer;
    Set<String> stopWords;

    public QueryPreprocessor(Set<String> stopWords) {
        stemmer = new porterStemmer();
        this.stopWords = stopWords;
    }

    public ArrayList<String> processTerm(String in) {
        String curr;

        ArrayList<String> out = new ArrayList<String>();

        curr = in.toLowerCase();

        Pattern pattern = Pattern.compile("[\\p{P}]+");
        Matcher matcher = pattern.matcher(curr);
        curr = matcher.replaceAll(" ");

        for (String sub : curr.split(" ")) {
            stemmer.setCurrent(sub);
            stemmer.stem();
            String sout = stemmer.getCurrent();
            if (sout.length() > 0 && !stopWords.contains(sout)) out.add(sout);
        }



        return out;
    }

    public QueryRequest preprocess(String query) {
        String unpquery = query.replaceAll("\"", " \" ");
        //FIXME + and - inside a string

        StringTokenizer st = new StringTokenizer(unpquery);
        QueryRequest request = new QueryRequest(query);

        boolean phrase = false;
        Modifier curmod = Modifier.OR;
        int pos = 0;
        ArrayList<DictionaryTerm> currphrase = new ArrayList<DictionaryTerm>();
        boolean skiplast = false;

        while (st.hasMoreTokens()) {
            String next = st.nextToken();

            if (!skiplast) {
                if (next.charAt(0) == '+') {
                    curmod = Modifier.AND;
                    next = next.substring(1);
                } else if (next.charAt(0) == '-') {
                    curmod = Modifier.NAND;
                    next = next.substring(1);
                } else {
                    curmod = Modifier.OR;
                }
            }

            if (next.length() == 0) {
                skiplast = true;
                continue;
            }

            if (next.charAt(0) == '"') {
                if (phrase) {
                    request.addPhrase(curmod, currphrase);

                    //debug
                    for (DictionaryTerm dictionaryTerm : currphrase) {
                        System.out.print(dictionaryTerm + " ");
                    }
                    System.out.println();

                    currphrase = new ArrayList<DictionaryTerm>();

                    phrase = !phrase;
                    skiplast = false;
                    continue;
                } else {
                    if (curmod == Modifier.OR) curmod = Modifier.AND;   //NO phrase modifier is an AND-modifier.
                }
                phrase = !phrase;
                next = next.substring(1);
            }

            if (next.length() == 0) {
                skiplast = true;
                continue;
            }


            ArrayList<String> processed = processTerm(next);

            for (String last : processed) {
                DictionaryTerm lt = new DictionaryTerm(last);
                if (phrase) {
                    Modifier modcurmod = (curmod == Modifier.NAND) ? Modifier.PNAND : curmod;
                    request.addTermOccurence(lt, pos, modcurmod); //matcher should not care, phrase filter will take care of it
                    currphrase.add(lt);
                    skiplast = true;
                } else {
                    request.addTermOccurence(lt, pos, curmod);
                    skiplast = false;
                }
                pos++;
            }
        }
        fixPNANDS(request);
        return request;
    }

    // an ugly hack to remove conflicts between AND/OR/NAND and PNAND
    public void fixPNANDS(QueryRequest request) {
        for (DictionaryTerm term : request.getTerms()) {
            QueryTermOccurence occ = request.getQueryOccurences(term);

            boolean hasPNAND = false;
            boolean hasOther = false;
            while (occ.hasNext()) {
                Modifier flag = occ.next().getSecond();
                if (flag == Modifier.PNAND) hasPNAND = true;
                else hasOther = true;
            }
            if (hasPNAND && hasOther) occ.removeMarked(Modifier.PNAND.ordinal());
        }
    }

    public static void main(String args[]) {
        String test = " lazy, dog +jump over -quick +\"hello dolly\" -foxy moo +bar +--kaa-boom-pang! +moo-cow";
        QueryPreprocessor pre = new QueryPreprocessor(new HashSet<String>());
        pre.preprocess(test);
    }
}
