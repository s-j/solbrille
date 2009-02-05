package com.ntnu.solbrille.utils;

import java.util.Iterator;

/**
 * @author <a href="mailto:olanatv@stud.ntnu.no">Ola Natvig</a>
 * @version $Id $.
 */
public abstract class EmbeddedLinkedListElement<T extends EmbeddedLinkedListElement<T>> {

    private final EmbeddedLinkedList<T> ownerList;

    private T listPrevious;
    private T listNext;

    protected EmbeddedLinkedListElement(EmbeddedLinkedList<T> ownerList) {
        this.ownerList = ownerList;
    }

    protected void removeFromList() {
        if (listPrevious != null) {
            listPrevious.listNext = listNext;
        }
        if (listNext != null) {
            listNext.listPrevious = listPrevious;
        }
    }

    Iterator<T> createIterator() {
        @SuppressWarnings({"unchecked"}) final T start = (T) this;
        return new Iterator<T>() {

            private T item = start;
            private T last;

            public boolean hasNext() {
                return item != null && item.listNext != null;
            }

            public T next() {
                T next = item;
                item = item.listNext;
                last = next;
                return next;
            }

            public void remove() {
                last.removeFromList();
            }
        };
    }

    public T getListPrevious() {
        return listPrevious;
    }

    public void setListPrevious(T listPrevious) {
        this.listPrevious = listPrevious;
    }

    public T getListNext() {
        return listNext;
    }

    public void setListNext(T listNext) {
        this.listNext = listNext;
    }

    public EmbeddedLinkedList<T> getOwnerList() {
        //noinspection ReturnOfCollectionOrArrayField
        return ownerList;
    }
}
