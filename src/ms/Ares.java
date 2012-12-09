package ms;
import robocode.*;
import java.awt.Color;
import java.awt.geom.*;
import java.io.*;
import java.util.*;


/**
 * ToDo:
 * - otherBot hits Wall, don't dodge
 * - abhängig von FirePower Distance in 1v1 Move1
 * - random distance for CornerJiggle
 */

/******************************************************************************************
 * Ares - a robot by Manfred Schuster
 ******************************************************************************************
 */

public class Ares extends AdvancedRobot
{
	static boolean debug = false;
	final boolean oVo_RamAlways = false;
	final double PI = Math.PI;
	final int ahead = 1;
	final int back = -1;
	final int maxScanTry = 3;
	final long HitTimeTurn = 33;
	final boolean TurnSometimes = false;

	static double fieldHeight;
	static double fieldWidth;
	static double centerX;
	static double centerY;
	static int numOthers;
	
	static int GoodBoys = 1;
	static boolean ColorNormal;
	static String winStat = "";
	static String MoveStat = "";
	static Random random = new Random();
	static int direction = 1;
	static double success;
	static long firstPossibleShoot = 30;

	double moveDistance = 32;
	boolean ColorSet;
	double gunPower;
	boolean gunLoad;
	long lastFireTime = 30;
	boolean ram;
	long ramStartTime;
	String ramName = "";
	long HitTime1 = 0;
	long HitTime2 = 0;
	double wiggle = 0;
	long sameDirection = 0;
	long maxSameDirection = 100;

	static Hashtable targets = new Hashtable();
	Enemy target;
	boolean useAntiGrav = true;
	boolean inCorner1 = false, inCorner2 = false, inCorner3 = false, inCorner4 = false;
	int CornerStep = 0;
	int CornerBehaviour = 1;

	ScannedRobotEvent Target;
	Enemy enScan = new Enemy();
	int radarDirection = 1;
	double ScanCone = 15;
	double ScanAngel;
	double ScanDistance;
	long ScanTime;
	int ScanTry;
	static String otherName = "";
	double otherEnergy;
	double otherVelocity  = 0;
	double otherVelocity2 = 0;
	int otherScans = 0;	
	double bearRad;
	double headRad;
	double otherHeadDiff;
	double speed;
	double x, y;

	int MoveType = 1;
	static int RoundsType1 = 0;
	static int RoundsType2 = 0;
	static int RoundsType3 = 0;
	static int RoundsType4 = 0;
	static int WinsType1 = 0;
	static int WinsType1A = 0;
	static int WinsType2 = 0;
	static int WinsType3 = 0;
	static int WinsType4 = 0;

	Map bulletsFired = new HashMap(20);
	int FireType = 2;
	int OverallFire = 0;
	static long numBulletsHit  [] = new long[6];
	static long numBulletsMiss [] = new long[6];
	static long numBulletsElse [] = new long[6];
	static long numBulletsFired[] = new long[6];
	static long numBulletsBreak[] = new long[6];
	double startToDisable=0;
	static boolean tryToDisable = true;

	int HitWallCounter = 0;
	double myRoundScore = 0;
	double myDamageRoundScore = 0;
	static double myScore1 = 0;
	static double myScore2 = 0;
	static double myScore3 = 0;
	static double myScore4 = 0;
	
	boolean dodge = false;
	static boolean useAlternateMove1 = false;
	boolean actAlternateMove1 = false;
	static boolean useFakeSpeed = true;
	static int workingMove = 0;
	long nextTurnTime = 0;
	boolean moveToTargetAtRoundStart = true;
	long mustDodgeTime = 0;
	boolean useDelayedDodge = true;
	
	/******************************************************************************************
	 * enemy information
	 ******************************************************************************************
	 */
	public class Enemy {
		String name;
		boolean	live;
		long		scanTime;
		double		x,y;
		double		energy;
		double		energyDiff;
		double 	head;
		double 	headRad;
		double		bear;
		double		bearRad;
		double		absBearRad;
		double		headDiff;
		double		velocity;
		double		velocityA;
		double		velocityAt;
		double		velocityB;
		double		lsVelocityA;
		double		lsVelocityB;
		long		lsTime;
		double		distance;
		long		lastFireTime;
		double		lastFireEnergy;
		boolean	lastFireReplied;
		long		nextFireTime;
		long		survivalPoints;
		long		finalist;
		long		finalistWinner;
		long		finalistLooser;
		double		hitByBulletDamage;
		double		totalHitByBulletDamage;
		int		survive;
		double		wallDistance;
		double		minWallDistance = 100;
		long		oVo_battles;
		int		oVo_workingMove;
		int		oVo_workingShot;
		int		oVo_workingFire;
		long		bulletsHit  [] = new long[6];
		long		bulletsMiss [] = new long[6];
		long		bulletsElse [] = new long[6];
		long		bulletsFired[] = new long[6];
		long		bulletsBreak[] = new long[6];
	}

	//******************************************************************************************/
	//  run: Main
	//******************************************************************************************/
	public void run() {
		initBot();
		GoodBot.Reset();
		while (getOthers() > 0)
		{
			// Radar
			setRadar();

			// Move
			if (getOthers() > 1 || MoveType == 3 && getTime() - target.lastFireTime < 100)
				antiGravMove();
			else if (Target != null)
				move();

			//Gun
			setGun();
			// Fire or execute
			
			double absGGTR = getGunTurnRemaining();
			double sqrtSD = Math.sqrt(ScanDistance);
			if ((MoveType!=4 || !target.lastFireReplied || getTime() - target.lastFireTime > 17) && gunPower > 0 &&
				/*target.energy > .05 && */gunLoad && getGunHeat() == 0 && (getTime() - ScanTime < 2) &&
			   (getOthers() != 1 || getTime() - target.lastFireTime < 20 || getEnergy() < target.energy || getEnergy()-3 > target.energy || target.energy == 0 || ScanDistance < 150 || target.lastFireTime == 0) &&
			   (absGGTR <= 15/Math.sqrt(ScanDistance) /*gunPower / 20 || ScanDistance < 150 && Math.abs(getGunTurnRemaining()) <= 5*/
			   || ScanDistance < 50 && absGGTR <= 100/sqrtSD
			   || ScanDistance < 100 && absGGTR <= 80/sqrtSD
			   || ScanDistance < 200 && absGGTR <= 60/sqrtSD
			   || ScanDistance < 300 && absGGTR <= 40/sqrtSD
			   || ScanDistance < 600 && absGGTR <= 30/sqrtSD
			   || ScanDistance < 800 && absGGTR <= 25/sqrtSD
			   || getTime() - lastFireTime > 3/getGunCoolingRate()
			   )) {
					numBulletsFired[0]++;
					numBulletsFired[FireType]++;
					gunLoad = false;
					bulletsFired.put(setFireBullet(gunPower), new Integer(FireType));
					lastFireTime = getTime();
					target.velocityB = target.velocityA/target.velocityAt;
					target.velocityA = 0;
					target.velocityAt = 0;
					target.lsTime = getTime();
					target.lsVelocityB = target.lsVelocityA;
					target.lsVelocityA = target.velocity;
					target.lastFireReplied = true;
					for (int i=1; i<5; i++) if (i!=FireType) numBulletsBreak[i]--; else numBulletsBreak[i]++;
			}
			Target = null;
			execute();
		}
	}

	//******************************************************************************************/
	// onScannedRobot:  We have a target.  Go get it.
	//******************************************************************************************/
	public void onScannedRobot(ScannedRobotEvent e) {
		Enemy en;
		if (targets.containsKey(e.getName())) {
			en = (Enemy)targets.get(e.getName());
		} else {
			en = new Enemy();
			targets.put(e.getName(),en);
			en.name	= e.getName();
			readBotFile(en);
		}
		/*if (en.energy>=0 && en.live==false && en.name != null)
			out.println("Scanned " + en.name);*/
		double absBearRad = (getHeadingRadians() + e.getBearingRadians())%(2*PI);
		long lastScanTime = en.scanTime;
		//out.println(getTime() - lastScanTime);
		en.live			= true;
		en.headDiff		= normalizeAngle(e.getHeadingRadians() - en.headRad) / (e.getTime() - lastScanTime);
		/*if (sign(en.velocity) != sign(e.getVelocity())) {
			if (e.getTime()-en.lastAheadBack<30)
				en.dirChanges++;
			en.timeAheadBack = (en.timeAheadBack + e.getTime()-en.lastAheadBack)/2;
			en.lastAheadBack = e.getTime();
		}*/
		en.x				= getX() + Math.sin(absBearRad) * e.getDistance();
		en.y				= getY() + Math.cos(absBearRad) * e.getDistance();
		en.scanTime			= e.getTime();
		en.energyDiff		= en.energy - e.getEnergy();
		en.energy			= e.getEnergy();
		en.head				= e.getHeading();
		en.headRad			= e.getHeadingRadians();
		en.bear				= e.getBearing();
		en.bearRad			= e.getBearingRadians();
		en.absBearRad		= absBearRad;
		en.distance			= e.getDistance();
		en.wallDistance		= wallDistance(en.x, en.y);
		if (en.wallDistance < en.minWallDistance) en.minWallDistance = en.wallDistance;
		en.velocity			= e.getVelocity();
		en.velocityA		+= en.velocity;
		en.velocityAt++;
		//out.println(getTime() + ": " + en.velocity + " -> " + en.x + "/" + en.y);

		if (GoodBot.badBot(e.getName(), this)) {
			if (otherName.compareTo(e.getName()) == 0 ||
				otherName.compareTo("") == 0 ||
				getTime() - ScanTime > 10 ||
				target.hitByBulletDamage < 75 && (
				ScanDistance - 200 > e.getDistance() && e.getDistance() < 750||
				(e.getDistance() < 100 && ScanDistance > 200) ||
				(e.getDistance() <  50 && ScanDistance > 100) ||
				(e.getEnergy() < 5 && otherEnergy > 20 && ScanDistance > 250) ||
				otherEnergy > 5 && ScanDistance > 650 && en.finalistWinner > target.finalistWinner)) {

				if (otherName.compareTo(e.getName()) != 0)
					resetTarget(e.getName());

				Target = e;
				target = en;

				ScanTry = maxScanTry;
				ScanCone = Math.abs(ScanCone);
				if (en.bear > 0) ScanCone*=-1;
				ScanAngel = e.getBearing() + getHeading();

				otherHeadDiff = en.headDiff;
				x = en.x;
				y = en.y;
				headRad = en.headRad;
				bearRad = en.bearRad;
				ScanTime = en.scanTime;
				speed = en.velocity;
				ScanDistance = en.distance;
				double otherEnergyDiff = en.energyDiff;
				otherEnergy = en.energy;
				
				if (otherEnergyDiff < 3.01 && otherEnergyDiff > 0.01) {
					if (lastScanTime-en.nextFireTime >= 0) {
						en.lastFireTime = lastScanTime;
						en.lastFireEnergy = otherEnergyDiff;
						en.nextFireTime = (long)(en.lastFireTime + (1+en.lastFireEnergy/5)/getGunCoolingRate());
						en.lastFireReplied = false;
						if (MoveType == 1 && getOthers()==1) {
							long dodgeTime = lastScanTime-25 + (long)(ScanDistance/(20-(3*otherEnergyDiff)));
							if (mustDodgeTime == 0 || dodgeTime < mustDodgeTime)
								mustDodgeTime = dodgeTime;
							if (useAlternateMove1 && wallDistance(getX(), getY()) > 40 && numOthers==1) {
								moveDistance = e.getDistance() / 4 + 25;
								if (moveDistance > 50)
									moveDistance = 50;
								if (wallDistance(getX(), getY()) > moveDistance)
									turnDirection();
								dodge = false;
							}
							else {
								moveDistance = 32;
								if (TurnSometimes && sameDirection >= maxSameDirection)
									turnDirection();
								dodge = (useDelayedDodge && (mustDodgeTime > en.nextFireTime || sameDirection < 3));
							}
							dodge = !dodge;
							if (dodge || e.getDistance() < 400 || random.nextDouble() < .1) {
								setAhead(moveDistance * direction);
								sameDirection = sameDirection + 1;
								mustDodgeTime = 0;
							}
						}
					}
					//else debug(lastScanTime + ": " + (lastScanTime-en.nextFireTime) + " - " + e.getEnergy());
				}
				if (getTime() > 10) {
					otherScans++;
					otherVelocity  += Math.abs(e.getVelocity());
					otherVelocity2 +=          e.getVelocity();
				}
			}
		}

		//set radar
		if (enScan.name != null)
			enScan = (Enemy)targets.get(enScan.name);
		if (getOthers() > 1 &&
			(enScan.name == null ||
			enScan.name.compareTo(e.getName()) == 0 ||
			enScan.live == false))
				getBestRadarDirection();
		else {
			setTurnRadarRight(normalRelativeAngle(ScanAngel - getRadarHeading()) * 2);
			/*if (getTime()-enScan.scanTime>20 &&
				enScan.name.compareTo(e.getName()) != 0 &&
				getOthers() > 1)
				out.println(getTime() + ": Scanner > " + enScan.name);*/
		}
	}
	
