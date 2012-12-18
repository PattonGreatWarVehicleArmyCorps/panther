package kanto;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

public class OniNear extends AdvancedRobot {
	int turnDirection = 1; // Clockwise or counterclockwise
	boolean hasTarget = false;
	int attackNum = 0;

	public static final double ATTACK_DISTANCE = 300;
	public static final int MAX_ATTACK = 15;

	/**
	 * run: Spin around looking for a target
	 */
	public void run() {
		// Set colors
		setBodyColor(Color.ORANGE);

		while (true) {
			setTurnRight(5 * turnDirection);
		}
	}

	/**
	 * onScannedRobot: We have a target. Go get it.
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (isFriend(e.getName())) {
			return;
		}

		if (e.getBearing() >= 0) {
			turnDirection = 1;
		} else {
			turnDirection = -1;
		}

		if (e.getDistance() <= ATTACK_DISTANCE) {
			hasTarget = true;
			attackNum = 0;
		}

		setTurnRight(e.getBearing());
		ahead(e.getDistance() + 5);
		scan(); // Might want to move ahead again!
	}

	private boolean isFriend(String name) {
		return name.startsWith("watanabedais01") || name.startsWith("nomura")
				|| name.startsWith("mshi");
	}

	/**
	 * onHitRobot: Turn to face robot, fire hard, and ram him again!
	 */
	public void onHitRobot(HitRobotEvent e) {
		if (isFriend(e.getName())) {
			return;
		}
		if (e.getBearing() >= 0) {
			turnDirection = 1;
		} else {
			turnDirection = -1;
		}
		turnRight(e.getBearing());

		// Determine a shot that won't kill the robot...
		// We want to ram him instead for bonus points
		fire(3);
		//ahead(10); // Ram him again!
	}

	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		turnRight(180 + e.getBearing());
		ahead(50);
		turnDirection *= -1;
	}
}