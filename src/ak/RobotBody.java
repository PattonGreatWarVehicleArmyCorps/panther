package ak;

import java.awt.geom.*;
import java.util.*;
import java.math.*;
import robocode.*;

/**
 * RobotBody - a class by Arun Kishore
 */
public class RobotBody {
	static int turnDirection = -1;
	static double prevAngle = -1;
	static int modValue = 20;
	static public Vector dodgeBullets = new Vector();

	public Point2D.Double getDirection(Vector otherBots, Fermat robot,
			int target) {
		int i, j, numOthers = robot.getOthers();
		OtherBot bot;
		Point2D.Double pos = new Point2D.Double();
		double forceX = 0, forceY = 0, force, ang, wallForce, diffEnergy;
		double fieldWidth = robot.getBattleFieldWidth(), fieldHeight = robot
				.getBattleFieldHeight(), robotWidth = robot.getWidth(), robotHeight = robot
				.getHeight();
		long currTime = robot.getTime();

		pos.x = robot.getX();
		pos.y = robot.getY();

		/*
		 * Calculating forces from other bots
		 */
		for (i = 0; i < otherBots.size(); i++) {
			bot = (OtherBot) otherBots.elementAt(i);
			if (bot.alive) {
				force = (-1.0 * (fieldWidth + fieldHeight))
						/ Math.pow(getDistance(pos.x, pos.y, bot.X, bot.Y), 2);
				ang = normalRelativeAngle(90 - Math.toDegrees(Math.atan2(pos.y
						- bot.Y, pos.x - bot.X)));
				forceX += Math.sin(Math.toRadians(ang)) * force;
				forceY += Math.cos(Math.toRadians(ang)) * force;
			}
		}

		/*
		 * Wall Avoidance
		 */
		if (robot.getOthers() == 1)
			wallForce = 5 * (fieldWidth + fieldHeight);
		else
			wallForce = 15 * (fieldWidth + fieldHeight);
		forceX += wallForce / Math.pow(fieldWidth - pos.x, 3);
		forceX -= wallForce / Math.pow(pos.x, 3);
		forceY += wallForce / Math.pow(fieldHeight - pos.y, 3);
		forceY -= wallForce / Math.pow(pos.y, 3);

		/*
		 * Corner Avoiding Force
		 */
		if (numOthers == 1) {
			double cornerForce;
			cornerForce = -0.005 * (fieldWidth + fieldHeight)
					/ Math.pow(getDistance(pos.x, pos.y, 0, 0), 1.0);
			ang = normalRelativeAngle(90 - Math.toDegrees(Math.atan2(pos.y,
					pos.x)));
			forceX += Math.sin(Math.toRadians(ang)) * cornerForce;
			forceY += Math.cos(Math.toRadians(ang)) * cornerForce;

			cornerForce = -0.005 * (fieldWidth + fieldHeight)
					/ Math.pow(getDistance(pos.x, pos.y, fieldWidth, 0), 1.0);
			ang = normalRelativeAngle(90 - Math.toDegrees(Math.atan2(pos.y,
					pos.x - fieldWidth)));
			forceX += Math.sin(Math.toRadians(ang)) * cornerForce;
			forceY += Math.cos(Math.toRadians(ang)) * cornerForce;

			cornerForce = -0.005
					* (fieldWidth + fieldHeight)
					/ Math.pow(
							getDistance(pos.x, pos.y, fieldWidth, fieldHeight),
							1.0);
			ang = normalRelativeAngle(90 - Math.toDegrees(Math.atan2(pos.y
					- fieldHeight, pos.x - fieldWidth)));
			forceX += Math.sin(Math.toRadians(ang)) * cornerForce;
			forceY += Math.cos(Math.toRadians(ang)) * cornerForce;

			cornerForce = -0.005 * (fieldWidth + fieldHeight)
					/ Math.pow(getDistance(pos.x, pos.y, 0, fieldHeight), 1.0);
			ang = normalRelativeAngle(90 - Math.toDegrees(Math.atan2(pos.y
					- fieldHeight, pos.x)));
			forceX += Math.sin(Math.toRadians(ang)) * cornerForce;
			forceY += Math.cos(Math.toRadians(ang)) * cornerForce;
		}

		/*
		 * Dodging bullets
		 */
		int botArray[] = new int[robot.otherBots.size()];
		int temp;
		for (i = 0; i < robot.otherBots.size(); i++)
			botArray[i] = i;
		for (i = 0; i < robot.otherBots.size() - 1; i++) {
			OtherBot botA = (OtherBot) robot.otherBots.elementAt(botArray[i]);
			for (j = i + 1; j < robot.otherBots.size(); j++) {
				OtherBot botB = (OtherBot) robot.otherBots
						.elementAt(botArray[j]);
				if (botB.lastAttackTime > botA.lastAttackTime) {
					temp = botArray[i];
					botArray[i] = botArray[j];
					botArray[j] = temp;
				}
			}
		}
		for (j = 0; j < robot.otherBots.size(); j++) {
			bot = (OtherBot) robot.otherBots.elementAt(botArray[j]);
			diffEnergy = bot.prevEnergy - bot.energy;
			if (bot.scanTime - bot.previousScanTime > 8 || !bot.attacking)
				continue;
			if (diffEnergy > 0.09 && diffEnergy < 3.01) {
				long fireTime;
				for (fireTime = ((numOthers == 1) ? bot.previousScanTime
						: currTime); fireTime <= currTime; fireTime++) {
					if (currTime - robot.updateTime[(int) (fireTime % 50)] > 25
							|| currTime
									- robot.updateTime[(int) ((fireTime + 49) % 50)] > 25)
						continue;
					Point2D.Double currPos = robot.pastPositions[(int) (fireTime % 50)];
					double currHeading = robot.pastHeadings[(int) (fireTime % 50)];
					double prevHeading = robot.pastHeadings[(int) ((fireTime + 49) % 50)];
					double currVelocity = robot.pastVelocities[(int) (fireTime % 50)];
					// Point2D.Double currPos =
					// robot.pastPositions[(int)(currTime-fireTime+(robot.getOthers())/4)];
					Point2D.Double botPos;
					if (numOthers == 1) {
						botPos = new Point2D.Double(
								bot.prevX
										+ ((fireTime - bot.previousScanTime) * (bot.X - bot.prevX))
										/ (bot.scanTime - bot.previousScanTime),
								bot.prevY
										+ ((fireTime - bot.previousScanTime) * (bot.Y - bot.prevY))
										/ (bot.scanTime - bot.previousScanTime));
					} else
						botPos = new Point2D.Double(bot.X, bot.Y);
					/*
					 * double currHeading =
					 * robot.pastHeadings[(int)(currTime-fireTime
					 * +(robot.getOthers())/4)]; double prevHeading =
					 * robot.pastHeadings
					 * [(int)(currTime-fireTime+(robot.getOthers())/4+1)];
					 * double currVelocity =
					 * robot.pastVelocities[(int)(currTime-
					 * fireTime+(robot.getOthers())/4)];
					 */
					DodgeBullet dBullet = new DodgeBullet(botPos.x, botPos.y,
							currPos, diffEnergy, fireTime, botArray[j]);
					// robot.out.println(bot.previousScanTime+" "+bot.scanTime+" "+fireTime);
					dodgeBullets.add(dBullet);
					if (Math.abs(currVelocity) > 0) {
						// add linear bullet
						long nextTime, time;
						Point2D.Double p = new Point2D.Double(currPos.x,
								currPos.y);
						double velocity = robot.avgVelocity;
						if (currVelocity < 0)
							velocity *= -1;
						for (i = 0; i < 10; i++) {
							nextTime = (long) Math
									.round(Math
											.sqrt(((botPos.x - p.x)
													* (botPos.x - p.x) + (botPos.y - p.y)
													* (botPos.y - p.y)))
											/ (20 - (3 * diffEnergy)));
							/*
							 * time = bot.scanTime + nextTime; double diff =
							 * time - fireTime;
							 */
							if (numOthers == 1)
								time = fireTime + nextTime;
							else
								time = bot.scanTime + nextTime;
							double diff = time - fireTime;
							p.x = currPos.x
									+ Math.sin(Math
											.toRadians(normalRelativeAngle(currHeading)))
									* currVelocity * diff;
							p.y = currPos.y
									+ Math.cos(Math
											.toRadians(normalRelativeAngle(currHeading)))
									* currVelocity * diff;
						}
						dBullet = new DodgeBullet(botPos.x, botPos.y, p,
								diffEnergy, fireTime, botArray[j]);
						dodgeBullets.add(dBullet);

						// add average linear bullet
						if (numOthers == 1) {
							p = new Point2D.Double(currPos.x, currPos.y);
							for (i = 0; i < 10; i++) {
								nextTime = (long) Math
										.round(Math
												.sqrt(((botPos.x - p.x)
														* (botPos.x - p.x) + (botPos.y - p.y)
														* (botPos.y - p.y)))
												/ (20 - (3 * diffEnergy)));
								/*
								 * time = bot.scanTime + nextTime; double diff =
								 * time - fireTime;
								 */
								time = fireTime + nextTime;
								double diff = time - fireTime;
								p.x = currPos.x
										+ Math.sin(Math
												.toRadians(normalRelativeAngle(currHeading)))
										* velocity * diff;
								p.y = currPos.y
										+ Math.cos(Math
												.toRadians(normalRelativeAngle(currHeading)))
										* velocity * diff;
							}
							dBullet = new DodgeBullet(botPos.x, botPos.y, p,
									diffEnergy, fireTime, botArray[j]);
							dodgeBullets.add(dBullet);
						}

						double headingChange = Math
								.toRadians(normalRelativeAngle(currHeading
										- prevHeading));
						if (Math.abs(headingChange) > 0.00001) {
							// add circular bullet
							p = new Point2D.Double(currPos.x, currPos.y);
							for (i = 0; i < 10; i++) {
								nextTime = (long) Math
										.round(Math
												.sqrt(((botPos.x - p.x)
														* (botPos.x - p.x) + (botPos.y - p.y)
														* (botPos.y - p.y)))
												/ (20 - (3 * diffEnergy)));
								/*
								 * time = bot.scanTime + nextTime; double diff =
								 * time - fireTime;
								 */
								if (numOthers == 1)
									time = fireTime + nextTime;
								else
									time = bot.scanTime + nextTime;
								double diff = time - fireTime;
								double tothead = Math.toDegrees(diff
										* headingChange);
								double radius = currVelocity / headingChange;
								p.y = currPos.y
										+ Math.sin(Math
												.toRadians(normalRelativeAngle(currHeading
														+ tothead)))
										* radius
										- Math.sin(Math
												.toRadians(normalRelativeAngle(currHeading)))
										* radius;
								p.x = currPos.x
										+ Math.cos(Math
												.toRadians(normalRelativeAngle(currHeading)))
										* radius
										- Math.sin(Math
												.toRadians(normalRelativeAngle(currHeading
														+ tothead))) * radius;
							}
							dBullet = new DodgeBullet(botPos.x, botPos.y, p,
									diffEnergy, fireTime, botArray[j]);
							dodgeBullets.add(dBullet);

							// add average circular bullet
							if (numOthers == 1) {
								p = new Point2D.Double(currPos.x, currPos.y);
								for (i = 0; i < 10; i++) {
									nextTime = (long) Math
											.round(Math
													.sqrt(((botPos.x - p.x)
															* (botPos.x - p.x) + (botPos.y - p.y)
															* (botPos.y - p.y)))
													/ (20 - (3 * diffEnergy)));
									/*
									 * time = bot.scanTime + nextTime; double
									 * diff = time - fireTime;
									 */
									time = fireTime + nextTime;
									double diff = time - fireTime;
									double tothead = Math.toDegrees(diff
											* headingChange);
									double radius = velocity / headingChange;
									p.y = currPos.y
											+ Math.sin(Math
													.toRadians(normalRelativeAngle(currHeading
															+ tothead)))
											* radius
											- Math.sin(Math
													.toRadians(normalRelativeAngle(currHeading)))
											* radius;
									p.x = currPos.x
											+ Math.cos(Math
													.toRadians(normalRelativeAngle(currHeading)))
											* radius
											- Math.sin(Math
													.toRadians(normalRelativeAngle(currHeading
															+ tothead)))
											* radius;
								}
								dBullet = new DodgeBullet(botPos.x, botPos.y,
										p, diffEnergy, fireTime, botArray[j]);
								dodgeBullets.add(dBullet);
							}
						}
					}
				}
				// bot.lastBulletTime = currTime;
			}
			// }
			// robot.out.println(dodgeBullets.size());
			for (i = 0; i < dodgeBullets.size(); i++) {
				DodgeBullet dBullet = (DodgeBullet) dodgeBullets.elementAt(i);
				Point2D.Double currPos = dBullet.getCurrentPosition(currTime);
				if (getDistance(currPos.x, currPos.y, dBullet.source.x,
						dBullet.source.y) > getDistance(pos.x, pos.y,
						dBullet.source.x, dBullet.source.y)
						+ 0.5
						* Math.sqrt(Math.pow(robot.getWidth(), 2)
								+ Math.pow(robot.getHeight(), 2))) {
					dodgeBullets.remove(i);
					i--;
				}
			}
			while (numOthers > 1 && dodgeBullets.size() > 40) {
				double minDamage = 1e30, distance, damage, minDistance = 1e30;
				int minBullet = -1;
				for (i = 0; i < dodgeBullets.size(); i++) {
					DodgeBullet dBullet = (DodgeBullet) dodgeBullets
							.elementAt(i);
					Point2D.Double currPos = dBullet
							.getCurrentPosition(currTime);
					distance = getDistance(pos.x, pos.y, currPos.x, currPos.y);
					if ((20 - dBullet.velocity) / 3 < 1)
						damage = ((20 - dBullet.velocity) / 3) * 4;
					else
						damage = (((20 - dBullet.velocity) / 3) * 6 - 2);
					if (damage < minDamage || damage == minDamage
							&& distance < minDistance) {
						minDamage = damage;
						minDistance = distance;
						minBullet = i;
					}
				}
				dodgeBullets.remove(minBullet);
				// dodgeBullets.remove((int)(Math.random()*(dodgeBullets.size()-1)));
			}
			for (i = 0; i < dodgeBullets.size(); i++) {
				DodgeBullet dBullet = (DodgeBullet) dodgeBullets.elementAt(i);
				if (dBullet.startTime > currTime)
					continue;
				Point2D.Double projPos = dBullet.getProjectedPosition(pos);
				double threshDist = Math.max(robotWidth, robotHeight);
				int wallCount = 0;
				if (projPos.x <= threshDist) {
					wallCount++;
					Point2D.Double wallPos = new Point2D.Double(0, pos.y);
					if (triangleArea(pos, dBullet.source, wallPos) > 0)
						turnDirection = -1;
					else
						turnDirection = 1;
				}
				if (projPos.y <= threshDist) {
					wallCount++;
					Point2D.Double wallPos = new Point2D.Double(pos.x, 0);
					if (triangleArea(pos, dBullet.source, wallPos) > 0)
						turnDirection = -1;
					else
						turnDirection = 1;
				}
				if (projPos.y >= fieldHeight - threshDist) {
					wallCount++;
					Point2D.Double wallPos = new Point2D.Double(pos.x,
							fieldHeight);
					if (triangleArea(pos, dBullet.source, wallPos) > 0)
						turnDirection = -1;
					else
						turnDirection = 1;
				}
				if (projPos.x >= fieldWidth - threshDist) {
					wallCount++;
					Point2D.Double wallPos = new Point2D.Double(fieldWidth,
							pos.y);
					if (triangleArea(pos, dBullet.source, wallPos) > 0)
						turnDirection = -1;
					else
						turnDirection = 1;
				}
				Point2D.Double currPos = dBullet.getCurrentPosition(currTime);
				if (wallCount != 1) {
					if (triangleArea(dBullet.target, dBullet.source, pos) > 0)
						turnDirection = 1;
					else
						turnDirection = -1;
				}
				double distance = getDistance(pos.x, pos.y, currPos.x,
						currPos.y);
				force = (-12.5 * ((fieldWidth + fieldHeight)))
						/ Math.pow(distance, 3.5);
				if ((20 - dBullet.velocity) / 3 < 1)
					force *= ((20 - dBullet.velocity) / 3) * 4;
				else
					force *= (((20 - dBullet.velocity) / 3) * 6 - 2);
				ang = normalRelativeAngle(turnDirection
						* 90
						+ 90
						- Math.toDegrees(Math.atan2(currPos.y
								- dBullet.source.y, currPos.x
								- dBullet.source.x)));
				forceX += Math.sin(Math.toRadians(ang)) * force;
				forceY += Math.cos(Math.toRadians(ang)) * force;
			}
		}
		return new Point2D.Double(forceX, forceY);
	}

