package ex3.behaviours;

import ex3.Main;
import ex3.PersonFinder.Person;
import ex3.PersonFinder.PersonFinderListener;
import ex3.Room;
import geometry_msgs.Twist;

public class FindARoom implements Behaviour, PersonFinderListener {

	private Main main;
	boolean suppressed;
	private Person[] people;
	private final long TIMEOUT = 30000;
	private final double ROTATE_SPEED = Math.toRadians(30);

	public FindARoom(Main main) {
		this.main = main;
		suppressed = false;
		main.getPersonFinder().addListener(this);
		people = new Person[0];
	}
	
	@Override
	public boolean takeControl() {
		return main.getMeetingRoom() == null;
	}

	@Override
	public void action() {
		suppressed = false;
		if (!takeControl()) return;
		
		main.getPersonFinder().stopFaceDetection();
		System.out.println("going to first room");
		main.getMove().goToRoom(Room.ROOMS[0], true);
		while(!suppressed && takeControl()) {
			System.out.println("is it empty?");
			if (isRoomEmpty()) {
				System.out.println("yes, done");
				main.setMeetingRoom(getCurrentRoom());
			} else {
				System.out.println("no, going to next room");
				Room currentRoom = getCurrentRoom();
				int currentRoomIndex = -1;
				for (int i=0; i<Room.ROOMS.length; i++) {
					if (Room.ROOMS[i] == currentRoom) {
						currentRoomIndex = i;
					}
				}
				Room nextRoom = Room.ROOMS[(currentRoomIndex+1) % Room.ROOMS.length];
				main.getMove().goToRoom(nextRoom, true);
			}
			Thread.yield();
		}
		main.getPersonFinder().startFaceDetection();
		System.out.println(main.getMeetingRoom());
	}

	private Room getCurrentRoom() {
		return Room.roomAt(main.getAmclPose());
	}
	
	private boolean isRoomEmpty() {
		main.getSpeech().play("is-room-in-use", true);
		main.getPersonFinder().startFaceDetection();
		boolean ret = true;
		long end = System.currentTimeMillis() + TIMEOUT;

		Twist twist = main.getCmdVelPublisher().newMessage();
		twist.getAngular().setZ(ROTATE_SPEED);
		
//		main.getMP3().play("countdown");
		while(!suppressed && ret && System.currentTimeMillis() < end) {
			for (Person person : people) {
				if (getCurrentRoom().contains(person.estimatePose())) {
					ret = false;
					break;
				}
			}
			main.getCmdVelPublisher().publish(twist);
		}
		
		twist.getAngular().setZ(0);
		main.getCmdVelPublisher().publish(twist);
		main.getPersonFinder().stopFaceDetection();
		
		if (ret) {
			main.getSpeech().play("room-empty", true);
		} else {
			main.getSpeech().play("room-nonempty", true);
		}
		return ret;
	}

	@Override
	public void suppress() {
		System.out.println("suppressed");
		suppressed = true;
		main.getMove().cancelGoal();
	}

	@Override
	public String getName() {
		return "Find A Room";
	}

	@Override
	public void peopleFound(Person[] people) {
		this.people = people;
	}

}
