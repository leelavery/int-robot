package ex3.behaviours;


/**
 * Arbitrator controls which Behaviour object will become active in a behaviour control system. Make sure to call start() after the Arbitrator is instantiated.<br>
 * This class has three major responsibilities: <br>
 * 1. Determine the highest priority behaviour that returns <b> true </b> to takeControl()<br>
 * 2. Suppress the active behaviour if its priority is less than highest priority. <br>
 * 3. When the action() method exits, call action() on the Behaviour of highest priority. <br>
 * The Arbitrator assumes that a Behaviour is no longer active when action() exits, <br>
 * therefore it will only call suppress() on the Behaviour whose action() method is running. <br>
 * It can make consecutive calls of action() on the same Behaviour. <br>
 * Requirements for a Behaviour: <br>
 * When suppress() is called, terminate action() immediately. <br>
 * When action() exits, the robot is in a safe state (e.g. motors stopped)
 * @see Behaviour
 * @author Roger Glassey
 */
public class Arbitrator {

	private final int NONE = -1;
	private Behaviour[] _behaviour;
	// highest priority behaviour that wants control ; set by start() used by monitor
	private int _highestPriority = NONE;
	private int _active = NONE; // active behaviour; set by monitor, used by start();
	private boolean _returnWhenInactive;
	/**
	 * Monitor is an inner class. It polls the behaviour array to find the behaviour of highest priority. If higher than the active behaviour, it calls active.suppress()
	 */
	private Monitor monitor;

	/**
	 * Allocates an Arbitrator object and initialises it with an array of Behaviour objects. The index of a behaviour in this array is its priority level, so the behaviour of the largest index has the highest the priority level. The behaviours in an Arbitrator can not be changed once the arbitrator is initialised.<BR>
	 * <B>NOTE:</B> Once the Arbitrator is initialised, the method start() must be called to begin the arbitration.
	 * @param behaviourList an array of Behaviour objects.
	 * @param returnWhenInactive if <B>true</B>, the <B>start()</B> method returns when no Behaviour is active.
	 */
	public Arbitrator(Behaviour[] behaviourList, boolean returnWhenInactive) {
		_behaviour = behaviourList;
		_returnWhenInactive = returnWhenInactive;
		monitor = new Monitor();
		monitor.setDaemon(true);
	}

	/**
	 * Same as Arbitrator(behaviourList, false) Arbitrator start() never exits
	 * @param behaviourList An array of Behaviour objects.
	 */
	public Arbitrator(Behaviour[] behaviourList) {
		this(behaviourList, false);
	}

	/**
	 * This method starts the arbitration of Behaviours and runs an endless loop. <BR>
	 * Note: Arbitrator does not run in a separate thread. The start() method will never return unless <br>
	 * 1. no action() method is running and <br>
	 * 2. no behaviour takeControl() returns <B> true </B> and <br>
	 * 3. the <i>returnWhenInacative </i> flag is true,
	 */
	public void start() {
		monitor.start();
		while (_highestPriority == NONE) {
			Thread.yield();// wait for some behaviour to take control
		}
		while (true) {
			synchronized (monitor) {
				if (_highestPriority != NONE) {
					_active = _highestPriority;

				} else if (_returnWhenInactive) {// no behaviour wants to run
					monitor.more = false;// 9 shut down monitor thread
					return;
				}
			}// monitor released before action is called
			if (_active != NONE) // _highestPrioirty could be NONE
			{
				_behaviour[_active].action();
				_active = NONE; // no active behaviour at the moment
			}
			Thread.yield();
		}
	}

	/**
	 * Finds the highest priority behaviour that returns <B>true </B> to takeControl(); If this priority is higher than the active behaviour, it calls active.suppress(). If there is no active behaviour, calls suppress() on the most recently active behaviour.
	 */
	private class Monitor extends Thread {

		boolean more = true;
		int maxPriority = _behaviour.length - 1;

		public void run() {
			while (more) {
				// FIND HIGHEST PRIORITY BEHAVIOR THAT WANTS CONTROL
				synchronized (this) {
					_highestPriority = NONE;
					for (int i = maxPriority; i >= 0; i--) {
						if (_behaviour[i].takeControl()) {
							_highestPriority = i;
							break;
						}
					}
					int active = _active;// local copy: avoid out of bounds error in 134
					if (active != NONE && _highestPriority > active) {
						_behaviour[active].suppress();
					}
				}// end synchronise block - main thread can run now
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
