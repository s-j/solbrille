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
        processors.add(new HtmlToText("content","cleanedContent"));
        processors.add(new Tokenizer("cleanedContent","tokens"));
        processors.add(new PunctuationRemover("tokens","cleanedTokens"));
        processors.add(new Stemmer("cleanedTokens","cleanedTokens"));
        processors.add(new Termizer("cleanedTokens","terms"));
        outputs.add(new StreamOutput());
    }
}