	//******************************************************************************************/
	// onHitRobot:  what shall I do? Will he kill me now?
	//******************************************************************************************/
	public void onHitRobot(HitRobotEvent e) {
		Enemy en = new Enemy();
		if (targets.containsKey(e.getName())) {
			en = (Enemy)targets.get(e.getName());
			en.energy = e.getEnergy();
		}

		if (otherName.compareTo(e.getName()) == 0)
			otherEnergy = e.getEnergy();

		if (e.isMyFault()) {
			double damage = otherEnergy - e.getEnergy();
			myRoundScore += damage * 2;
			myDamageRoundScore += damage * 2;
		}

		CornerStep = 0;
		turnDirection();
	}
	
	//******************************************************************************************/
	// onHitWall:  Handle collision with wall.
	//******************************************************************************************/
	public void onHitWall(HitWallEvent e)
	{
		//debug("I hit the wall!");
		HitWallCounter++;
		CornerStep = 0;
		setToXY(centerX, centerY, (int)wallDistance(getX(), getY()) - 40);
	}

	//******************************************************************************************/
	// onHitByBullet:  Ouch...
	//******************************************************************************************/
	public void onHitByBullet(HitByBulletEvent e)
	{
		if (mustDodgeTime != 0) {
			mustDodgeTime = 1;
			useDelayedDodge = false;
		}
		moveToTargetAtRoundStart = false;
		useFakeSpeed = !useFakeSpeed;
		//new Target?
		if (Target != null)
			if (e.getName().compareTo(Target.getName()) != 0 && GoodBot.badBot(e.getName(), this))
				Target=null;
		
		Enemy en = new Enemy();
		if (targets.containsKey(e.getName())) {
			en = (Enemy)targets.get(e.getName());
			double damage = getBulletDamage(e.getPower(), getEnergy());
			en.hitByBulletDamage += damage;
			en.totalHitByBulletDamage += damage;
			en.energy += e.getPower() * 3;
			if (target.hitByBulletDamage*.05 < en.hitByBulletDamage ||
				en.hitByBulletDamage>16 || target.distance - en.distance < centerX)
				target = en;
		}
		
		if (otherName.compareTo(e.getName()) != 0 && GoodBot.badBot(e.getName(), this))
			{
				if (target.name.compareTo(e.getName()) == 0)
					resetTarget(e.getName());
			}
		else {
			//HitPoints extra addieren???
			otherEnergy += e.getPower() * 3;
			HitTime2 = HitTime1;
			HitTime1 = getTime();
			if ((HitTime1 - HitTime2 <= HitTimeTurn &&  HitTime2 != 0 || random.nextDouble() < .25 && ScanDistance > 750)
				&& sameDirection > 1 && wallDistance(getX(), getY()) > 150 && ScanDistance > 80) {
				HitTime1 = 0;
				HitTime2 = 0;
				turnDirection();
				//if (getOthers() > 1) {
					maxSameDirection = maxSameDirection + 5;
					if (maxSameDirection > (16 - getOthers()) && sameDirection > 1)
						maxSameDirection = 5;
				//}
			}
			
			if (MoveType == 1 &&
				actAlternateMove1 == useAlternateMove1 &&
				getEnergy() <= 40 &&
				otherEnergy-25 > getEnergy() &&
				getTime()/(getEnergy()+1) < 10 &&
				numOthers==1 &&
				!oVo_RamAlways) {
					useAlternateMove1 = !useAlternateMove1;
					if (useAlternateMove1)
						debug("Starting alternate movement 1 at time " + getTime());
					else
						debug("Starting normal movement 1 at time " + getTime());
				}
		}
	}

	//******************************************************************************************/
	// onBulletHit
	//******************************************************************************************/
    public void onBulletHit(BulletHitEvent e)
	{
		Bullet bullet = e.getBullet();
		if (numOthers==1) {
			double damage = getBulletDamage(bullet.getPower(), otherEnergy);
			myRoundScore += damage;
			myDamageRoundScore += damage;
		}
		Integer f = (Integer)bulletsFired.get(bullet);
		if (f == null) { // alter Gegner
            //out.println("Bullet null hit???");
        } else {
			int ft = f.intValue();
			numBulletsHit[0]++;
			if (ft < 6) {
				if (otherName.compareTo(e.getName()) == 0) {
					otherEnergy = e.getEnergy();
					target.energy = e.getEnergy();
					numBulletsHit[ft]++;
					numBulletsBreak[ft]=0;
	            } else
					numBulletsElse[ft]++;
			} else debug("unknown FireType hit");
		}
    }

	//******************************************************************************************/
	// onBulletMissed
	//******************************************************************************************/
    public void onBulletMissed(BulletMissedEvent e)
	{
		//out.println("BulletMissed");
		Bullet bullet = e.getBullet();
		Integer f = (Integer)bulletsFired.get(bullet);
		if (f == null) {
            //out.println("Bullet null missed???");
        } else {
			numBulletsMiss[0]++;
            int ft = f.intValue();
			if (ft < 6)
				numBulletsMiss[ft]++;
			else
				debug("unknown FireType missed");
        }
    }
	
	//******************************************************************************************/
	// onBulletHitBullet
	//******************************************************************************************/
	public void onBulletHitBullet(BulletHitBulletEvent e) {
		//out.println("BulletHitBullet");
		Bullet bullet = e.getBullet();
		Integer f = (Integer)bulletsFired.get(bullet);
		if (f == null) {
            //out.println("Bullet null hit bullet???");
        } else {
			numBulletsElse[0]++;
            int ft = f.intValue();
			if (ft < 6)
				numBulletsMiss[ft]++;
			else
				debug("unknown FireType hit bullet");
        }
	}

	//******************************************************************************************/
	//  onRobotDeath: RobotDeath
	//******************************************************************************************/
	public void onRobotDeath(RobotDeathEvent e) {
		
		myRoundScore += 50 + myDamageRoundScore *.2;
		Enemy en;
		if (targets.containsKey(e.getName())) {
			en = (Enemy)targets.get(e.getName());
			en.live = false;
			en.energy = 0;
			en.survivalPoints += (numOthers - getOthers()-1) * 50;
			if (getOthers() == 0)
				en.finalistLooser++;
		}


		//debug("Robot Death: " + e.getName());
		GoodBot.BotDeath(e.getName(), this);
		
		// Target destroyed
		if (Target != null)
			if (e.getName().compareTo(Target.getName()) == 0) Target=null;
		if (otherName.compareTo(e.getName()) == 0 && getOthers() != 0)
			resetTarget("");
		
		//now the last enemy
		if (getOthers() == 1) {
			Enumeration enu = targets.elements();
			while (enu.hasMoreElements()) {
				en = (Enemy)enu.nextElement();
				if (en.live)
					target = en;
			}
			resetFireTypes(); //most bots move different in 1v1
			if (target.finalistWinner*2 - target.finalistLooser > 0) MoveType = 3;
			else if (target.oVo_workingMove > 0) MoveType = target.oVo_workingMove;
			
			if (wallDistance(getX(), getY()) < 60) setToXY(centerX, centerY, (int)wallDistance(getX(), getY()) - 40);
			else setAhead(0);
		}
		
		// Victory
		if (getOthers() == 0) {
			myRoundScore += 10 * numOthers;
			Statistik();
			doVictoryDance();
		}
		
	}

	//******************************************************************************************/
	// onWin: na also
	//******************************************************************************************/
	public void onWin(WinEvent event) {
		if (MoveType == 1) {
			WinsType1++;
			if (useAlternateMove1) WinsType1A++;
		}
		else if (MoveType == 2)
			WinsType2++;
		else if (MoveType == 3)
			WinsType3++;
		else if (MoveType == 4)
			WinsType4++;
	}
	
	//******************************************************************************************/
	// onDeath: shit
	//******************************************************************************************/
	public void onDeath(DeathEvent event) {
		if (numOthers == 1)
			debug(target.name + " has " + (double)Math.round(otherEnergy*10)/10 + " energy left at time " + getTime());
		Statistik();
	}
	
