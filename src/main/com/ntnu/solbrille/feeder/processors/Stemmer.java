package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import java.util.StringTokenizer;

/**
 * Stemmer that stems words. Assumes that there is no punctuation
 *
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class Stemmer extends AbstractDocumentProcessor {
    public Stemmer(String inputField, String outputField) {
        super(inputField, outputField);
    }


    @Override
    public boolean process(Struct document) {
        SnowballStemmer stemmer = new porterStemmer();
        String content = document.getField(getInputField()).getValue();

        StringTokenizer st = new StringTokenizer(content);
        StringBuilder sb = new StringBuilder();
        while (st.hasMoreTokens()) {
            String next = st.nextToken().toLowerCase();
            String last;
            //Do only 1 repeat
            last = next;
            stemmer.setCurrent(last);
            stemmer.stem();
            next = stemmer.getCurrent();
            sb.append(next + "\n");
        }
        document.setField(getOutputField(), sb.toString());
        return true;
    }
}