	double getDistance(double x1, double y1, double x2, double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		return Math.sqrt(x * x + y * y);
	}

	double triangleArea(Point2D.Double a, Point2D.Double b, Point2D.Double c) {
		return (b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y);
	}

	public double normalRelativeAngle(double angle) {
		if (angle > 180)
			return ((angle + 180) % 360) - 180;
		if (angle < -180)
			return ((angle - 180) % 360) + 180;
		return angle;
	}

	class DodgeBullet {
		Point2D.Double source;
		Point2D.Double target;
		double velocity;
		double startTime;
		int bot;

		DodgeBullet(double sourceX, double sourceY, Point2D.Double target,
				double power, double startTime, int bot) {
			source = new Point2D.Double(sourceX, sourceY);
			this.target = new Point2D.Double(target.x, target.y);
			this.startTime = startTime;
			this.velocity = 20 - 3 * power;
			this.bot = bot;
		}

		Point2D.Double getCurrentPosition(double time) {
			double distance = velocity * (time - startTime);
			return new Point2D.Double(source.x
					+ (distance * (target.x - source.x))
					/ getDistance(target.x, target.y, source.x, source.y),
					source.y
							+ (distance * (target.y - source.y))
							/ getDistance(target.x, target.y, source.x,
									source.y));
		}

		Point2D.Double getProjectedPosition(Point2D.Double botPos) {
			double distance = Math.max(
					getDistance(botPos.x, botPos.y, source.x, source.y),
					getDistance(target.x, target.y, source.x, source.y));
			return new Point2D.Double(source.x
					+ (distance * (target.x - source.x))
					/ getDistance(target.x, target.y, source.x, source.y),
					source.y
							+ (distance * (target.y - source.y))
							/ getDistance(target.x, target.y, source.x,
									source.y));
		}
	}
}
