package ex3.deprecated;

import java.util.Arrays;

import sensor_msgs.LaserScan;

import ex3.Main;
import ex3.behaviours.Behaviour;
import geometry_msgs.Twist;

public class ExploreLocalise implements Behaviour {

	private Main main;
	private static final double threshold = 0.3;
	private int min_i;

	public ExploreLocalise(Main main) {
		this.main = main;
	}

	@Override
	public boolean takeControl() {
		return checkCovariance(main.getAmclPose().getPose().getCovariance());
	}

	private boolean checkCovariance(double[] covariance) {
		// { x, y, z, x rot, y rot, z rot }
		double sum = 0;
		for (double cov : covariance)
			sum += cov;
		System.out.println(Arrays.toString(covariance) + " " + sum);
		return false;
	}

	/**
	 * Method takes the full size laser data array and returns a smaller array of with values of averaged blocks of a specified size
	 * 
	 * @param blockSize the size of blocks to use.
	 * @param ranges the full array of laser data to be averaged
	 * @return the array containing the average values across the block size given.
	 */
	private int getMaxDistanceSegment(int blockSize, float[] ranges) {
		int not_used = 0;
		int max_block = 0;
		min_i = 0;
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

			if (ranges[i] < ranges[min_i]) {
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
		return max_block;
	}

	@Override
	public void action() {

		float speed = 0.3f;

		// get block averages of 100
		int blockSize = 100;
		LaserScan laserData = main.getLaserData();
		float[] ranges = laserData.getRanges();
		int max_block = getMaxDistanceSegment(blockSize, ranges);

		// calculate angle to turn to midpoint of block with largest range
		// start at rightmost turn
		double angle = Math.toRadians(-90);
		// move to midpoint of rightmost block
		angle += blockSize / 2 * laserData.getAngleIncrement();
		// increment by the angle covered by each block multiplied by the block
		// number
		angle += max_block * blockSize * laserData.getAngleIncrement();

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

		Twist twist = main.getCmdVelPublisher().newMessage();
		// set the speed and angle on the twist and publish to robot.
		twist.getAngular().setZ(angle / 2);
		twist.getLinear().setX(speed);
		main.getCmdVelPublisher().publish(twist);
	}

	@Override
	public void suppress() {

	}

	@Override
	public String getName() {
		return "Localising";
	}

}
