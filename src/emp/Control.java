package emp;

import robocode.*;

public class Control implements Consts{

   private Yngwie yngwie;
   private boolean robotUpdate;
   private int robotPriority;
   private double robotDegrees;
   private boolean robotRight;
   private boolean robotMaximum;

   private boolean robotDistUpdate;
   private double  robotDistance;
   private double  robotExpectedVelocity;

   private boolean turretUpdate;
   private int turretPriority;
   private double turretDegrees;
   private boolean turretRight;
   private boolean turretMaximum;

   private boolean radarUpdate;
   private int radarPriority;
   private double radarDegrees;
   private boolean radarRight;
   private boolean radarMaximum;

   private boolean gunFire;
   private double gunPower;
   private int gunStrategy;
   private Enemy gunEnemy;

   public Control(Yngwie y){
      yngwie = y;
      robotUpdate = false;
      turretUpdate = false;
      radarUpdate = false;
      gunFire = false;
      gunPower = 0.0;
      gunStrategy = 0;
      gunEnemy = null;
      robotDistUpdate = false;
      robotDistance = 0.0;
      robotExpectedVelocity = 0.0;
   }

   private void req(int C,int P, double Degrees, boolean R, boolean M){
      if (C == cRobot){
         robotUpdate = true;
         robotPriority = P;
         robotDegrees = Degrees;
         robotRight = R;
         robotMaximum = M;
      }
      else if (C == cTurret){
         turretUpdate = true;
         turretPriority = P;
         turretDegrees = Degrees;
         turretRight = R;
         turretMaximum = M;
      }
      else if (C == cRadar){
         radarUpdate = true;
         radarPriority = P;
         radarDegrees = Degrees;
         radarRight = R;
         radarMaximum = M;
      }
   }

   public void TurnTo(int CallerID,int Priority,double DoHeading,boolean Maximum){
      if (CallerID == cRobot)
         req(CallerID,Priority,My.absADiffDeg(yngwie.getHeading(),DoHeading),My.isToRightDeg(yngwie.getHeading(),DoHeading),Maximum);
      else if (CallerID == cTurret)
         req(CallerID,Priority,My.absADiffDeg(yngwie.getGunHeading(),DoHeading),My.isToRightDeg(yngwie.getGunHeading(),DoHeading),Maximum);
      else if (CallerID == cRadar)
         req(CallerID,Priority,My.absADiffDeg(yngwie.getRadarHeading(),DoHeading),My.isToRightDeg(yngwie.getRadarHeading(),DoHeading),Maximum);
   }

   public void TurnRight(int CallerID,int Priority,double Degrees){
      req(CallerID,Priority,Degrees,true,false);
   }

   public void TurnLeft(int CallerID,int Priority,double Degrees){
      req(CallerID,Priority,Degrees,false,false);
   }

   public void TurnRightMax(int CallerID,int Priority){
      req(CallerID,Priority,0.0,true,true);
   }

   public void TurnLeftMax(int CallerID,int Priority){
      req(CallerID,Priority,0.0,false,true);
   }

   public void Fire(double Power,int Strat, Enemy en){
      gunFire = true;
      gunPower = Power;
      gunStrategy = Strat;
      gunEnemy = en;
   }

   public double CalcRobotTurning(){
      if (robotRight){
         if (robotMaximum){
            return My.getRobotMaxTurning(yngwie.getVelocity());
         }
         else {
            return Math.min(robotDegrees,My.getRobotMaxTurning(yngwie.getVelocity()));
         }
      }
      else {
         if (robotMaximum){
            return -My.getRobotMaxTurning(yngwie.getVelocity());
         }
         else {
            return -Math.min(robotDegrees,My.getRobotMaxTurning(yngwie.getVelocity()));
         }
      }
   }

   public double CalcRobotVelocity(){
      if (robotDistUpdate)
         return robotExpectedVelocity;
      else
         return yngwie.getVelocity();
   }

   public double NextXPos(){
      return yngwie.getX()+CalcRobotVelocity()*My.sinDeg(My.AddDegrees(yngwie.getHeading(),CalcRobotTurning()));
   }

   public double NextYPos(){
      return yngwie.getY()+CalcRobotVelocity()*My.cosDeg(My.AddDegrees(yngwie.getHeading(),CalcRobotTurning()));
   }

