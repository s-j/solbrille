package com.ntnu.solbrille.index;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.outputs.FeederOutput;
import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
import com.ntnu.solbrille.index.occurence.OccurenceIndexBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class IndexerOutput implements FeederOutput {
    private static final class IndexerOutputMutex {
    }

    private final Object mutex = new IndexerOutputMutex();

    private final OccurenceIndexBuilder indexBuilder;
    private final DocumentStatisticsIndex statisticIndex;

    private Log LOG = LogFactory.getLog(this.getClass());

    public IndexerOutput(OccurenceIndexBuilder indexBuilder, DocumentStatisticsIndex statisticIndex) {
        this.indexBuilder = indexBuilder;
        this.statisticIndex = statisticIndex;
    }


    @Override
    public void put(Struct document) {
        try {
            if (document.getField("flush") != null) {
                LOG.info("Flushing!");
                indexBuilder.updateIndex();
                LOG.info("Flushed index phase " + indexBuilder.getIndexPhase());
                return;
            }


            Map<String, ? extends List<Integer>> terms = (Map<String, ? extends List<Integer>>) document.getField("terms").getValue();
            URI uri = (URI) document.getField("uri").getValue();
            LOG.info("Feeded document: " + uri);
            if (statisticIndex.getDocumentIdFor(uri) > -1) {
                LOG.info("Duplicate document: " + uri);
            } else {
                long documentId = statisticIndex.getNextDocumentId();
                indexBuilder.addDocument(documentId, uri, ((CharSequence) document.getField("cleanedContent").getValue()).length(), terms);
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
