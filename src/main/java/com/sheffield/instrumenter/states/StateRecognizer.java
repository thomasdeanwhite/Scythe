package com.sheffield.instrumenter.states;

public abstract class StateRecognizer {

	public static final float SIMILARITY_THRESHOLD = 0.4f;

	public abstract int recognizeState();

	public abstract boolean isProcessing();

}
