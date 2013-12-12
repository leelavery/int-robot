// This basic node subscribes to incoming laser and sonar messages,
// decides what to do based on the incoming data, then publishes
// movement commands to the robot.

// Start with the package declaration.
package ex1;

// Remember to Javadoc and comment your code so your team mates can understand it!
/**
 *
 * @author Mark Rowan
 */

// We will need to import the relevant ROS libraries.
import geometry_msgs.Twist;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import sensor_msgs.LaserScan;

// To make your program into a Node which can run in the ROS system,
// it must extend the AbstractNodeMain class.
public class Ex1 extends AbstractNodeMain {

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
		return GraphName.of("ex1/ex1a");
	}

	// When the Node starts up, this method will be executed.
	@Override
	public void onStart(ConnectedNode node) {

		// Create a Publisher for Twist messages on topic "cmd_vel"
		pub = node.newPublisher("cmd_vel", Twist._TYPE);

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

	// block index with largest average range
	private int max_block, min_i;

	/**
	 * Method takes the full size laser data array and returns a smaller array
	 * of with values of averaged blocks of a specified size
	 * 
	 * @param blockSize the size of blocks to use.
	 * @param ranges the full array of laser data to be averaged
	 * @return the array containing the average values across the block size given.
	 */
	private float[] getAverageBlocks(int blockSize, float[] ranges) {
		int not_used = 0;
		float[] ret = new float[ranges.length / blockSize];
		for (int i = 0; i < 500; i++) {
			int val = i / blockSize;
			float range = ranges[i];
			if (range == 0.0 || range == Float.POSITIVE_INFINITY)
				range = 5.6f;
			if (range <= 0.02 || range == Float.NEGATIVE_INFINITY) {
				not_used++;
				continue;
			}
			ret[val] += range;

			if (range < ranges[min_i]) {
				min_i = i;
			}

			if ((i + 1) % blockSize == 0) {
				ret[val] /= (blockSize - not_used);
				not_used = 0;
				if (ret[val] > ret[max_block]) {
					max_block = val;
				}
			}
		}
		return ret;
	}

	public void doSomething(LaserScan message) {
		// Create a Twist message using the Publisher
		Twist twist = pub.newMessage();

		// get block averages of 100
		int blockSize = 100;
		float[] ranges = message.getRanges();
		float[] over_avg = getAverageBlocks(blockSize, ranges);

		// calculate angle to turn to midpoint of block with largest range
		// start at rightmost turn
		double angle = Math.toRadians(-90);
		// move to midpoint of rightmost block
		angle += blockSize / 2 * message.getAngleIncrement();
		// increment by the angle covered by each block multiplied by the block
		// number
		angle += max_block * blockSize * message.getAngleIncrement();

		double threshold = 0.3; // closest distance to obstacle
		double speed = ranges[min_i] - threshold; // proportional controller
		// keep speed within a set upper and lower bound
		if (speed <= 0.1) {
			speed = 0.1;
		} else if (speed > 1) {
			speed = 1;
		}
		
		// if the minimum value of the raw ranges is less than 40cm..
		if (ranges[min_i] < threshold) {
			// ..stop and..
			speed = 0;
			int degreesToCorrect = 45;
			// ..turn left if the obstacle is to the right..
			if (min_i < 250) {
				angle = Math.toRadians(degreesToCorrect);
			} else {
				// ..and turn right if obstacle is to the left
				angle = Math.toRadians(-degreesToCorrect);
			}
		}

		// print out the averaged blocks in reverse order (leftmost block
		// first.. human readable)!
		for (int i = over_avg.length - 1; i >= 0; i--) {
			System.out.print((over_avg[i]) + " ");
		}
		System.out.println();

		// set the speed and angle on the twist and publish to robot.
		twist.getAngular().setZ(angle / 2);
		twist.getLinear().setX(speed);
		pub.publish(twist);
	}
}