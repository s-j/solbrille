package com.ntnu.solbrille.feeder.test;

import com.ntnu.solbrille.feeder.DefaultFeeder;
import com.ntnu.solbrille.feeder.Feeder;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.*;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class FeederTest  extends TestCase{
    public void  testDefaultFeeder()  {

        Feeder feeder = new DefaultFeeder();
        try {
           
            FileReader fr = new FileReader("utils/test/com/ntnu/solbrille/feeder/test/test.html");
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(fr);
            String line;

            while((line = br.readLine()) != null) {
                sb.append(line);
            }

            feeder.feed(new URI("http://www.slashdot.org/"),sb.toString());
            
            
        } catch (URISyntaxException e) {
            fail();
        } catch (IOException e) {
            fail();
        }

    }
}
