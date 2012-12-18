package santoken01;
import robocode.*;
import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

public class AdvancedWall extends TeamRobot{

	boolean turn = true;
	double moveAmount;
	private String target;

	public void run() {
		setColors(new Color(255,000,051),new Color(255,000,051),new Color(255,000,051));
		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		turnLeft(getHeading()%90);
		ahead(moveAmount);
		turnRight(90);
		while(true){
			turnGunRight(180);
			scan();
			turnGunLeft(180);
			scan();
		}

	}

	public void onHitRobot(HitRobotEvent e) {
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		fire(2);
		if(turn){
			ahead(200);
			turn = false;
		}else{
			back(200);
			turn = true;
		}
	}
	
	public void onMessageReceived(MessageEvent e) {
		Object msg = e.getMessage();
		if (!(msg instanceof String))
			return;
		String message = (String) msg;
		if (message == null || "".equals(message))
			return;
		if (message.startsWith("target:"))
			target = message.split(":")[1];
	}

}																
										