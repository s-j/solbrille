package com.ntnu.solbrille.query;

import java.util.Iterator;

import com.ntnu.solbrille.query.QueryRequest.Modifier;
import com.ntnu.solbrille.utils.IntArray;
import com.ntnu.solbrille.utils.Pair;

public class QueryTermOccurence implements Iterator<Pair<Integer, Modifier>>{
	private IntArray pos, flags;
	private int curr;
	
	
	public QueryTermOccurence(IntArray pos, IntArray flags){
		this.pos = pos;
		this.flags = flags;
		curr = 0;
	}
	
	@Override
	public boolean hasNext() {
		return curr < pos.size();
	}

	@Override
	public Pair<Integer, Modifier> next() {
		if (curr >= pos.size() ) return null;
		Pair<Integer, Modifier> next = new Pair<Integer, Modifier>(pos.get(curr), Modifier.values()[flags.get(curr)]);
		curr++;
		return next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	public int size(){
		return pos.size();
	}
	
	public void reset(){
		curr = 0;
	}

    public void removeMarked(int flag){
        IntArray newpos = new IntArray();
        IntArray newflags = new IntArray();
        for (int i=0; i<pos.size(); i++){
            if (flags.get(i) != flag){
                newflags.add(flags.get(i));
                newpos.add(pos.get(i));
            }
        }
        pos = newpos;
        flags = newflags;
        reset();
    }

    public Pair<Integer, Modifier> get(int i){
        Pair<Integer, Modifier> ret = new Pair<Integer, Modifier>(pos.get(i), Modifier.values()[flags.get(i)]);
		return ret;
    }
}
