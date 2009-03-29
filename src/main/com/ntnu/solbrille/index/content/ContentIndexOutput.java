package com.ntnu.solbrille.index.content;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.outputs.FeederOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;

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
            if (document.getField("flush") != null) {
                return;
            }
            URI uri = (URI) document.getField("uri").getValue();
            contentIndexBuilder.addDocument(uri, (List<String>) document.getField("tokens").getValue());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
