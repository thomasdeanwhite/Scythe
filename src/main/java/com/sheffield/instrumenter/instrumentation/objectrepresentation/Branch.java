package com.sheffield.instrumenter.instrumentation.objectrepresentation;

public class Branch extends CoverableGoal {
	private int trueHits;
	private int falseHits;

	public Branch(String className, int lineNumber) {
		super(className, lineNumber);
	}

	public void trueHit(int trueHits) {
		this.trueHits += trueHits;
	}

	public void falseHit(int falseHits) {
		this.falseHits += falseHits;
	}

	public int getTrueHits() {
		return trueHits;
	}

	public int getFalseHits() {
		return falseHits;
	}

	public void reset() {
		trueHits = 0;
		falseHits = 0;
	}

	@Override
	public String toString() {
		return lineNumber + "#true, " + lineNumber + "#false";
	}

	public boolean equals(Branch other) {
		if (this == other) {
			return true;
		}
		if (lineNumber == other.lineNumber && className.equals(other.className)) {
			return true;
		}
		return false;
	}

	public void setClassName (String name){
		className = name;
	}

}
