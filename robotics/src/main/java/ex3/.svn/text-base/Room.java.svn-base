package ex3;

import geometry_msgs.Pose;
import geometry_msgs.PoseStamped;
import geometry_msgs.PoseWithCovariance;
import geometry_msgs.PoseWithCovarianceStamped;

import java.awt.geom.Path2D;

public class Room {

	private String name;
	private Path2D polygon;
	private Point[] coordinates;
	
	public static final Room ROOM1 = new Room("room-one", new Point[] { new Point(14.2492, 13.2904), new Point(16.1735, 15.2341), new Point(20.669, 10.345), new Point(18.348, 8.363) });
	public static final Room ROOM2 = new Room("room-two", new Point[] { new Point(9.7898, 17.1142), new Point( 12.0623, 19.1449), new Point(16.027, 15.229), new Point(13.851, 12.908) });
	public static final Room[] ROOMS = new Room[] { ROOM1, ROOM2 };
	
	public Room(String name, Point[] coordinates) {
		this.name = name;
		polygon = new Path2D.Double();
		this.coordinates = coordinates;
		polygon.moveTo(coordinates[coordinates.length-1].getX(), coordinates[coordinates.length-1].getY());
		for(Point p : coordinates) {
			polygon.lineTo(p.getX(), p.getY());
		}
	}
	
	public boolean contains(Pose p) {
		return polygon.contains(p.getPosition().getX(), p.getPosition().getY());
	}
	
	public Path2D getPolygon() {
		return polygon;
	}
	
	public String getName() {
		return name;
	}
	
	public Point getCentroid() {
	    double centroidX = 0;
	    double centroidY = 0;

	    for (Point p : coordinates) {
	        centroidX += p.getX();
	        centroidY += p.getY();
	    }
	    int totalPoints = coordinates.length;

	    return new Point(name, centroidX / totalPoints, centroidY / totalPoints, 0);
	}

	public static Room roomAt(PoseWithCovarianceStamped pose) {
		return roomAt(pose.getPose());
	}
	
	private static Room roomAt(PoseWithCovariance pose) {
		return roomAt(pose.getPose());
	}
	
	public static Room roomAt(PoseStamped pose) {
		return roomAt(pose.getPose());
	}	
	
	public static Room roomAt(Pose p) {
		for (Room room : ROOMS) {
			if (room.contains(p)) {
				return room;
			}
		}
		return null;
	}
	
}
