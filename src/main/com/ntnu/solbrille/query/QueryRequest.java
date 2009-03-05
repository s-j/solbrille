package com.ntnu.solbrille.query;

import java.util.HashMap;
import java.util.Set;

/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class QueryRequest {
	public static enum Modifier{AND, OR, NAND};
	
	private String strquery; 
	private HashMap<String, Modifier> terms;
	//TODO: could be nice to add a list of term modifications in each of queries
	
	public QueryRequest(String strquery){
		this.strquery = strquery;
		String tokens[] = strquery.split(" ");
		for (String token : tokens) {
			Modifier mod = Modifier.OR;
			if ( token.charAt(0) == '+'){
				mod = Modifier.AND;
				token = token.substring(1);
			} else if (token.charAt(0) == '-'){
				mod = Modifier.NAND;
				token = token.substring(1);
			}
			//just use the last occurrence of this term
			terms.put(token, mod);
		}
	}
	
	public Set<String> getTerms(){
		return terms.keySet();
	}
	
	public void updateTerm(String oldterm, String newterm){
		terms.put(newterm, terms.remove(oldterm));
	}
	
	public void deleteTerm(String term){
		terms.remove(term);
	}
	
	public Modifier getModifier(String term){
		return terms.get(term);
	}
	
	public String getQueryString(){
		return strquery;
	}
}
