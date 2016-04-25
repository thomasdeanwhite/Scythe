package com.sheffield.instrumenter;

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.JFrame;

import com.sheffield.instrumenter.states.StateTracker;

public class Display extends JFrame {

	private static Display display;
	public static final int MAX_STRING_LENGTH = 40;

	public static Display getDisplay() {
		if (display == null) {
			display = new Display();
		}
		return display;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final int FONT_SIZE = 12;
	public static final int PADDING = 5;
	public static final int COMMANDS_TO_DISPLAY = 10;

	public static final int HEIGHT = 1024;
	public static final int WIDTH = 400;

	private OutputCanvas output;

	protected class OutputCanvas extends Canvas {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private ArrayList<String> commands;

		private StateTracker stateTracker;

		public OutputCanvas() {
			setSize(Display.WIDTH, Display.HEIGHT);
			commands = new ArrayList<String>();
		}

		public void addLine(String text) {
			commands.add(text);
			paint(getGraphics());
			// invalidate();
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2d = (Graphics2D) g;
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, getWidth(), getHeight());
			g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g2d.setColor(Color.WHITE);
			Font font = new Font(Font.SANS_SERIF, Font.BOLD, FONT_SIZE);
			g2d.setFont(font);
			int start = Math.max(commands.size() - COMMANDS_TO_DISPLAY, 0);
			for (int i = start; i < commands.size(); i++) {
				g2d.drawString(commands.get(i), PADDING, (i - start) * (PADDING + FONT_SIZE) + FONT_SIZE + PADDING);
			}

			if (stateTracker != null) {
				stateTracker.paint(g2d);
			}

		}

		public void drawTrackerChange(StateTracker st) {
			stateTracker = st;
			paint(getGraphics());
		}

	}

	private Display() {
		super("TGOUT");
		if (Properties.SHOW_GUI) {
			setLayout(new FlowLayout());
			output = new OutputCanvas();
			add(output);
			setUndecorated(true);
			setVisible(true);
			setSize(Display.WIDTH, HEIGHT);
			setAlwaysOnTop(true);
			setLocation(0, 0);
			output.invalidate();
		} else {
			setVisible(false);
		}
	}

	public void addCommand(String s) {
		if (Properties.SHOW_GUI) {
			String command = s;
			if (s.length() > MAX_STRING_LENGTH) {
				command = s.substring(0, MAX_STRING_LENGTH) + "...";
			}

			output.addLine(command);
		}
		// invalidate();
	}

	public void drawTrackerChange(StateTracker st) {
		if (Properties.SHOW_GUI) {
			output.drawTrackerChange(st);
		}
	}

}
