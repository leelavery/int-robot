package ex3.behaviours;

import ex3.Main;
import ex3.Move;
import ex3.Point;

public class Explore implements Behaviour {

	private Move move;
	private int currentPoint;
	
	public Explore(Main main) {
		this.move = main.getMove();
		currentPoint = 0;
	}
	
	@Override
	public boolean takeControl() {
		return true;
	}
	
	@Override
	public void action() {
		currentPoint += (move.goToPoint(Point.POINTS[currentPoint], true) ? 1 : 0);
		currentPoint = currentPoint % Point.POINTS.length;
	}
	
	@Override
	public void suppress() {
		move.cancelGoal();
	}

	@Override
	public String getName() {
		return "Explore";
	}
	
}
