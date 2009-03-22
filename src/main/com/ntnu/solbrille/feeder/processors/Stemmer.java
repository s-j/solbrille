package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * Stemmer that stems words. Assumes that there is no punctuation
 *
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class Stemmer extends AbstractDocumentProcessor{
    public Stemmer(String inputField, String outputField) {
        super(inputField, outputField);
    }


    public void process(Struct document) {
        SnowballStemmer stemmer = new porterStemmer();
        
        List<String> tokens = (List<String>) document.getField(getInputField()).getValue();
        ArrayList<String> output = new ArrayList<String>();

        for(String token :tokens) {
            if(token.isEmpty()) {
                output.add(token);
            } else {
                stemmer.setCurrent(token.toLowerCase());
                stemmer.stem();
                output.add(stemmer.getCurrent());
            }
        }
        document.setField(getOutputField(),output);

    }
}
