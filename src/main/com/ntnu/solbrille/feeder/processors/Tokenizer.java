package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.utils.iterators.CachedIterator;
import com.ntnu.solbrille.utils.iterators.CachedIteratorAdapter;
import com.ntnu.solbrille.utils.iterators.CastingIteratorToEnumerator;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class Tokenizer extends AbstractDocumentProcessor {
    public Tokenizer(String inputField, String outputField) {
        super(inputField, outputField);
    }

    private static boolean isWhiteSpace(String str) {
        for (char chr : str.toCharArray()) {
            if (!Character.isWhitespace(chr))
                return false;
        }
        return true;
    }

    @Override
    public void process(Struct document) throws Exception {
        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer((String) document.getField(getInputField()).getValue(),
                " -_~\\/`'^\"'.:,;\t\n\r",
                true);
        CachedIterator<String> tokens = new CachedIteratorAdapter<String>(new CastingIteratorToEnumerator<String>(st));
        String token = "";
        if (tokens.hasNext()) {
            token = tokens.next();
        }

        while (tokens.hasNext()) {
            tokens.next();
            if (Character.isLetter(tokens.getCurrent().charAt(0))) {
                if (!token.isEmpty()) {
                    list.add(token);
                }
                token = tokens.getCurrent();
            } else {
                token += tokens.getCurrent();
            }
        }
        if (!token.isEmpty()) {
            list.add(token);
        }
        document.setField(getOutputField(), list);
    }
}
