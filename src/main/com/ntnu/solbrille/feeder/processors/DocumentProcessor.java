package com.ntnu.solbrille.feeder.processors;

import com.ntnu.solbrille.feeder.Struct;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public interface DocumentProcessor {
    public void process(Struct document) throws Exception;
}
