// This basic node just publishes repeated movement commands to the robot.

// Start with the package declaration.
package mover;

// Remember to Javadoc and comment your code so your team mates can understand it!
/**
 *
 * @author Leonard
 */

// We will need to import the relevant ROS libraries.
import org.ros.node.*;
import org.ros.namespace.GraphName;
import org.ros.node.topic.Publisher;
import geometry_msgs.Twist;

// To make your program into a Node which can run in the ROS system,
// it must extend the AbstractNodeMain class.
public class Mover extends AbstractNodeMain {

    // We need a Publisher -- this will only accept data of type Twist,
    // using the <Generic> type mechanism. (Twists are a type of built-in
    // ROS message which can contain movement commands).
    private Publisher<Twist> pub;

    // Extending AbstractNodeMain requires you to implement a couple of methods.
    // You should give your ROS Node a meaningful name here.
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("mover/mover");
    }

    // When the Node starts up, this method will be executed.
    @Override
    public void onStart(ConnectedNode node) {
        
        // Create a Publisher for Twist messages on topic "cmd_vel"
        pub = node.newPublisher("cmd_vel", Twist._TYPE);
        
        // Create a Twist message using the Publisher
        Twist twist = pub.newMessage();
            
        // Set the Twist message's X component to move forward slowly (0.5).
        // Setting a negative value would cause the robot to move backward.
        //twist.getLinear().setX(0.5);
        twist.getAngular().setZ(Math.toRadians(45));
        
        while (true) {
            // Publish the Twist message to cause the robot to move.
            pub.publish(twist);
            try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            // We also need to add a Thread.sleep() here to avoid flooding the network.
            // Without it, the CPU and network will become overloaded and the robot
            // will move erratically.
        }
    }
}