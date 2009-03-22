package com.ntnu.solbrille.index;

import com.ntnu.solbrille.feeder.Struct;
import com.ntnu.solbrille.feeder.processors.AbstractDocumentProcessor;
import com.ntnu.solbrille.index.document.DocumentStatisticsIndex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class DocumentIdGenerator extends AbstractDocumentProcessor {

    private Log LOG = LogFactory.getLog(this.getClass());

    private final DocumentStatisticsIndex documentStatitsicsIndex;

    public DocumentIdGenerator(String inputField, String outputField, DocumentStatisticsIndex statistics) {
        super(inputField, outputField);
        documentStatitsicsIndex = statistics;
    }


    @Override
    public boolean process(Struct document) throws Exception {
        String uri = document.getField(getInputField()).getValue();
        long docId = documentStatitsicsIndex.getDocumentIdFor(new URI(uri));
        if (docId < 0) {
            docId = documentStatitsicsIndex.getNextDocumentId();
        } else {
            LOG.debug("Duplicate document: " + uri);
            return false;
        }
        document.setField(getOutputField(), Long.valueOf(docId).toString());
        return true;
    }
}
