package emp;

import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 *
 */

public class Enemy implements Consts{

   public String Name;
   public double HitX;
   public double HitY;
   public double HitPower;
   public int HitStrategy;
   public double[][] Records;
   public int RC;
   public boolean Death;
   public Vector Strategies;
   public double BulletDamage;
   public boolean Scanned;// scanned in last scanner sweep
   public long LastTimeHitMe;
   public long LastTimeFired;
   public double EnergyAdjust;
   public double TargetScore;

   public Vector BulletList;

   public Enemy(String N)
   {
      Name = N;

      Records = new double[cRecordCount][cVariables];
      BulletList = new Vector(500,100);

      Strategies = new Vector(10,5);
//      Strategies.add(0,new Strategy(cFinisherPredictor));  niet nodig
//      Strategies.add(new Strategy(cNextPosPredictor)); slecht
//      Strategies.add(new Strategy(cXYPredictor)); slecht
//      Strategies.add(new Strategy(cCurPosPredictor)); //slecht
//      Strategies.add(new Strategy(cSquarePredictor)); // vrij slecht..

      Strategies.add(0,new Strategy(cVelocityPredictor));

      Strategies.add(1,new Strategy(cTurnDispPredictor));

      Strategies.add(2,new Strategy(cLinearPredictor)); //getest
      Strategies.add(3,new Strategy(cSprayPredictor)); //getest
      Strategies.add(4,new Strategy(cDodgePredictor)); //getest
      Strategies.add(5,new Strategy(cDodge2Predictor)); //getest
      if (Yngwie.Melee)
         Strategies.add(6,new Strategy(cCloseRangePredictor));
//      Strategies.add(0,new Strategy(cLinearPredictor)); //getest
//      Strategies.add(1,new Strategy(cDodge2Predictor)); //getest

      Clear();
   }

   public boolean AddBulletItem(BulletTracker bt, long curtime){
      double hittime = curtime + bt.Counter;
      if (Time() < hittime) // ik heb hem nog niet gezien, hou hem in de bullettrackerlist
         return false;

      // dus ik heb hem gezien nadat de bullet de distance overbrugt heeft
      int j = RC-1;
      double minerror = Math.abs(hittime - Records[j][cTime]);
      while (Math.abs(hittime - Records[j-1][cTime]) < minerror) // error wordt kleiner
      {
         minerror = Math.abs(hittime - Records[j-1][cTime]);
         j--;
      }
      if (minerror == 0)
      {
         BulletItem bi = new BulletItem(bt.Power,bt.Distance,bt.EnemyVelocity,bt.EnemyBearing,bt.OneOnOne);
         bi.Deviation = My.Distance(bt.EnemyX,bt.EnemyY,Records[j][cX],Records[j][cY]);
         bi.DeviationAngle = My.ADiffDeg(bt.EnemyHeading,My.AngleFromTo(bt.EnemyX,bt.EnemyY,Records[j][cX],Records[j][cY]));
//         Yngwie.instance.out.println("adding1 "+Time()+" h "+hittime+" p "+bt.Power+" x "+bt.StartX+" y "+bt.StartY);
//         Yngwie.instance.out.println("adding2 x "+bt.EnemyX+" y "+bt.EnemyY+" x2 "+Records[j][cX]+" y2 "+Records[j][cY]+" d "+bi.Deviation+ " dangle "+bi.DeviationAngle);
         BulletList.addElement(bi);
      }
      return true;
   }

   public double EasyLevel(){
      Strategy maxStrat = (Strategy) Strategies.elementAt(0);
      Strategy strat;
      for (int j = 1; j < Strategies.size(); j++){
         strat = (Strategy) Strategies.elementAt(j);
         if (strat.Faith() > maxStrat.Faith())
            maxStrat = strat;
      }
      return maxStrat.Faith();
   }

   public void Clear(){
      RC = 0;
      Death = false;
      HitStrategy = 0;
      HitPower = 0.0;
      BulletDamage = 0.0;
      Scanned = true;
      LastTimeHitMe = -1000;
      LastTimeFired = -1;
      EnergyAdjust = 0.0;
      TargetScore = 0.0;
   }

   public void IncreaseRecord(){
      double[][] Temp = new double[Records.length * 2][cVariables];

      // dit lijkt de hele 2-dimensionale array te kopieren!
      System.arraycopy(Records,0,Temp,0,Records.length);
      Records = Temp;
   }

   public long Time(){        // Last Seen time
      if (RC == 0)
         return -1;
      else
         return (long) Records[RC-1][cTime];
   }
   public double X(){           // Last Seen X pos
      if (RC == 0)
         return -1.0;
      else
         return Records[RC-1][cX];
   }

   public double Y(){           // Last Seen Y pos
      if (RC == 0)
         return -1.0;
      else
         return Records[RC-1][cY];
   }

   public double Velocity(){
      if (RC == 0)
         return -1.0;
      else
         return Records[RC-1][cVelocity];
   }

   public double Heading(){
      if (RC == 0)
         return -1.0;
      else
         return Records[RC-1][cHeading];
   }

   public double Energy(){
      if (RC == 0)
         return 100.0;
      else
         return Records[RC-1][cEnergy];
   }

   public double Turned(){
      if (RC == 0)
         return -1.0;
      else
         return Records[RC-1][cTurned];
   }

   public double Fired(){
      if (RC == 0)
         return -1.0;
      else
         return Records[RC-1][cFired];
   }

   public long Others(){
      if (RC == 0)
         return -1;
      else
         return (long) Records[RC-1][cOthers];
   }
}