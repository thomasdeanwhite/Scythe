package com.sheffield.instrumenter.states;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class EuclideanStateRecognizer extends StateRecognizer {

	protected HashMap<Integer, HashMap<String, Float>> states;
	protected HashMap<Integer, Float> stateSizes;

	public EuclideanStateRecognizer() {
		states = new HashMap<Integer, HashMap<String, Float>>();
		stateSizes = new HashMap<Integer, Float>();
	}

	@Override
	public int recognizeState() {
		return 0;
	}

	protected Similarity calculateSimilarity(int state, HashMap<String, Float> differences) {

		Set<String> keys = new TreeSet<String>(differences.keySet());
		keys.addAll(states.get(state).keySet());
		HashMap<String, Float> calls = states.get(state);
		HashMap<String, Float> changes = new HashMap<String, Float>();
		float similarity = 0f;

		for (String s : keys) {
			if (differences.containsKey(s)) {
				float difference = Math.abs(differences.get(s));
				if (calls.containsKey(s)) {
					float diff = (float) Math.log(difference / calls.get(s));
					float sim = 1f - diff;
					similarity += sim;
					changes.put(s, differences.get(s) * sim);
				} else {
					changes.put(s, differences.get(s));
				}
			} else {

			}
		}

		similarity = similarity / keys.size();
		return new Similarity(changes, similarity);
	}

	protected int newState(HashMap<String, Integer> callFrequencies) {
		HashMap<String, Float> cf = new HashMap<String, Float>();

		float stateSize = 0f;

		for (String s : callFrequencies.keySet()) {
			float f = callFrequencies.get(s);
			stateSize += f;
			cf.put(s, f);
		}

		int newState = states.keySet().size();

		stateSizes.put(newState, stateSize);

		states.put(newState, cf);

		return newState;
	}

	protected int mergeStates(int state, HashMap<String, Float> differences) {

		for (String s : differences.keySet()) {
			float newFreq = 0f;
			boolean update = true;
			if (states.get(state).containsKey(s)) {
				if (differences.containsKey(s)) {
					newFreq = states.get(state).get(s) + differences.get(s);
				} else {
					update = false;
				}

			} else if (differences.containsKey(s)) {
				newFreq = differences.get(s);
			}

			if (update) {
				states.get(state).put(s, newFreq);
			}
		}

		Set<String> keys = new TreeSet<String>(differences.keySet());
		keys.addAll(states.get(state).keySet());

		HashMap<String, Float> calls = states.get(state);

		float stateSize = 0f;

		for (String key : calls.keySet()) {
			stateSize += calls.get(key);
		}

		stateSizes.put(state, stateSize);

		return state;
	}

	protected Similarity isSameState(int state, HashMap<String, Float> differences) {
		Similarity s = calculateSimilarity(state, differences);
		if (s.getSimilarity() > SIMILARITY_THRESHOLD) {
			s.setSameState();
		}
		return s;
	}

	@Override
	public boolean isProcessing() {
		// TODO Auto-generated method stub
		return false;
	}

}
