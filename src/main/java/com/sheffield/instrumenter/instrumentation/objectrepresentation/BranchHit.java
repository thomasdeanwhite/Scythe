package com.sheffield.instrumenter.instrumentation.objectrepresentation;

public class BranchHit {
	private Branch branch;
	private int trueCounterId;
	private int falseCounterId;

	public BranchHit(Branch branch, int trueCounterId, int falseCounterId) {
		this.branch = branch;
		this.trueCounterId = trueCounterId;
		this.falseCounterId = falseCounterId;
	}

	public Branch getBranch() {
		return branch;
	}

	public int getTrueCounterId() {
		return trueCounterId;
	}

	public int getFalseCounterId() {
		return falseCounterId;
	}

	public void reset() {
		branch.reset();
	}
}