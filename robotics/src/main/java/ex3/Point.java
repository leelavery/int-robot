package ex3;

public class Point {

	public static final Point ROBOTLAB = new Point("robot-lab", 20.116, 3.647, 4.01425);
	public static final Point COMMONROOM = new Point("common-room", 12.179, 27.388, 0.87266);
	public static final Point HOTCOLD = new Point("hot-cold", 27.31, 12.085, 0.87266);
	public static final Point OTHER = new Point("other-place", 3.787, 19.542, 4.01425);
	public static final Point[] POINTS = new Point[] { ROBOTLAB, COMMONROOM, HOTCOLD, OTHER };

	private String name;
	private double x, y, theta;

	public Point(double x, double y) {
		this("", x, y, 0);
	}
	
	public Point(String name, double x, double y, double theta) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.theta = theta;
	}

	public double getTheta() {
		return theta;
	}

	public String getName() {
		return name;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

}
