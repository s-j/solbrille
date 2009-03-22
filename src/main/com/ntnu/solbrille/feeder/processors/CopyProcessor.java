package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class CopyProcessor extends AbstractDocumentProcessor {

    public CopyProcessor(String inputField, String outputField) {
        super(inputField, outputField);
    }


    @Override
    public boolean process(Struct document) throws Exception {
        document.setField(getOutputField(), document.getField(getInputField()));
        return true;
    }
}
