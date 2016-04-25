package com.sheffield.instrumenter.states;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class StateNode {
	private ArrayList<StateNode> nodes;
	private int state;

	public StateNode(int state) {
		nodes = new ArrayList<StateNode>();
		this.state = state;
	}

	public ArrayList<StateNode> getChildren() {
		return nodes;
	}

	public void addChild(StateNode child) {
		if (!nodes.contains(child)){
			nodes.add(child);
		}
	}

	public boolean containsState(int state, ArrayList<Integer> nodesSeen) {

		if (this.state == state) {
			return true;
		}

		if (nodesSeen.contains(this.state)) {
			// stuck in a loop
			return false;
		}

		nodesSeen.add(this.state);

		for (StateNode s : nodes) {
			if (s.containsState(state, nodesSeen)) {
				return true;
			}
		}

		return false;
	}

	public StateNode getStateNode(int state, ArrayList<Integer> nodesSeen) {

		if (this.state == state) {
			return this;
		}

		if (nodesSeen.contains(this.state)) {
			// stuck in a loop
			return null;
		}

		nodesSeen.add(this.state);

		for (StateNode s : nodes) {
			StateNode sn = s.getStateNode(state, nodesSeen);

			if (sn != null) {
				return sn;
			}
		}
		return null;
	}

	public String toString(ArrayList<Integer> nodesSeen) {
		if (nodesSeen.contains(state)) {
			// stuck in a loop
			return "@" + state + ":[>]";
		}

		nodesSeen.add(state);

		String returnValue = "@" + state + ":[";
		String value = "";
		for (StateNode s : nodes) {
			value += s.toString(nodesSeen);
		}

		if (value.length() == 0) {
			value = "_";
		}

		return returnValue + value + "]";
	}

	public void paint(Graphics2D g, int x, int y, int radius, int spacing, ArrayList<Integer> nodesSeen) {

		int children = getChildren().size() - 1;

		int xStart = x - (radius * 2 * children * spacing / 2);

		int yStart = y + spacing + radius * 2;

		if (!nodesSeen.contains(state)) {
			nodesSeen.add(state);
			for (int i = 0; i < nodes.size(); i++) {
				
				g.setColor(Color.GREEN);
				g.drawLine(xStart + (i * (radius * 2 * children * spacing / 2)), yStart, x, y);
				nodes.get(i).paint(g, xStart + (i * (radius * 2 * children * spacing / 2)), yStart, radius, spacing, nodesSeen);
			}
			g.setColor(Color.WHITE);
		} else {
			g.setColor(Color.ORANGE);
		}


		g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

		g.setColor(Color.BLUE);
		g.drawString("" + state, x, y + radius / 2);
	}
}
