package com.ntnu.solbrille.utils;

import net.sf.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import com.ntnu.solbrille.feeder.Feeder;
import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.outputs.FeederOutput;
import com.ntnu.solbrille.feeder.processors.Tokenizer;
import com.ntnu.solbrille.feeder.processors.PunctuationRemover;
import com.ntnu.solbrille.feeder.processors.Termizer;

/**
 * Created by IntelliJ IDEA.
 * User: janmaxim
 * Date: Mar 26, 2009
 * Time: 8:23:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class StemmingUtil {

    public static int[] createPositionList(String query, String text) {
        StemmingUtil util = new StemmingUtil();
        HashMap<String, IntArray> queryMap = util.stem(query);
        HashMap<String, IntArray> teaserMap = util.stem(text);
        return util.match(queryMap, teaserMap);
    }

    public HashMap<String, IntArray> stem(String text) {
        Stemmer stemmer = new Stemmer();
        return stemmer.stem(text);
    }

    public int[] match(HashMap<String, IntArray> query, HashMap<String, IntArray> teaser) {
        IntArray values = new IntArray();
        for (String s : query.keySet()) {
            if (teaser.containsKey(s)) {
                values.addAll(teaser.get(s));
            }
        }

        int[] returnValues = new int[values.size()];
        for (int i = 0; i < returnValues.length; i++) {
            returnValues[i] = values.get(i);
        }

        Arrays.sort(returnValues);

        return returnValues;
    }

    private class Stemmer extends Feeder {

        private StemmerOutput output;

        public Stemmer() {
            output = new StemmerOutput();

            processors.add(new Tokenizer("content", "tokens"));
            processors.add(new PunctuationRemover("tokens", "cleanedTokens"));
            processors.add(new com.ntnu.solbrille.feeder.processors.Stemmer("cleanedTokens", "stemmedTokens"));
            processors.add(new Termizer("stemmedTokens", "terms"));
            outputs.add(output);
        }

        public HashMap<String, IntArray> stem(String text) {
            Struct struct = new Struct();
            struct.setField("content", text);
            feed(struct);
            while (!output.isDone()) { }
            return output.getOutput();
        }
    }

    private class StemmerOutput implements FeederOutput {

        private HashMap<String, IntArray> terms;
        private boolean done = false;

        public StemmerOutput() {
            terms = new HashMap<String, IntArray>();
        }

        public void put(Struct s) {
            terms = (HashMap<String, IntArray>) s.getField("terms").getValue();
            done = true;
        }

        public boolean isDone() {
            return (done == true);
        }

        public HashMap<String, IntArray> getOutput() {
            return terms;
        }

    }
}
