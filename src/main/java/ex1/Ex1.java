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
import java.util.ArrayList;
import java.util.HashMap;

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
public class Ex1 extends AbstractNodeMain {

    // We need a Publisher -- this will only accept data of type Twist,
    // using the <Generic> type mechanism. (Twists are a type of built-in
    // ROS message which can contain movement commands).
    private Publisher<Twist> pub;
    //private Publisher<String> debug;
    
    // We also need a Subscriber to listen to messages of type LaserScan.
    private Subscriber<LaserScan> sub;

    // Extending AbstractNodeMain requires you to implement a couple of methods.
    // You should give your ROS Node a meaningful name here.
    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ex1/ex1");
    }

    // When the Node starts up, this method will be executed.
    @Override
    public void onStart(ConnectedNode node) {
        
        // Create a Publisher for Twist messages on topic "cmd_vel"
        pub = node.newPublisher("cmd_vel", Twist._TYPE);
        
        //debug = node.newPublisher("debug", String._TYPE);
        
        // Create a Subscriber to listed to LaserScan messages on topic "base_scan"
        sub = node.newSubscriber("base_scan", LaserScan._TYPE);
        
        // Add a MessageListener for LaserScan message events
        sub.addMessageListener(new MessageListener<LaserScan>() {
            @Override
            public void onNewMessage(LaserScan message) {
            
                // We can now pass this LaserScan message to another method for processing.
                doSomething(message);
            }
        });
    }
    
    public void doSomething(LaserScan message) {
        // Create a Twist message using the Publisher
        Twist twist = pub.newMessage();
      
        float[] ranges = message.getRanges();
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        int max_i = 0;
        int sweep = 10;
        int z_start = -1, z_end = 0;
        ArrayList<Integer[]> map = new ArrayList<Integer[]>();
        for(int i = sweep; i < ranges.length - sweep; i++) {
        	if (ranges[i] >= 0.02 && ranges[i] < min && (i > 180 && i < 320)) {
        		min = ranges[i];
        	}
        	float sum = 0;
        	for (int j = i-sweep; j < i+sweep; j++) {
        		sum += (ranges[j] > 0 ? ranges[j] : 10);
        	}
        	float avg = sum / ((2 * sweep) + 1);
        	if(avg > max) {
        		max = avg;
        		max_i = i;
        	}
        	if(ranges[i] == 0 && z_start == -1) {
        		z_start = i;
        	}
        	if(z_start != -1 && ranges[i] != 0) {
        		z_end = i - 1;
        		map.add(new Integer[] { (z_start + z_end) / 2, z_end - z_start });
        		z_start = -1;
        	}
        }
        int biggest = -1;
        int biggest_angle = -1;
        for(Integer[] a : map) {
        	if(a[1] > biggest) {
        		biggest_angle = a[0];
        		biggest = a[1];
        	}
        }
        
        double angle = (biggest_angle - 250) * 0.36; 
        System.out.println(min + " " + max + " " + biggest_angle + " " + angle);
        for(float m:ranges) {
        	System.out.print(m + " ");
        }
        System.out.println();
        
        /*for (int i = max_i; i < 500 && i >= 0; i += (angle == Math.abs(angle) ? -1 : 1)) {
        	if (ranges[i] < 0.3 && ranges[i] >= 0.02) {
        		angle = 0;
        	}
        }*/
        
        if (min < 0.7) {
        	twist.getLinear().setX(0);
        	//twist.getAngular().setZ(Math.toRadians(((Math.random()*180)+90)/2));
        	twist.getAngular().setZ(Math.toRadians(0));
        } else {
        	twist.getAngular().setZ(Math.toRadians(angle/2));
        	twist.getLinear().setX(0.3);
        }
    	pub.publish(twist);
       
    }
}