	//******************************************************************************************/
	// onSkippedTurn
	//******************************************************************************************/
	public void onSkippedTurn(SkippedTurnEvent event) {
		if (getOthers() > 0 && getTime() > 10)
			debug("Skipped Turn at time " + getTime());
	}

	/******************************************************************************************
	 * custon events
	 ******************************************************************************************
	 */
	public void onCustomEvent(CustomEvent e) {
		if (getOthers() != 0) {
			if (e.getCondition() instanceof RadarTurnCompleteCondition)
				setRadar();
		}
	}
	/******************************************************************************************
	 * if (debug) out.println(...)
	 ******************************************************************************************
	 */
	private void debug(String s) {
		if (debug) out.println(s);
	}
	
	/******************************************************************************************
	 * testCorner: test for opponents in the corner
	 * @return force
	 ******************************************************************************************
	 */
	private double testCorner(double force, double x, double y) {
		double forceSub = 0;
		Enemy en;
    	Enumeration e = targets.elements();
	    //enemies
		while (e.hasMoreElements()) {
    	    en = (Enemy)e.nextElement();
			if (en.live) {
				//force -= (getEnergy() - en.energy*10) * 10/getRange(x, y, en.x, en.y);
				forceSub += 20000/Math.pow(getRange(x, y, en.x, en.y), 1.1);
			}
	    }
	    if (forceSub > force)
	    	forceSub = force;
	    force -= forceSub;
	    //out.println(getTime() + " " + force);
	    return(force);
	}
	
	/******************************************************************************************
	 * doCornerJiggle: Jiggles in the corner
	 ******************************************************************************************
	 */
	private void doCornerJiggle(int CornerNr) {
		double StartX = getWidth() + 200;
		double StartY = getHeight();
		double CornerTurn = 90;
		double Distance = 333;
		double Turn1 = getWidth() + 120;
		double Turn2 = getHeight() + 120;

		switch(CornerNr) {
			case 1: //Bottom-Left
				CornerTurn *= -1;
				break;
			case 2: //Top-Left
				StartY = fieldHeight - StartY;
				Turn2 = fieldHeight - Turn2;
				break;
			case 3: //Bottom-Right
				StartX = fieldWidth - StartX;
				Turn1 = fieldWidth - Turn1;
				break;
			case 4: //Top-Right
				CornerTurn *= -1;
				StartX = fieldWidth - StartX;
				StartY = fieldHeight - StartY;
				Turn1 = fieldWidth - Turn1;
				Turn2 = fieldHeight - Turn2;
				break;
		}

		setMaxVelocity(8);
		
		switch (CornerStep) {
			case 0: //move to startpoint
				if (Math.abs(getX() - StartX) < 1 && Math.abs(getY() - StartY) < 1) {
					setMove(normalRelativeAngle(90 - getHeading()), 0);
					CornerStep++;
				}
				else
					setToXY(StartX, StartY, 0);
				break;
			case 1: //start to corner
			    if (Math.abs(getHeading() - 90) < .01) {
					setBack(Distance*(CornerNr>2?-1:1));
					CornerStep++;
			    }
				else if (Math.abs(getHeading() - 270) < .01) {
					setAhead(Distance*(CornerNr>2?-1:1));
					CornerStep++;
			    }
			    else
			    	setMove(normalRelativeAngle(90 - getHeading()), 0);
				break;
			case 2: //turn
				if (getX()*(CornerNr>2?-1:1) - Turn1*(CornerNr>2?-1:1) < 0) {
					setTurnLeft(CornerTurn);
					CornerStep++;
				}
				break;
			case 3: //set back
				if (getDistanceRemaining() == 0)
				    if (Math.abs(getHeading()) < .01) {
						setBack(Distance*(CornerNr%2==0?-1:1));
						CornerStep++;
				    }
					else if (Math.abs(getHeading() - 180) < .01) {
						setAhead(Distance*(CornerNr%2==0?-1:1));
						CornerStep++;
					}
				break;
			case 4: //turn
			//
				if (getY()*(CornerNr%2==0?-1:1) - Turn2*(CornerNr%2==0?-1:1) < 0) {
					setTurnRight(CornerTurn);
					CornerStep++;
				}
				break;
			case 5: //to step 1
				if (getDistanceRemaining() == 0)
					CornerStep = 1;
		}
	}
	
	
	/******************************************************************************************
	 * Statistik: prints out statistic onWin or onDeath
	 ******************************************************************************************
	 */
	private void Statistik() {
		if (winStat.length() < MoveStat.length()) {
			if (HitWallCounter > 0)
				debug ("I hit the wall " + HitWallCounter + " times.");
			StatistikSurvivalPoints();	
			if (MoveType == 1)
				myScore1 += (int)myRoundScore;
			else if (MoveType == 2)
				myScore2 += (int)myRoundScore;				
			else if (MoveType == 3)
				myScore3 += (int)myRoundScore;				
			else if (MoveType == 4)
				myScore4 += (int)myRoundScore;				
			if (numOthers==1)
				debug("system: " + getName() + " (" + (int)myRoundScore + " points)");
			debug("-------------------------");
			debug("Typ Hit Miss Else Fired Percent (Eco-Faktor: " + getEcoFaktor() + ")");
			if ((numOthers==1 || getOthers() < 2) && !oVo_RamAlways) {
				if (numBulletsFired[1] > 0) StatistikHits("T1",1, "% (circular)");
				if (numBulletsFired[2] > 0) StatistikHits("T2",2, "% (circular history)");
				if (numBulletsFired[3] > 0) StatistikHits("T3",3, "% (linear)");
				if (numBulletsFired[4] > 0) StatistikHits("T4",4, "% (predictiv average speed)");
			}
			StatistikHits("TG",0, "%");
			//debug("TG:" + numBulletsHit[0]  + "/" + numBulletsMiss[0]  + "/" + numBulletsElse[0]  + "/" + numBulletsFired[0]  + "=" + HitPercent(0) + "%");
			debug("-------------------------");
	
			if (getOthers() < 9)
				winStat += (getOthers() + 1);
			else
				winStat += "-";		
				if (numOthers==1)
					debug("Move    : " + MoveStat);
			StatistikWinStat(winStat);
			if (getRoundNum() + 1 == getNumRounds() && numOthers == 1) {
				if (RoundsType2 > 0) {
					debug("M1: " + RoundsType1 + "/" + WinsType1 + " " +
						(int)myScore1 + "/" + (int)(myScore1/RoundsType1));
					debug("M2: " + RoundsType2 + "/" + WinsType2 + " " +
						(int)myScore2 + "/" + (int)(myScore2/RoundsType2));
				}
				if (RoundsType3 > 0) {
					debug("M3: " + RoundsType3 + "/" + WinsType3 + " " +
						(int)myScore3 + "/" + (int)(myScore3/RoundsType3));
				}
				if (RoundsType4 > 0) {
					debug("M4: " + RoundsType4 + "/" + WinsType4 + " " +
						(int)myScore4 + "/" + (int)(myScore4/RoundsType4));
				}
				debug("MG: " + (RoundsType1 + RoundsType2 + RoundsType3 + RoundsType4) +
					"/" + (WinsType1 + WinsType2 + WinsType3 + WinsType4) + " " +
					(int)(myScore1 + myScore2 + myScore3 + myScore4) + "/" + 
					(int)((myScore1 + myScore2 + myScore3 + myScore4)/
					(RoundsType1 + RoundsType2 + RoundsType3 + RoundsType4)));
				writeBotFile();
			}
		}
	}
	
	/******************************************************************************************
	 * StatistikHits: prints out hit-statistic
	 ******************************************************************************************
	 */
	private void StatistikHits(String s1, int type, String s2) {
		debug(s1 +
			  formatStringVal(String.valueOf(numBulletsHit[type]),5) +  
			  formatStringVal(String.valueOf(numBulletsMiss[type]),5) +
			  formatStringVal(String.valueOf(numBulletsElse[type]),5)+
			  formatStringVal(String.valueOf(numBulletsFired[type]),6) + "  =" +
			  formatStringVal(String.valueOf(HitPercent(type)),4) +
			  s2);
	}
	/******************************************************************************************
	 * Statistik Survival Points
	 ******************************************************************************************
	 */
	private void StatistikSurvivalPoints() {
		Enumeration e = targets.elements();
		Enemy en;
		debug("-------------------------");
		while (e.hasMoreElements()) {
			en = (Enemy)e.nextElement();
			if (en.live) {
				en.survive++;
				en.survivalPoints += numOthers * 50 *
									((double)(numOthers-getOthers()+1)/numOthers /2+.5);
				if (getOthers() == 1) {
					en.survivalPoints += 10 * numOthers;
					en.finalistWinner++;
				}
				if (getOthers() == 2)
					en.finalist++;
			}

			if (numOthers != 1) {
				debug(formatStringVal(String.valueOf(en.survivalPoints), 6) +
					  formatStringVal(String.valueOf(en.finalistWinner), 3) +
					  formatStringVal(String.valueOf(en.finalistLooser), 3) +
					  formatStringVal(String.valueOf(en.finalist), 3) +
					  formatStringVal(String.valueOf(((double)Math.round(en.hitByBulletDamage*10))/10), 6) +
					  formatStringVal(String.valueOf(((double)Math.round(en.totalHitByBulletDamage*10))/10), 7) +
					  //(en.live?formatStringVal(String.valueOf(en.survive), 3):"  ") +
					  "  " + en.name);
			}
		}
	}

	/******************************************************************************************
	 * StatistikWinStat: Counts the places
	 ******************************************************************************************
	 */
	private void StatistikWinStat(String s) {
		int place[] = new int[11];
		int maxPlace = 0;
		int maxPos = 0;
		int Stelle = 0;
		double overall = 0;
		String sout[] = new String[11];
		for (int i=0; i<s.length(); i++) {
			int n = Character.getNumericValue(s.charAt(i));
			if (n==-1) n=10;
			if (++place[n] > maxPos) maxPos = place[n];
			if (n>maxPlace) maxPlace = n;
			overall += n;
		}
		
		while (maxPos>0) {
			Stelle++;
			String rets = "";
			int imax = numOthers+2;
			if (numOthers > 9) imax = 11;
			for (int i=1; i<imax; i++) {
				if (place[i]>0)
					rets = rets.concat(String.valueOf(place[i]%10));
				else if (Stelle==1)
					rets = rets.concat("-");
				else
					rets = rets.concat(" ");
				place[i]/=10;
			}
			maxPos /= 10;
			sout[Stelle] = rets;
		}
		
		debug("Survival: " + winStat);
		debug("Places  : " + sout[Stelle] + "  >>>  Avg. " + (double)Math.round(overall/s.length()*10)/10);
		while (--Stelle>0) debug("          " + sout[Stelle]);
		success = (double)Math.round((numOthers+1-(overall/s.length()))*1000 / numOthers)/10;
		debug("Success : " + success + "%");
	}
	/******************************************************************************************
	 * HitPercent: how many percent of FireType hit
	 * @return percent
	 ******************************************************************************************
	 */
	private long HitPercent (int Type) {
		long z = 0;
		long n = 0;
		if (Type < 6) {
			z = numBulletsHit[Type];
			n = z + numBulletsMiss[Type];
		}
		if (n == 0) 
			return(0);
		else
			return (100 * z / n);
	}
	
