package com.ntnu.solbrille.index;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.outputs.FeederOutput;
import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
import com.ntnu.solbrille.index.occurence.OccurenceIndexBuilder;
import com.ntnu.solbrille.utils.IntArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

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
            String rawContent = (String) document.getField("content").getValue();
            Map<String, IntArray> content = (Map<String, IntArray>) document.getField("terms").getValue();
            URI uri = (URI) document.getField("uri").getValue();
            if (statisticIndex.getDocumentIdFor(uri) < 0) {
                long documentId = statisticIndex.getNextDocumentId();
                indexBuilder.addDocument(documentId, uri, rawContent, content);
            } else {
                LOG.info("Dumplacte document: " + uri);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
