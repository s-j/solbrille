package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
        String content = document.getField(getInputField()).getValue();

        Pattern pattern = Pattern.compile("\\p{Punct}+");
        Matcher matcher = pattern.matcher(content);
        String output = matcher.replaceAll(" ");
        document.setField(getOutputField(),output);

    }
}
