package ex2;

import geometry_msgs.Pose;
import geometry_msgs.PoseArray;
import geometry_msgs.PoseWithCovariance;
import geometry_msgs.PoseWithCovarianceStamped;

import java.util.ArrayList;
import java.util.Random;

import nav_msgs.OccupancyGrid;
import sensor_msgs.LaserScan;

public class PFLocaliser extends AbstractLocaliser {

	private final double SENSOR_TRANSLATION_NOISE, SENSOR_ROTATION_NOISE;
	private final double INIT_TRANSLATION_NOISE, INIT_ROTATION_NOISE;
	private final int NUM_LASER_POINTS, MIN_CLUSTER_SIZE;
	private final int MAX_NUM_PARTICLES, MIN_NUM_PARTICLES;
	private final double EPSILON;
	private final Random RANDOM;

	public PFLocaliser() {
		super();
		MAX_NUM_PARTICLES = 700; // Maximum number of particles in cloud
		MIN_NUM_PARTICLES = 100; // Minimum number of particles in cloud
		
		INIT_TRANSLATION_NOISE = 1.0; // Noise added around inital position when set
		INIT_ROTATION_NOISE = Math.toRadians(30); // Noise added around initial orientation when set
		
		ODOM_ROTATION_NOISE = 0.1; // Odometry model rotation noise
		ODOM_DRIFT_NOISE = 0.1; // Odometry model y axis (side-to-side) noise
		ODOM_TRANSLATION_NOISE = Math.toRadians(3); // Odometry model x axis (forward) noise
		
		SENSOR_TRANSLATION_NOISE = 0.05; // Noise added to positions on particles after sensor model
		SENSOR_ROTATION_NOISE = Math.toRadians(3); // Noise added to orientation on particles after sensor model
		
		NUM_LASER_POINTS = 100; // Number of laser points to test against world
		
		MIN_CLUSTER_SIZE = 8; // Minimum size of cluster from DBScan algorithm
		EPSILON = 1.4; // Epsilon value (largest distance of point from cluster) for DBScan algorithm
		
		RANDOM = new Random(); // Random object used by methods in this class to add noise to particle cloud
	}

	@Override
	public PoseArray initialisePF(PoseWithCovarianceStamped initialpose) {
		// Create the pose array
		PoseArray pa = messageFactory.newFromType(PoseArray._TYPE);

		// Initialise empty array list for initial particle cloud.
		ArrayList<Pose> poses = new ArrayList<Pose>(MAX_NUM_PARTICLES);

		// Get X, Y, and heading from initial pose set in rviz
		double initX = initialpose.getPose().getPose().getPosition().getX();
		double initY = initialpose.getPose().getPose().getPosition().getY();
		double initHeading = PFLocaliser.getHeading(initialpose.getPose().getPose().getOrientation());

		// Generate the max number of particles initially
		for (int i = 0; i < MAX_NUM_PARTICLES; i++) {
			// Create a new pose
			Pose pose = messageFactory.newFromType(Pose._TYPE);

			// Get a random amount of X, Y and heading to alter the initial value, using the noise values
			double ranX = RANDOM.nextGaussian() / 2 * INIT_TRANSLATION_NOISE;
			double ranY = RANDOM.nextGaussian() / 2 * INIT_TRANSLATION_NOISE;
			double ranHeading = RANDOM.nextGaussian() / 2 * INIT_ROTATION_NOISE;

			// Set values by altering init values by the random gaussian values above
			pose.getPosition().setX(initX + ranX);
			pose.getPosition().setY(initY + ranY);
			pose.setOrientation(rotateQuaternion(createQuaternion(), initHeading + ranHeading));

			// Add pose to the array list of particles
			poses.add(pose);
		}

		// Set the PoseArray to use the generated ArrayList of particles
		pa.setPoses(poses);

		// Set the required headers
		pa.getHeader().setFrameId("/map");
		pa.getHeader().setStamp(node.getCurrentTime());

		// Return the PoseArray
		return pa;
	}