	/******************************************************************************************
	 * InitBot: Initializes the bot at the beginning of a round
	 ******************************************************************************************
	 */
	private void initBot () {
		//define some helping variables
		fieldHeight = getBattleFieldHeight();
		fieldWidth = getBattleFieldWidth();
		centerX = fieldWidth / 2;
		centerY = fieldHeight / 2;
		numOthers = getOthers();
		firstPossibleShoot = (long)(getGunHeat() / getGunCoolingRate());
		
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		//targets = new Hashtable();
		resetHashtableTargets();
		target = new Enemy();
		useAntiGrav = true;
		inCorner1 = false;
		inCorner2 = false;
		inCorner3 = false;
		inCorner4 = false;

		if (getRoundNum() == 0) {
			resetTarget("");
			out.println ("Let the games begin!");
			//if (debug) debug = System.getProperty("user.name").substring(0,8).equals("MSchuste");
			if (oVo_RamAlways) {
				out.println ("RamAlwaysMode");
				out.println ("");
			}
		}
		/*else {
			otherVelocity = 0;
			otherVelocity2 = 0;
			otherScans = 0;
		}*/

		// set to center
		////////////////////////////setToXY(centerX, centerY, 0);
		setTurnGunRight(360);
		setTurnRadarRight(360);
		if (getOthers() > 1) 
			maxSameDirection = 3;
		
		//if (getRoundNum()>4 && getRoundNum()>getNumRounds()/2 && success<45 && workingMove==0)
		//	workingMove = 3;
		
		long bonMove1 = 0;
		long bonMove2 = 0;
		long bonMove3 = 0;
		long bonMove4 = 0;
		if (workingMove == 1)
			bonMove1 = Math.round(getNumRounds()/2.5);
		else if (workingMove == 2)
			bonMove2 = Math.round(getNumRounds()/2.5);
		else if (workingMove == 3)
			bonMove3 = Math.round(getNumRounds()/2.5);
		else if (workingMove == 4)
			bonMove4 = Math.round(getNumRounds()/2.5);

		if (workingMove > 4) {
			MoveType = workingMove;
			MoveStat += workingMove;
		}
		else {
			double fMove1 = (WinsType1/.55 + bonMove1 - RoundsType1)*1000 + (int)(myScore1/RoundsType1);
			double fMove2 = (WinsType2/.60 + bonMove2 - RoundsType2)*1000 + (int)(myScore2/RoundsType2);
			double fMove3 = (WinsType3/.60 + bonMove3 - RoundsType3)*1000 + (int)(myScore3/RoundsType3);
			double fMove4 = (WinsType4/.65 + bonMove4 - RoundsType4)*1000 + (int)(myScore4/RoundsType4);
			if (getOthers() > 1 || (oVo_RamAlways && numOthers==1) ||
				fMove1 >= fMove2 && fMove1 >= fMove3 && fMove1 >= fMove4) {
				MoveType = 1;
				RoundsType1++;
				if (WinsType1A > .6*WinsType1)
					useAlternateMove1 = true;
				//useAlternateMove1 = false; //Test
				actAlternateMove1 = useAlternateMove1;
				if (oVo_RamAlways) MoveStat += "R";
				else if (actAlternateMove1) MoveStat += "I";
				else MoveStat += "1";
			}
			else if (fMove2 >= fMove3 && fMove2 >= fMove4) {
				MoveType = 2;
				RoundsType2++;
				MoveStat += "2";
			}
			else if (fMove3 >= fMove4) {
				MoveType = 3;
				RoundsType3++;
				MoveStat += "3";
			}
			else {
				MoveType = 4;
				RoundsType4++;
				MoveStat += "4";
			}
		}
		//MoveType = 4; //TEST
		
			
		setEventPriority("ScannedRobotEvent",	10); //10
		setEventPriority("HitRobotEvent",		20); //20
		setEventPriority("HitWallEvent",		30); //30
		setEventPriority("HitByBulletEvent",	40); //40
		setEventPriority("BulletHitEvent",		50); //50
		setEventPriority("BulletHitBulletEvent",50); //50
		setEventPriority("BulletMissedEvent",	60); //60
		setEventPriority("RobotDeathEvent",		70); //70
		addCustomEvent(new RadarTurnCompleteCondition(this));

		if (ColorNormal == false) {
			if (oVo_RamAlways)
				setColors(Color.pink, Color.red, Color.black);
			else
				setColors(Color.pink, Color.yellow, Color.orange); /* black, blue, cyan, darkGray, gray, green, lightGray, magenta, orange, pink, red, white, yellow */	
			//setColors(Color.magenta, Color.magenta, Color.magenta);
			ColorNormal = true;
			ColorSet = true;
		}
		else
			ColorSet = false;
	}

	/******************************************************************************************
	 * move
	 ******************************************************************************************
	 */
	private void move() {
		CornerStep = 0;
		/** determine wether to ram or not **/
		/*if ((getOthers() == 1 && Target.getEnergy() <  3.8 &&
			(Target.getEnergy() < .1 || Target.getEnergy() + 100 < getEnergy()) || getEnergy() - target.energy > 50 && getTime()-target.lastFireTime > firstPossibleShoot)
			||(Target.getEnergy() <= 1 && Target.getEnergy() + 25 < getEnergy() && ramStartTime != 0))*/
		if ((getOthers() == 1 && (Target.getEnergy() < 3.8 && getEnergy() > 50 || Target.getEnergy() < .1) && getTime()-target.lastFireTime > 5 && getTime() - lastFireTime > 20)
			|| ramStartTime < getTime() && Target.getEnergy() <= 10 && Target.getEnergy() + 25 < getEnergy())

			if(ramStartTime == 0) {
				ramStartTime = target.lastFireTime + (long)(target.distance/11 + 30);
				ramName = Target.getName();
			}
			else {} //needed
		else
			ramStartTime = 0;
		
		/** ram **/
		if (ramStartTime != 0 && ramStartTime < getTime() && ramName.compareTo(Target.getName()) == 0
			|| oVo_RamAlways && numOthers==1
			|| getTime() < firstPossibleShoot && getOthers()==1 && Target.getDistance() > 300 && MoveType < 3 && moveToTargetAtRoundStart) {
			double Bearing = Target.getBearing();
			double hisHeading = Target.getHeading();
			double hisAngel   = Target.getBearing() + getHeading();
			double hisSpeed   = Target.getVelocity();
			Bearing += (90 - Math.abs(normalRelativeAngle(hisHeading - hisAngel)-90)) / 20 * hisSpeed;
			if (getTime() < firstPossibleShoot)
				setMove(Bearing, Math.min(Target.getDistance(), (firstPossibleShoot - getTime()) * 8));
			else {
				ram = true;
				setMove(Bearing, Target.getDistance());
			}
			//setMaxVelocity(8);
		}
		else { /** normal movement **/
			moveToTargetAtRoundStart = false;
			if (MoveType == 1)
				moveType1Rotation();
			else if (MoveType == 2)
				moveType2();
			else if (MoveType == 3)
				moveType3();
			else if (MoveType == 4)
				moveType4();
			/** if near - circle around enemy**/
			if (ScanDistance <= 80 || getOthers() > 1) {
				sameDirection = sameDirection + 1;
				if (TurnSometimes && sameDirection >= maxSameDirection + 5 && ScanDistance > 100) {
					turnDirection();
				}
				//if (ScanDistance > 100)
					setMaxVelocity(getVelocityMax(8));
					setAhead(50 * direction);
				//else
				//	setMaxVelocity(getVelocityMax(5.5));
				//setAhead((moveDistance - 2 + 2 * getOthers()) * direction);
			}
			else if (Math.abs(getTurnRemaining()) > 45 && Math.abs(getDistanceRemaining()) < 20)
				setMaxVelocity(getVelocityMax(3));
			else
				setMaxVelocity(getVelocityMax(8));
			if (MoveType == 2 || MoveType == 3)
				//setMaxVelocity(getVelocityMax(8));
				setMaxVelocity(getVelocityMax(8-random.nextDouble()/5));
		}

		if (mustDodgeTime <= getTime() && mustDodgeTime != 0) {
			setAhead(moveDistance * direction);
			sameDirection = sameDirection + 1;
			mustDodgeTime = 0;
		}
	}

	/******************************************************************************************
	 * moveType1
	 ******************************************************************************************
	 */
	private void moveType1Rotation() {
		ram = false;
		
		double Angel = Target.getBearing() + 90 + wiggle(10);
		if (MoveType == 2)
				Angel = Target.getBearing() + 90 + wiggle2(5);

		if (Math.abs(Math.abs(Target.getBearing())-90) < 40) {
			double toTargetFaktor = 10;
			if (getEnergy()-5 < Target.getEnergy() && ScanDistance < 100)
				toTargetFaktor = -20;
			else if (getEnergy()+16 < Target.getEnergy()) {
				toTargetFaktor *= -.1;
				if (ScanDistance < 100)
					toTargetFaktor = -15;
				else if (ScanDistance < 150)
					toTargetFaktor = -10;
				else if (ScanDistance < 200)
					toTargetFaktor = -5;
				else if (ScanDistance < 300)
					toTargetFaktor = -3;
				toTargetFaktor *= (1D - (1D / ((Target.getEnergy() - getEnergy()) / getEnergy())));
			}
			else if (wallDistance(getX(), getY()) > 40 && useAlternateMove1 && numOthers==1 && ScanDistance > 300)
				toTargetFaktor = 30;
			else if ((MoveType == 1 && ScanDistance < 250) || (MoveType == 2 && ScanDistance < 100))
				toTargetFaktor = -5;
				
			/*if (normalRelativeAngle(Math.abs(Angel + 30 * direction)) > 90)
				if (Target.getBearing()>0)
					Angel -= toTargetFaktor * direction;
				else
					Angel += toTargetFaktor * direction;
			else*/
				//out.println(toTargetFaktor);
				if (Target.getBearing()>0)
					Angel += toTargetFaktor * direction;
				else
					Angel -= toTargetFaktor * direction;
		}

		double d = avoidWall(Angel);
		if (((Math.abs(Target.getBearing()) <= 45 && direction == ahead) ||
			(Math.abs(Target.getBearing()) >= 135 && direction == back)) &&
			wallDistance(getX(), getY()) < 75) {
			turnDirection();
			//debug(getTime() + ": turn direction");
			}
		
		setMove(d, 0);
		if (useFakeSpeed && !useAlternateMove1 && getDistanceRemaining() == 0)
			setBack(11E-03 * direction);
	}

