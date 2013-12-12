package ex3.behaviours;

public interface Behaviour {

	/**
	 * Returns a boolean value to indicate if this behaviour should become active. For example, if a touch sensor indicates the robot has bumped into an object, this method should return true. This method should return quickly, not perform a long calculation.
	 * @return true if this behaviour should become active
	 */
	boolean takeControl();

	/**
	 * The code in this method begins performing its task when the behaviour becomes active.
	 */
	void action();

	/**
	 * The code in the suppress() method should immediately terminate the code running in the action() method. It also should exit quickly.
	 */
	void suppress();

	/**
	 * Returns a human readable printout of the behaviour 
	 * @return a string of the behaviours name
	 */
	String getName();
}
