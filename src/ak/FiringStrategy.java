package ak;
import java.awt.geom.*;

/**
 * FiringStrategy - a class by Arun Kishore
 */
public class FiringStrategy
{
	public static int count=9;
	
	public static Point2D.Double applyStrategy(int strategyID,OtherBot bot,double firePower,Fermat robot)
	{
		switch(strategyID)
		{
			case 0:
				return AvgLinearStrafe(bot,firePower,robot);				
			case 1:
				return AvgCircularStrafe(bot,firePower,robot);
			case 2:
				if(robot.getOthers() > 1)
					return new Point2D.Double(-1,-1);
				else					
					return AvgCircularStop(bot,firePower,robot);				
			case 3:
				if(robot.getOthers() > 1)
					return new Point2D.Double(-1,-1);
				else
					return AvgLinearStop(bot,firePower,robot);
			case 4:
				return AvgVelCircular(bot,firePower,robot);					
			case 5:
				return AvgLinear(bot,firePower,robot);			
			case 6:
				return Circular(bot,firePower,robot);
			case 7:
				return Linear(bot,firePower,robot);
			default:
				return new Point2D.Double(bot.X,bot.Y);
		}
	}
		
	static Point2D.Double Linear(OtherBot bot,double firePower,Fermat robot)
	{	
		long nextTime,time;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			p.x = bot.X + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*bot.velocity*diff;
			p.y = bot.Y + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*bot.velocity*diff;	
		}
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}	
	
	static Point2D.Double AvgLinear(OtherBot bot,double firePower,Fermat robot)
	{	
		long nextTime,time;
		double velocity;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		velocity = bot.avgVelocity;
		if(bot.velocity < 0)
			velocity *= -1;
		if(bot.velocity == 0)
			velocity = 0;
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			p.x = bot.X + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
			p.y = bot.Y + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
		}
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}
	
	static Point2D.Double AvgLinearStrafe(OtherBot bot,double firePower,Fermat robot)
	{
		if(bot.patternAnalyzer.stddev==-1 || bot.patternAnalyzer.stddev > 3)
			return new Point2D.Double(-1,-1);
		long nextTime=0,time;
		double velocity;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		velocity = bot.avgVelocity;
		if(bot.velocity < 0)
			velocity *= -1;
		if(bot.velocity == 0)
			velocity = 0;
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			p.x = bot.X + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
			p.y = bot.Y + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
		}
		nextTime %= 2*bot.patternAnalyzer.avgReverseTime;
		if(nextTime > bot.patternAnalyzer.avgReverseTime)
			nextTime = (long)bot.patternAnalyzer.avgReverseTime - nextTime;
		time = robot.getTime() + nextTime;
		double diff = time - bot.scanTime;
		p.x = bot.X + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
		p.y = bot.Y + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}
	
	static Point2D.Double AvgLinearStop(OtherBot bot,double firePower,Fermat robot)
	{
		if(bot.patternAnalyzer.movestddev==-1 || bot.patternAnalyzer.movestddev > 3 || Math.abs(bot.velocity)>0)
			return new Point2D.Double(-1,-1);
		long nextTime=0,time;
		double velocity;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		velocity = bot.avgVelocity;
		if(Math.random() < 0.5)
			velocity *= -1;
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			p.x = bot.X + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
			p.y = bot.Y + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
		}
		if(nextTime > bot.patternAnalyzer.avgMovingTime)
			nextTime = (long)bot.patternAnalyzer.avgMovingTime;
		time = robot.getTime() + nextTime;
		double diff = time - bot.scanTime;
		p.x = bot.X + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
		p.y = bot.Y + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*velocity*diff;
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}
	
	static Point2D.Double Circular(OtherBot bot,double firePower,Fermat robot)
	{
		if(Math.abs(bot.headingChange) < 0.00001)
			return new Point2D.Double(-1,-1);
		long nextTime,time;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			double tothead = Math.toDegrees(diff*bot.headingChange);
			double radius = bot.velocity/bot.headingChange;		
			p.y = bot.Y + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*radius;
			p.x = bot.X + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius;
		}
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}
	
	static Point2D.Double AvgVelCircular(OtherBot bot,double firePower,Fermat robot)
	{
		if(Math.abs(bot.headingChange) < 0.00001 || bot.velocity == 0)
			return new Point2D.Double(-1,-1);
		long nextTime,time;
		double velocity;
		velocity = bot.avgVelocity;
		if(bot.velocity < 0)
			velocity *= -1;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			double tothead = Math.toDegrees(diff*bot.headingChange);
			double radius = velocity/bot.headingChange;
			p.y = bot.Y + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*radius;
			p.x = bot.X + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius;
		}
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}
	
	static Point2D.Double AvgHeadCircular(OtherBot bot,double firePower,Fermat robot)
	{
		if(Math.abs(bot.avgHeadingChange) < 0.00001 || Math.abs(bot.headingChange) < 0.00001)
			return new Point2D.Double(-1,-1);
		long nextTime,time;
		double velocity;
		double headingChange = bot.avgHeadingChange;
		if(bot.headingChange < 0)
			headingChange *= -1;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			double tothead = Math.toDegrees(diff*headingChange);
			double radius = bot.velocity/headingChange;
			p.y = bot.Y + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*radius;
			p.x = bot.X + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius;
		}
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}
	
	static Point2D.Double AvgVelHeadCircular(OtherBot bot,double firePower,Fermat robot)
	{
		if(Math.abs(bot.avgHeadingChange) < 0.00001 || Math.abs(bot.headingChange)<0.00001 || bot.velocity==0)
			return new Point2D.Double(-1,-1);
		long nextTime,time;
		double velocity = bot.avgVelocity;
		double headingChange = bot.avgHeadingChange;
		if(bot.headingChange < 0)
			headingChange *= -1;
		if(bot.velocity< 0)
			velocity *= -1;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			double tothead = Math.toDegrees(diff*headingChange);
			double radius = velocity/headingChange;
			p.y = bot.Y + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*radius;
			p.x = bot.X + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius;
		}
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}
	
	static Point2D.Double AvgCircularStrafe(OtherBot bot,double firePower,Fermat robot)
	{
		if(bot.patternAnalyzer.stddev==-1 || bot.patternAnalyzer.stddev > 3)
			return new Point2D.Double(-1,-1);
		if(Math.abs(bot.headingChange) < 0.00001)
			return new Point2D.Double(-1,-1);
		long nextTime,time;
		double velocity;
		velocity = bot.avgVelocity;
		if(bot.velocity < 0)
			velocity *= -1;
		if(bot.velocity == 0)
			velocity = 0;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		nextTime = 0;
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			double tothead = Math.toDegrees(diff*bot.headingChange);
			double radius = velocity/bot.headingChange;
			p.y = bot.Y + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*radius;
			p.x = bot.X + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius;
		}
		nextTime %= 2*bot.patternAnalyzer.avgReverseTime;
		if(nextTime > bot.patternAnalyzer.avgReverseTime)
			nextTime = (long)bot.patternAnalyzer.avgReverseTime - nextTime;
		time = robot.getTime() + nextTime;
		double diff = time - bot.scanTime;
		double tothead = Math.toDegrees(diff*bot.headingChange);
		double radius = velocity/bot.headingChange;
		p.y = bot.Y + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*radius;
		p.x = bot.X + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius;
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}
	
	static Point2D.Double AvgCircularStop(OtherBot bot,double firePower,Fermat robot)
	{
		if(bot.patternAnalyzer.movestddev==-1 || bot.patternAnalyzer.movestddev > 3 || Math.abs(bot.velocity) > 0)
			return new Point2D.Double(-1,-1);
		double headingChange = 0.0001;
		long nextTime,time;
		double velocity;
		velocity = bot.avgVelocity;
		if(Math.random() < 0.5)
			velocity *= -1;
		Point2D.Double p = new Point2D.Double(bot.X, bot.Y);
		nextTime = 0;
		for(int i = 0; i < 10; i++)
		{
			nextTime = (long)Math.round(Math.sqrt(((robot.getX()-p.x)*(robot.getX()-p.x)+(robot.getY()-p.y)*(robot.getY()-p.y)))/(20-(3*firePower)));
			time = robot.getTime() + nextTime;
			double diff = time - bot.scanTime;
			double tothead = Math.toDegrees(diff*headingChange);
			double radius = velocity/bot.headingChange;
			p.y = bot.Y + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*radius;
			p.x = bot.X + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius;
		}
		if(nextTime > bot.patternAnalyzer.avgMovingTime)
			nextTime = (long)bot.patternAnalyzer.avgMovingTime;
		time = robot.getTime() + nextTime;
		double diff = time - bot.scanTime;
		double tothead = Math.toDegrees(diff*bot.headingChange);
		double radius = velocity/bot.headingChange;
		p.y = bot.Y + Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading)))*radius;
		p.x = bot.X + Math.cos(Math.toRadians(normalRelativeAngle(bot.heading)))*radius - Math.sin(Math.toRadians(normalRelativeAngle(bot.heading + tothead)))*radius;
		if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
			return new Point2D.Double(-1,-1);
		if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
			return new Point2D.Double(-1,-1);
		return p;
	}
	
	static double findGunTurn(double x,double y,Fermat robot)
	{
		double angle = normalAbsoluteHeading(Math.toDegrees(Math.PI/2 - Math.atan2(robot.getY() - y, robot.getX() - x)));
		return normalRelativeAngle(robot.getGunHeading() - angle + 180);
	}
	
	static Point2D.Double IntervalFire(OtherBot bot,double firePower,Fermat robot)
	{
		int i,cnt=0;
		Point2D.Double[] intervals = new Point2D.Double[count];
		for(i=0;i<count-1;i++)
		{
			intervals[i] = applyStrategy(i,bot,firePower,robot);
			if(intervals[i].x > 0)
			{
				intervals[cnt].x = normalAbsoluteHeading(Math.toDegrees(Math.PI/2 - Math.atan2(intervals[i].y - robot.getY(),intervals[i].x - robot.getX()))-3.0);
				intervals[cnt].y = normalAbsoluteHeading(intervals[cnt].x+6.0);
				cnt++;
			}
		}
		if(cnt < 2)
			return new Point2D.Double(-1,-1);
		while(cnt > 1)
		{
			for(i=0;i<cnt-1;i++)
				intervals[i] = interSect(intervals[i],intervals[i+1]);
			cnt--;
		}	
		if(intervals[0].x > 0)
		{
			Point2D.Double p =  getPoint(normalAbsoluteHeading(intervals[0].x+normalAbsoluteHeading(intervals[0].y-intervals[0].x)/2.0),robot);
			if(robot.target==robot.previousTarget && Math.abs(findGunTurn(p.x,p.y,robot)) > 60)
				return new Point2D.Double(-1,-1);
			if(p.x <= 0 || p.x >= robot.getBattleFieldWidth() || p.y <= 0 || p.y >= robot.getBattleFieldHeight())
				return new Point2D.Double(-1,-1);
			return p;
		}
		else
			return new Point2D.Double(-1,-1);
	}
	
	static Point2D.Double getPoint(double angle,Fermat robot)
	{
		return new Point2D.Double(
		robot.getX() + Math.cos(Math.toRadians(normalRelativeAngle(90-(robot.getHeading()+angle)))),
		robot.getY() + Math.sin(Math.toRadians(normalRelativeAngle(90-(robot.getHeading()+angle))))
		);
	}
	
	static Point2D.Double interSect(Point2D.Double intA,Point2D.Double intB)
	{
		if(intA.x < 0 || intB.x < 0)
			return new Point2D.Double(-1,-1);
		double angleA,angleB,angleC;
		angleA = normalAbsoluteHeading(intA.y-intA.x);
		angleB = normalAbsoluteHeading(intB.x-intA.x);
		if(angleA >= angleB)
		{
			angleC = normalAbsoluteHeading(intB.y-intA.x);
			if(angleC > angleA)
				return new Point2D.Double(intB.x,intA.y);
			else
				return new Point2D.Double(intB.x,intB.y);
		}
		angleA = normalAbsoluteHeading(intB.y-intB.x);
		angleB = normalAbsoluteHeading(intA.x-intB.x);
		if(angleA >= angleB)
		{
			angleC = normalAbsoluteHeading(intA.y-intB.x);
			if(angleC > angleA)
				return new Point2D.Double(intA.x,intB.y);
			else
				return new Point2D.Double(intA.x,intA.y);
		}
		return new Point2D.Double(-1,-1);
	}
	
	public static double normalAbsoluteHeading(double angle)
	{
		if (angle < 0)
			return 360 + (angle % 360);
		else
			return angle % 360;
	}

	public static double normalRelativeAngle(double angle) 
	{
		if (angle > 180)
			return ((angle + 180) % 360) - 180;
		if (angle < -180)
			return ((angle - 180) % 360) + 180;
		return angle;
	}
}