	/******************************************************************************************
	 * moveType2
	 ******************************************************************************************
	 */
	private void moveType2() {
		moveType1Rotation();
		if (nextTurnTime - getTime() < 0) {
			nextTurnTime = getTime() + (long) (Math.random() * 15 + 10);
			if (random.nextDouble() < .9)
				turnDirection();
		}
		setAhead(100*direction);
	}

	/******************************************************************************************
	 * moveType3
	 ******************************************************************************************
	 */
	private void moveType3() {

		//out.println(getTime() + " - M3");
		if (getDistanceRemaining() == 0)
		{
			turnDirection();
			double distance = (Math.random()*60 + 120) * direction;
			setAhead(distance);
		}

		double dh = -(10 - 0.75 * getVelocity());

		double v = (getVelocity()+1) * direction;

		double predPosX = getX() + v * Math.sin(Math.toRadians(getHeading() + dh));
		double predPosY = getY() + v * Math.cos(Math.toRadians(getHeading() + dh));
		double leftGoodnessAhead = evaluatePosition(predPosX, predPosY, x, y, (getHeading() + dh), Target.getHeading());

		predPosX = getX() + v * Math.sin(Math.toRadians(getHeading() - dh));
		predPosY = getY() + v * Math.cos(Math.toRadians(getHeading() - dh));
		double rightGoodnessAhead = evaluatePosition(predPosX, predPosY, x, y, (getHeading() - dh), Target.getHeading());
		
		v = (getVelocity()-2) * direction;

		predPosX = getX() + v * Math.sin(Math.toRadians(getHeading() + dh));
		predPosY = getY() + v * Math.cos(Math.toRadians(getHeading() + dh));
		double leftGoodnessBack = evaluatePosition(predPosX, predPosY, x, y, (getHeading() + dh), Target.getHeading());

		predPosX = getX() + v * Math.sin(Math.toRadians(getHeading() - dh));
		predPosY = getY() + v * Math.cos(Math.toRadians(getHeading() - dh));
		double rightGoodnessBack = evaluatePosition(predPosX, predPosY, x, y, (getHeading() - dh), Target.getHeading());

		//out.println(leftGoodnessAhead + " " + rightGoodnessAhead + " " + leftGoodnessBack + " " + rightGoodnessBack);

		if ((leftGoodnessAhead<0 || rightGoodnessAhead<0 || leftGoodnessBack<0 || rightGoodnessBack<0) &&
			Math.max(leftGoodnessBack, rightGoodnessBack) > Math.max(leftGoodnessAhead, rightGoodnessAhead)) {
			turnDirection();
			double distance = (Math.random()*60 + 120) * direction;
			setAhead(distance);
		}
		
		
		if (Math.max(leftGoodnessAhead, leftGoodnessBack) > Math.max(rightGoodnessAhead, rightGoodnessBack))
			setTurnLeft(360);
		else
			setTurnRight(360);
	}

	/******************************************************************************************
	 * moveType4
	 ******************************************************************************************
	 */
	private void moveType4() {
		double toX, toY;
		toX = (fieldWidth  - x) * .8 + fieldWidth  * .1;
		toY = (fieldHeight - y) * .8 + fieldHeight * .1;
		setToXY(toX, toY, 0);
	}

	/******************************************************************************************
	 * antiGravMove: sets a Move away from the enemies to a corner
	 ******************************************************************************************
	 */
	private void antiGravMove() {
   		double getX = getX();
   		double getY = getY();
   		double getWidth = getWidth();
   		double getHeight = getHeight();
   		long getTime = getTime();
   		long getOthers = getOthers();
   		
   		double xforce = 0;
	    double yforce = 0;
	    double force;
	    double forceSub;
	    double ang;
	    double gpX, gpY;
	    int nearEnemies = 0;
	    int veryNearEnemies = 0;
		Enemy en;
    	Enumeration e = targets.elements();
	    //enemies
		while (e.hasMoreElements()) {
    	    en = (Enemy)e.nextElement();
			if (en.live) {
				double d = getRange(getX,getY,en.x,en.y);
		        force = -1000/Math.pow(d,2);
		        ang = normalizeAngle(Math.PI/2 - Math.atan2(getY - en.y, getX - en.x)); 
		        xforce += Math.sin(ang) * force;
		        yforce += Math.cos(ang) * force;
		        if (d<150)
		        	nearEnemies++;
		        if (d<50)
			        veryNearEnemies++;
			}
	    }
	    
		//Corners
		double cornerDistanceX = getWidth  * 2.5; //2.5
		if (getTime%100>50)
			cornerDistanceX += 100-getTime%100;
		else
			cornerDistanceX += getTime%100;
		double cornerDistanceY = getHeight * 2.5; //2.5
		double cornerStrength = getRange(0, 0, fieldWidth / 3, fieldHeight/3);
		//Bottom-Left
		gpX = cornerDistanceX;
		gpY = cornerDistanceY;
		forceSub = testCorner(cornerStrength, 50, 50);
		force = forceSub / Math.pow(getRange(getX,getY,gpX,gpY), 1.5);
	    ang = normalizeAngle(Math.PI/2 - Math.atan2(getY - gpY, getX - gpX)); 
		if (getRange(getX, getY, gpX, gpY) > 100 &&
			(force < .4 || forceSub < cornerStrength/3)) {
	        xforce += Math.sin(ang) * force;
    	    yforce += Math.cos(ang) * force;
    	    //out.println(getTime + " added corner 1 " + forceSub);
		}
		else
			inCorner1 = true;
		if (getRange(getX, getY, gpX, gpY) > 100 && getOthers>1 &&
			(force < .2 || forceSub < cornerStrength/2))
			inCorner1 = false;
		//Top-Left
		gpX = cornerDistanceX;
		gpY = fieldHeight - cornerDistanceY;
		forceSub = testCorner(cornerStrength, 50, fieldHeight-50);
		force = forceSub / Math.pow(getRange(getX,getY,gpX,gpY), 1.5);
	    ang = normalizeAngle(Math.PI/2 - Math.atan2(getY - gpY, getX - gpX)); 
		if (getRange(getX, getY, gpX, gpY) > 100 &&
			(force < .4 || forceSub < cornerStrength/3)) {
	        xforce += Math.sin(ang) * force;
    	    yforce += Math.cos(ang) * force;
    	    //out.println(getTime + " added corner 2 " + forceSub);
		}
		else
			inCorner2 = true;
		if (getRange(getX, getY, gpX, gpY) > 100 && getOthers>1 &&
			(force < .2 || forceSub < cornerStrength/2))
			inCorner2 = false;
		//Bottom-Right
		gpX = fieldWidth - cornerDistanceX;
		gpY = cornerDistanceY;
		forceSub = testCorner(cornerStrength, fieldWidth-50, 50);
		force = forceSub / Math.pow(getRange(getX,getY,gpX,gpY), 1.5);
	    ang = normalizeAngle(Math.PI/2 - Math.atan2(getY - gpY, getX - gpX)); 
		if (getRange(getX, getY, gpX, gpY) > 100 &&
			(force < .4 || forceSub < cornerStrength/3)) {
	        xforce += Math.sin(ang) * force;
    	    yforce += Math.cos(ang) * force;
    	    //out.println(getTime + " added corner 3 " + forceSub);
		}
		else
			inCorner3 = true;
		if (getRange(getX, getY, gpX, gpY) > 100 && getOthers>1 &&
			(force < .2 || forceSub < cornerStrength/2))
			inCorner3 = false;
		//Top-Right
		gpX = fieldWidth - cornerDistanceX;
		gpY = fieldHeight - cornerDistanceY;
		forceSub = testCorner(cornerStrength, fieldWidth-50, fieldHeight-50);
		force = forceSub / Math.pow(getRange(getX,getY,gpX,gpY), 1.5);
	    ang = normalizeAngle(Math.PI/2 - Math.atan2(getY - gpY, getX - gpX)); 
		if (getRange(getX, getY, gpX, gpY) > 100 &&
			(force < .4 || forceSub < cornerStrength/3)) {
	        xforce += Math.sin(ang) * force;
    	    yforce += Math.cos(ang) * force;
    	    //out.println(getTime + " added corner 4 " + forceSub);
		}
		else
			inCorner4 = true;
		if (getRange(getX, getY, gpX, gpY) > 100 && getOthers>1 &&
			(force < .2 || forceSub < cornerStrength/2))
			inCorner4 = false;
		//out.println(force);*/

	    //not to center
		gpX = centerX;
		gpY = centerY;
		double centerForce = getRange(0, 0, fieldWidth / 15, fieldHeight/15);
		force = -centerForce/Math.pow(getRange(getX,getY,gpX,gpY),1.5);
	    ang = normalizeAngle(Math.PI/2 - Math.atan2(getY - gpY, getX - gpX)); 
        xforce += Math.sin(ang) * force;
        yforce += Math.cos(ang) * force;

	
	    //wall
	    xforce += 5000/Math.pow(getRange(getX, getY, fieldWidth, getY), 3);
	    xforce -= 5000/Math.pow(getRange(getX, getY, 0, getY), 3);
	    yforce += 5000/Math.pow(getRange(getX, getY, getX, fieldHeight), 3);
	    yforce -= 5000/Math.pow(getRange(getX, getY, getX, 0), 3);
	    
		if (useAntiGrav)
			if (Math.abs(xforce)<.02 && Math.abs(yforce)<.02)
				useAntiGrav = false;
			else {}
		else
			if (Math.abs(xforce)>.05 || Math.abs(yforce)>.05)
				useAntiGrav = true;
		
		if ((useAntiGrav && !inCorner1 && !inCorner2 && !inCorner3 && !inCorner4) /*||
			nearEnemies>1 && HitTime2 != 0 ||
			veryNearEnemies>0 && HitTime1 != 0*/) {
				CornerStep = 0;
				setToXY(getX - xforce, getY - yforce, 20);
			}
		else {
			if ((inCorner1 | inCorner2 | inCorner3 | inCorner4) &&
				(nearestEnemy(getX, getY).distance > 400 || nearestEnemy(getX, getY).distance > 200 && CornerStep != 0))
				doCornerJiggle(inCorner1?1:inCorner2?2:inCorner3?3:inCorner4?4:4);
			else {
				CornerStep = 0;
				if (HitTime1 - HitTime2 > HitTimeTurn
					|| HitTime2 == 0
					|| ScanDistance < 150)
					CornerBehaviour = 2;
				if (nearEnemies != 0)
					CornerBehaviour = 1;
				if (CornerBehaviour == 1) {
					if (getTime%19 == 0) {
					//if (nextTurnTime - getTime < 0) {
						//nextTurnTime = getTime + (long) (Math.random() * 15 + 10);
						turnDirection();
					}
					setAhead(direction * 300);
					//setTurnRightRadians(bearRad + (PI/2));
					setTurnRightRadians(avoidWall(bearRad + (PI/2)));
				}
				else {
					if (Target != null)
						moveType3();
				}
			}
		}
		setMaxVelocity(getVelocityMax(8));	
	}
	/******************************************************************************************
	 * evaluates a Position
	 ******************************************************************************************
	 */
	private double evaluatePosition(double predPosX, double predPosY, double tX, double tY, double robotHeading, double targetHeading)
	{
		final double minDistance = 200.0;
		final double minWallDistance = 50.0;
		final double maxWallDistance = 200.0;
		final double wallWeight = 3.0;
		final double bearingWeight = 1.0;

		double dX = tX - predPosX;
		double dY = tY - predPosY;
		double distance = getRange(predPosX, predPosX, tX, tY);

		//wall min distance
		double wallGoodnessX = 0;
		double wallGoodnessY = 0;

		if (predPosX < minWallDistance) wallGoodnessX = -wallWeight * (minWallDistance - predPosX);
		if (predPosY < minWallDistance) wallGoodnessY = -wallWeight * (minWallDistance - predPosY);
		if (predPosX > (getBattleFieldWidth() - minWallDistance)) wallGoodnessX = -wallWeight * (minWallDistance + predPosX - getBattleFieldWidth());
		if (predPosY > (getBattleFieldHeight() - minWallDistance)) wallGoodnessY = -wallWeight * (minWallDistance + predPosY - getBattleFieldHeight());

		//wall max distance
		double maxWallGoodness = 0;
		if (getOthers() > 2) {
			double wD = wallDistance(predPosX, predPosY);
			if (wD > maxWallDistance)
				maxWallGoodness = (wD - maxWallDistance) / 3;
		}
		
		//target
		double targetGoodness = 0;
		if (distance < minDistance)
			targetGoodness = minDistance - distance;

		//bearing
		double bearing_deg = normalRelativeAngle(robotHeading-Math.toDegrees(Math.atan2(dX,dY)));
		double bearingGoodness = bearingWeight * Math.abs(Math.abs(Math.abs(bearing_deg) - 90) -90);

		return maxWallGoodness + wallGoodnessX + wallGoodnessY + targetGoodness + bearingGoodness;
	}

