package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentInfoRetriever extends AbstractDocumentProcessor {
    public DocumentInfoRetriever(String inputField, String outputField) {
        super(inputField, outputField);
    }

    @Override
    public void process(Struct document) throws Exception {
        String content = document.getField(getInputField()).getValue();
        Struct possibleUri = document.getField("uri");
        String uri = possibleUri != null ? possibleUri.getValue() : "";
    }
}