   public void setRobotVelocity(double Velocity){
      robotDistUpdate = true;
      if (Velocity >= 0){
         if (yngwie.getVelocity() >= Velocity){
            if (yngwie.getVelocity()-cRobotMaxDeceleration >= Velocity){
               robotExpectedVelocity = yngwie.getVelocity()-cRobotMaxDeceleration;
               robotDistance = 0.0;
            }
            else{
               robotExpectedVelocity = Math.min(yngwie.getVelocity()+cRobotMaxAcceleration,8.0);
               robotDistance = 100.0;
            }
         }
         else if (yngwie.getVelocity() < 0.0){
            if (yngwie.getVelocity()+cRobotMaxDeceleration <= 0.0){
               robotExpectedVelocity = yngwie.getVelocity()+cRobotMaxDeceleration;
               robotDistance = 0.0;
            }
            else{
               robotExpectedVelocity = 0.0;
               robotDistance = 0.0;
            }
         }
         else{
            if (yngwie.getVelocity()+cRobotMaxAcceleration <= Velocity){
               robotExpectedVelocity = yngwie.getVelocity()+cRobotMaxAcceleration;
               robotDistance = 100.0;
            }
            else{
               robotExpectedVelocity = Math.min(yngwie.getVelocity()+cRobotMaxAcceleration,8.0);
               robotDistance = 100.0;
            }
         }
      }
      else {
         if (yngwie.getVelocity() <= Velocity){
            if (yngwie.getVelocity()+cRobotMaxDeceleration <= Velocity){
               robotExpectedVelocity = yngwie.getVelocity()+cRobotMaxDeceleration;
               robotDistance = 0.0;
            }
            else{
               robotExpectedVelocity = Math.max(yngwie.getVelocity()-cRobotMaxAcceleration,-8.0);
               robotDistance = -100.0;
            }
         }
         else if (yngwie.getVelocity() > 0.0){
            if (yngwie.getVelocity()-cRobotMaxDeceleration >= 0.0){
               robotExpectedVelocity = yngwie.getVelocity()-cRobotMaxDeceleration;
               robotDistance = 0.0;
            }
            else{
               robotExpectedVelocity = 0.0;
               robotDistance = 0.0;
            }
         }
         else{
            if (yngwie.getVelocity()-cRobotMaxAcceleration >= Velocity){
               robotExpectedVelocity = yngwie.getVelocity()-cRobotMaxAcceleration;
               robotDistance = -100.0;
            }
            else{
               robotExpectedVelocity = Math.max(yngwie.getVelocity()-cRobotMaxAcceleration,-8.0);
               robotDistance = -100.0; //Velocity;
            }
         }
      }
   }

   public void Update(){
      double robotTurning  = 0.0;
      double turretTurning = 0.0;
      double radarTurning  = 0.0;

      if (robotUpdate){
         robotTurning = CalcRobotTurning();
         robotUpdate = false;
      }

      if (robotDistUpdate){
         if (robotDistance >= 0.0)
            yngwie.setAhead(robotDistance);
         else
            yngwie.setBack(-robotDistance);
         robotDistance = 0.0;
         robotExpectedVelocity = 0.0;
         robotDistUpdate = false;
      }

      if (turretUpdate){
         if (turretRight){
            if (turretMaximum){
               turretTurning = My.getTurretMaxTurning();
            }
            else {
               turretTurning = Math.min(turretDegrees - robotTurning,My.getTurretMaxTurning());
            }
         }
         else {
            if (turretMaximum){
               turretTurning = -My.getTurretMaxTurning();
            }
            else {
               turretTurning = -Math.min(turretDegrees + robotTurning,My.getTurretMaxTurning());
            }
         }
         turretUpdate = false;
      }

      if (radarUpdate){
         if (radarRight){
            if (radarMaximum){
               radarTurning = My.getRadarMaxTurning();
            }
            else {
               radarTurning = Math.min(radarDegrees - robotTurning - turretTurning,My.getRadarMaxTurning());
            }
         }
         else {
            if (radarMaximum){
               radarTurning = -My.getRadarMaxTurning();
            }
            else {
               radarTurning = -Math.min(radarDegrees + robotTurning + turretTurning,My.getRadarMaxTurning());
            }
         }
         radarUpdate = false;
      }

      if (robotTurning < 0.0)
         yngwie.setTurnLeft(-robotTurning);
      else
         yngwie.setTurnRight(robotTurning);

      if (turretTurning < 0.0)
         yngwie.setTurnGunLeft(-turretTurning);
      else
         yngwie.setTurnGunRight(turretTurning);
      if (radarTurning < 0.0)
         yngwie.setTurnRadarLeft(-radarTurning);
      else
         yngwie.setTurnRadarRight(radarTurning);

      if (gunFire){
         if (yngwie.getGunHeat() == 0.0){
            Bullet bullet = yngwie.setFireBullet(gunPower);
//            yngwie.out.println(yngwie.getTime()+" shooting strat "+gunStrategy+" at "+gunEnemy.Name+ " pow "+bullet.getPower()+" dist "+yngwie.Distance(gunEnemy));
            BulletTracker bt = new BulletTracker(bullet,gunStrategy,gunEnemy.Name,Yngwie.OneOnOne);
            bt.SetPosition(yngwie.getX(),yngwie.getY(),gunEnemy.X(),gunEnemy.Y());
            bt.EnemyVelocity = gunEnemy.Velocity();
            bt.EnemyBearing = My.absADiffDeg2(yngwie.AngleTo(gunEnemy),gunEnemy.Heading());
            bt.EnemyHeading = gunEnemy.Heading();
            yngwie.bullettrackers.addElement(bt);
         }
         gunFire = false;
      }
   }
}