	/******************************************************************************************
	 * set move
	 ******************************************************************************************
	 */
	private void setMove(double Angel, double Distance) {
		Angel = normalRelativeAngle(Angel);
		if (Math.abs(Angel) > 90) {
			setTurnLeft((normalRelativeAngle(Angel + 180)) * (-1) );
	    	if (getOthers() != 1 || ram || MoveType!=1 || Distance!=0)
				setBack(Distance);
		}
		else {
			setTurnRight(Angel);
	    	if (getOthers() != 1 || ram || MoveType!=1 || Distance!=0)
				setAhead(Distance);
		}
		if (Math.abs(getDistanceRemaining()) > 300)
			setMaxVelocity(getVelocityMax(8 - Math.abs(getTurnRemaining()/5)));
		else
			setMaxVelocity(getVelocityMax(8 - Math.abs(getTurnRemaining()/7)));
		
	}	

	/******************************************************************************************
	 * set move To Coordinates x, y
	 ******************************************************************************************
	 */
	private void setToXY(double x, double y, int minDistance) {
		double c;
		double h = Math.sqrt(Math.pow((getX() - x), 2) + Math.pow((getY() - y),2));
		double t = 0;
		if (h != 0)
			t = (((Math.asin(((Math.abs(getY() - y)) / h))) / PI) * 180);
    	if (getX() <= x && getY() <= y)
			c = 90 - t;
	    else if (getX() <= x && getY() >= y)
			c = 90 + t;
    	else if (getX() >= x && getY() >= y)
			c = 270 - t;
	    else
			c = 270 + t;		
		setMove(c-getHeading(), minDistance > h ? minDistance : h);
	}	

	/******************************************************************************************
	 * normalize angel
	 * @return angle (-180 to 180)
	 ******************************************************************************************
	 */
	private double normalRelativeAngle(double a) {
		while (a > 180)
			a -= 360;
		while (a <= -180)
			a += 360;
		return a;
	}

	/******************************************************************************************
	 * normalize radian angel
	 * @return angel (-PI to PI)
	 ******************************************************************************************
	 */
	private double normalizeAngle( double r ) {
		while(r > PI)
			r -= 2 * PI;
		while (r <- Math.PI)
		 	r += 2 * PI;
		return r;
	}

	/******************************************************************************************
	 * calculates an angel between two coordinates
	 ******************************************************************************************
	 */
	private double calcAngle (double x1, double y1, double x2, double y2) {
		double dX = x2 - x1;
		double dY = y2 - y1;
		double direction = 0;
		//calculate direction
		if (dX == 0) {
			if (dY < 0) {direction = 180;} else {direction = 0;}
		} else {
			direction = 360-toDegrees(Math.atan(dY / dX))%360 + 90;
			if (dX < 0) {direction += 180;}
		}
		return direction;
	}

	/******************************************************************************************
	 * toRadians
	 ******************************************************************************************
	 */
	private double toRadians(double d) {
		return (double) (PI * d / 180);
	}

	/******************************************************************************************
	 * toDegrees
	 ******************************************************************************************
	 */
	private double toDegrees(double d) {
		return (double) (180 * d / PI);
	}

	/******************************************************************************************
	 * sets the radar
	 ******************************************************************************************
	 */
	private void setRadar() {
		if (ScanTry --< 1 || getOthers() > 1 &&
			(getGunHeat() > 5*getGunCoolingRate()
			|| nearestEnemy(getX(), getY()).distance < 333
			|| (enScan.name != null && getTime() - enScan.scanTime < 8))) {
				setTurnRadarRight(360 * radarDirection);
		}
		else {
			if (getRadarTurnRemaining() ==0 ) ScanCone*=-1;
			//setTurnRadarRight(normalRelativeAngle(ScanCone + ScanAngel - getRadarHeading()));
			setTurnRadarRight(normalRelativeAngle(ScanAngel - getRadarHeading()) * 1.5); //less than 2 for thin angel
		}
	}

	/******************************************************************************************
	 * turn the radar to the oldest scan
	 ******************************************************************************************
	 */
	private void getBestRadarDirection() {
		Enumeration e = targets.elements();
		Enemy en = new Enemy();
		Enemy oldestEn = new Enemy();
		int scannedEnemies = 0;
		long oldestScanTime = getTime()+1;
		double bearing = 0;
		while (e.hasMoreElements()) {
			en = (Enemy)e.nextElement();
			if (en.live) {
				if (en.energy >= 0)
					scannedEnemies++;
				if (en.scanTime < oldestScanTime) {
					oldestEn = en;
					oldestScanTime = en.scanTime;
				}
			}
		}
		enScan = oldestEn;
		bearing = normalRelativeAngle(calcAngle(getX(), getY(), enScan.x, enScan.y)
				  - getRadarHeading());
		double radarTurn = bearing + sign(bearing) * 22.5;
		if (getOthers() == scannedEnemies)
			radarDirection = sign(radarTurn);
		setTurnRadarRight(360 * radarDirection);
	}

	/******************************************************************************************
	 * sign
	 ******************************************************************************************
	 */
	private int sign(double d) {
		if (d>0)
			return 1;
		else
			return -1;
			
	}
	/******************************************************************************************
	 * set the gun
	 ******************************************************************************************
	 */
	private void setGun() {
		if (Target != null) {
			setGunPower();
			//gunPower=.1;
			if (gunPower >= .1)	{
				getBestFireType();
				//FireType = 2;
				switch (FireType) {
					case 1: //circular
						circularAiming();
						break;
					case 2: //circular history
						otherHeadDiff *= .1;
						//speed = getAvgSpeed(true);
						if (sign(target.lsVelocityB) == sign(target.velocity))
							speed = target.velocityB;
						else
							speed = -target.velocityB;
						circularAiming();
						break;
					case 3:  //linear
						setTurnGunRight(normalRelativeAngle(getHeading() + Target.getBearing() - getGunHeading()));
						break;
					case 4: //predictiv average speed
						speed = getAvgSpeed(false);
						otherHeadDiff = 0;
						circularAiming();
						//predictivAiming(speed);
						break;
					case 5: //predictiv
						predictivAiming(Target.getVelocity());
						break;
				}
				gunLoad = true;
			}
			else
				setTurnGunRight(normalRelativeAngle(getHeading() + Target.getBearing() - getGunHeading()));
			
		}
	}

	/******************************************************************************************
	 * set the gun power
	 ******************************************************************************************
	 */
	private void setGunPower() {
		
		if (Target.getDistance() < 250)
			gunPower = Math.min(3, getEnergy() - .2);
		else {
			if (target.oVo_workingFire > 0 && getOthers() == 1)
				gunPower = Math.min(target.oVo_workingFire, getEnergy() - .2);
			else {
				if (getEnergy()<=50 && MoveType==1)
					gunPower = Math.min(Math.max(Math.min(3, 4.3 - Target.getDistance()/200), .1), getEnergy() - .2);
				else
					gunPower = Math.min(Math.min(3, Math.max(4.3 + getEcoFaktor() - Target.getDistance()/200 - getOthers()/10, .1)), getEnergy() - .2);
				if (getOthers() > 4 && getEnergy() > 50)
					gunPower = Math.max(3, gunPower + (getOthers()-4)/10);
				if (getEnergy() <= 3)
					gunPower = Math.min(.1, getEnergy() - .2);
			}
		}
		if (ScanDistance > 200 || target.energy < .1 || ramStartTime < getTime())
			if (getOthers() == 1 && tryToDisable && !oVo_RamAlways &&
				(getEnergy() > 50 || getEnergy() > 16 && target.energy < .1) &&
				(startToDisable - getEnergy() < 3 || startToDisable == 0))
				gunPower = Math.min(gunPower, getBulletDisablePower(target));			
			else if (tryToDisable && startToDisable - getEnergy() > 20 && startToDisable != 0) {
		        tryToDisable = false;
		        debug("Disable disabled...");
		}
	}

