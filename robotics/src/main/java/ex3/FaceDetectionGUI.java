package ex3;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.Rect;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import ex3.PersonFinder.Person;
import ex3.PersonFinder.PersonFinderListener;


/**
 * Paint troll smile on all detected faces.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class FaceDetectionGUI extends JFrame implements WebcamPanel.Painter, PersonFinderListener {

	private static final long serialVersionUID = 1L;

	private static final Stroke STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, new float[] { 1.0f }, 0.0f);

	private Person[] people;
		
	private WebcamPanel.Painter painter = null;

	public FaceDetectionGUI(Webcam webcam, int fps) {
		super();

		people = new Person[0];
		WebcamPanel panel = new WebcamPanel(webcam, false);
		panel.setFPSDisplayed(true);
		panel.setFPSLimited(true);
		panel.setFPSLimit(fps);
		panel.setPainter(this);
		panel.start();

		painter = panel.getDefaultPainter();

		add(panel);

		setTitle("Face Detector Example");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);

	}

	@Override
	public void paintPanel(WebcamPanel panel, Graphics2D g2) {
		System.out.println("paint panel");
		if (painter != null) {
			painter.paintPanel(panel, g2);
		}
	}

	@Override
	public void paintImage(WebcamPanel panel, BufferedImage image, Graphics2D g2) {
		if (painter != null) {
			painter.paintImage(panel, image, g2);
		}

		for (Person person : people) {
			Rect face = person.getRect();
			
			g2.setStroke(STROKE);
			g2.setColor(Color.RED);
			g2.fillRect(face.x, face.y, face.width, face.height);
			
		}
	}
	
	public static void main(String[] args) throws IOException {
	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	    Webcam webcam = Webcam.getWebcams().get(Webcam.getWebcams().size() - 1);
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		
		PersonFinder personFinder = new PersonFinder(webcam);
		personFinder.addListener(new FaceDetectionGUI(webcam, 20));
		personFinder.startFaceDetection();
	}

	@Override
	public void peopleFound(Person[] people) {
		this.people = people;
	}
}