package emp;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Gunner implements Consts{
   private Yngwie yngwie;

   private double GunPosX;
   private double GunPosY;
   private double GunPower;
   private int GunStrategy;
   private String GunEnemy;

   public Enemy Target;
   public long lastTimeFired;

   public Gunner(Yngwie y){
      yngwie = y;
      Target = null;
      lastTimeFired = -1;
      GunPosX = 0.0;
      GunPosY = 0.0;
      GunPower = 0.0;
      GunStrategy = 0;
      GunEnemy = "";
   }

   public void Update(){
      if (yngwie.EC.Enemies.size() == 0)
         return;

      Target = yngwie.EC.getBestEnemy();
      if (Target == null)
         return;

      double curHeading = yngwie.getGunHeading();
      if ((GunEnemy == Target.Name) &&    // de gepredicte enemy is de huidige target (en dus niet dood)
          (yngwie.getGunHeat() == 0.0) && // we kunnen schieten
          (GunPower >= 0.09) &&           // een legale bullet power (niet nodig?)
          (yngwie.getTime()-Target.Time() <= 2) &&  // we hebben de target net nog gezien
          (Math.abs(curHeading - My.AngleFromTo(yngwie.getX(),yngwie.getY(),GunPosX,GunPosY)) < 0.5)) // er mag maar een kleine afwijking zijn
      {
         yngwie.control.Fire(GunPower,GunStrategy,Target);
         lastTimeFired = yngwie.getTime();
      }

      Target.HitPower = 0.0;
      GunPower        = 0.0;
      if ((lastTimeFired != yngwie.getTime()) && // er moet niet net geschoten zijn
          (yngwie.getGunHeat() - yngwie.getGunCoolingRate() <= 0.0) && // en er moet straks geschoten kunnen worden
          (yngwie.getTime() == Target.Time())) // en de target moet nu te zien zijn
         yngwie.predictor.Predict(Target);     // predict dan de enemy

      if (Target.HitPower >= 0.09) // als er iets gepredict is
      {  // turn dan de gun naar het predicte punt
         double targetHeading = My.AngleFromTo(yngwie.control.NextXPos(),yngwie.control.NextYPos(),Target.HitX,Target.HitY);
         yngwie.control.TurnTo(cTurret,3,targetHeading,false);
         GunPosX = Target.HitX;
         GunPosY = Target.HitY;
         GunPower = Target.HitPower;
         GunStrategy = Target.HitStrategy;

         GunEnemy = Target.Name;
      }
      else // draai anders de gun richting de volgende verwachte positie van de enemy
         yngwie.control.TurnTo(cTurret,3,yngwie.NextTurnAngleTo(Target),false);
   }
}