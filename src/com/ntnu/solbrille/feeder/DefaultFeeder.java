package com.ntnu.solbrille.feeder;

import com.ntnu.solbrille.feeder.processors.TextToHtml;
import com.ntnu.solbrille.feeder.processors.Stemmer;
import com.ntnu.solbrille.feeder.processors.PunctuationRemover;
import com.ntnu.solbrille.feeder.processors.LinkExtractor;
import com.ntnu.solbrille.feeder.outputs.StreamOutput;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class DefaultFeeder extends Feeder{
    public DefaultFeeder() {
        processors.add(new LinkExtractor("content","link"));
        processors.add(new TextToHtml("content","content"));
        processors.add(new PunctuationRemover("content","content"));
        processors.add(new Stemmer("content","content"));
        output = new StreamOutput();
    }
}
