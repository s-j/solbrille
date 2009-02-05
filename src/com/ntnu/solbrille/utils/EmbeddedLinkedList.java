package com.ntnu.solbrille.utils;

import java.util.AbstractQueue;
import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public class EmbeddedLinkedList<T extends EmbeddedLinkedListElement<T>>
        extends AbstractQueue<T> {

    private T head;
    private T tail;

    @Override
    public Iterator<T> iterator() {
        return head != null ? head.createIterator() : new Iterator<T>() {

            public boolean hasNext() {
                return false;
            }

            public T next() {
                return null;
            }

            public void remove() {
            }
        };
    }

    @Override
    public int size() {
        T item = head;
        int i = 0;
        while (item != null) {
            //noinspection LawOfDemeter
            item = item.getListNext();
            i++;
        }
        return i;
    }

    public boolean offer(T e) {
        addLast(e);
        return true;
    }

    public void addFirst(T item) {
        if (head != null) {
            head.setListPrevious(item);
        } else {
            tail = item;
        }
        item.setListNext(head);
        head = item;
    }

    public void addLast(T item) {
        if (tail != null) {
            tail.setListNext(item);
        } else {
            head = item;
        }
        item.setListPrevious(tail);
        tail = item;
    }

    public T poll() {
        T result = head;
        if (head == null) {
            return result;
        }
        T headNext = head.getListNext();
        if (headNext != null) {
            //noinspection LawOfDemeter
            headNext.setListPrevious(head.getListPrevious());
            head = head.getListNext();
        }
        return result;
    }

    public T peek() {
        return head;
    }
}
