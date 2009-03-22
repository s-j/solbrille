package com.ntnu.solbrille.utils;

import java.util.AbstractList;
import java.util.List;
import java.util.Collection;
import java.util.Arrays;

/**
 * @author <a href="mailto:arnebef@yahoo-inc.com">Arne Bergene Fossaa</a>
 * @version $Id $.
 */
public class IntArray extends AbstractList<Integer>
{

    public final static int DEFAULT_INIT_SIZE = 100;

    private float factor = 1.5f;
    private boolean shouldReduce = false;
    private int currarr[];
    private int allocated;
    private int size = 0;


    public IntArray() {
        this(DEFAULT_INIT_SIZE);

    }

    public IntArray(int initSize) {
        currarr =  new int[initSize];
    }

    public IntArray(List<Integer> ilist) {
        this(ilist.size());
        addAll(ilist);
    }

    public IntArray(IntArray ilist) {
        this(ilist.size);
        size = ilist.size;
        factor = ilist.factor;
        shouldReduce = ilist.shouldReduce;
        System.arraycopy(ilist.currarr,0,currarr,0,size);
    }


    public void add(int i) {
        expand(1);
        currarr[size-1] = i;
    }

    public void add(int index,int value) {
        expand(1);
        System.arraycopy(currarr,index,currarr,index+1,size-index);
        currarr[index] = value;
    }

    public boolean addAll(int index, Collection<? extends Integer> collection) {
        expand(collection.size());
        System.arraycopy(currarr,index,currarr,index+collection.size(),size-index);
        int pos = index;
        for(Integer i:collection) {
            currarr[pos++] = i;
        }
        return true;
    }

    @Override
    public Integer remove(int i) {
        if (i < 0 || i >= currarr.length)
            throw new IndexOutOfBoundsException();
        int tmp = currarr[i];
        System.arraycopy(currarr,i+1,currarr,i,currarr.length-i-1);
        shrink(1);
        return tmp;
    }


    private void expand(int i) {
        if(size + i > currarr.length) {
            int ncap = Math.max((int)(size * factor),size+1);
            int ncurrarr[] = new int[ncap];
            System.arraycopy(currarr,0,ncurrarr,0,currarr.length);
            currarr = ncurrarr;
        }
        size += i;
    }


    private void shrink(int i) {
        int nsize = (int) ((size-i)*factor*factor);
        if(nsize < currarr.length) {
            int ncap = (int)((size-i)*factor);
            int ncurrarr[] = new int[ncap];
            System.arraycopy(currarr,0,ncurrarr,0,size-i);
            currarr = ncurrarr;
        }
        size -= i;
    }

    public int[] getArray() {
        int arr[]= new int[size];
        System.arraycopy(currarr,0,arr,0,size);
        return arr;
    }

    public Integer[] toArray() {
        Integer arr[] = new Integer[size];
        for(int i = 0;i<size;i++)
            arr[i] = currarr[i];
        return arr;
    }


    @Override
    public boolean add(Integer i) {
        add(i.intValue());
        return true;
    }

    @Override
    public void add(int index,Integer i) {
        add(index,i.intValue());
    }

    @Override
    public Integer get(int i) {
        return currarr[i];
    }

    public int getInt(int i) {
        return currarr[i];
    }

    @Override
    public int size() {
        return size;
    }

    public Integer set(int index,Integer element) {
        int last = currarr[index];
        currarr[index] = element;
        return last;
    }



    public float getFactor() {
        return factor;
    }

    public void setFactor(float factor) {
        this.factor = factor;
    }

    public boolean isShouldReduce() {
        return shouldReduce;
    }

    public void setShouldReduce(boolean shouldReduce) {
        this.shouldReduce = shouldReduce;
    }

    public boolean equals(Object o) {
        if(!(o instanceof IntArray)) {
            return false;
        }
        IntArray ia = (IntArray) o;
        return Arrays.equals(Arrays.copyOf(currarr,size),Arrays.copyOf(ia.currarr,ia.size));
    }



}
