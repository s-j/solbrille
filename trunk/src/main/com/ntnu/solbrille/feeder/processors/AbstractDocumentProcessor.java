package com.ntnu.solbrille.feeder.processors;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public abstract class AbstractDocumentProcessor implements DocumentProcessor {

    final private String inputField;
    final private String outputField;

    public AbstractDocumentProcessor(String inputField,String outputField) {
        this.inputField  = inputField;
        this.outputField = outputField;
    }

    public String getInputField() {
        return inputField;
    }

    public String getOutputField() {
        return outputField;
    }

    
}
