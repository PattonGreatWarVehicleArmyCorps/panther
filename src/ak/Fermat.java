/*
	Version 1.6:-
	Updates:
	- More independent 1v1 and Melee modes.
	- Replaced attackers list with boolean information in 'OtherBot'
	- Removed interval targetting completely
	- Added Corner-avoidance in 1v1
	- Changed constants to deal with changes in Battle Field size
	- Removed pattern analysis into a seperate class
	- Better measure of Average Velocity, including stop ticks
	- Avoids lucky shots from counting for statistics
	- Resets data after 5 consecutive losses in 1v1. Stores lastWin in data.
	- Avoids unnecessary calculations for lower probability strategies
	- Added error-checking for all index accesses
	- Improved estimation of start time of incoming bullets
	- Changed constants dealing with dodging bullets near walls
	- Considers width of robot while removing inactive bullets
	- Uses Inner-wall strategy for better wall-avoidance
	- Changed the fire-power equation to 900/distance
	- Now prints stats after each round
 */

package ak;

import java.math.*;
import java.util.*;
import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.io.*;

/**
 * Fermat - a robot by Arun Kishore
 */

public class Fermat extends AdvancedRobot {
	/**
	 * run: Fermat's default behavior
	 */
	Vector otherBots = new Vector();
	Vector bulletVector = new Vector();
	RobotBody robotBody = new RobotBody();
	int target, previousTarget, oneOnOneTarget = -1;
	double pastVelocities[];
	Point2D.Double pastPositions[];
	double pastHeadings[];
	double updateTime[];
	double avgVelocity = -1;
	boolean written = false;
	boolean oneWritten = false, oneOnOne, won = false;
	int hits, misses, hitBullets, hitWalls, hitRobots, skippedTurns, turns = 0;

	public void run() {
		Point2D.Double dirP;
		double turn;
		int i;
		Bullet bullet;
		OtherBot bot = new OtherBot(this, 0, 0, 0, 0, 0, 0, "dummy");
		setColors(Color.black, Color.black, Color.blue);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRight(360);
		robotBody.dodgeBullets.clear();
		previousTarget = -1;
		won = false;
		if (getOthers() > 1)
			oneOnOne = false;
		else
			oneOnOne = true;
		hits = misses = hitBullets = hitWalls = hitRobots = skippedTurns = 0;
		pastVelocities = new double[50];
		pastPositions = new Point2D.Double[50];
		pastHeadings = new double[50];
		updateTime = new double[50];
		for (i = 0; i < 50; i++)
			updateTime[i] = -50;
		while (true) {
			doUpdates();
			target = findBestTarget();
			if (getOthers() == 1 && !written && target != -1) {
				oneOnOneTarget = target;
				writeToDataFile();
				readOneOnOneData(target);
				written = true;
			}
			if (target == -1 || getOthers() == 0) {
				execute();
				continue;
			}
			bot = (OtherBot) otherBots.elementAt(target);
			for (i = 0; i < bulletVector.size(); i++) {
				AdvancedBullet adBullet = (AdvancedBullet) bulletVector
						.elementAt(i);
				if (adBullet.bullet != null) {
					bot = (OtherBot) otherBots.elementAt(adBullet.targetBot);
					if (adBullet.bullet.getVictim() != null
							&& adBullet.bullet.getVictim() == bot.name) {
						Statistic stat = (Statistic) bot.stats
								.elementAt(adBullet.strategy);
						stat.hits++;
						stat.lastHitTime = getTime();
						bulletVector.remove(i);
						i--;
					} else if (!adBullet.bullet.isActive()
							|| getDistance(adBullet.bullet.getX(),
									adBullet.bullet.getY(), adBullet.start.x,
									adBullet.start.y) > getDistance(bot.X,
									bot.Y, adBullet.start.x, adBullet.start.y)
									+ Math.max(getWidth() / 2, getHeight() / 2)) {
						Statistic stat = (Statistic) bot.stats
								.elementAt(adBullet.strategy);
						stat.lastHitTime = -1;
						bulletVector.remove(i);
						i--;
					}
				} else {
					bulletVector.remove(i);
					i--;
				}
			}
			bot.fireBot(this);
		}
	}

