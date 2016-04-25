package com.sheffield.instrumenter.instrumentation.objectrepresentation;

public class Line extends CoverableGoal {
	private int hits;

	public Line(String className, int lineNumber) {
		super(className, lineNumber);
	}

	public void hit(int newHits) {
		this.hits += newHits;
	}

	public int getHits() {
		return hits;
	}

	public void reset() {
		hits = 0;
	}

	@Override
	public String toString() {
		return String.valueOf(lineNumber);
	}
}
