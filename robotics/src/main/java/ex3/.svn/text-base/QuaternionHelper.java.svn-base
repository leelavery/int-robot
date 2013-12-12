package ex3;

import org.ros.message.MessageFactory;

import geometry_msgs.Quaternion;

public class QuaternionHelper {

	MessageFactory mf;
	
	public QuaternionHelper(MessageFactory mf) {
		this.mf = mf;
	}
	
	/**
     * Converts a basic rotation about the z-axis (in radians) into the Quaternion
     * notation required by ROS transform and pose messages.
     * @param yaw rotate by this amount in radians
     * @param q Quaternion to be rotated
     * @return quaternion rotation about the z-axis (fine for robots which only
     * rotate using two-wheeled differential drive, for example)
     */
    public Quaternion rotateQuaternion(Quaternion q_orig, double yaw) {
        // Create a temporary Quaternion to represent the change in heading
        Quaternion q_headingChange = createQuaternion();

        double p = 0;
        double y = yaw / 2.0;
        double r = 0;
     
        double sinp = Math.sin(p);
        double siny = Math.sin(y);
        double sinr = Math.sin(r);
        double cosp = Math.cos(p);
        double cosy = Math.cos(y);
        double cosr = Math.cos(r);
     
        q_headingChange.setX(sinr * cosp * cosy - cosr * sinp * siny);
        q_headingChange.setY(cosr * sinp * cosy + sinr * cosp * siny);
        q_headingChange.setZ(cosr * cosp * siny - sinr * sinp * cosy);
        q_headingChange.setW(cosr * cosp * cosy + sinr * sinp * siny);

        // Multiply new (heading-only) quaternion by the existing (pitch and bank) quaternion
        // Order is important! Original orientation is the second argument;
        // rotation which will be applied to the quaternion is the first argument. 
        return multiply_quaternions(q_headingChange, q_orig);
    }


    /**
     * Multiplies two quaternions to give the rotation of qb by qa.
     * @param qa Quaternion rotation amount which will be applied to qb.
     * @param qb Quaternion to which rotation of qa will be applied.
     * @return Quaternion qb rotated by the amount represented by qa.
     */
    private Quaternion multiply_quaternions( Quaternion qa, Quaternion qb ) {
        Quaternion combined = createQuaternion();
        
        combined.setW(qa.getW()*qb.getW() - qa.getX()*qb.getX() - qa.getY()*qb.getY() - qa.getZ()*qb.getZ());
        combined.setX(qa.getX()*qb.getW() + qa.getW()*qb.getX() + qa.getY()*qb.getZ() - qa.getZ()*qb.getY());
        combined.setY(qa.getW()*qb.getY() - qa.getX()*qb.getZ() + qa.getY()*qb.getW() + qa.getZ()*qb.getX());
        combined.setZ(qa.getW()*qb.getZ() + qa.getX()*qb.getY() - qa.getY()*qb.getX() + qa.getZ()*qb.getW());
        return combined;

    }


    /**
     * 
     * @return A blank identity quaternion (x,y,z = 0, w = 1) representing zero rotation.
     */
    public Quaternion createQuaternion() {
        Quaternion q = mf.newFromType(Quaternion._TYPE);
        
        // Set up 'identity' (blank) quaternion
        q.setX(0);
        q.setY(0);
        q.setZ(0);
        q.setW(1);
        return q;
    }

    /**
     * 
     * @param q Quaternion describing a particular orientation about the z-axis
     * @return Equivalent orientation about the z-axis in radians
     */
    public static double getHeading(Quaternion q) {
        double w = q.getW();
        double x = q.getX();
        double y = q.getY();
        double z = q.getZ();
        
//        double pitch = Math.atan2(2*(y*z + w*x), w*w - x*x - y*y + z*z);
//        double roll = Math.asin(-2*(x*z - w*y));
        double yaw = Math.atan2(2*(x*y + w*z), w*w + x*x - y*y - z*z);
        return yaw;
    }
	
}