	void doUpdates() {
		int i;
		Point2D.Double dirP;
		setTurnRadarLeft(getScanAngle());
		updateAttackers();
		pastVelocities[(int) (getTime() % 50)] = getVelocity();
		pastPositions[(int) (getTime() % 50)] = new Point2D.Double(getX(),
				getY());
		pastHeadings[(int) (getTime() % 50)] = getHeading();
		updateTime[(int) (getTime() % 50)] = getTime();
		dirP = robotBody.getDirection(otherBots, this, 0);
		goTo(getX() - dirP.x, getY() - dirP.y);
		if (avgVelocity == -1) {
			avgVelocity = Math.abs(getVelocity());
			turns = 1;
		} else if (getVelocity() != 0) {
			turns++;
			avgVelocity = (avgVelocity * (turns - 1) + Math.abs(getVelocity()))
					/ turns;
		}
	}

	void writeOneOnOneData(int target) {
		if (target == -1)
			return;
		try {
			OtherBot bot = (OtherBot) otherBots.elementAt(target);
			PrintStream tps = new PrintStream(new RobocodeFileOutputStream(
					getDataFile(bot.name + ".1v1.txt")));
			for (int j = 0; j < bot.stats.size(); j++) {
				Statistic stat = (Statistic) bot.stats.elementAt(j);
				tps.println(stat.shots);
				tps.println(stat.hits);
			}
			tps.println(bot.lastWin);
			if (tps.checkError())
				out.println("Error writing data to robot file");
			tps.close();
		} catch (IOException ex) {
			out.println("Could not write Data");
		}
	}

