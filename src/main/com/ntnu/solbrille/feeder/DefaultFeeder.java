package com.ntnu.solbrille.feeder;

import com.ntnu.solbrille.feeder.processors.*;
import com.ntnu.solbrille.feeder.outputs.StreamOutput;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class DefaultFeeder extends Feeder{
    public DefaultFeeder() {
        processors.add(new ContentRetriever("uri","content"));
        processors.add(new LinkExtractor("content","link"));
        processors.add(new TextToHtml("content","content"));
        processors.add(new PunctuationRemover("content","content"));
        processors.add(new Stemmer("content","content"));
        outputs.add(new StreamOutput());
    }
}
