package com.sheffield.instrumenter.analysis.task;

public abstract class AbstractTask implements Task {
	private long startTime;
	private long endTime;

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public long getEndTime() {
		return endTime;
	}

	@Override
	public void start() {
		startTime = System.currentTimeMillis();
	}

	@Override
	public void end() {
		endTime = System.currentTimeMillis();
	}
}