package com.sheffield.instrumenter.states;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.net.Socket;

public class ScreenGrabber {

	public static final Dimension DIMENSION;

	public static Robot robot = null;

	public static final Rectangle dimensions;// Toolkit.getDefaultToolkit().getScreenSize());

	public static String HOST = "localhost";
	public static int PORT = 1935;
	public static String STREAM = "test";
	public static String APPLICATION = "live";

	public static Socket livestream = new Socket();
	public static DataInputStream dis;

	// public static FFmpegFrameRecorder frec = new FFmpegFrameRecorder(URL,
	// 1920, 1080);

	static {
		GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		GraphicsDevice gd = gds[0];

		DIMENSION = new Dimension(gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());

		dimensions = new Rectangle(DIMENSION);
		// try {
		// String subFolder = APPLICATION + "/" + STREAM;
		// String host = HOST;
		//
		// livestream.connect(new InetSocketAddress(host, PORT));
		//
		// PrintWriter out = new PrintWriter(livestream.getOutputStream());
		// out.println("GET " + subFolder + " HTTP/1.0");
		// out.println("User-Agent: Wget/1.8.2");
		// out.println("Host: " + host);
		// out.println("Accept: */*");
		// out.println("Connection: Keep-Alive");
		// out.println("");
		// out.flush();
		//
		// dis = new DataInputStream(livestream.getInputStream());
		// String line = "";
		//
		// while ((line = dis.readLine()) != null){
		// com.sheffield.leapmotion_tester.App.out.println(line);
		// }
		// } catch (IOException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// System.exit(-2);
		// }
		try {
			robot = new Robot();
		} catch (AWTException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// try {
		// LeapMotionApplicationHandler.addUrlToSystemClasspath(new URL("file:/"
		// + "sikulixlibs/windows/libs32"));
		// } catch (MalformedURLException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		// windows="video=\"screen-capture-recorder\"" linux=":0.0+" + 0 + "," +
		// 0 mac=-i "<screen device index>:"
		// com.sheffield.leapmotion_tester.App.out.println("Setting up
		// GRABBER.");
		// GRABBER = new FFmpegFrameGrabber(URL);
		// // windows = dshow linux = x11grab mac = avfoundation
		// GRABBER.setFormat("mp4");
		//// int w = (int) DIMENSION.getWidth(), h = (int)
		// DIMENSION.getHeight();
		//// GRABBER.setImageWidth(w);
		//// GRABBER.setImageHeight(h);
		// try {
		// GRABBER.start();
		// com.sheffield.leapmotion_tester.App.out.println("Setup Complete!");
		// } catch (Exception e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		// frec.setFormat("flv");
		//
		// try {
		// frec.start();
		// } catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static BufferedImage capture() {

		// try {
		// com.sheffield.leapmotion_tester.App.out.println("Grabbing screen");
		// //GRABBER.start();
		// Frame f = GRABBER.grab();
		// Java2DFrameConverter fc = new Java2DFrameConverter();
		// BufferedImage bi = fc.convert(f);
		// //GRABBER.stop();
		// return bi;
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		return null;
	}

	public static BufferedImage captureRobot() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedImage bi = robot.createScreenCapture(dimensions);
		robot = null;
		return bi;
	}
}
