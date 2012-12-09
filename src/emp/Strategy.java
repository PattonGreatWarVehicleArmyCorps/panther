package emp;

public class Strategy implements Consts{
   public int ID;
   public double Score;
   public double AvgScore;
   public int Fired;
   public int successes;

   public Strategy (int vID){
      ID = vID;
      Score = 0.0;
      Fired = 0;
      successes = 0;
   }

   public double SuccessRatio(){
      if (Fired > 0)
         return 100.0*successes/Fired;
      else
         return 0.0;
   }

   public double Faith(){
      if (Yngwie.Melee)
         return (55.0/(0.40*Fired+1.0) + 5.0 * (AvgScore-0.5) + 1.5*(SuccessRatio()-6.0));
      else
         return (65.0/(0.30*Fired+1.0) + 5.0 * (AvgScore-0.5) + 1.5*(SuccessRatio()-6.0));
   }

   public void Success(double BulletPower){
      Score += My.getBulletDamage(BulletPower);
      Score += My.getBulletGain(BulletPower);
      Score += BulletLost(BulletPower);
      successes++;
      Fired++;
      AvgScore = Score / (double)Fired;
   }

   public void Failed(double BulletPower){
      Score += BulletLost(BulletPower);
      Fired++;
      AvgScore = Score / (double)Fired;
   }

   private static double BulletLost(double power){
      return -power;
   }
}