package com.ntnu.solbrille.feeder.processors;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public abstract class AbstractDocumentProcessor implements DocumentProcessor {

    private String inputField;
    private String outputField;

    public AbstractDocumentProcessor(String inputField,String outputField) {
        this.setInputField(inputField);
        this.setOutputField(outputField);
    }

    public String getInputField() {
        return inputField;
    }

    public void setInputField(String inputField) {
        this.inputField = inputField;
    }

    public String getOutputField() {
        return outputField;
    }

    public void setOutputField(String outputField) {
        this.outputField = outputField;
    }
}