	@Override
	public PoseArray updateParticleCloud(LaserScan scan, OccupancyGrid map, PoseArray particlecloud) {
		// Create a new PoseArray to hold the updated particle cloud
		PoseArray pa = messageFactory.newFromType(PoseArray._TYPE);

		// Create a new ArrayList to hold poses with weights assigned, size equal to current num particles
		ArrayList<WeightedPose> weights = new ArrayList<WeightedPose>(particlecloud.getPoses().size());

		// Iterate over all particles in cloud and assign weight as given by SensorModel class
		// Keep track of sum of all weights to normalise later
		double sum = 0.0;
		for (Pose p : particlecloud.getPoses()) {
			double weight = SensorModel.getWeight(scan, map, p, NUM_LASER_POINTS);
			sum += weight;
			weights.add(new WeightedPose(p, weight));
		}

		// Normalise by dividing each weight by total sum of all weights
		for (WeightedPose weightedPose : weights) {
			weightedPose.setWeight(weightedPose.getWeight() / sum);
		}

		// Calculate number of particles using the average weight
		int numParticles = calcNumParticles(sum / weights.size());

		// Create a new ArrayList to hold the new set of poses after update
		// Size set by how sure we are of the position, calculated by the likelihood above
		ArrayList<Pose> poses = new ArrayList<Pose>(numParticles);

		// Pick new sample of particles of the size calculated above, based on normalised weights
		// Sum of all weights is now 1, so we can assign each pose a 'block' between 0-1
		for (int i = 0; i < numParticles; i++) {
			// Get a random number between 0-1
			double ran = RANDOM.nextDouble();
			// Loop over list of poses with weights
			for (WeightedPose weightedPose : weights) {
				// Remove the weight from the random value, removing the size of this poses 'block'
				ran -= weightedPose.getWeight();

				// If the random value is now less than or equal to 0, the chosen random number
				// corresponds this poses 'block' and we choose this pose to add to the new sample
				if (ran <= 0.0) {
					// Create a new pose object
					Pose pose = messageFactory.newFromType(Pose._TYPE);

					// Set X, Y, and heading by uses this poses values and
					// adding the sensor noise
					pose.getPosition().setX(weightedPose.getPose().getPosition().getX() + (RANDOM.nextGaussian() * SENSOR_TRANSLATION_NOISE));
					pose.getPosition().setY(weightedPose.getPose().getPosition().getY() + (RANDOM.nextGaussian() * SENSOR_TRANSLATION_NOISE));
					pose.setOrientation(rotateQuaternion(createQuaternion(), PFLocaliser.getHeading(weightedPose.getPose().getOrientation()) + (RANDOM.nextGaussian() * SENSOR_ROTATION_NOISE)));

					// Add to list of resampled poses
					poses.add(pose);

					// Break to choose next particle
					break;
				}
			}
		}

		// Set the poses in the particle cloud and return it
		pa.setPoses(poses);
		return pa;
	}

	@Override
	public Pose estimatePose(PoseArray particlecloud) {
		// Scan the particle cloud for clusters of particles
		Cluster[] clusters = new DBScan().getClusters(particlecloud.getPoses(), EPSILON, MIN_CLUSTER_SIZE);

		// Find the biggest cluster by first initialising the largest cluster to an empty one
		Cluster largest_cluster = new Cluster();
		for (Cluster c : clusters) {
			// If this cluster is bigger than our current biggest, update it
			if (c.getSize() > largest_cluster.getSize()) {
				largest_cluster = c;
			}
		}
		// Get the centre of the largest cluster and return it as the estimated pose
		return largest_cluster.getAverage();
	}

	/**
	 * Gets the number of particles to use adaptively based on the
	 * average weight of all the poses before normalisation
	 * @param avg_weight The average weight of the poses before normalisation
	 * @return The number of particles to use in the new sample
	 */
	private int calcNumParticles(double avg_weight) {

		/*
		 * SensorModel.getWeight() sets p=1.0, 
		 * then adds p += pz*pz*pz NUM_LASER_POINTS times, 
		 * where pz is a value between 0-1 for probability 
		 * of that laser point so px*pz*pz is between 0-1, 
		 * so p is between 1.0-(NUM_LASER_POINTS+1)
		 */

		// Max weight is equal to NUM_LASER_POINTS + 1.0
		// so this gets a value between 0-1 for likelihood
		double likelihood = avg_weight / (NUM_LASER_POINTS + 1.0);

		// Flips the likelihood such that high likelihood returns low multiplier, and vice versa.
		double multiplier = 1 - likelihood;
		
		// Multiply by MAX_NUM_PARTICLES to get the number to use, round to get an int value
		// Gets a num particles between 0-MAX_NUM_PARTICLES.
		int numParticles = (int) Math.round(multiplier * MAX_NUM_PARTICLES);
		
		// Ensure num particles is always greater than the minimum
		if (numParticles < MIN_NUM_PARTICLES) numParticles = MIN_NUM_PARTICLES;
		
		System.out.println("Number of Particles: " + numParticles);
		return numParticles;
	}
	
	
	/*
	 * ONLY DEBUGGING METHODS BELOW
	 */

	/**
	 * Prints the particle cloud
	 */
	public void printParticleCloud() {
		printPoseArray(getParticleCloud());
	}

	/**
	 * Prints the given PoseArray
	 * @param poseArray the poseArray to print
	 */
	public static void printPoseArray(PoseArray poseArray) {
		System.out.println("Particle Cloud:");
		System.out.println("====================================================");
		for (Pose p : poseArray.getPoses()) {
			System.out.println(poseToString(p));
		}
		System.out.println("====================================================");
	}

	/**
	 * Returns a string representation of the given pose
	 * @param p The pose to be converted to string
	 * @return The string representation of the given pose
	 */
	public static String poseToString(PoseWithCovarianceStamped p) {
		return poseToString(p.getPose());
	}

	/**
	 * Returns a string representation of the given pose
	 * @param p The pose to be converted to string
	 * @return The string representation of the given pose
	 */
	public static String poseToString(PoseWithCovariance p) {
		return poseToString(p.getPose());
	}

	/**
	 * Returns a string representation of the given pose
	 * @param p The pose to be converted to string
	 * @return The string representation of the given pose
	 */
	public static String poseToString(Pose p) {
		return "(" + String.valueOf(p.getPosition().getX()) + ", " + String.valueOf(p.getPosition().getY()) + ") " + String.valueOf(getHeading(p.getOrientation()));
	}

}
