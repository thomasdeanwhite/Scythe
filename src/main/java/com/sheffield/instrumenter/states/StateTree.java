package com.sheffield.instrumenter.states;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import com.sheffield.instrumenter.Display;

public class StateTree {
	private ArrayList<StateNode> nodes;

	public StateTree() {
		nodes = new ArrayList<StateNode>();
	}

	public ArrayList<StateNode> getChildren() {
		return nodes;
	}

	public void addChild(StateNode child) {
		nodes.add(child);
	}

	public boolean containsState(int state) {
		ArrayList<Integer> nodesSeen = new ArrayList<Integer>();
		for (StateNode s : nodes) {
			if (s.containsState(state, nodesSeen)) {
				return true;
			}
		}

		return false;
	}

	public StateNode getStateNode(int state) {
		ArrayList<Integer> nodesSeen = new ArrayList<Integer>();
		for (StateNode s : nodes) {
			StateNode sn = s.getStateNode(state, nodesSeen);
			if (sn != null)
				return sn;

		}
		return null;
	}

	public String toString() {
		String returnValue = "@root:";
		String value = "\n";
		ArrayList<Integer> nodesSeen = new ArrayList<Integer>();
		for (StateNode s : nodes) {
			value += s.toString(nodesSeen) + "\n";
		}
		return returnValue + value;
	}

	public void paint(Graphics2D g) {
		int children = getChildren().size()-1;

		int x = Display.getDisplay().getWidth() / 2;
		int y = Display.getDisplay().getHeight() / 2;

		int radius = 12;
		int spacing = 5;

		int xStart = x - ((radius+spacing) * children / 2);

		ArrayList<Integer> nodesSeen = new ArrayList<Integer>();
		
		for (int i = 0; i < nodes.size(); i++) {
			g.setColor(Color.GREEN);
			g.drawLine(xStart + (i * (radius * 2 * children * spacing / 2)), y + (radius * 2) + spacing, x, y);
			nodes.get(i).paint(g, xStart + (i * (radius * 2 * children * spacing / 2)), y + (radius * 2) + spacing,
					radius, spacing, nodesSeen);
		}
		
		g.setColor(Color.WHITE);
		g.fillOval(x - radius, y - radius, radius * 2, radius * 2);

		g.setColor(Color.GRAY);
		g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

	}
}
