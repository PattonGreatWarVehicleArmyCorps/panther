package kanto;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Hashtable;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;



public class machinGunV1 extends AdvancedRobot {

	Hashtable targets; // all enemies are stored in the hashtable
	Enemy target; // our current enemy
	final double PI = Math.PI; // just a constant
	int direction = 1; // direction we are heading... 1 = forward, -1 =
						// backwards
	double firePower; // the power of the shot we will be using
	double midpointstrength = 0; // The strength of the gravity point in the
									// middle of the field
	int midpointcount = 0; // Number of turns since that strength was changed.

	public void run() {
		targets = new Hashtable();
		target = new Enemy();
		target.distance = 100000;
		setBodyColor(Color.ORANGE);
		// the next two lines mean that the turns of the robot, gun and radar
		// are independant
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2 * PI); // turns the radar right around to get a
										// view of the field
		while (true) {
			move();
			doFirePower(); // select the fire power to use
			doScanner(); // Oscillate the scanner over the bot
			doGun();
			out.println(target.distance); // move the gun to predict where the
											// enemy will be
			fire(firePower);
			execute(); // execute all commands
		}
	}

	private void move() {
		setTurnRight(1000);
		setMaxVelocity(5);
		setAhead(1000);

	}

	void doFirePower() {
		firePower = 400 / target.distance;// selects a bullet power based on our
											// distance away from the target
		if (firePower > 3) {
			firePower = 3;
		}
	}

	/** keep the scanner turning **/
	void doScanner() {
		setTurnRadarLeftRadians(2 * PI);
	}

	/** Move the gun to the predicted next bearing of the enemy **/
	void doGun() {
		long time = getTime()
				+ (int) Math
						.round((getRange(getX(), getY(), target.x, target.y) / (20 - (3 * firePower))));
		Point2D.Double p = target.guessPosition(time);

		// offsets the gun by the angle to the next shot based on linear
		// targeting provided by the enemy class
		double gunOffset = getGunHeadingRadians()
				- (Math.PI / 2 - Math.atan2(p.y - getY(), p.x - getX()));
		setTurnGunLeftRadians(normaliseBearing(gunOffset));
	}

	// if a bearing is not within the -pi to pi range, alters it to provide the
	// shortest angle
	double normaliseBearing(double ang) {
		if (ang > PI)
			ang -= 2 * PI;
		if (ang < -PI)
			ang += 2 * PI;
		return ang;
	}

	// if a heading is not within the 0 to 2pi range, alters it to provide the
	// shortest angle
	double normaliseHeading(double ang) {
		if (ang > 2 * PI)
			ang -= 2 * PI;
		if (ang < 0)
			ang += 2 * PI;
		return ang;
	}

	// returns the distance between two x,y coordinates
	public double getRange(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double h = Math.sqrt(xo * xo + yo * yo);
		return h;
	}

	// gets the absolute bearing between to x,y coordinates
	public double absbearing(double x1, double y1, double x2, double y2) {
		double xo = x2 - x1;
		double yo = y2 - y1;
		double h = getRange(x1, y1, x2, y2);
		if (xo > 0 && yo > 0) {
			return Math.asin(xo / h);
		}
		if (xo > 0 && yo < 0) {
			return Math.PI - Math.asin(xo / h);
		}
		if (xo < 0 && yo < 0) {
			return Math.PI + Math.asin(-xo / h);
		}
		if (xo < 0 && yo > 0) {
			return 2.0 * Math.PI - Math.asin(-xo / h);
		}
		return 0;
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		if (isFriend(e.getName())) {
			return;
		}
		Enemy en;
		if (targets.containsKey(e.getName())) {
			en = (Enemy) targets.get(e.getName());
		} else {
			en = new Enemy();
			targets.put(e.getName(), en);
		}
		// the next line gets the absolute bearing to the point where the bot is
		double absbearing_rad = (getHeadingRadians() + e.getBearingRadians())
				% (2 * PI);
		// this section sets all the information about our target
		en.name = e.getName();
		double h = normaliseBearing(e.getHeadingRadians() - en.heading);
		h = h / (getTime() - en.ctime);
		en.changehead = h;
		en.x = getX() + Math.sin(absbearing_rad) * e.getDistance(); // works out
																	// the x
																	// coordinate
																	// of where
																	// the
																	// target is
		en.y = getY() + Math.cos(absbearing_rad) * e.getDistance(); // works out
																	// the y
																	// coordinate
																	// of where
																	// the
																	// target is
		en.bearing = e.getBearingRadians();
		en.heading = e.getHeadingRadians();
		en.ctime = getTime(); // game time at which this scan was produced
		en.speed = e.getVelocity();
		en.distance = e.getDistance();
		en.live = true;
		if ((en.distance < target.distance) || (target.live == false)) {
			target = en;
		}
	}

	private boolean isFriend(String name) {
		return name.startsWith("watanabedais01") || name.startsWith("nomura")
				|| name.startsWith("mshi") || name.startsWith("kanto");
	}

	public void onRobotDeath(RobotDeathEvent e) {
		Enemy en = (Enemy) targets.get(e.getName());
		en.live = false;
	}
}

class EnemyV1 {
	/*
	 * ok, we should really be using accessors and mutators here, (i.e getName()
	 * and setName()) but life's too short.
	 */
	String name;
	public double bearing, heading, speed, x, y, distance, changehead;
	public long ctime; // game time that the scan was produced
	public boolean live; // is the enemy alive?

	public Point2D.Double guessPosition(long when) {
		double diff = when - ctime;
		double newY = y + Math.cos(heading) * speed * diff;
		double newX = x + Math.sin(heading) * speed * diff;

		return new Point2D.Double(newX, newY);
	}
}
