package com.ntnu.solbrille.index;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.outputs.FeederOutput;
import com.ntnu.solbrille.index.occurence.OcccurenceIndexBuilder;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class IndexerOutput implements FeederOutput {
    private final OcccurenceIndexBuilder indexBuilder;

    public IndexerOutput(OcccurenceIndexBuilder indexBuilder) {
        this.indexBuilder = indexBuilder;
    }

    public void put(Struct document) {
        //indexBuilder.addDocument();
    }
}
