package com.ntnu.solbrille.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.ntnu.solbrille.index.occurence.DictionaryTerm;
import com.ntnu.solbrille.utils.IntArray;
import com.ntnu.solbrille.utils.Pair;
//import com.sun.tools.javac.util.List;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class QueryRequest {
	public static enum Modifier{AND, OR, NAND};
	private String strquery; 
	
	private HashMap<DictionaryTerm, Pair<IntArray, IntArray>> terms;
	private HashMap<DictionaryTerm, Long> termstats;
	private ArrayList<ArrayList<DictionaryTerm>> phrases; //pairs of start,endpos..
	
	//TODO: could be nice to add a list of term modifications in each of queries
	public QueryRequest(String strquery){
		this.strquery = strquery;
		terms = new HashMap<DictionaryTerm, Pair<IntArray,IntArray>>();
		termstats = new HashMap<DictionaryTerm, Long>();
		phrases = new ArrayList<ArrayList<DictionaryTerm>>();
	}

	public Pair<IntArray, IntArray> addTerm(DictionaryTerm term, IntArray occs, IntArray flags){
		return terms.put(term, new Pair<IntArray, IntArray>(occs, flags));
	}
	
	public void addTermOccurence(DictionaryTerm term, int pos, Modifier flag){
		Pair<IntArray, IntArray> cur = terms.get(term);
		if (cur == null) {
			cur = new Pair<IntArray, IntArray>(new IntArray(), new IntArray());
		}
		cur.getFirst().add(pos);
		cur.getSecond().add(flag.ordinal());
	}
	
	public void setDocumentCount(DictionaryTerm term, long count){
		termstats.put(term, new Long(count));
	}
	
	public long getDocumentCount(DictionaryTerm term){
		Long v = termstats.get(term);
		if (v!=null) return v.longValue();
		else return 0;
	}
	
	
	public Set<DictionaryTerm> getTerms(){
		return terms.keySet();
	}
	
	public QueryTermOccurence getOccurence(DictionaryTerm term){
		Pair<IntArray, IntArray> tmp = terms.get(term);
		return new QueryTermOccurence(tmp.getFirst(), tmp.getSecond());
	}
	
	public void addPhrase(ArrayList<DictionaryTerm> phrase){
		phrases.add(phrase);
	}
	
	//TODO: change to a better type
	public ArrayList<ArrayList<DictionaryTerm>> getPhrases(){
		return phrases;
	}
	
	public void updateTerm(DictionaryTerm oldterm, DictionaryTerm newterm){
		terms.put(newterm, terms.remove(oldterm));
	}
	
	public QueryTermOccurence deleteTerm(DictionaryTerm term){
		Pair<IntArray, IntArray> termlists = terms.remove(term);
		return new QueryTermOccurence(termlists.getFirst(), termlists.getSecond());
	}
	
	public String getQueryString(){
		return strquery;
	}
	
	public long getQueryOccurenceCount(DictionaryTerm term){
		Pair<IntArray, IntArray> termlists = terms.get(term);
		return termlists.getFirst().size();
		
	}
	
	public QueryTermOccurence getQueryOccurences(DictionaryTerm term){
		Pair<IntArray, IntArray> termlists = terms.get(term);
		return new QueryTermOccurence(termlists.getFirst(), termlists.getSecond());
	}
	
}
