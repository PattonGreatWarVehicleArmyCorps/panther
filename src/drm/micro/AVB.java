package drm.micro;

import robocode.*;
import java.util.Random;

public class AVB extends AdvancedRobot
{
	// Properties
	static double oppEnergy;
	static long   movementTime;
	static double speed;
	static int    reverseInt = 1;
	static double offset     = 0D;
	static Random random     = new Random();
	
	// Methods
	public void run() {
		setAdjustGunForRobotTurn(true);
		movementTime = 0L;
		oppEnergy    = 100D;
		do{
			turnRadarRightRadians(1);
		}while(true);
	}
	public void onBulletHit(BulletHitEvent e) {
		offset -= .04;
	}
	public void onScannedRobot(ScannedRobotEvent e) {
		double oppBulletPower = oppEnergy - (oppEnergy = e.getEnergy());

		// turn body and decide movement speed.
		if (0L == movementTime) {
			setTurnRightRadians(Math.sin(Math.toRadians(e.getBearing() + 90 + 5 * reverseInt)));
			if (oppBulletPower > 0D){
				reverseInt   = - reverseInt;
				movementTime = getTime()
				   + (long)(e.getDistance() / (20D - 3D * oppBulletPower));
				speed = random.nextDouble() * 2D + 6D ;
			}
		}
		// decide stop.
		if ((getTime() > movementTime)
			|| (chkOutOfBattleField(Math.toRadians(getHeading() + (reverseInt - 1) * -90), 70))
		    || (random.nextDouble() < 0.01)){
			speed        = 0D;
			movementTime = 0L;
		}
		//accelerate.
		setAhead(getDistanceToKeepPositiveVelocity(speed) * reverseInt);

		double absoluteBearing = e.getBearing() + getHeading();
		// turn radar.
		setTurnRadarRightRadians(3.0 * Math.sin(Math.toRadians(absoluteBearing - getRadarHeading())));

		// turn gun.
		double sign = (double)Math.round(random.nextDouble() * 1.5);
		if (Math.toRadians(e.getHeading() - absoluteBearing) < 0) sign = -sign;
		if (e.getVelocity() < 0) sign = -sign;
		setTurnGunRightRadians(Math.asin(Math.sin(Math.toRadians(absoluteBearing - getGunHeading())) + offset * sign ));

		// shoot.
		if  ((getEnergy() > .1 && setFireBullet(Math.min(getEnergy() / 10.0, e.getEnergy() / 4.0)) != null) && ((offset += .02) > .4))
			offset = -.2;
//out.println("offset = " + offset + " : sign = " + sign );
		execute();
	}
	double getDistanceToKeepPositiveVelocity(double velocity){
		double ret = 0;
		while(velocity > 0D){
			ret      += velocity;
			velocity -= 2.0D;
		}
		return ret;
	}
	boolean chkOutOfBattleField(double headingRadians, double distance){
		double x = getX() + distance * Math.sin(headingRadians);
		double y = getY() + distance * Math.cos(headingRadians);
		return ((x < 0) || (x > getBattleFieldWidth()) || (y < 0) || (y > getBattleFieldHeight()));
	}
}
