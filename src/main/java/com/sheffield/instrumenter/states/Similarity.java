package com.sheffield.instrumenter.states;

import java.util.HashMap;

public class Similarity {
	private HashMap<String, Float> changes;
	private float similarity;
	private boolean sameState = false;
	
	protected Similarity(HashMap<String, Float> changes, float similarity){
		this.changes = changes;
		this.similarity = similarity;
		sameState = false;
	}
	
	public float getSimilarity(){
		return similarity;
	}
	
	public HashMap<String, Float> getChanges(){
		return changes;
	}
	
	public boolean isSameState(){
		return sameState;
	}
	
	protected void setSameState(){
		sameState = true;
	}
}
