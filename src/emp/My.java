package emp;

public class My implements Consts{

   public static boolean DiffSign(double a, double b){
      if (((a < 0.0) && (b < 0.0)) || ((a >= 0.0) && (b >= 0.0)))
         return true;
      else
         return false;
   }

   public static double abs(double a) {
      return (a <= 0.0D) ? 0.0D - a : a;
   }

   // returns the absolute angle difference in degrees between angle A1 & angle A2, angles in degrees
   public static double absADiffDeg(double A1, double A2){
      return Math.min(360.0-Math.abs(A1-A2),Math.abs(A1-A2));
   }

   // returns the angle difference between the leftangle & the right angle
   public static double absADiffDeg2(double leftAngle, double rightAngle){
      double diff = rightAngle - leftAngle;
      return (diff >= 0.0)? diff : diff+360.0;
   }

   // returns the angle difference in degrees between angle A1 & angle A2, angles in degrees
   // positive when DestA right of SourcA, negative otherwise
   public static double ADiffDeg(double SourceA, double DestA){
      if (isToRightDeg(SourceA,DestA))
         return absADiffDeg(SourceA,DestA);
      else
         return -absADiffDeg(SourceA,DestA);
   }

   // returns if RightAngle is right of angle LeftAngle, angles in degrees
   public static boolean isToRightDeg(double LeftAngle,double RightAngle){
      if (((LeftAngle - RightAngle) >= 180.0) || ((RightAngle >= LeftAngle) && ((RightAngle- LeftAngle) <= 180.0)))
         return true;
      else
         return false;
   }

   public static double sqr(double X){
      return X * X;
   }

   public static double Distance(double X1, double Y1, double X2, double Y2){
      return Math.sqrt((X1-X2)*(X1-X2)+(Y1-Y2)*(Y1-Y2));
   }

   public static boolean IsAngleBetween(double Angle, double Left, double Right){
      if (abs(absADiffDeg2(Left,Right)-(absADiffDeg2(Left,Angle)+absADiffDeg2(Angle,Right))) > 1)
         return false;
      else
         return true;
   }

   // this should be the same as atan2
   public static double AngleFromTo(double X1,double Y1, double X2, double Y2){
      if ((X1 < X2) && (Y1 < Y2))
         return Math.toDegrees(Math.atan((X2-X1)/(Y2-Y1)));
      else if ((X1 < X2) && (Y1 > Y2))
         return (90.0 + Math.toDegrees(Math.atan((Y1-Y2)/(X2-X1))));
      else if ((X1 > X2) && (Y1 > Y2))
         return (180.0 + Math.toDegrees(Math.atan((X1-X2)/(Y1-Y2))));
      else if ((X1 > X2) && (Y1 < Y2))
         return (270.0 + Math.toDegrees(Math.atan((Y2-Y1)/(X1-X2))));
      else if ((X1 < X2) && (Y1 == Y2))
         return 90.0;
      else if ((X1 == X2) && (Y1 > Y2))
         return 180.0;
      else if ((X1 > X2) && (Y1 == Y2))
         return 270.0;
      else if ((X1 == X2) && (Y1 < Y2))
         return 0.0;
      return 0.0;
   }


   public static double sinDeg(double X){
      return Math.sin(Math.toRadians(X));
   }

   public static double cosDeg(double X){
      return Math.cos(Math.toRadians(X));
   }

   public static double AddDegrees(double A1, double A2){
      return (A1+A2+360.0) % 360.0;
   }

   public static double getBulletGain(double Power){
      return 3.0 * Power;
   }

   public static double getBulletDamage(double Power){
      return (Power <= 1.0) ? (4.0 * Power) : ((4.0 * Power) + 2.0 * (Power-1.0));
   }

   public static double getBulletPowerToKill(double Energy){
      if (Energy <= 4.0)
         return (Energy / 4.0) + 0.1;
      else
         return (Energy - 4.0) / 6.0 + 1.01;
   }

   public static double getBulletVelocity(double Power){
      return (20.0 - 3.0 * Power);
   }

   public static double getRobotMaxTurning(double Velocity){
      return (10.0 - 0.75 * My.abs(Velocity));
   }

   public static double getTurretMaxTurning(){
      return 20.0;
   }

   public static double getRadarMaxTurning(){
      return 45.0;
   }

   public static double getRobotAccVelocity(double curSpeed,boolean Accelerate){
      if (curSpeed >= 0.0)
      {
         if (Accelerate)
            return Math.min(curSpeed+cRobotMaxAcceleration,cRobotMaxVelocity);
         else if (curSpeed > 0.0) // decelerate & speed > 0.0
            return Math.max(curSpeed-cRobotMaxDeceleration,0.0);
         else // decelerate & speed = 0.0
            return - cRobotMaxAcceleration;
      }
      else
      {
         if (Accelerate)
            return Math.max(curSpeed-cRobotMaxAcceleration,-cRobotMaxVelocity);
         else // decelerate
            return Math.min(curSpeed+cRobotMaxDeceleration,0.0);
      }
   }
}