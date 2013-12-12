package ex3;

import geometry_msgs.Pose;
import geometry_msgs.PoseStamped;
import geometry_msgs.Quaternion;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import actionlib_msgs.GoalID;
import actionlib_msgs.GoalStatusArray;

public class Move {

	private Publisher<PoseStamped> goal;
	private Publisher<GoalID> cancel;
	private ConnectedNode node;
	private QuaternionHelper qh;
	private int seqNo;
	private Speech speech;

	private int failCount = 0;

	private static final int FAIL_THRESHOLD = 2;

	protected int status = -1;
	protected static final int PENDING = 0; // The goal has yet to be processed by the action server
	protected static final int ACTIVE = 1; // The goal is currently being processed by the action server
	protected static final int PREEMPTED = 2; // The goal received a cancel request after it started executing and has since completed its execution (Terminal State)
	protected static final int SUCCEEDED = 3; // The goal was achieved successfully by the action server (Terminal State)
	protected static final int ABORTED = 4; // The goal was aborted during execution by the action server due to some failure (Terminal State)
	protected static final int REJECTED = 5; // The goal was rejected by the action server without being processed, because the goal was unattainable or invalid (Terminal State)
	protected static final int PREEMPTING = 6; // The goal received a cancel request after it started executing and has not yet completed execution
	protected static final int RECALLED = 8; // The goal received a cancel request before it started executing and was successfully cancelled (Terminal State)

	public Move(Main main) {
		seqNo = 0;
		node = main.getNode();
		speech = main.getSpeech();

		goal = node.newPublisher("move_base_simple/goal", PoseStamped._TYPE);
		cancel = node.newPublisher("move_base/cancel", GoalID._TYPE);
		Subscriber<GoalStatusArray> statusSub = node.newSubscriber("move_base/status", GoalStatusArray._TYPE);
		statusSub.addMessageListener(new MessageListener<GoalStatusArray>() {
			public void onNewMessage(GoalStatusArray gsa) {
				if (gsa.getStatusList().size() > 0) {
					status = gsa.getStatusList().get(gsa.getStatusList().size() - 1).getStatus();
				}
			}
		});
		qh = main.getQuaternionHelper();
		cancelGoal();
	}

	public boolean goToRoom(Room room, boolean blocking) {
		return goToPoint(room.getCentroid(), blocking);
	}

	public boolean goToPoint(Point p, boolean blocking) {
		speech.play("going-" + p.getName());
		return goTo(p.getX(), p.getY(), p.getTheta(), blocking);
	}

	public boolean goToPose(Pose p, boolean blocking) {
		return goTo(p.getPosition().getX(), p.getPosition().getY(), QuaternionHelper.getHeading(p.getOrientation()), blocking);
	}

	public boolean goTo(double x, double y, double theta, boolean blocking) {
		cancelGoal();
		System.out.println("Going to: (" + x + ", " + y + ", " + theta + ")");
		sendGoal(x, y, theta);
		while (status != ACTIVE) {
			Thread.yield();
		}
		// long time1 = System.currentTimeMillis() + 3000;
		// while(System.currentTimeMillis() > time1) {
		// System.out.println(status);
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		while (blocking && status == ACTIVE) {
			Thread.yield();
		}
		if (status == ABORTED) {
			failCount++;
			if (failCount >= FAIL_THRESHOLD) {
				return false;
			} else {
				return goTo(x, y, theta, blocking);
			}
		} else if (status == PREEMPTED|| status == PREEMPTING ) {
			System.out.println("Cancelled moving to: (" + x + ", " + y + ", " + theta + ")");
			return false;
		}
		System.out.println("Arrived at: (" + x + ", " + y + ", " + theta + ")");
		return true;

	}

	private void sendGoal(double x, double y, double theta) {
		PoseStamped poseS = goal.newMessage();
		poseS.getHeader().setSeq(++seqNo);
		poseS.getHeader().setFrameId("/map");

		while (true) {
			try {
				poseS.getHeader().setStamp(node.getCurrentTime());
				break;
			} catch (NullPointerException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}
		Pose pose = poseS.getPose();
		pose.getPosition().setX(x);
		pose.getPosition().setY(y);

		Quaternion q = qh.createQuaternion();
		q = qh.rotateQuaternion(q, Math.toRadians(theta));
		pose.setOrientation(q);

		System.out.println("Sent new goal pose");
		goal.publish(poseS);
	}

	public void cancelGoal() {
		// System.out.println(status);
		GoalID gid = cancel.newMessage();

		while (true) {
			try {
				gid.setStamp(node.getCurrentTime());
				break;
			} catch (NullPointerException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}
		System.out.println("Cancelling goal pose");
		cancel.publish(gid);

		while (status == PREEMPTING || status == ACTIVE) {
			// System.out.println(status);
			Thread.yield();
		}
		System.out.println("Cancelled goal pose");
	}

	public GraphName getDefaultNodeName() {
		return GraphName.of("move");
	}
	
}
