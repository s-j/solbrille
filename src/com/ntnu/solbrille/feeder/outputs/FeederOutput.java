package com.ntnu.solbrille.feeder.outputs;

import com.ntnu.solbrille.feeder.Struct;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public interface FeederOutput {

    public void put(Struct document);
}
