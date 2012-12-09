package emp;

public class BulletItem{
   public double Distance;
   public double EnemyVelocity;
   public double EnemyBearing;
   public double firePower;
   public double Deviation;
   public double DeviationAngle;
   public double Duration;
   public boolean OneOnOne;

   public BulletItem(double Power,double Dist, double vel,double bear,boolean vOneOnOne){
      firePower = Power;
      Distance = Dist;
      EnemyVelocity = vel;
      EnemyBearing = bear;
      Duration = Distance / My.getBulletVelocity(firePower);
      OneOnOne = vOneOnOne;
   }
}