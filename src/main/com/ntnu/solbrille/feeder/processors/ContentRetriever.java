package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Gets the content of the an uri in a field in the struct
 *
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class ContentRetriever extends AbstractDocumentProcessor{

    private static Log LOG = LogFactory.getLog(ContentRetriever.class);

    private boolean overwrite = false;

    public ContentRetriever(String inputField, String outputField) {
        super(inputField, outputField);
    }

    public void process(Struct document) throws URISyntaxException, IOException {
        if(overwrite || document.getField(getOutputField()) == null) {
            URI uri = new URI(document.getField(getInputField()).getValue());
            Object o = uri.toURL().getContent(new Class[]{InputStream.class});
            if(o == null) {
                LOG.error("Not able to retrive inputstream for uri: " + uri.toString());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((InputStream)o));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }

            document.setField(getOutputField(),sb.toString());
        }

    }

    /**
    If the outputfield should be overwritten if it already excists
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
    If the outputfield should be overwritten if it already excists
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
