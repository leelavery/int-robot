package ex3.deprecated;

import org.ros.node.topic.Publisher;

import ex3.Main;
import ex3.behaviours.Behaviour;
import geometry_msgs.Twist;

public class CollisionPrevention implements Behaviour {

	private Main main;
	private Publisher<Twist> pub;
	private double threshold;
	private double radiansToCorrect;

	public CollisionPrevention(Main main, Publisher<Twist> pub) {
		this.main = main;
		this.pub = pub;
		this.threshold = 0.4;
		this.radiansToCorrect = Math.toRadians(45);
	}

	@Override
	public boolean takeControl() {
		if (main.getLaserData() == null) {
			return false;
		}
		for (float laserPoint : main.getLaserData().getRanges()) {
			if (laserPoint > main.getLaserData().getRangeMin() && laserPoint <= threshold) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void action() {
		float[] ranges = main.getLaserData().getRanges();
		double angle = 0;
		int min_i = 0;

		// find index of the min i
		for (int i = 0; i < ranges.length; i++) {
			if (ranges[i] < ranges[min_i]) {
				min_i = i;
			}
		}

		if (min_i < 250) {
			angle = radiansToCorrect;
		} else {
			// ..and turn right if obstacle is to the left
			angle = -radiansToCorrect;
		}
		Twist twist = pub.newMessage();
		twist.getLinear().setX(0);
		twist.getAngular().setZ(angle);
		pub.publish(twist);
	}

	@Override
	public void suppress() {
		System.out.println("Suppress called in collision prevention.. this shouldn't happen");
		System.exit(1);
	}

	@Override
	public String getName() {
		return "Collision Prevention";
	}

}