	/******************************************************************************************
	 * what is the best fire type???
	 ******************************************************************************************
	 */
    private void getBestFireType() {
		int maxf3 = 25 * (getRoundNum() + 1);
		int maxf4 = 25 * (getRoundNum() + 1);
		double RoundFaktor = (double)(getRoundNum() + 1) / getNumRounds();
		int f1 = (int)Math.round(numBulletsHit[1] * 2.5 - numBulletsMiss[1] - (numBulletsBreak[1]>4?10*numBulletsBreak[1]:-HitPercent(1) * RoundFaktor));
		int f2 = (int)Math.round(numBulletsHit[2] * 2.5 - numBulletsMiss[2] - (numBulletsBreak[2]>4?10*numBulletsBreak[2]:-HitPercent(2) * RoundFaktor + Math.min(3, getOthers()-1)));
		int f3 = (int)Math.round(numBulletsHit[3] * 2.5 - numBulletsMiss[3] - (numBulletsBreak[3]>4?10*numBulletsBreak[3]:-HitPercent(3) * RoundFaktor));
		int f4 = (int)Math.round(numBulletsHit[4] * 2.5 - numBulletsMiss[4] - (numBulletsBreak[4]>4?10*numBulletsBreak[4]:-HitPercent(4) * RoundFaktor));
		OverallFire = f1 + f2 + f3 + f4;
		int lastFireType = FireType;

		if (oVo_RamAlways)
			FireType = 1;
		else if (target.oVo_workingShot > 0 && getOthers() == 1)
			FireType = target.oVo_workingShot;
		else {
			if (f1 >= f2 && (f1 >= f3  || f3 <= -maxf3) && (f1 >= f4 || f4 <= -maxf4) && ScanTime != 0)
				FireType = 1;
			else if ((f2 >= f3  || f3 <= -maxf3) && (f2 >= f4 || f4 <= -maxf4))
				FireType = 2;
			else if (f3 >= f4 || f4 <= -maxf4)
				FireType = 3;
			else
				FireType = 4;
		}
    }

	/******************************************************************************************
	 * Eco-Shooting
	 ******************************************************************************************
	 */
	private double getEcoFaktor() {
		double ecoFaktor = (double)OverallFire/((double)getRoundNum()+1)/20;
		if (ecoFaktor > -1 || target.energy>getEnergy())
			return(ecoFaktor);
		else
			return(-1);
	}

	/******************************************************************************************
	 * get the power needed to disable a bot
	 ******************************************************************************************
	 */
    private double getBulletDisablePower(Enemy e) {
        double maxDamage = 16;
        if (e.energy <= 16 + e.lastFireEnergy * 5) {
        	maxDamage = e.energy - 3.7;
        	if (maxDamage < .1)
	        	if (getTime() - target.lastFireTime > 17 && getTime() - lastFireTime > 17 && e.energy > .3)
	        		maxDamage = e.energy - .1;
	        	else
	        		maxDamage = 0;
        	
        	if (startToDisable == 0 && maxDamage == 0)
        		startToDisable = getEnergy();
        }
        if (maxDamage>4) return (maxDamage + 2) / 6;
        else return maxDamage / 4;
    }
	/******************************************************************************************
	 * aims predictiv
	 ******************************************************************************************
	 */
	private void predictivAiming(double vpredictiv)
	{
		double hisBearing = Target.getBearingRadians() + Math.toRadians(getHeading());
		double hisHeading = Target.getHeadingRadians();
		double fireAngle = Math.asin(vpredictiv / (20-(3*gunPower)) * Math.sin(hisHeading - hisBearing)) + hisBearing;
		double turnGunAngle = fireAngle - Math.toRadians(getGunHeading());
		setTurnGunRight(Math.toDegrees(normalizeAngle(turnGunAngle)));
	}

	/******************************************************************************************
	 * aims circular
	 ******************************************************************************************
	 */
	private void circularAiming()
	{
		boolean willHitWall = false;
		Point2D.Double p = new Point2D.Double(x, y);
		for (int i = 0; i < 5; i++){
        	p = circularPos(Target.getTime() + (int)Math.round((getRange(getX(),getY(),p.x,p.y) / (20-(3*gunPower)) )));
		}
		if (p.x < target.minWallDistance) {
			p.x = target.minWallDistance;
			willHitWall = true;
		}
		if (p.x > (fieldWidth - target.minWallDistance)) {
			p.x =  fieldWidth - target.minWallDistance;
			willHitWall = true;
		}
		if (p.y < target.minWallDistance) {
			p.y = target.minWallDistance;
			willHitWall = true;
		}
		if (p.y > (fieldHeight - target.minWallDistance)) {
			p.y =  fieldHeight - target.minWallDistance;
			willHitWall = true;
		}	
		double turnGunAngle = getGunHeadingRadians() - (PI/2 - Math.atan2(p.y - getY(), p.x - getX()));
		setTurnGunLeftRadians(normalizeAngle(turnGunAngle));
		
		if (willHitWall && gunPower > 0.1 && !oVo_RamAlways) {
			gunPower = Math.max(.1, gunPower - .5);
			circularAiming();
		}
	}

	/******************************************************************************************
	 * average speed in current round
	 * @return speed
	 ******************************************************************************************
	 */
	private double getAvgSpeed(boolean abs)
	{
		double av = 0;
		if (otherScans > 0)
			if (Target.getVelocity() != 0)
				if (abs)
					av = otherVelocity  / otherScans;
				else
					av = otherVelocity2 / otherScans;
			if (Target.getVelocity() < 0)
				av *= -1;
			if (Target.getVelocity() == 0 && Target.getEnergy() > 0) {
				if (abs)			
					av = otherVelocity  / otherScans;
				else
					av = otherVelocity2 / otherScans;
				if (random.nextDouble() > .5)
					av *= -1;
			}
		else
			av = 0;
		return av;
	}

