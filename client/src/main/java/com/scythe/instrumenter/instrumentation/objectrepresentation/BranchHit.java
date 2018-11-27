package com.scythe.instrumenter.instrumentation.objectrepresentation;

import com.scythe.instrumenter.analysis.ClassAnalyzer;
import com.scythe.instrumenter.instrumentation.ClassStore;
import java.lang.reflect.InvocationTargetException;

public class BranchHit {
	private Branch branch;
	private int counterId;
	private float distance;
	private boolean seen = false;
	private int classId = 0;

	public boolean isSeen() {
		return distance != -1;
	}

	public int getDistanceId() {
		return distanceId;
	}

	private int distanceId;



	public BranchHit(Branch branch, int counterId, int distanceId) {
		this.branch = branch;
		this.counterId = counterId;
		this.distanceId = distanceId;
		this.distance = -1f;

		classId = ClassAnalyzer.getClassId(branch.getClassName());
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
		return branch.getHits() > 0;
	}

	public void collect(){
		try {
			ClassAnalyzer.collectHitCountersForClass(ClassStore.get(branch.getClassName()), false);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}