package osaka.senbatsu;

import java.awt.Color;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * KaitenMan - a robot by (your name here)
 */
public class KaitenMan extends AdvancedRobot {

	int dameged = 0;

	/**
	 * attack level
	 */
	static double FIRE_LEVEL = 1.5;

	static int escapeThreshold = 10;

	/**
	 * run: KaitenMan's default behavior
	 */
	public void run() {
		setBodyColor(Color.ORANGE);
		while (true) {
			setMaxVelocity(5);
			turnRight(10000);
			// Limit our speed to 5
			setMaxVelocity(5);
			// Start moving (and turning)
			ahead(10000);
			// Repeat.

		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (!(e.getName().equals("zumiOne") || e.getName().equals("zumiTwo") || e
				.getName().equals("zumiThree"))) {
			fire(FIRE_LEVEL);
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		if (escapeThreshold <= dameged) {
			ahead(300);
			dameged = 0;
		}
		dameged += 1;
	}

	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		turnRight(180 + e.getBearing());
		ahead(200);
	}
}
