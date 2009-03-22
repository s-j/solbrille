package com.ntnu.solbrille.index;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.outputs.FeederOutput;
import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
import com.ntnu.solbrille.index.occurence.OccurenceIndexBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class IndexerOutput implements FeederOutput {
    private final OccurenceIndexBuilder indexBuilder;
    private final DocumentStatisticsIndex statisticIndex;

    private Log LOG = LogFactory.getLog(this.getClass());

    public IndexerOutput(OccurenceIndexBuilder indexBuilder, DocumentStatisticsIndex statisticIndex) {
        this.indexBuilder = indexBuilder;
        this.statisticIndex = statisticIndex;
    }


    public void put(Struct document) {
        try {
            String content = document.getField("content").getValue();
            String uri = document.getField("uri").getValue();
            if (statisticIndex.getDocumentIdFor(new URI(uri)) < 0) {
                long documentId = statisticIndex.getNextDocumentId();
                indexBuilder.addDocument(documentId, new URI(uri), content);
            } else {
                LOG.info("Dumplacte document: " + uri);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
