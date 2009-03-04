package com.ntnu.solbrille.query;

import java.util.HashMap;
import java.util.Set;

import com.ntnu.solbrille.query.PreparedQuery.Modifier;
/**
 * @author <a href="mailto:simonj@idi.ntnu.no">Simon Jonassen</a>
 * @version $Id $.
 */
public class UnPreparedQuery {
	private HashMap<String, Modifier> terms;
	
	public UnPreparedQuery(String query){
		String tokens[] = query.split(" ");
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
}
