package com.ntnu.solbrille.feeder;

import com.ntnu.solbrille.feeder.processors.DocumentProcessor;
import com.ntnu.solbrille.feeder.outputs.FeederOutput;

import java.util.List;
import java.util.ArrayList;
import java.net.URI;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class Feeder {
    List<DocumentProcessor> processors = new ArrayList<DocumentProcessor>();

    FeederOutput output;

    public Struct feed(Struct document) {
        for(DocumentProcessor processor:processors) {
            processor.process(document);

        }
        if(output != null)
            output.put(document);
        return document;
    }

    public Struct feed(URI uri,String docString) {
        Struct document = new Struct();
        document.setField("uri",uri.toString());
        document.setField("content",docString);
        return feed(document);
    }



}
