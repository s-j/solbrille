package com.ntnu.solbrille.query;

public class ProcessedQueryResult implements Comparable<ProcessedQueryResult>{
	private float score;
	private UnprocessedQueryResult result;
	
	public ProcessedQueryResult(long documentID, float score, UnprocessedQueryResult result){
		this.score=score;
		this.result=result;
	}
	
	public float getScore(){
		return score;
	}
	
	public long getDocumentId(){
		return result.gedocumentId;
	}
	
	public UnprocessedQueryResult getUnprocessedResult(){
		return result;
	}
	
	public int compareTo(ProcessedQueryResult result){
		if (this.score < score) return -1;
		else if (this.score == score) return 0;
		else return 1;
	}
}

