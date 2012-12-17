package osaka.senbatsu;

import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * KadoMan - a robot by (your name here)
 */
public class KadoManV1 extends Robot {

	/**
	 * for be along wall 
	 */
	boolean isWalled = false;
	boolean gotWall = false;
	double wallBearing = 0;
	
	/**
	 * attack level
	 */
	static double FIRE_LEVEL = 1.5;

	/**
	 * run: KadoMan's default behavior
	 */
	public void run() {

		// setAdjustRadarForGunTurn(true);

		// Robot main loop
		while (true) {
			double rand = Math.random() % 4;
			// Replace the next 4 lines with any behavior you would like
			if (!isWalled) {
				ahead(500);
			} else if (!gotWall) {
				withWall(wallBearing);
				gotWall = true;
			} else {
				ahead(100 * rand);
				turnGunRight(180);
				back(200 * rand);
				turnGunLeft(180);
				ahead(100 * rand);
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		fire(FIRE_LEVEL);
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
	}

	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		if (isWalled) {
			double x = getX();
			double y = getY();
		} else {
			wallBearing = e.getBearing();
			isWalled = true;
		}
	}

	public void withWall(double bearing) {
		turnRight(90 + bearing);
	}

	/**
	 * option
	 */

	private static final double DOUBLE_PI = (Math.PI * 2);
	private static final double HALF_PI = (Math.PI / 2);

	public double calculateBearingToXYRadians(double sourceX, double sourceY,
			double sourceHeading, double targetX, double targetY) {
		return normalizeRelativeAngleRadians(Math.atan2((targetX - sourceX),
				(targetY - sourceY)) - sourceHeading);
	}

	public double normalizeAbsoluteAngleRadians(double angle) {
		if (angle < 0) {
			return (DOUBLE_PI + (angle % DOUBLE_PI));
		} else {
			return (angle % DOUBLE_PI);
		}
	}

	public static double normalizeRelativeAngleRadians(double angle) {
		double trimmedAngle = (angle % DOUBLE_PI);
		if (trimmedAngle > Math.PI) {
			return -(Math.PI - (trimmedAngle % Math.PI));
		} else if (trimmedAngle < -Math.PI) {
			return (Math.PI + (trimmedAngle % Math.PI));
		} else {
			return trimmedAngle;
		}
	}
}
