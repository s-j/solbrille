package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;

/**
 * Clears a field from the document
 *
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id$.
 */
public class RemoveField implements DocumentProcessor{

    public String fieldName;

    public RemoveField(String fieldName) {
        this.fieldName = fieldName;
    }


    @Override
    public void process(Struct document) throws Exception {
        document.clearField(fieldName);
    }
}
