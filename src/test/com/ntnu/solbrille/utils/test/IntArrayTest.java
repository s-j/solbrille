package com.ntnu.solbrille.utils.test;
import junit.framework.TestCase;

import java.util.Collections;

import com.ntnu.solbrille.utils.IntArray;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class IntArrayTest extends TestCase {

    public void testIntArray() {
        int t[] = new int[]{0,5,1,6,2,7,3,8,4,9};

        IntArray ia = new IntArray(1);
        ia.setFactor(2);

        for(int i = 0;i<t.length;i++) {
            ia.add(t[i]);
        }
        assertEquals(7,(int)ia.get(5));
        Collections.sort(ia);
        for(int i = 0;i<10;i++) {
            assertEquals(i,(int)ia.get(i));
        }
        ia.remove(0);
        ia.remove(5);
        ia.remove(5);
        ia.remove(5);
        ia.remove(5);
        assertEquals(1,(int)ia.get(0));
    }

    public void testBugFromSimon() {
        IntArray ia = new IntArray(0);
        ia.add(1);
        ia.add(2);
        ia.remove(1);
        ia.remove(0);

        
    }
}
