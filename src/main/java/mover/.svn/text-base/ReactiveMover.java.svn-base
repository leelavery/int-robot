// This basic node subscribes to incoming laser and sonar messages,
// decides what to do based on the incoming data, then publishes
// movement commands to the robot.

// Start with the package declaration.
package mover;

// Remember to Javadoc and comment your code so your team mates can understand it!
/**
 *
 * @author Mark Rowan
 */

// We will need to import the relevant ROS libraries.
import org.ros.node.*;
import org.ros.namespace.GraphName;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.message.MessageListener;
import geometry_msgs.Twist;
import sensor_msgs.LaserScan;
import std_msgs.String;

// To make your program into a Node which can run in the ROS system,
// it must extend the AbstractNodeMain class.
public class ReactiveMover extends AbstractNodeMain {

	// We need a Publisher -- this will only accept data of type Twist,
	// using the <Generic> type mechanism. (Twists are a type of built-in
	// ROS message which can contain movement commands).
	private Publisher<Twist> pub;
	// private Publisher<String> debug;

	// We also need a Subscriber to listen to messages of type LaserScan.
	private Subscriber<LaserScan> sub;

	// Extending AbstractNodeMain requires you to implement a couple of methods.
	// You should give your ROS Node a meaningful name here.
	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("mover/reactivemover");
	}

	// When the Node starts up, this method will be executed.
	@Override
	public void onStart(ConnectedNode node) {

		// Create a Publisher for Twist messages on topic "cmd_vel"
		pub = node.newPublisher("cmd_vel", Twist._TYPE);

		// debug = node.newPublisher("debug", String._TYPE);

		// Create a Subscriber to listed to LaserScan messages on topic
		// "base_scan"
		sub = node.newSubscriber("base_scan", LaserScan._TYPE);

		// Add a MessageListener for LaserScan message events
		sub.addMessageListener(new MessageListener<LaserScan>() {
			@Override
			public void onNewMessage(LaserScan message) {

				// We can now pass this LaserScan message to another method for
				// processing.
				doSomething(message);
			}
		});
	}

	public void doSomething(LaserScan message) {
		// Create a Twist message using the Publisher
		Twist twist = pub.newMessage();

		// get the ranges
		float[] ranges = message.getRanges();
		// search for the min, ignoring erroneous tiny values...
		float min = Float.MAX_VALUE;
		for (float m : ranges) {
			if (m >= 0.02 && m < min) {
				min = m;
			}
		}

		// set threshold to object closeness, random angle to turn if object is
		// detected, and speed to travel at in proportional to object closeness
		double threshold = 0.3;
		double angle = Math.toRadians((Math.random() * 180 + 90) / 2);
		double speed = min - threshold;
		// keep speed within a set upper and lower bound
		if (speed <= 0.1) {
			speed = 0.1;
		} else if (speed > 1) {
			speed = 1;
		}
		// if we are too close to an object.. turn by the chosen random angle
		if (min < threshold) {
			twist.getAngular().setZ(angle);
		} else {
			// else don't turn.. keep going straight by the specified speed.
			twist.getAngular().setZ(0);
			twist.getLinear().setX(speed);
		}
		// publish to the robot
		pub.publish(twist);
	}
}