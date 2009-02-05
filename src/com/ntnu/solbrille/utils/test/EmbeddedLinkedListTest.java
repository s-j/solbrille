package com.ntnu.solbrille.utils.test;

import com.ntnu.solbrille.utils.EmbeddedLinkedList;
import com.ntnu.solbrille.utils.EmbeddedLinkedListElement;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class EmbeddedLinkedListTest extends TestCase {

    private static class TestElement extends EmbeddedLinkedListElement<TestElement> {

        private int value;

        TestElement(int value, EmbeddedLinkedList<TestElement> ownerList) {
            super(ownerList);
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    @SuppressWarnings({"MessageMissingOnJUnitAssertion", "UnqualifiedStaticUsage", "LawOfDemeter", "ChainedMethodCall"})
    public void testEmbeddedLinkedListTest() {
        EmbeddedLinkedList<TestElement> list = new EmbeddedLinkedList<TestElement>();
        list.addFirst(new TestElement(1, list));
        list.addLast(new TestElement(2, list));
        list.addFirst(new TestElement(0, list));
        TestElement head = list.peek();
        assertEquals(0, head.getValue());
        assertNull(head.getListPrevious());
        assertEquals(1, head.getListNext().getValue());
        assertEquals(head.getListNext().getListPrevious().getValue(), head.getValue());
        assertEquals(2, head.getListNext().getListNext().getValue());
        assertEquals(0, list.poll().getValue());
        assertNull(list.peek().getListPrevious());
        assertEquals(1, list.poll().getValue());
        assertEquals(2, list.poll().getValue());
    }

}
