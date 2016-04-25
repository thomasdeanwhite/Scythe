package com.sheffield.instrumenter.states;

import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.HashMap;

import com.sheffield.instrumenter.Display;

public class ImageStateRecognizer extends StateRecognizer {

	private HashMap<Integer, BufferedImage> stateImages;

	// 256 colours * 3 colours
	public static final int MAX_VALUE_PER_PIXEL = 256;

	private boolean processing;

	public ImageStateRecognizer() {
		stateImages = new HashMap<Integer, BufferedImage>();
		// ScreenGrabber.captureRobot();
		processing = false;
	}

	@Override
	public int recognizeState() {
		processing = true;
		try {
			// App.out.println("Grabbing screen!");
			Display.getDisplay().setVisible(false);
			BufferedImage image = ScreenGrabber.captureRobot();
			if (image == null) {
				processing = false;
				Display.getDisplay().setVisible(true);
				// App.out.println("Invalid screen!");
				return 0;
			}

			// App.out.println("Grabbed screen!");

			float bestSimilarity = 0f;
			int bestState = -1;

			for (int i : stateImages.keySet()) {
				BufferedImage state = stateImages.get(i);
				float sim = calculateSimilarity(image, state);
				// App.out.println("@s"+ i + " similarity: " + sim);
				if (sim > SIMILARITY_THRESHOLD && sim > bestSimilarity) {
					bestSimilarity = sim;
					bestState = i;
				}
			}
			processing = false;

			Display.getDisplay().setVisible(true);

			if (bestState == -1) { // cannot match state, create new one.
				int newState = stateImages.keySet().size();
				// App.out.println("Found state @s" + newState);
				stateImages.put(newState, image);
				return newState;
			} else {
				return bestState;
			}

		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		processing = false;
		Display.getDisplay().setVisible(true);
		return -1;
	}

	protected Similarity calculateSimilarity(int state, HashMap<String, Float> differences) {
		return new Similarity(null, 1);
	}

	protected float calculateSimilarity(BufferedImage img, BufferedImage img2) {
		DataBuffer db = img.getData().getDataBuffer();
		DataBuffer db2 = img2.getData().getDataBuffer();

		int size = db.getSize();
		float difference = 0f;
		for (int i = 0; i < size; i++) {
			difference += Math.abs(db.getElemFloat(i) - db2.getElemFloat(i));
		}

		difference /= MAX_VALUE_PER_PIXEL * size;

		float similarity = 1f - difference;

		return similarity;
	}

	@Override
	public boolean isProcessing() {
		// TODO Auto-generated method stub
		return processing;
	}

}
