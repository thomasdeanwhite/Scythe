package com.sheffield.instrumenter.analysis;

import com.sheffield.instrumenter.analysis.task.AbstractTask;

public class InstrumentingTask extends AbstractTask {
	private String className;

	public InstrumentingTask(String className) {
		this.className = className;
	}

	@Override
	public String asString() {
		return "Instrumenting " + className;
	}
}