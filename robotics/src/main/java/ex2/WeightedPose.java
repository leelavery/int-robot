package ex2;

import geometry_msgs.Pose;

public class WeightedPose {

	private Pose pose;
	private double weight;
	
	/**
	 * Create a new weighted pose with given pose and weight
	 * @param pose The pose to use
	 * @param weight The weight associated with the pose 
	 */
	public WeightedPose(Pose pose, double weight) {
		this.pose = pose;
		this.weight = weight;
	}

	/**
	 * Returns the pose without weight
	 * @return The pose
	 */
	public Pose getPose() {
		return pose;
	}

	/**
	 * Sets the pose to be used with the weight
	 * @param pose The pose to use with the weight
	 */
	public void setPose(Pose pose) {
		this.pose = pose;
	}

	/**
	 * Gets the weight associated with the pose
	 * @return The weight of the pose
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Sets the weight of the pose
	 * @param weight The new weight of the pose
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
}
