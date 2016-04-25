package com.sheffield.instrumenter.analysis.task;

public interface Task {
	public void start();

	public void end();

	public long getStartTime();

	public long getEndTime();

	public String asString();
}
