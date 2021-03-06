package com.ntnu.solbrille.feeder.test;

import com.ntnu.solbrille.feeder.DefaultFeeder;
import com.ntnu.solbrille.feeder.Feeder;
import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class FeederTest extends TestCase {
    public void testDefaultFeeder() {

        Feeder feeder = new DefaultFeeder();
        try {
            FileReader fr = new FileReader("src/test/com/ntnu/solbrille/feeder/test/test.html");
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(fr);
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            feeder.feed(new URI("http://www.slashdot.org/"));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


        } catch (URISyntaxException e) {
            fail();
        } catch (IOException e) {
            fail(e.getMessage());
        }

    }
}
