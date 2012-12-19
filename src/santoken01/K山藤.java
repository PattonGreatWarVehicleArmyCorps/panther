package santoken01;

import robocode.*;

import java.awt.Color;
import java.io.IOException;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

public class KéRì° extends TeamRobot {

	boolean turn = true;
	double moveAmount;
	private String target;

	public void run() {
		setColors(new Color(255, 000, 051), new Color(255, 000, 051),
				new Color(255, 000, 051));
		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight());
		turnLeft(getHeading() % 90);
		ahead(moveAmount);
		turnRight(90);
		while (true) {
			turnGunRight(180);
			scan();
			turnGunLeft(180);
			scan();
		}

	}

	public void onScannedRobot(ScannedRobotEvent e) {
		if (!isFriend(e))
			fire((600 / e.getDistance()) * 2);
		if (turn) {
			ahead(200);
			turn = false;
		} else {
			back(200);
			turn = true;
		}
	}

	private boolean isFriend(ScannedRobotEvent e) {
		return e.getName().contains("Yèhó¢") || e.getName().contains("KéRì°")
				|| e.getName().contains("TëÂíÀ");
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