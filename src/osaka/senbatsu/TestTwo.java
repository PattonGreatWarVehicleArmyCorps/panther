package osaka.senbatsu;

import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * TestTwo - a robot by (your name here)
 */
public class TestTwo extends Robot {
	/**
	 * run: TestTwo's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar

		// Robot main loop
		while (true) {

			turnRight(adjustHeadingForWalls(0));
			ahead(100);
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		fire(1);
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
		// Replace the next line with any behavior you would like
		back(20);
	}

	/**
	 * avoid
	 */
	private static final double WALL_AVOID_INTERVAL = 10;
	private static final double WALL_AVOID_FACTORS = 20;
	private static final double WALL_AVOID_DISTANCE = (WALL_AVOID_INTERVAL * WALL_AVOID_FACTORS);

	private double adjustHeadingForWalls(double heading) {
		double fieldHeight = getBattleFieldHeight();
		double fieldWidth = getBattleFieldWidth();
		double centerX = (fieldWidth / 2);
		double centerY = (fieldHeight / 2);
		double currentHeading = getHeading();
		double x = getX();
		double y = getY();
		boolean nearWall = false;
		double desiredX;
		double desiredY;

		// If we are too close to a wall, calculate a course toward
		// the center of the battlefield.
		if ((y < WALL_AVOID_DISTANCE)
				|| ((fieldHeight - y) < WALL_AVOID_DISTANCE)) {
			desiredY = centerY;
			nearWall = true;
		} else {
			desiredY = y;
		}
		if ((x < WALL_AVOID_DISTANCE)
				|| ((fieldWidth - x) < WALL_AVOID_DISTANCE)) {
			desiredX = centerX;
			nearWall = true;
		} else {
			desiredX = x;
		}

		// Determine the safe heading and factor it in with the desired
		// heading if the bot is near a wall
		if (nearWall) {
			double desiredBearing = calculateBearingToXYRadians(x, y,
					currentHeading, desiredX, desiredY);
			double distanceToWall = Math.min(Math.min(x, (fieldWidth - x)),
					Math.min(y, (fieldHeight - y)));
			int wallFactor = (int) Math.min(
					(distanceToWall / WALL_AVOID_INTERVAL), WALL_AVOID_FACTORS);
			return ((((WALL_AVOID_FACTORS - wallFactor) * desiredBearing) + (wallFactor * heading)) / WALL_AVOID_FACTORS);
		} else {
			return heading;
		}
	}

	/**
	 * new function
	 */
	 public double getRelativeHeadingRadians() {
	 double relativeHeading = getHeading();
	 if (getVelocity() < 1) {
	 relativeHeading =
	 normalizeAbsoluteAngleRadians(relativeHeading + Math.PI);
	 }
	 return relativeHeading;
	 }
	
	 public void reverseDirection() {
	 double distance = (getDistanceRemaining() * getVelocity());
	 direction *= -1;
	 setAhead(distance);
	 }
	
	 public void setAhead(double distance) {
		double relativeDistance = (distance * direction);
	 super.ahead(relativeDistance);
	 if (distance < 0) {
	 direction *= -1;
	 }
	 }
	
	 public void setBack(double distance) {
	 double relativeDistance = (distance * direction);
	 super.back(relativeDistance);
	 if (distance > 0) {
	 direction *= -1;
	 }
	 }
	
	 public void setTurnLeftRadiansOptimal(double angle) {
	 double turn = normalizeRelativeAngleRadians(angle);
	 if (Math.abs(turn) > HALF_PI) {
	 reverseDirection();
	 if (turn < 0) {
	 turn = (HALF_PI + (turn % HALF_PI));
	 } else if (turn > 0) {
	 turn = -(HALF_PI - (turn % HALF_PI));
	 }
	 }
	 setTurnLeftRadians(turn);
	 }
	
	 public void setTurnRightRadiansOptimal(double angle) {
	 double turn = normalizeRelativeAngleRadians(angle);
	 if (Math.abs(turn) > HALF_PI) {
	 reverseDirection();
	 if (turn < 0) {
	 turn = (HALF_PI + (turn % HALF_PI));
	 } else if (turn > 0) {
	 turn = -(HALF_PI - (turn % HALF_PI));
	 }
	 }
	 setTurnRightRadians(turn);
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
