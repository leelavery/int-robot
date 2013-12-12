package ex3;

/*************************************************************************
 *  Compilation:  javac -classpath .:jl1.0.jar MP3.java         (OS X)
 *                javac -classpath .;jl1.0.jar MP3.java         (Windows)
 *  Execution:    java -classpath .:jl1.0.jar MP3 filename.mp3  (OS X / Linux)
 *                java -classpath .;jl1.0.jar MP3 filename.mp3  (Windows)
 *  
 *  Plays an MP3 file using the JLayer MP3 library.
 *
 *  Reference:  http://www.javazoom.net/javalayer/sources.html
 *
 *
 *  To execute, get the file jl1.0.jar from the website above or from
 *
 *      http://www.cs.princeton.edu/introcs/24inout/jl1.0.jar
 *
 *  and put it in your working directory with this file MP3.java.
 *
 *************************************************************************/

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MP3 {
	private String filename;
	private Player player;
	private final String directory = "/data/private/robot/workspace/robotics/sounds/";

	// private long lastPlayed = -1;

	// constructor that takes the name of an MP3 file
	public MP3() {
	}

	public void close() {
		if (player != null)
			player.close();
	}

	public void play(String fn) {
		play(fn, false);
	}

	public void play(String fn, boolean blocking) {
		try {
			filename = directory + fn + ".mp3";
			FileInputStream fis = new FileInputStream(filename);
			BufferedInputStream bis = new BufferedInputStream(fis);
			play(bis, blocking);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void play(InputStream audioIn, boolean blocking) {
		try {
			play2(audioIn, blocking);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	// play the MP3 file to the sound card
	private void play2(InputStream audioIn, boolean blocking) throws JavaLayerException {
		player = new Player(audioIn);
		// run in new thread to play in background
		if (blocking) {
			player.play();
		} else {
			new Thread() {
				public void run() {
					try {
						player.play();
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}
			}.start();
		}
	}
}
