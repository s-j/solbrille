package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.index.occurence.InvertedDocumentInfo;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentIndexBuilder extends AbstractLifecycleComponent {
    private static final class DocumentIndexBuilderMutex {
    }

    private final Object mutex = new DocumentIndexBuilderMutex();

    private StaticInformationEntry globalStatisticsDelta = new StaticInformationEntry();
    private final DocumentStatisticsIndex statisticsIndex;

    public DocumentIndexBuilder(DocumentStatisticsIndex statisticsIndex) {
        this.statisticsIndex = statisticsIndex;
    }

    public void addDocument(long documentId, URI uri, InvertedDocumentInfo docInfo) {
        synchronized (mutex) {
            globalStatisticsDelta.registerNewDocumentIndexed(1, docInfo.getDocumentSize(), docInfo.getTotalTokens());
        }
        try {
            statisticsIndex.registerDocumentIndexed(documentId, uri.toString(), docInfo); // no need to be consistent (yet)
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void updateIndex(Map<Long, Float> tfIdfAccumulator) throws IOException, InterruptedException {
        StaticInformationEntry oldDelta;
        synchronized (mutex) {
            oldDelta = globalStatisticsDelta;
            globalStatisticsDelta = new StaticInformationEntry();
        }
        statisticsIndex.updateGlobalStatistics(oldDelta, tfIdfAccumulator);
    }

    public long getTotalNumberOfDocuments() {
        return statisticsIndex.getTotalNumberOfDocuments();
    }

    @Override
    public void start() {
        setIsRunning(true);
    }

    @Override
    public void stop() {
        setIsRunning(false);
    }
}
