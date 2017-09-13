package com.scythe.instrumenter.instrumentation.objectrepresentation;

public class BranchHit {
	private Branch branch;
	private int counterId;
	private float distance;
	private boolean seen = false;

	public boolean isSeen() {
		return seen;
	}

	public int getDistanceId() {
		return distanceId;
	}

	private int distanceId;



	public BranchHit(Branch branch, int counterId, int distanceId) {
		this.branch = branch;
		this.counterId = counterId;
		this.distanceId = distanceId;
		this.distance = 1f;
	}

	public void setDistance(float distance){
		this.distance = distance;
	}

	public float getDistance() {
		return distance;
	}

	public Branch getBranch() {
		return branch;
	}

	public int getCounterId() {
		return counterId;
	}

	public void reset() {
		branch.reset();
	}

	public boolean covered (){
		return distance == 0;
	}
}