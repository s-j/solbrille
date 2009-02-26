package com.ntnu.solbrille.feeder.outputs;

import com.ntnu.solbrille.feeder.Struct;

import java.io.PrintStream;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class StreamOutput implements FeederOutput{

    PrintStream ps;

    public StreamOutput() {
        ps = System.out;
    }

    public StreamOutput(PrintStream ps) {
        this.ps = ps;
    }

    public void put(Struct document) {
        ps.print("<document>\n");
        ps.print(document.toString());
        ps.print("</document>\n");
    }
}
