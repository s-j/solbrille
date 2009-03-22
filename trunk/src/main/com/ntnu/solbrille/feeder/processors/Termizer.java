package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.index.occurence.DocumentOccurence;
import com.ntnu.solbrille.utils.IntArray;

import java.util.*;

/**
 * Creates a positionlist for each term.
 *
 * Empty string are removed
 *
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class Termizer extends AbstractDocumentProcessor{
    public  Termizer(String inputField, String outputField) {
        super(inputField, outputField);
    }

    @Override
    public void process(Struct document) throws Exception {
        List<String> stringTerms = (List<String>)document.getField(getInputField()).getValue();
        document.setField(getOutputField(),invertDocument(stringTerms));
    }

    private static Map<String,IntArray> invertDocument(List<String> terms) {

        int position = 0;
        Map<String,IntArray> invertedDocument = new HashMap<String,IntArray>();
        for(String token:terms) {
            if(!token.isEmpty()) {
                IntArray occurence = invertedDocument.get(token);
                if (occurence != null) {
                    occurence.add(position);
                } else {
                    occurence = new IntArray(1);
                    occurence.add(position);
                    invertedDocument.put(token, occurence);
                }
            }
            position++;
        }
        return invertedDocument;
    }
}