	/******************************************************************************************
	 * Gets the distance between two x,y coordinates
	 * @return distance
	 ******************************************************************************************
	 */
	private double getRange(double x1, double y1, double x2, double y2)
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = Math.sqrt( xo*xo + yo*yo );
		return h;	
	}
	
	/******************************************************************************************
	 * Gets the distance to nearest wall
	 * @return distance
	 ******************************************************************************************
	 */
    private double wallDistance(double x, double y) {
        double distX = Math.min(fieldWidth - x, x);
        double distY = Math.min(fieldHeight - y, y);
        return Math.min(distY,distX);
    }

	/******************************************************************************************
	 * calculates the circular position
	 * @return point
	 ******************************************************************************************
	 */
	private Point2D.Double circularPos(long HitTime) {
		double t = HitTime - ScanTime;
		double newY, newX;
		if (Math.abs(otherHeadDiff) > 0.0001) {
			double radius = speed / otherHeadDiff;
			double tothead = t * otherHeadDiff;
			newY = y + (Math.sin(headRad + tothead) * radius) - (Math.sin(headRad          ) * radius);
			newX = x + (Math.cos(headRad          ) * radius) - (Math.cos(headRad + tothead) * radius);
		}
		else { //linear
			newY = y + Math.cos(headRad) * speed * t;
			newX = x + Math.sin(headRad) * speed * t;
		}
		return new Point2D.Double(newX, newY);
	}
	
	/******************************************************************************************
	 * calculates a bearing not hitting the wall
	 * @return corrected bearing
	 ******************************************************************************************
	 */
	private double avoidWall(double Bearing) {
		double wd = (Math.max(getWidth(), getHeight())) / 2 + moveDistance; // wall distance
		int ba = 30; // bumper angel

		double a = normalRelativeAngle(getHeading());
		double aa = Math.abs(a);
		double Angel = normalRelativeAngle(Bearing);
		if (Math.abs(Angel) > 45)
			Angel = normalRelativeAngle(Angel + 180);
		double b = normalRelativeAngle(getHeading() + Angel);
		double bb = Math.abs(b);
			
		// turn on top
		if (getY() > fieldHeight - wd) {
			if ((aa < ba && direction == ahead) ||
				(aa > (180 - ba) && direction == back)) {
				turnDirection();
				return Angel;
			}
			else {
				if ((aa <= 90 && bb <= 90 && direction == ahead) ||
					(aa >= 90 && bb >= 90 && direction == back)) {
					if (getX() < wd && // left
						(direction == ahead && a < 0 || (direction == back && a > 0)))
						turnDirection();
					if (getX() > fieldWidth - wd && // right
						(direction == ahead && a > 0 || (direction == back && a < 0)))
						turnDirection();
					Angel = 90 - a;
					if (a != aa) Angel -= 180;
					return normalRelativeAngle(Angel);
				}
			}
		}

		// turn on bottom
		if (getY() < wd) {
			if ((aa > (180 - ba) && direction == ahead) ||
				(aa < ba && direction == back)) {
				turnDirection();
				return Angel;
			}
			else 
				if ((aa >= 90 && bb >= 90 && direction == ahead) ||
					(aa <= 90 && bb <= 90 && direction == back)) {
					if (getX() < wd && // left
						(direction == ahead && a < 0 || (direction == back && a > 0)))
						turnDirection();
					if (getX() > fieldWidth - wd && // right
						(direction == ahead && a > 0 || (direction == back && a < 0)))
						turnDirection();
					if (a <= -90 && b <= -90 && direction == ahead) 
						Angel = aa - 90;
					else
						Angel = 90 - a;
					if (a != aa) Angel -= 180;
					return normalRelativeAngle(Angel);
				}
		}

		// turn on left
		if (getX() < wd) {
			if ((Math.abs(getHeading() - 270) - ba <= 0 && direction == ahead) ||
				(Math.abs(getHeading() - 90) - ba <= 0 && direction == back)) {
				turnDirection();
				return Angel;
			}
			else 
				if  ((Math.abs(getHeading() - 270) - 90 <= 0 && Math.abs(getHeading() + Angel - 270) - 90 <= 0 && direction == ahead) ||
					((Math.abs(getHeading() -  90) - 90 <= 0 && Math.abs(getHeading() + Angel -  90) - 90 <= 0 ||
					getHeading() == 0 && getHeading() + Angel < 0) && direction == back) ||
					(getHeading() == 0 && Angel < 0 && direction == ahead))
					if (aa <= 90) {
						return -a;
					}
					else {
						Angel = 180 - a;
						if (a != aa) Angel -= 360;
						return Angel;
					}
		}

		// turn on right
		if (getX() > fieldWidth - wd) {
			if ((Math.abs(getHeading() -  90) - ba <= 0 && direction == ahead) ||
				(Math.abs(getHeading() - 270) - ba <= 0 && direction == back)) {
				turnDirection();
				return Angel;
			}
			else 
				if  ((Math.abs(getHeading() -  90) - 90 <= 0 && Math.abs(getHeading() + Angel -  90) - 90 <= 0 && direction == ahead) ||
					((Math.abs(getHeading() - 270) - 90 <= 0 && Math.abs(getHeading() + Angel - 270) - 90 <= 0 ||
					getHeading() == 0 && getHeading() + Angel < 0) && direction == back))
					if (aa <= 90) {
						return -a;
					}
					else {
						Angel = 180 - a;
						if (a != aa) Angel -= 360;
						return Angel;
					}
		}
		
		return Angel;
	}

	/******************************************************************************************
	 * calculates a wiggle angel
	 * @return wiggle angle
	 ******************************************************************************************
	 */
	private double wiggle(int wiggle_angle) {
		int wiggle_start = 30;
		wiggle_angle *= (wiggle_start - getEnergy()) / wiggle_start;
		if (getTurnRemaining() == 0 || Math.abs(wiggle) > wiggle_angle)
       		if (getEnergy() < wiggle_start)
				wiggle = wiggle_angle - (wiggle_angle * 2 * random.nextDouble());
			else
				wiggle = 0;
		return (wiggle);
	}

	/******************************************************************************************
	 * calculates a wiggle angel
	 * @return wiggle angle
	 ******************************************************************************************
	 */
	private double wiggle2(int wiggle_angle) {
		int wiggle_start = 100;
   		if (getEnergy() < wiggle_start)
   			if (getTime()%20<10)
   				wiggle = wiggle_angle;
   			else
   				wiggle = -wiggle_angle;
		else
			wiggle = 0;
		return (wiggle);
	}

	/******************************************************************************************
	 * turns the Direction
	 ******************************************************************************************
	 */
    private void turnDirection() {
		direction *= -1;
		sameDirection = 0;
	}

	/******************************************************************************************
	 * gets the damage of a bullet
	 ******************************************************************************************
	 */
    private double getBulletDamage(double power, double energy) {
		double damage = 4 * power;
		if (power > 1)
			damage += 2 * (power - 1);
		if (energy < damage)
			return energy;
		else
			return damage;
	}

	/******************************************************************************************
	 * turns the Direction
	 ******************************************************************************************
	 */
    private String formatStringVal(String s, int length) {
		while (s.length() < length)
			s = " ".concat(s);
		return s;
	}

	/******************************************************************************************
	 * nearestEnemy: returns the nearest enemy
	 * @return nearestEnemy
	 ******************************************************************************************
	 */
	private Enemy nearestEnemy(double ofX, double ofY) {
		double minDistance = 99999;
		Enemy nEnemy = new Enemy();
		Enemy en;
    	Enumeration e = targets.elements();
		while (e.hasMoreElements()) {
    	    en = (Enemy)e.nextElement();
			double d = getRange(ofX, ofY, en.x, en.y);
			if (d < minDistance && getTime() - en.scanTime < 16) {
				minDistance = d;
				nEnemy = en;
			}
	    }
	    if (minDistance == 99999)
	    	nEnemy.distance = 0;
	    return(nEnemy);
	}
	
	/******************************************************************************************
	 * resets the target information
	 * @param name of the new target
	 ******************************************************************************************
	 */
	private void resetTarget(String name) {
		bulletsFired = new HashMap(20);
		otherName = name;
		otherEnergy = 0;
		otherVelocity = 0;
		otherVelocity2 = 0;
		otherScans = 0;
		ramStartTime = 0;
		resetFireTypes();
		OverallFire = 0;
		ScanTime = 0;
		CornerBehaviour = 1;
	}

	/******************************************************************************************
	 * resets all targets in hashtable targets
	 ******************************************************************************************
	 */
	private void resetHashtableTargets() {
		Enumeration e = targets.elements();
		Enemy en;
		while (e.hasMoreElements()) {
			en = (Enemy)e.nextElement();
			en.live = false;
			en.energy = -1;
			en.hitByBulletDamage = 0;
			en.nextFireTime = (long)(getGunHeat()/getGunCoolingRate());
			en.lastFireTime = 0;
		}
	}

	/******************************************************************************************
	 * resets the fire types
	 ******************************************************************************************
	 */
	private void resetFireTypes() {
		/*numBulletsHit  [0] = 0;*/ numBulletsHit  [1] = 0;	numBulletsHit  [2] = 0;	numBulletsHit  [3] = 0;	numBulletsHit  [4] = 0;
		/*numBulletsMiss [0] = 0;*/ numBulletsMiss [1] = 0; numBulletsMiss [2] = 0; numBulletsMiss [3] = 0; numBulletsMiss [4] = 0;
		/*numBulletsElse [0] = 0;*/ numBulletsElse [1] = 0; numBulletsElse [2] = 0; numBulletsElse [3] = 0; numBulletsElse [4] = 0;
		/*numBulletsFired[0] = 0;*/ numBulletsFired[1] = 0; numBulletsFired[2] = 0; numBulletsFired[3] = 0;	numBulletsFired[4] = 0;
	}

	/******************************************************************************************
	 * getVelocityMax:  slow down speed near walls
	 ******************************************************************************************
	 */
    private double getVelocityMax(double v_max) {
		/*if (getEnergy() > 50)
			return(v_max);
		else {*/
			double v = 8;
			if (wallDistance(getX(), getY()) < 39)
				v = 2 + (wallDistance(getX(), getY()) - 21) / 18 * 6;
			return(Math.min(v_max, v));
		//}
    }
	/******************************************************************************************
	 * readBotFile - read stored informations for a bot
	 ******************************************************************************************
	 */
	private void readBotFile(Enemy en) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(getDataFile(en.name + ".txt")));
			en.oVo_battles  = Long.parseLong(r.readLine());
			en.oVo_workingMove = Integer.parseInt(r.readLine());
			workingMove = en.oVo_workingMove;
			if (numOthers == 1 && en.oVo_workingMove > 1) {
				RoundsType1--;
				MoveType = en.oVo_workingMove;
				switch (en.oVo_workingMove) {
					case 2:
						RoundsType2++;
						MoveStat = "2";
					break;
					case 3:
						RoundsType3++;
						MoveStat = "3";
					break;
					case 4:
						RoundsType4++;
						MoveStat = "4";
					break;
				}
			}
			en.oVo_workingShot = Integer.parseInt(r.readLine());
			en.oVo_workingFire = Integer.parseInt(r.readLine());
			for (int i=0; i<5; i++) en.bulletsHit  [i] = Long.parseLong(r.readLine());
			for (int i=0; i<5; i++) en.bulletsMiss [i] = Long.parseLong(r.readLine());
			for (int i=0; i<5; i++) en.bulletsElse [i] = Long.parseLong(r.readLine());
			for (int i=0; i<5; i++) en.bulletsFired[i] = Long.parseLong(r.readLine());
		} catch (IOException eio) {
		} catch (NumberFormatException eio) {
		}
	}

	/******************************************************************************************
	 * writeBotFile - write informations for a bot
	 ******************************************************************************************
	 */
	private void writeBotFile() {
		Enumeration e = targets.elements();
		Enemy en;
		while (e.hasMoreElements()) {
			en = (Enemy)e.nextElement();

			try {
				PrintStream w = new PrintStream(new RobocodeFileOutputStream(getDataFile(en.name + ".txt")));
				w.println((en.oVo_battles + getNumRounds()));
				if (getNumRounds() > 100 && WinsType1 > getNumRounds() * .9) w.println("1");
				else if (getNumRounds() > 100 && WinsType2 > getNumRounds() * .9) w.println("2");
				else if (getNumRounds() > 100 && WinsType3 > getNumRounds() * .9) w.println("3");
				else w.println(en.oVo_workingMove);
				if (getNumRounds() > 100 && numBulletsFired[1] > numBulletsFired[0] * .95) w.println("1");
				else if (getNumRounds() > 100 && numBulletsFired[2] > numBulletsFired[0] * .95) w.println("2");
				else if (getNumRounds() > 100 && numBulletsFired[3] > numBulletsFired[0] * .95) w.println("3");
				else if (getNumRounds() > 100 && numBulletsFired[4] > numBulletsFired[0] * .95) w.println("4");
				else w.println(en.oVo_workingShot);
				w.println(en.oVo_workingFire);
				for (int i=0; i<5; i++) w.println(en.bulletsHit  [i] + numBulletsHit  [i]);
				for (int i=0; i<5; i++) w.println(en.bulletsMiss [i] + numBulletsMiss [i]);
				for (int i=0; i<5; i++) w.println(en.bulletsElse [i] + numBulletsElse [i]);
				for (int i=0; i<5; i++) w.println(en.bulletsFired[i] + numBulletsFired[i]);
				if (w.checkError())
					out.println("I could not store the bot-infos!");
				w.close();
			} catch (IOException eio) {
				out.println("IOException trying to write: " + eio);
			}

		}
	}

	/******************************************************************************************
	 * VictoryDance
	 ******************************************************************************************
	 */
	private void doVictoryDance() {
		double endTime = getTime() + 120;
		stop();
		if (ColorSet == false) {
			setColors(Color.black, Color.black, Color.black);
			ColorNormal = false;
		}
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setTurnGunLeft(normalRelativeAngle(getGunHeading()));
		setTurnRadarLeft(normalRelativeAngle(getRadarHeading()));
		setToXY(centerX, centerY, 0);
		setMaxVelocity(0);
		waitFor(new TurnCompleteCondition(this));
		setMaxVelocity(8);
		waitFor(new MoveCompleteCondition(this));
		setMove(normalRelativeAngle(90 - getHeading()), 0);
		waitFor(new TurnCompleteCondition(this));
		double startRadar = getTime();
		double radarSpinTime = endTime - startRadar;
		if (getRoundNum()%2 != 0 || fieldHeight <= 800) {
			do {
				double turnAngel = (getTime()-startRadar)%(radarSpinTime/2);
				if (turnAngel > radarSpinTime/4)
					turnAngel = radarSpinTime/2 - turnAngel;
				setTurnRadarRight((getTime()-startRadar)%radarSpinTime>radarSpinTime/2?turnAngel:-turnAngel);
				waitFor(new TurnCompleteCondition(this));
			} while (startRadar + radarSpinTime > getTime());
			do fire(.1); while (true);
		}
		else {
			fire(3);
			double e = getEnergy();
			do fire(.1); while(e == getEnergy());
		}	 
	}

}