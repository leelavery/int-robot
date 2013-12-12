package ex3.behaviours;

import java.util.Arrays;

import org.opencv.core.Rect;

import sensor_msgs.LaserScan;

import ex3.InviteScreen;
import ex3.Main;
import ex3.PersonFinder.Person;
import ex3.PersonFinder.PersonFinderListener;
import geometry_msgs.Twist;

public class HumanDetection implements Behaviour, PersonFinderListener {

	private static final int ROTATION_THRESHOLD = 10;
	private static final double INTIMACY_THRESHOLD = 0.4;
	private static final double ROTATE_SPEED = Math.toRadians(5);
	private static final double SPEED = 0.2; //0.1
	private static final long TIMEOUT = 4000;
	private static final long SLEEP_TIME = 8000;

	private Main main;
	private volatile Person[] people;
	private InviteScreen invite;

	public HumanDetection(Main main) {
		this.main = main;
		people = new Person[0];
		main.getPersonFinder().addListener(this);
		invite = new InviteScreen(main.getSpeech());
	}

	@Override
	public boolean takeControl() {
		return people.length > 0;
	}

	private Person getClosest() {
		Person[] people = Arrays.copyOf(this.people, this.people.length);
		Person closest = null;
		for (Person person : people) {
			if (closest == null || person.getRect().width > closest.getRect().width) {
				closest = person;
			}
		}
		return closest;
	}

	@Override
	public void action() {
		if (!takeControl())	return;

		main.getSpeech().play("seen-person", true);
		
		int camWidth = main.getWebcam().getViewSize().width;
		Twist twist = main.getCmdVelPublisher().newMessage();
		twist.getLinear().setX(SPEED);

		Person closest = getClosest();
		if (closest == null) return;
		Rect r = closest.getRect();
		while (getLaserRange() > INTIMACY_THRESHOLD && System.currentTimeMillis() - closest.getTimestamp() < TIMEOUT) {
			int cOffset = (r.x + (r.width / 2))-(camWidth / 2);
			if (cOffset > ROTATION_THRESHOLD) {
				twist.getAngular().setZ(-ROTATE_SPEED);
			} else if (cOffset < -ROTATION_THRESHOLD) {
				twist.getAngular().setZ(ROTATE_SPEED);
			} else {
				twist.getAngular().setZ(0);
			}
			Person temp = getClosest();
			closest = (temp == null ? closest : temp);
			r = closest.getRect();
			main.getCmdVelPublisher().publish(twist);
		}
		twist.getAngular().setZ(0);
		twist.getLinear().setX(0);
		main.getCmdVelPublisher().publish(twist);
		if (r.width >= INTIMACY_THRESHOLD) {
			main.getSpeech().play("question-meeting", true);
			if (invite.invite()) {
				main.getSpeech().play("follow-me", true);
				main.getPersonFinder().stopFaceDetection();
				main.getMove().goToRoom(main.getMeetingRoom(), true);
				main.getSpeech().play("wait-here", true);
				main.getPersonFinder().stopFor(SLEEP_TIME);
			} else {
				main.getSpeech().play("negative-response", true);
				main.getPersonFinder().stopFor(SLEEP_TIME);
			}
		}
	}

	private float getLaserRange() {
		
		LaserScan laserData = main.getLaserData();
		float[] laserReadings = laserData.getRanges();
		
		float min_range = Float.MAX_VALUE;
		for (int i = laserReadings.length/3; i < 2*laserReadings.length/3; i++) {
			float reading = laserReadings[i];
			if (reading <= laserData.getRangeMin()) continue;
			if (reading == 0) reading = laserData.getRangeMax();
			if (reading < min_range) min_range = laserReadings[i];
		}
		return min_range;
	}

	@Override
	public void suppress() { 
		
	}

	@Override
	public String getName() {
		return "Human Detection";
	}

	@Override
	public void peopleFound(Person[] people) {
		this.people = people;
	}

}
