package ex2;

import geometry_msgs.Pose;

import java.util.HashSet;

public class Cluster {

	private HashSet<Pose> contents;
	
	/**
	 * Creates a new, empty cluster
	 */
	public Cluster() {
		contents = new HashSet<Pose>();
	}
	
	/**
	 * Adds the specified pose to this cluster
	 * @param p The pose to add
	 */
	public void addPose(Pose p) {
		contents.add(p);
	}

	/**
	 * Returns the particle closest to the average position in this cluster 
	 * @return the particle closest to the average position in this cluster
	 */
	public Pose getAverage() {
		// Initialise averages to 0
		double avg_x = 0, avg_y = 0;
		double avg_w = 0, avg_z = 0;
		
		// Iterate over contents of cluster and sum up values from the poses
		for (Pose p : contents) {
			avg_x += p.getPosition().getX();
			avg_y += p.getPosition().getY();
			avg_w += p.getOrientation().getW();
			avg_z += p.getOrientation().getZ();
		}
		
		// Divide by the size of the summed contents
		avg_x /= contents.size(); avg_y /= contents.size();
		avg_w /= contents.size(); avg_z /= contents.size();

		// Initialise ret to null for now, and set smallest value to max double
		// (Ensures that ret will definitely not be null at end of loop)
		Pose ret = null;
		double smallest_distance = Double.MAX_VALUE;
		for (Pose p : contents) {
			// Calculate Euclidian distance of this point from the average
			// Uses the 4 values to get the euclidian distance 
			double distance = Math.sqrt(Math.pow(p.getPosition().getX() - avg_x, 2) +	Math.pow(p.getPosition().getY() - avg_y, 2) + Math.pow(p.getOrientation().getW() - avg_w, 2) + Math.pow(p.getOrientation().getZ() - avg_z, 2));
			// If this distance is smaller than our current smallest
			// then update ret and smallest value
			if (distance < smallest_distance) {
				ret = p;
				smallest_distance = distance;
			}
		}
		// Return the point that was nearest to the average
		return ret;
	}

	/**
	 * Returns true if pose p is in this cluster
	 * @param p The pose to test
	 * @return True if the pose is in this cluster, false otherwise
	 */
	public boolean contains(Pose p) {
		return contents.contains(p);
	}
	
	@Override
	public String toString() {
		String s = "";
		for(Pose p : contents) {
			s += "X: " + p.getPosition().getX() + " Y: " + p.getPosition().getY() + "\n";
		}
		s += "\n";
		return s;
	}
	
	/**
	 * Returns the size of the cluster
	 * @return The cluster size
	 */
	public int getSize() {
		return contents.size();
	}
	
}
