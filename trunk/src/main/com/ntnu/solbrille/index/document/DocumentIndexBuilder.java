package com.ntnu.solbrille.index.document;

import com.ntnu.solbrille.index.occurence.InvertedDocumentInfo;
import com.ntnu.solbrille.utils.AbstractLifecycleComponent;

import java.net.URI;
import java.net.URISyntaxException;

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
            statisticsIndex.registerDocuemntIndexed(documentId, uri.toString(), docInfo); // no need to be consistent (yet)
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void updateIndex() {
        StaticInformationEntry oldDelta;
        synchronized (mutex) {
            oldDelta = globalStatisticsDelta;
            globalStatisticsDelta = new StaticInformationEntry();
        }
        statisticsIndex.updateGlobalStatistics(oldDelta);
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
