package com.ntnu.solbrille.query;

import java.util.HashMap;

import com.ntnu.solbrille.index.occurence.DictionaryTerm;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class PreparedQuery {
	public static enum Modifier{AND, OR, NAND};
	
	private HashMap<DictionaryTerm, Modifier> terms = new HashMap<DictionaryTerm, Modifier>();
	
	public void addTerm(DictionaryTerm term, Modifier mod){
		terms.put(term, mod);
	}
	
	public HashMap<DictionaryTerm, Modifier> getTerms(){
		return terms;
	}
	
}
