package com.sheffield.instrumenter.instrumentation.objectrepresentation;

public class LineHit {
	private int counterId;
	private Line line;

	public LineHit(Line line, int counterId) {
		this.line = line;
		this.counterId = counterId;
	}

	public Line getLine() {
		return line;
	}

	public int getCounterId() {
		return counterId;
	}

	public void reset() {
		line.reset();
	}
}