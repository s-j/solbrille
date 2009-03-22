package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class Tokenizer extends AbstractDocumentProcessor{
    public Tokenizer(String inputField, String outputField) {
        super(inputField, outputField);
    }

    private static boolean isWhiteSpace(String str) {
        for(char chr:str.toCharArray()) {
            if(!Character.isWhitespace(chr))
                return false;
        }
        return true;
    }

    @Override
    public void process(Struct document) throws Exception {
        List<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer((String)document.getField(getInputField()).getValue(),
                " -_~\\/`'^\"'.:,;\t\n\r",
                false);
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(!isWhiteSpace(token))
                list.add(token.trim());
        }
        document.setField(getOutputField(),list);
    }
}