	void readOneOnOneData(int target) {
		OtherBot bot = (OtherBot) otherBots.elementAt(target);
		bot.avgVelocity = Math.abs(bot.velocity);
		bot.patternAnalyzer = new PatternAnalyzer();
		bot.stats = new Vector();
		bot.dodgeMoves = new Vector();
		bot.scans = 1;
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					getDataFile(bot.name + ".1v1.txt")));
			for (int i = 0; i < FiringStrategy.count; i++) {
				Statistic stat = new Statistic();
				stat.shots = Integer.parseInt(br.readLine());
				stat.hits = Integer.parseInt(br.readLine());
				stat.lastHitTime = -1;
				bot.stats.addElement(stat);
			}
			bot.lastWin = Integer.parseInt(br.readLine());
		} catch (Exception exp1) {
			bot.stats.clear();
			for (int i = 0; i < FiringStrategy.count; i++) {
				Statistic stat = new Statistic();
				stat.shots = 2;
				stat.hits = 1;
				stat.lastHitTime = -1;
				bot.stats.addElement(stat);
			}
			bot.lastWin = 0;
		}
		if (bot.lastWin > 4) {
			bot.stats.clear();
			for (int i = 0; i < FiringStrategy.count; i++) {
				Statistic stat = new Statistic();
				stat.shots = 2;
				stat.hits = 1;
				stat.lastHitTime = -1;
				bot.stats.addElement(stat);
			}
			bot.lastWin = 0;
			bot.dodgeMoves.clear();
		}
	}

	double getScanAngle() {
		int i, scanBot = -1;
		long minTime = 1000000000;
		double turn, minDist = 1e30;
		OtherBot bot;
		if (otherBots.size() < getOthers())
			return 360;
		for (i = 0; i < otherBots.size(); i++) {
			bot = (OtherBot) otherBots.elementAt(i);
			if (bot.alive
					&& (bot.scanTime < minTime || bot.scanTime == minTime
							&& getDistance(bot.X, bot.Y, getX(), getY()) < minDist)) {
				minTime = bot.scanTime;
				minDist = getDistance(bot.X, bot.Y, getX(), getY());
				scanBot = i;
			}
		}
		if (scanBot == -1 || (getTime() - minTime) > 8)
			return 360;
		bot = (OtherBot) otherBots.elementAt(scanBot);
		turn = findRadarTurn(bot.X, bot.Y);
		if (turn < 0)
			return turn - 20;
		else
			return turn + 20;
	}

	public void onHitWall(HitWallEvent e) {
		hitWalls++;
	}

	public void onHitRobot(HitRobotEvent e) {
		hitRobots++;
	}

	public void onSkippedTurn(SkippedTurnEvent e) {
		skippedTurns++;
	}

	public void onBulletMissed(BulletMissedEvent e) {
		misses++;
	}

	public void onBulletHit(BulletHitEvent e) {
		hits++;
	}

	void updateAttackers() {
		int i, j, nearest = -1;
		OtherBot botA, botB;
		double dist, min = 1e30;
		for (i = 0; i < otherBots.size(); i++) {
			botA = (OtherBot) otherBots.elementAt(i);
			if (!botA.alive)
				continue;
			dist = (botA.X - getX()) * (botA.X - getX()) + (botA.Y - getY())
					* (botA.Y - getY());
			if (dist < min) {
				min = dist;
				nearest = i;
			}
			for (j = 0; j < otherBots.size(); j++)
				if (i != j) {
					botB = (OtherBot) otherBots.elementAt(j);
					if (!botB.alive)
						continue;
					if ((botA.X - botB.X) * (botA.X - botB.X)
							+ (botA.Y - botB.Y) * (botA.Y - botB.Y) < dist)
						break;
				}
			if (j == otherBots.size()) {
				botA.lastAttackTime = getTime();
				botA.attacking = true;
			}
		}
		int attackers = 0;
		for (i = 0; i < otherBots.size(); i++) {
			botB = (OtherBot) otherBots.elementAt(i);
			if (botB.alive == false || getTime() - botB.lastAttackTime > 35)
				botB.attacking = false;
			if (botB.attacking)
				attackers++;
		}
		if (attackers == 0 && nearest != -1) {
			botB = (OtherBot) otherBots.elementAt(nearest);
			botB.lastAttackTime = getTime();
			botB.attacking = true;
		}
	}

	int findBestTarget() {
		int i, target = -1;
		double value, minValue = 1e30, turn;
		OtherBot bot;
		for (i = 0; i < otherBots.size(); i++) {
			bot = (OtherBot) otherBots.elementAt(i);
			if (bot.alive) {
				turn = Math.abs(findGunTurn(bot.X, bot.Y));
				value = Math.pow(
						((bot.X - getX()) * (bot.X - getX()) + (bot.Y - getY())
								* (bot.Y - getY())), 2)
						* Math.pow(45 + turn, 0.5);
				if (value < minValue) {
					minValue = value;
					target = i;
				}
			}
		}
		return target;
	}

	double findGunTurn(double x, double y) {
		double angle = normalAbsoluteHeading(Math.toDegrees(Math.PI / 2
				- Math.atan2(getY() - y, getX() - x)));
		return normalRelativeAngle(getGunHeading() - angle + 180);
	}

	double findRadarTurn(double x, double y) {
		double angle = normalAbsoluteHeading(Math.toDegrees(Math.PI / 2
				- Math.atan2(getY() - y, getX() - x)));
		return normalRelativeAngle(getRadarHeading() - angle + 180);
	}

	/** Move in the direction of an x and y coordinate **/
	void goTo(double x, double y) {
		double angle = normalAbsoluteHeading(Math.toDegrees(Math.PI / 2
				- Math.atan2(getY() - y, getX() - x)));
		int dir = 1;
		angle = normalRelativeAngle(getHeading() - angle + 180);
		if (angle > 90) {
			angle -= 180;
			dir = -1;
		} else if (angle < -90) {
			angle += 180;
			dir = -1;
		}
		setTurnLeft(angle);
		setAhead(20 * dir);
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		int i;
		OtherBot bot;
		for (i = 0; i < otherBots.size(); i++) {
			bot = (OtherBot) otherBots.elementAt(i);
			if (bot.name == e.getName()) {
				bot.update(
						e.getEnergy(),
						getX()
								+ e.getDistance()
								* Math.cos(Math
										.toRadians(normalRelativeAngle(90 - (getHeading() + e
												.getBearing())))),
						getY()
								+ e.getDistance()
								* Math.sin(Math
										.toRadians(normalRelativeAngle(90 - (getHeading() + e
												.getBearing())))), e
								.getHeading(), e.getVelocity(), getTime());
				break;
			}
		}
		if (i == otherBots.size()) {
			bot = new OtherBot(
					this,
					e.getEnergy(),
					getX()
							+ e.getDistance()
							* Math.cos(Math
									.toRadians(normalRelativeAngle(90 - (getHeading() + e
											.getBearing())))),
					getY()
							+ e.getDistance()
							* Math.sin(Math
									.toRadians(normalRelativeAngle(90 - (getHeading() + e
											.getBearing())))), e.getHeading(),
					e.getVelocity(), getTime(), e.getName());
			otherBots.addElement(bot);
		}
	}

	public void printStats() {
		out.println("Hits/Shots(%): " + hits + "/" + (hits + misses) + " ( "
				+ (int) (hits * 100.0 / (hits + misses)) + "% )");
		out.println("Hit by Bullets: " + hitBullets);
		out.println("Hit Other Robots: " + hitRobots);
		out.println("Hit Walls: " + hitWalls);
		out.println("Skipped Turns: " + skippedTurns);
	}

	public void onRobotDeath(RobotDeathEvent e) {
		int i;
		OtherBot bot;
		for (i = 0; i < otherBots.size(); i++) {
			bot = (OtherBot) otherBots.elementAt(i);
			if (bot.name == e.getName()) {
				bot.alive = false;
				break;
			}
		}
	}

	public double normalAbsoluteHeading(double angle) {
		if (angle < 0)
			return 360 + (angle % 360);
		else
			return angle % 360;
	}

	public double normalRelativeAngle(double angle) {
		if (angle > 180)
			return ((angle + 180) % 360) - 180;
		if (angle < -180)
			return ((angle - 180) % 360) + 180;
		return angle;
	}

	public void onHitByBullet(HitByBulletEvent e) {
		int j;
		hitBullets++;
		for (j = 0; j < otherBots.size(); j++) {
			OtherBot bot = (OtherBot) otherBots.elementAt(j);
			if (bot.name == e.getName()) {
				bot.lastAttackTime = getTime();
				bot.attacking = true;
				break;
			}
		}
	}

	public void writeToDataFile() {
		DataWriter dataWriter = new DataWriter(this);
		dataWriter.start();
	}

	public void onDeath(DeathEvent e) {
		if (!won) {
			out.println("Robots Remaining: " + getOthers());
			printStats();
			if (getOthers() > 1 && !written) {
				writeToDataFile();
				written = true;
			} else {
				if (oneOnOneTarget == -1)
					return;
				OtherBot bot = (OtherBot) otherBots.elementAt(oneOnOneTarget);
				bot.lastWin++;
				writeOneOnOneData(oneOnOneTarget);
			}
		}
	}

	public void onWin(WinEvent e) {
		out.println("I won, Yo!");
		won = true;
		printStats();
		if (oneOnOneTarget == -1)
			return;
		OtherBot bot = (OtherBot) otherBots.elementAt(oneOnOneTarget);
		bot.lastWin = 0;
		writeOneOnOneData(oneOnOneTarget);
	}

	double getDistance(double x1, double y1, double x2, double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		return Math.sqrt(x * x + y * y);
	}
}