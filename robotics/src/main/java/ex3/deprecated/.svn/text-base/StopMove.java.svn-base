package ex3.deprecated;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.xml.soap.MessageFactory;

import geometry_msgs.Point;
import geometry_msgs.Point32;
import geometry_msgs.Polygon;
import geometry_msgs.PolygonStamped;
import geometry_msgs.Pose;
import geometry_msgs.PoseStamped;

import move_base_msgs.MoveBaseActionGoal;
import move_base_msgs.MoveBaseGoal;

import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.NodeConfiguration;
import org.ros.node.topic.Publisher;

import actionlib_msgs.GoalID;

public class StopMove extends AbstractNodeMain {

	private Publisher<GoalID> pb;
	
	public void onStart(ConnectedNode node) {
		pb = node.newPublisher("move_base/cancel", GoalID._TYPE);
		GoalID goal = pb.newMessage();
		while (true) {
			try {
				goal.setStamp(node.getCurrentTime());
				break; // no exception, so let's stop waiting
			} catch (NullPointerException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}
		goal.setId("cockmunch");
		pb.publish(goal);
		System.out.println("apparently done");
	}

	public GraphName getDefaultNodeName() {
		return GraphName.of("penis2");
	}

}
