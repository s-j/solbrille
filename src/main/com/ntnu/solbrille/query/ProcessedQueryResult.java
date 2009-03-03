package com.ntnu.solbrille.query;

public class ProcessedQueryResult implements Comparable<ProcessedQueryResult>{
	private long documentId;
	private float score;
	
	public ProcessedQueryResult(long documentID, float score){
		this.documentId=documentID;
		this.score=score;
	}
	
	public float getScore(){
		return score;
	}
	
	public long getDocumentId(){
		return documentId;
	}
	
	public int compareTo(ProcessedQueryResult result){
		if (this.score < score) return -1;
		else if (this.score == score) return 0;
		else return 1;
	}
}

