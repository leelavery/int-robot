package ex3;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;
import javaFlacEncoder.FLACFileWriter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.TargetDataLine;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class Speech extends Thread {

	private static final int MS_TEST = 40;
	private static final int INIT_NOISE_LENGTH = 3000;
	private static final int HANGOVER_THRESHOLD = 400 / MS_TEST;

	TargetDataLine line;
	AudioInputStream in;
	ByteArrayOutputStream out;
	private final String apiURL = "https://www.google.com/speech-api/v1/";
	private String lang;
	private boolean listening, recognising;
	private double init_eMin;
	private HashSet<SpeechListener> listeners;
	private final String directory = "/home/robot/catkin_ws/src/leonard/robotics/sounds/";

	public Speech() {
		super();
		lang = "en-gb";
		listeners = new HashSet<SpeechListener>();
		init_eMin = -1;
		out = new ByteArrayOutputStream();
		try {
			line = AudioSystem.getTargetDataLine(new AudioFormat(16000, 16, 1, true, false));
			in = new AudioInputStream(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
		startListening();
		start();
	}

	public void synthesize(String text, boolean block) {
		try {
			System.out.println("saying: " + text);
			String textEncoded = URLEncoder.encode(text, "utf-8");
			URL url = new URL(apiURL + "synthesize?lang=" + lang + "&text=" + textEncoded);

			AudioInputStream in = AudioSystem.getAudioInputStream(url);
			play(in, block);
		} catch (Exception e) {
			System.out.println("Error synthesizing speech: " + e.getMessage());
		}
	}

	public void addListener(SpeechListener listener) {
		listeners.add(listener);
	}

	public void removeListener(SpeechListener listener) {
		listeners.remove(listener);
	}

	public RecognitionResult recognize(AudioInputStream audioIn) {
		URLConnection urlCon = null;
		String rLine, recog = "";
		float confidence = 0;
		try {
			URL url = new URL("https://www.google.com/speech-api/v1/recognize?lang=" + lang);
			urlCon = url.openConnection();
			urlCon.setRequestProperty("content-type", "audio/x-flac; rate=16000;");
			urlCon.setDoInput(true);
			urlCon.setDoOutput(true);

			System.out.println("Recognising sound...");
			AudioSystem.write(audioIn, FLACFileWriter.FLAC, urlCon.getOutputStream());

			BufferedReader rIn = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
			while ((rLine = rIn.readLine()) != null) {
				System.out.println(rLine);
				String[] tokens = rLine.split(",");
				for (String token : tokens) {
					String[] tokens2 = token.split(":");
					if (tokens2[0].equals("\"hypotheses\"")) {
						recog = tokens2[2];
					}
				}
				recog = rLine;
			}
		} catch (Exception e) {
		} finally {
			try { audioIn.close(); } catch (IOException e1) { }
			try { urlCon.getOutputStream().close(); } catch (IOException e) { }
			try { urlCon.getInputStream().close(); } catch (IOException e) { }
		}
		return new RecognitionResult(recog, confidence);
	}

	private int getBytesInMs(int ms, AudioFormat format) {
		return (int) (ms * format.getFrameSize() * format.getFrameRate()) / 1000;
	}

	public void startListening() {
		listening = true;
	}

	public void stopListening() {
		listening = false;
	}

	public void startRecognising() {
		startListening();
		recognising = true;
	}

	public void stopRecognising() {
		recognising = false;
	}
	
	public static class RecognitionResult {
		private final String text;
		private final float confidence;

		public RecognitionResult(String text, float confidence) {
			this.text = text;
			this.confidence = confidence;
		}

		public String getText() {
			return text;
		}

		public float getConfidence() {
			return confidence;
		}

	}

	public void setTrainingData() {
		setTrainingData(INIT_NOISE_LENGTH);
	}

	public void setTrainingData(int millis) {
		try {
			line.open();
			line.start();

			double sumRMS = 0;
			int numBytesRead;
			byte[] data = new byte[getBytesInMs(50, in.getFormat())];
			int samples = (millis / MS_TEST);
//			System.out.println(samples);
			for (int i = 0; i < samples; i++) {
				numBytesRead = in.read(data, 0, data.length);
				double energy = calcEnergy(getAmplitude(data, numBytesRead, in.getFormat())[0]);
				sumRMS += energy;
			}
			init_eMin = sumRMS / samples;
			System.out.println("initial mean noise: " + init_eMin);
		} catch (Exception e) {
			System.out.println("Error setting training data: " + e.getMessage());
		} finally {
			line.stop();
			line.close();
		}
	}
	
	@Override
	public void run() {
		listening = true;
		recognising = false;
		
		//startRecognising();
		if (init_eMin < 0) setTrainingData();

		try {
			line.open();
			line.start();

			byte[] data = new byte[getBytesInMs(50, in.getFormat())];
			int numBytesRead;
			double eMax = 0, eMin = init_eMin;
			double delta = 1.0;
			int inactiveFrames = 0;

			System.out.println("listening");
			while(true) {
				while(!listening) {
					System.out.println("not listening...");
					Thread.sleep(1000);
				}
				while (listening && (numBytesRead = in.read(data, 0, data.length)) != -1) {
//					System.out.println("listening...");
					double rms = calcEnergy(getAmplitude(data, numBytesRead, in.getFormat())[0]);

					if (rms > eMax) {
						eMax = rms;
					}
					if (rms < eMin) {
						if (rms == 0) {
							eMin = init_eMin;
						} else {
							eMin = rms;
						}
						delta = 1.0;
					} else {
						delta *= 1.0001;
					}

					double lambda = (eMax - eMin) / eMax;
					double threshold = (1 - lambda) * eMax + lambda * eMin;
					if (rms > threshold) {
						out.write(data);
						inactiveFrames = 0;
					} else if (inactiveFrames <= HANGOVER_THRESHOLD) {
						out.write(data);
						inactiveFrames++;
					} else if (out.size() > 0 && inactiveFrames > HANGOVER_THRESHOLD) {
						System.out.println("recognising: " + recognising);
						double voiceEnergy = calcEnergy(getAmplitude(out.toByteArray(), out.size(), in.getFormat())[0]);
						if (recognising && voiceEnergy > threshold) {
							System.out.println("Sending Heard Sound: " + voiceEnergy + " > " + threshold);
							notifyListeners(recognize(new AudioInputStream(new ByteArrayInputStream(out.toByteArray()), in.getFormat(), AudioSystem.NOT_SPECIFIED)));
						} else if (recognising) {
							System.out.println("Sorry, I can't quite hear you...");
						}
						out.reset();
					}
					eMin *= delta;
					Thread.yield();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			line.stop();
			line.close();
			listening = false;
			System.out.println("stopped listening");
		}
	}

	public void play(String fn) {
		play(fn, false);
	}

	public void play(String fn, boolean blocking) {
		try {
			String filename = directory + fn + ".mp3";
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
		// run in new thread to play in background
		stopListening();
		final Player player = new Player(audioIn);
		if (blocking) {
			player.play();
			startListening();
		} else {
			new Thread() {
				public void run() {
					try {
						player.play();
						startListening();
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}
			}.start();
		}
	}

	
	//@Override
	public void finalize() {
		line.stop();
		line.close();
		try {
			in.close();
		} catch (IOException e) {
		}
	}

	private void notifyListeners(RecognitionResult data) {
		for (SpeechListener listener : listeners) {
			listener.speechHeard(data);
		}
	}

	public static void main(String[] args) {

		Speech test = new Speech();

		test.startListening();

	}

	public static double calcEnergy(double[] frameValues) {
		double sum = 0;
		if (frameValues.length == 0) {
			return sum;
		} else {
			for (int i = 0; i < frameValues.length; i++) {
				sum += (frameValues[i] * frameValues[i]);
			}
		}
		double mean = sum / frameValues.length;

		return Math.sqrt(mean);
	}

	public static byte[] getFrame(int frameNum, byte[] bytes, AudioFormat format) {
		byte[] toReturn = new byte[format.getFrameSize()];
		for (int i = 0; i < toReturn.length; i++) {
			toReturn[i] = bytes[(frameNum * format.getFrameSize()) + i];
		}
		return toReturn;
	}

	public static double[][] getAmplitude(byte[] bytes, int length, AudioFormat format) {
		int numChannels = format.getChannels(), bytesPerFrame = format.getFrameSize();
		double[][] toReturn = new double[numChannels][length / (bytesPerFrame * numChannels)];

		for (int frameNum = 0; frameNum < toReturn[0].length; frameNum++) {
			byte[] frame = getFrame(frameNum, bytes, format);
			for (int channelNo = 0; channelNo < numChannels; channelNo++) {
				toReturn[channelNo][frameNum] = getLevel(frame, channelNo, format) / Math.pow(2, format.getSampleSizeInBits() - 1);
			}
		}
		return toReturn;
	}

	/* *********************************************
	 * ************** COPIED METHODS ************************************************************
	 */

	public static int getLevel(byte[] frame, int channel, AudioFormat format) {
		int sampleSizeInBits = format.getSampleSizeInBits();
		boolean isBigEndian = format.isBigEndian();
		if (format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
			if (sampleSizeInBits == 8)// 8 bits == 1 byte
				return frame[sampleSizeInBits * channel / 8];
			else if (sampleSizeInBits == 16)
				return bytesToInt16(frame, sampleSizeInBits * channel / 8, isBigEndian);
			else if (sampleSizeInBits == 24)
				return bytesToInt24(frame, sampleSizeInBits * channel / 8, isBigEndian);
			else if (sampleSizeInBits == 32)
				return bytesToInt32(frame, sampleSizeInBits * channel / 8, isBigEndian);
			else {
				System.err.println("Unsupported audio encoding.  The sample size is not recognized as a standard format.");
				return -1;
			}
		} else if (format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
			if (sampleSizeInBits == 8)
				return unsignedByteToInt(frame[sampleSizeInBits * channel / 8]);
			else if (sampleSizeInBits == 16)
				return unsignedByteToInt16(frame, sampleSizeInBits * channel / 8, isBigEndian);
			else if (sampleSizeInBits == 24)
				return unsignedByteToInt24(frame, sampleSizeInBits * channel / 8, isBigEndian);
			else if (sampleSizeInBits == 32)
				return unsignedByteToInt32(frame, sampleSizeInBits * channel / 8, isBigEndian);
			else {
				System.err.println("Unsupported audio encoding.  The sample size is not recognized as a standard format.");
				return -1;
			}
		} else {
			System.err.println("unsupported audio encoding: " + format.getEncoding() + ".  Currently only PCM is supported.  Please try again with a different file.");
			return -1;
		}

	}

	/**
	 * Converts 2 successive bytes starting at <code>byteOffset</code> in <code>buffer</code> to a signed integer sample with 16bit range.
	 * <p>
	 * For little endian, buffer[byteOffset] is interpreted as low byte, whereas it is interpreted as high byte in big endian.
	 * <p>
	 * This is a reference function.
	 */
	private static int bytesToInt16(byte[] buffer, int byteOffset, boolean bigEndian) {
		return bigEndian ? ((buffer[byteOffset] << 8) | (buffer[byteOffset + 1] & 0xFF)) :

		((buffer[byteOffset + 1] << 8) | (buffer[byteOffset] & 0xFF));
	}

	/**
	 * Converts 3 successive bytes starting at <code>byteOffset</code> in <code>buffer</code> to a signed integer sample with 24bit range.
	 * <p>
	 * For little endian, buffer[byteOffset] is interpreted as lowest byte, whereas it is interpreted as highest byte in big endian.
	 * <p>
	 * This is a reference function.
	 */
	private static int bytesToInt24(byte[] buffer, int byteOffset, boolean bigEndian) {
		return bigEndian ? ((buffer[byteOffset] << 16) // let Java handle sign-bit
				| ((buffer[byteOffset + 1] & 0xFF) << 8) // inhibit sign-bit handling
		| ((buffer[byteOffset + 2] & 0xFF))) :

		((buffer[byteOffset + 2] << 16) // let Java handle sign-bit
				| ((buffer[byteOffset + 1] & 0xFF) << 8) // inhibit sign-bit handling
		| (buffer[byteOffset] & 0xFF));
	}

	/**
	 * Converts a 4 successive bytes starting at <code>byteOffset</code> in <code>buffer</code> to a signed 32bit integer sample.
	 * <p>
	 * For little endian, buffer[byteOffset] is interpreted as lowest byte, whereas it is interpreted as highest byte in big endian.
	 * <p>
	 * This is a reference function.
	 */
	private static int bytesToInt32(byte[] buffer, int byteOffset, boolean bigEndian) {
		return bigEndian ? ((buffer[byteOffset] << 24) // let Java handle sign-bit
				| ((buffer[byteOffset + 1] & 0xFF) << 16) // inhibit sign-bit handling
				| ((buffer[byteOffset + 2] & 0xFF) << 8) // inhibit sign-bit handling
		| (buffer[byteOffset + 3] & 0xFF)) :

		((buffer[byteOffset + 3] << 24) // let Java handle sign-bit
				| ((buffer[byteOffset + 2] & 0xFF) << 16) // inhibit sign-bit handling
				| ((buffer[byteOffset + 1] & 0xFF) << 8) // inhibit sign-bit handling
		| (buffer[byteOffset] & 0xFF));
	}

	private static int unsignedByteToInt(byte b) {
		/*
		 * & 0xFF while seemingly doing nothing to the individual bits, forces java to recognize the byte as unsigned. so, we return to the calling function a number between 0 and 256.
		 */
		return ((int) b & 0xFF);
	}

	private static int unsignedByteToInt16(byte[] buffer, int offset, boolean isBigEndian) {
		/*
		 * here, we want to take the first byte and shift it left 8 bits then concatenate on the 8 bits in the second byte. now we have a 16 bit number that java will recognize as unsigned, so we return a number in the range [0, 65536]
		 */

		if (isBigEndian) {
			return ((unsignedByteToInt(buffer[offset]) << 8) | unsignedByteToInt(buffer[offset + 1]));
		} else {
			return ((unsignedByteToInt(buffer[offset + 1]) << 8) | unsignedByteToInt(buffer[offset]));
		}

	}

	public static int unsignedByteToInt24(byte[] buffer, int offset, boolean isBigEndian) {
		if (isBigEndian) {
			return ((unsignedByteToInt(buffer[offset]) << 16) | (unsignedByteToInt(buffer[offset + 1]) << 8) | unsignedByteToInt(buffer[offset + 2]));
		} else {
			return ((unsignedByteToInt(buffer[offset + 2]) << 16) | (unsignedByteToInt(buffer[offset + 1]) << 8) | unsignedByteToInt(buffer[offset]));
		}
	}

	public static int unsignedByteToInt32(byte[] buffer, int offset, boolean isBigEndian) {
		if (isBigEndian) {
			return ((unsignedByteToInt(buffer[offset]) << 24) | (unsignedByteToInt(buffer[offset + 1]) << 16) | (unsignedByteToInt(buffer[offset + 2]) << 8) | unsignedByteToInt(buffer[offset + 3]));
		} else {
			return ((unsignedByteToInt(buffer[offset + 3]) << 24) | (unsignedByteToInt(buffer[offset + 2]) << 16) | (unsignedByteToInt(buffer[offset + 1]) << 8) | unsignedByteToInt(buffer[offset]));
		}
	}

	public static interface SpeechListener {
		public void speechHeard(RecognitionResult data);
	}

}