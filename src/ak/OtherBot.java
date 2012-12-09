package ak;
import java.awt.geom.*;
import java.util.*;
import java.io.*;
import robocode.*;

/**
* OtherBot - a class by Arun Kishore
*/
public class OtherBot
{
	public double energy;
	public double prevEnergy;
	public double X,prevX;
	public double Y,prevY;
	public double heading;
	public double headingChange;
	public double velocity;
	public double avgVelocity,avgHeadingChange;
	public double lastAttackTime;
	public long scanTime,previousScanTime,lastBulletTime;
	public boolean alive;
	public boolean attacking;
	public String name;
	public PatternAnalyzer patternAnalyzer;
	int scans,lastWin,headingScans;
	Vector stats;
	Vector dodgeMoves;

	OtherBot(Fermat robot,double energy,double X,double Y,double heading,double velocity,long scanTime,String name)
	{
		this.energy = energy;
		this.prevEnergy = 100;
		this.X = prevX = X;
		this.Y = prevY = Y;
		this.heading = heading;
		this.velocity = velocity;
		this.previousScanTime = 0;
		this.scanTime = scanTime;
		this.name = name;
		this.headingChange = avgHeadingChange = 0;
		this.alive = true;
		this.lastAttackTime = 0;
		lastBulletTime = -1;
		attacking = false;
		avgVelocity = Math.abs(velocity);
		patternAnalyzer = new PatternAnalyzer();
		stats = new Vector();
		dodgeMoves = new Vector();
		scans = 1;
		headingScans = 1;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(robot.getDataFile(name+".txt")));
			for(int i=0;i<FiringStrategy.count;i++)
			{
				Statistic stat = new Statistic();
				stat.shots = Integer.parseInt(br.readLine());
				stat.hits = Integer.parseInt(br.readLine());
				stat.lastHitTime = -1;
				stats.addElement(stat);
			}
		}
		catch(Exception exp1)
		{
			stats.clear();
			for(int i=0;i<FiringStrategy.count;i++)
			{
				Statistic stat = new Statistic();
				stat.shots = 2;
				stat.hits = 1;
				stat.lastHitTime = -1;				
				stats.addElement(stat);
			}
		}
	}

	public void update(double energy,double X,double Y,double heading,double velocity,long scanTime)
	{
		patternAnalyzer.analyzePatterns(velocity,scanTime);
		double h=Math.toRadians(normalRelativeAngle(heading - this.heading));
		headingChange = h/(scanTime - this.scanTime);
		this.prevEnergy = this.energy;
		this.previousScanTime = this.scanTime;
		lastBulletTime = this.scanTime - 1;
		this.energy = energy;
		prevX = this.X;
		prevY = this.Y;
		this.X = X;
		this.Y = Y;
		this.heading = heading;
		this.velocity = velocity;
		this.scanTime = scanTime;		
		if(velocity != 0)
		{
			scans++;
			avgVelocity = (avgVelocity*(scans-1)+Math.abs(velocity))/scans;
		}
		if(headingChange != 0)
		{
			headingScans++;
			avgHeadingChange = (avgHeadingChange*(headingScans-1)+Math.abs(headingChange))/headingScans;
		}
	}

	public void fireBot(Fermat robot)
	{
		Point2D.Double p,strategicPos;
		int i,strategy=0;
		double maxProbability=-1,score,probability,firePower=3,reqdFirePower,lastHitTime=-1,firingPower=0;
		strategicPos = new Point2D.Double(0,0);
		double distance = Math.sqrt((X-robot.getX())*(X-robot.getX())+(Y-robot.getY())*(Y-robot.getY()));
		for(i=0;i<FiringStrategy.count;i++)
		{
			Statistic stat = ((Statistic)stats.elementAt(i));
			probability = stat.getRatio();
			firePower = 1000/distance;
			/*if(probability > 0.2)
				firePower += (probability + 0.4);*/
			if(firePower > 3)
				firePower = 3;
			if(firePower < 0.1)
				firePower = 0.1;
			if(energy+0.1 < 4)
				reqdFirePower = (energy+0.1)/4;
			else
				reqdFirePower = (energy+2.1)/6;
			if(reqdFirePower < 0)
				reqdFirePower = 0.1;
			if(reqdFirePower < firePower)
				firePower = reqdFirePower;
			if(robot.getEnergy()-0.1 < firePower)
				firePower = robot.getEnergy()-0.1;
			if(robot.target != robot.previousTarget)
				stat.lastHitTime = -1;	
			if(/*stat.lastHitTime > lastHitTime || lastHitTime==stat.lastHitTime && */probability > maxProbability)
			{
				p = FiringStrategy.applyStrategy(i,this,firePower,robot);
				if(p.x > 0)
				{
					lastHitTime = stat.lastHitTime;
					maxProbability = probability;
					strategy = i;
					strategicPos = p;
					firingPower = firePower;
				}
			}
		}
		double turnAngle = findGunTurn(strategicPos.x,strategicPos.y,robot);
		robot.setTurnGunLeft(turnAngle);
		if(Math.abs(robot.getGunTurnRemaining()) < 20)
		{
			long prevTime = robot.getTime();
			robot.waitFor(new GunTurnCompleteCondition(robot));
			long nextTime = robot.getTime();			
			if(robot.getGunHeat()==0 && firingPower > 0)
			{
				if(prevTime != nextTime)
					robot.doUpdates();
				AdvancedBullet adBullet = new AdvancedBullet();
				adBullet.strategy = strategy;
				Statistic stat = (Statistic)stats.elementAt(adBullet.strategy);
				stat.shots++;
				//robot.out.println("Fired At: "+robot.getTime());
				adBullet.start = new Point2D.Double(robot.getX(),robot.getY());				
				adBullet.bullet = robot.fireBullet(firingPower);
				adBullet.targetBot = robot.target;
				robot.bulletVector.addElement(adBullet);
				robot.previousTarget = robot.target;
			}
		}
		else
			robot.execute();
	}

	double findGunTurn(double x,double y,Fermat robot)
	{
		double angle = normalAbsoluteHeading(Math.toDegrees(Math.PI/2 - Math.atan2(robot.getY() - y, robot.getX() - x)));
		return normalRelativeAngle(robot.getGunHeading() - angle + 180);
	}

	public double normalAbsoluteHeading(double angle)
	{
		if (angle < 0)
			return 360 + (angle % 360);
		else
			return angle % 360;
	}

	public double normalRelativeAngle(double angle) 
	{
		if (angle > 180)
			return ((angle + 180) % 360) - 180;
		if (angle < -180)
			return ((angle - 180) % 360) + 180;
		return angle;
	}
}