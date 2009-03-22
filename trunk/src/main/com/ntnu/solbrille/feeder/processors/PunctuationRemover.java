package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

/**
 * A pattern that removes all non-whitespace characters
 *
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class PunctuationRemover extends  AbstractDocumentProcessor{

    public PunctuationRemover(String inputField, String outputField) {
        super(inputField, outputField);
    }

    public void process(Struct document) {
        List<String> tokens = (List<String>) document.getField(getInputField()).getValue();

        Pattern pattern = Pattern.compile("[\\p{P}]+");
        List<String> output = new ArrayList<String>();
        for(String token:tokens) {
            output.add(pattern.matcher(token).replaceAll(" "));
        }
        document.setField(getOutputField(),output);
    }
}
