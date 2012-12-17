package kanto;

import java.awt.Color;

import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * KadoMan - a robot by (your name here)
 */
public class osakaOne extends Robot {

	/**
	 * for be along wall
	 */
	boolean isWalled = false;
	boolean gotWall = false;
	boolean isCenter = true;
	double wallBearing = 0;
	int dameged = 0;

	/**
	 * attack level
	 */
	static double FIRE_LEVEL = 2.3;

	static int escapeThreshold = 2;

	/**
	 * run: KadoMan's default behavior
	 */
	public void run() {
		setBodyColor(Color.ORANGE);
		// Robot main loop
		while (true) {
			if (!isWalled) {
				ahead(500);
			} else if (!gotWall) {
				withWall(wallBearing);
				gotWall = true;
			} else if (!isCenter) {
				ahead(getBattleFieldWidth() / 2);
				isCenter = true;
			} else {
				double rand = Math.random() % 5;
				if (rand % 2 == 0) {
					ahead(70 * rand);
					turnGunRight(150);
					back(140 * rand);
					turnGunLeft(150);
					ahead(70 * rand);
				} else {
					back(70 * rand);
					turnGunRight(150);
					ahead(140 * rand);
					turnGunLeft(150);
					back(70 * rand);

				}
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (!e.getName().startsWith("kanto")) {
			fire(FIRE_LEVEL);
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		if (escapeThreshold <= dameged && isWalled == true && gotWall == true
				&& isCenter == true) {
			isWalled = false;
			gotWall = false;
			isCenter = false;
			dameged = 0;
		}
		dameged += 1;
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
