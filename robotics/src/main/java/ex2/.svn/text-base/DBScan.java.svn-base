package ex2;

import geometry_msgs.Pose;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DBScan {

	private HashSet<Pose> visited;
	private ArrayList<Cluster> clusters;

	/**
	 * Create a new DBScan object which will keep track of visited poses
	 */
	public DBScan() {
		visited = new HashSet<Pose>();
		clusters = new ArrayList<Cluster>();
	}

	/**
	 * Returns an array of clusters found in the list of particles given, using DBScan with given values for epsilon and min points
	 * @param particles The list of particles to find clusters in
	 * @param epsilon The epsilon value (max distance a particle can be away from the cluster)
	 * @param minPoints The minimum number of points before a new cluster can be formed
	 * @return The array of clusters found
	 */
	public Cluster[] getClusters(List<Pose> particles, double epsilon, int minPoints) {
		// Clear the list of clusters and visited points if any exist from a previous scan
		clusters.clear();
		visited.clear();

		// Look at all particles in the list
		for (Pose p : particles) {
			// If it has already been visited skip over it
			if (visited.contains(p)) {
				continue;
			} else {
				// Mark the point as visited
				visited.add(p);
				// Get all the neighbours within the epsilon distance of this point
				ArrayList<Pose> neighbours = regionQuery(p, particles, epsilon);
				// If the list of neighbours is big enough, start a new cluster and
				// add it to the list of clusters, else the point is noise so do nothing
				if (neighbours.size() >= minPoints) {
					clusters.add(expandCluster(p, neighbours, minPoints, epsilon, particles));
				}
			}
		}
		// Return the list of clusters as an array
		return clusters.toArray(new Cluster[clusters.size()]);
	}

	/**
	 * Expands a cluster starting from the given pose and its neighbours, by connecting all neighbours to there own neighbours until all points are out of the epsilon range of this cluster
	 * @param pose The pose to start the cluster from
	 * @param neighbours The neighbours of the given pose
	 * @param minPoints The min points
	 * @param epsilon
	 * @param particles
	 * @return
	 */
	public Cluster expandCluster(Pose pose, ArrayList<Pose> neighbours, int minPoints, double epsilon, List<Pose> particles) {
		// Create a new, empty cluster.
		Cluster c = new Cluster();
		// Add the starting pose to the cluster
		c.addPose(pose);

		// Iterate over the neighbours of the poses in the cluster so far
		// (starts with the neighbours of the first point and add new neighbours as points are visited)
		for (int i = 0; i < neighbours.size(); i++) {
			// Get the next pose in the list of neighbours
			Pose p = neighbours.get(i);

			// If it has been visited already, skip over it
			if (visited.contains(p)) {
				continue;
			} else {
				// Mark the point as visited
				visited.add(p);

				// Get the neighbours of this point
				ArrayList<Pose> n = regionQuery(p, particles, epsilon);

				// If the number of neighbours is enough to not be classed as noise,
				// Avoids linking two clusters as one by a thin line of particles.
				if (n.size() >= minPoints) {
					neighbours.addAll(n);
				}
			}
			// If this pose is not already in a cluster, add it to the current one
			if (!inAnyClusters(p)) {
				c.addPose(p);
			}
		}
		// Return the cluster
		return c;
	}

	/**
	 * Tests if the pose p is in any of the clusters found so far
	 * @param p The pose p to test
	 * @return True if p is in a cluster, false otherwise
	 */
	public boolean inAnyClusters(Pose p) {
		for (Cluster c : clusters) {
			if (c.contains(p)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a list of all the neighbours surrounding the pose given within the epsilon value
	 * @param pose The pose to get neighbours for
	 * @param particles The list of particles to select neighbours from
	 * @param epsilon The epsilon value to use for max neighbour distance
	 * @return The list of particles within epsilon value of the pose given
	 */
	private ArrayList<Pose> regionQuery(Pose pose, List<Pose> particles, double epsilon) {
		// Start a new list for the neighbours
		ArrayList<Pose> neighbours = new ArrayList<Pose>();

		// Iterate over all poses in the list of particles
		for (Pose p : particles) {
			// If the distance is not larger than the epsilon value, add to the list
			if (getDistance(p, pose) <= epsilon) {
				neighbours.add(p);
			}
		}
		// Return all the found particles
		return neighbours;
	}

	/**
	 * Gets the euclidian distance between the two poses given
	 * @param a The first pose to test
	 * @param b The second pose to test
	 * @return the distance between the two poses given
	 */
	public static double getDistance(Pose a, Pose b) {
		return Math.sqrt(Math.pow(a.getPosition().getX() - b.getPosition().getX(), 2) + Math.pow(a.getPosition().getY() - b.getPosition().getY(), 2) + Math.pow(a.getOrientation().getW() - b.getOrientation().getW(), 2) + Math.pow(a.getOrientation().getZ() - b.getOrientation().getZ(), 2));
	}

/*	*//**
	 * Used for testing the DBScan algorithm with various values for epsilon and minPoints to find optimal values
	 * @param args command line arguments, not used.
	 * @throws IOException 
	 *//*
	public static void main(String[] args) throws IOException {
		MessageFactory messageFactory = NodeConfiguration.newPrivate().getTopicMessageFactory();
		
		FileWriter writer = new FileWriter("data.csv", true);
		
		Random r = new Random();

		int num_points = 600;
		int num_tests = 100;
		double cluster_width = 2;

		double[] epsilon_array = new double[] { 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0 };
		int[] min_points_array = new int[] { 2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };

		long first_start = System.currentTimeMillis();
		Pose[][] tests = new Pose[num_tests][num_points];
		for (int i=0; i<num_tests; i++) {
			for (int j = 0; j < num_points; j++) {
				Pose p = messageFactory.newFromType(Pose._TYPE);
				p.getPosition().setX(r.nextGaussian() * cluster_width);
				p.getPosition().setY(r.nextGaussian() * cluster_width);
				tests[i][j] = p;
			}
		}
		
		ArrayList<String> results = new ArrayList<String>();
		String header = "Epsilon,MinPoints,Clusters,Noise (%),Time Taken (ms)\n";
		writer.append(header);
		writer.flush();
		results.add(header);
		System.out.print(header);
		
		for (double epsilon : epsilon_array) {
			for (int min_points : min_points_array) {

				double sumNoise = 0;
				double sumClusters = 0;
				long sumTime = 0;
				
				for (int i = 0; i < num_tests; i++) {
					long start = System.currentTimeMillis();
					Cluster[] clusters = new DBScan().getClusters(Arrays.asList(tests[i]), epsilon, min_points);
					long end = System.currentTimeMillis();
					sumTime += (end - start);
					int noise = num_points;

					for (int j = 0; j < clusters.length; j++) {
						noise -= clusters[j].getSize();
					}
					sumNoise += noise;
					sumClusters += clusters.length;
				}

				double avg_clusters = (sumClusters / num_tests);
				double noise_percent = ((sumNoise / num_tests) / num_points)*100;
				double avg_time = (sumTime / num_tests);
				
				String result = epsilon + "," + min_points + "," + avg_clusters + "," + noise_percent + "," + avg_time + "\n";
				writer.append(result);
				writer.flush();
				results.add(result);
				System.out.print(result);
			}
		}
		FileWriter writer2 = new FileWriter("data2.csv", true);
		for (String result : results) {
			writer2.append(result);
			writer2.flush();
		}
		writer.close();
		writer2.close();
		
		long end_time = System.currentTimeMillis();
		System.out.println("Completed Everything in: " + ((end_time - first_start)/(1000*60)) + "mins");
		

	}
*/
}