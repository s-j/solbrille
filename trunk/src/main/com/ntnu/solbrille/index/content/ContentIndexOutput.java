package com.ntnu.solbrille.index.content;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.outputs.FeederOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class ContentIndexOutput implements FeederOutput {

    private Log LOG = LogFactory.getLog(this.getClass());

    private final ContentIndexBuilder contentIndexBuilder;

    public ContentIndexOutput(ContentIndexBuilder contentIndexBuilder) {
        this.contentIndexBuilder = contentIndexBuilder;
    }

    @Override
    public void put(Struct document) {
        try {
            long docId = Long.valueOf(document.getField("documentId").getValue());
            LOG.info("Storing content of document: " + docId + " in content index.");
            String content = document.getField("original").toString();
            contentIndexBuilder.addDocument(docId, Arrays.asList(content.split("\\s")));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
