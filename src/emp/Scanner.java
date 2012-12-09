package emp;

import java.util.Vector;

public class Scanner implements Consts{

   private Yngwie yngwie;

   private boolean ScanRound;
   private boolean Assist;
   private boolean EnemyFinding;
   private Enemy LostEnemy;

   public boolean TaskDone;
   private int Scans;

   private double lastHeading;

   private boolean CheckingProblem;
   private double ProblemHeading;

   private boolean FirstTime;
   private boolean RightSweeping;
   private long StartSweepTime;
   private long LastRoundCheck;
   private Enemy LeftSweepEnemy;
   private Enemy RightSweepEnemy;


   public Scanner(Yngwie y){
      yngwie = y;
      lastHeading = 0;

      LeftSweepEnemy = null;
      RightSweepEnemy = null;
      RightSweeping = true;
      CheckingProblem = false;
      ProblemHeading = 0.0;
      StartSweepTime = 0;
      LastRoundCheck = -1;
      FirstTime = false;
      ScanRound();
   }

   public void CheckHeading(double heading){
      if ((Assist) && (LeftSweepEnemy != null))
      {
         double a = yngwie.NextTurnAngleTo(LeftSweepEnemy);
         double b = yngwie.NextTurnAngleTo(RightSweepEnemy);
         if (!My.IsAngleBetween(heading,a,b))
         {
            CheckingProblem = true;
            ProblemHeading  = heading;
         }
      }
   }

   private boolean CheckSweepCompleted(){
      if (StartSweepTime == yngwie.getTime())
         return false;

      Enemy en;
      if (LeftSweepEnemy == null || LeftSweepEnemy.Death || RightSweepEnemy.Death)
      {

         for (int i=0; i < yngwie.EC.Enemies.size();i++)
         {
            en = (Enemy) yngwie.EC.Enemies.elementAt(i);
            en.Scanned = true;
         }
         return true;
      }

      for (int i=0; i < yngwie.EC.Enemies.size();i++)
      {
         en = (Enemy) yngwie.EC.Enemies.elementAt(i);
         if ((yngwie.getTime()-en.Time() > 15) && (yngwie.Distance(en)+(yngwie.getTime()-en.Time())*cRobotMaxVelocity >= cMaxScannerRange))
            en.Scanned = true;
         if (yngwie.motor.Aggressors.indexOf(en) == -1)
            en.Scanned = true;
         if (!en.Scanned)
            return false;
      }
      return true;
   }

   private void StartNewSweep(){
      Enemy en;
      double LeftSweepHeading = -1;
      double RightSweepHeading = -1;
      LeftSweepEnemy = null;
      RightSweepEnemy = null;

      double[] Headings;
      Enemy[] Enemies;

      Headings = new double[yngwie.motor.Aggressors.size()];
      Enemies  = new Enemy[yngwie.motor.Aggressors.size()];

      for (int i=0; i < Enemies.length;i++)
      {
         en = (Enemy) yngwie.motor.Aggressors.elementAt(i);
         en.Scanned = false;
         Headings[i] = yngwie.NextTurnAngleTo(en);
         Enemies[i]  = en;
      }

      double temp;
      Enemy temp2;
      for (int i=0; i < Enemies.length; i++)
      {
         for (int j=i+1; j < Enemies.length; j++)
         {
            if (Headings[j] < Headings[i])
            {
               temp = Headings[i];
               Headings[i] = Headings[j];
               Headings[j] = temp;
               temp2 = Enemies[i];
               Enemies[i] = Enemies[j];
               Enemies[j] = temp2;
            }

         }
      }

      if (Enemies.length == 0)
         return;
      else if (Enemies.length == 1){
         LeftSweepEnemy  = Enemies[0];
         RightSweepEnemy = LeftSweepEnemy;
      }
      else{
         double NotSweepArea = My.absADiffDeg2(Headings[Headings.length-1],Headings[0]);
         RightSweepEnemy = Enemies[Enemies.length-1];
         LeftSweepEnemy  = Enemies[0];

         for (int i=0; i < (Enemies.length-1); i++)
         {
            temp = My.absADiffDeg2(Headings[i],Headings[i+1]);
            if (temp > NotSweepArea)
            {
               NotSweepArea = temp;
               RightSweepEnemy = Enemies[i];
               LeftSweepEnemy  = Enemies[i+1];
            }
         }
      }
      StartSweepTime = yngwie.getTime();
   }

   public void ScanRound(){
      Clear();
      ScanRound = true;
      StartSweepTime = yngwie.getTime();
   }


   public void AssistGunner(){
      Clear();
      Assist = true;
      FirstTime = true;
   }

   public void FindEnemy(Enemy en, boolean toRight){
      Clear();
      EnemyFinding = true;
      LostEnemy = en;
      StartSweepTime = yngwie.getTime();
      RightSweeping = toRight;
   }

   public boolean AllSpotted(){
      int count = 0;
      Enemy en;
      for (int i=0; i < yngwie.EC.Enemies.size();i++)
      {
         en = (Enemy) yngwie.EC.Enemies.elementAt(i);
         if (!en.Death && (en.Time() > StartSweepTime))
            count++;
      }
      if (count >= yngwie.getOthers())
         return true;
      else
         return false;
   }

   public void Update(){
      double curHeading = yngwie.getRadarHeading();
      if (ScanRound){
         if ((Scans > 8) || AllSpotted()){
            TaskDone = true;
            ScanRound = false;
            LastRoundCheck = yngwie.getTime();
            AssistGunner();
            Update();
         }
         else
            yngwie.control.TurnRightMax(cRadar,3);
      }
      else if (EnemyFinding){
         if (LostEnemy.Death || (LostEnemy.Time() == yngwie.getTime()))
         {
            AssistGunner();
            Update();
            return;
         }
         if (RightSweeping)
            yngwie.control.TurnRightMax(cRadar,3);
         else
            yngwie.control.TurnLeftMax(cRadar,3);
      }
      else if (Assist){
         double diff;
         if (CheckingProblem){
            if (My.isToRightDeg(curHeading,ProblemHeading))
               if (My.isToRightDeg(ProblemHeading,lastHeading))
               {
                  CheckingProblem = false;
               }
               else
               {
                  yngwie.control.TurnRightMax(cRadar,0);
                  return;
               }
            else
            {
               if (My.isToRightDeg(lastHeading,ProblemHeading))
               {
                  CheckingProblem = false;
               }
               else
               {
                  yngwie.control.TurnLeftMax(cRadar,0);
                  return;
               }
            }
         }

         if ((yngwie.getTime()-yngwie.gunner.lastTimeFired < 4 ) && (yngwie.getTime()-LastRoundCheck > 40) &&
             ((yngwie.gunner.Target == null) || ((yngwie.getTime()-yngwie.gunner.Target.LastTimeFired < 4) && (yngwie.Distance(yngwie.gunner.Target) > 200.0))))
         {
            if (!yngwie.OneOnOne){
               ScanRound();
               Update();
            }
         }

         if (CheckSweepCompleted() || FirstTime){
            FirstTime = false;
            StartNewSweep();
         }

         if (LeftSweepEnemy == null)
         {
            StartNewSweep(); // toch even checken of er iets gezien is.
            yngwie.control.TurnLeftMax(cRadar,2);
         }
         else if ((LeftSweepEnemy == RightSweepEnemy) &&(yngwie.getTime() - LeftSweepEnemy.Time() > 10))
            yngwie.control.TurnLeftMax(cRadar,2);
         else if (LeftSweepEnemy != null)
         {
            double LeftEnemyHeading =  yngwie.NextTurnAngleTo(LeftSweepEnemy);
            if (!RightSweeping && (My.IsAngleBetween(LeftEnemyHeading,curHeading,lastHeading)) &&
                (LeftSweepEnemy.Time() != yngwie.getTime()) && (StartSweepTime != yngwie.getTime()))
            {
               FindEnemy(LeftSweepEnemy,false);
               Update();
               return;
            }
            double RightEnemyHeading =  yngwie.NextTurnAngleTo( RightSweepEnemy);
            if (RightSweeping && (My.IsAngleBetween(RightEnemyHeading,lastHeading,curHeading)) &&
                (RightSweepEnemy.Time() != yngwie.getTime()) && (StartSweepTime != yngwie.getTime()))
            {
               FindEnemy(RightSweepEnemy,true);
               Update();
               return;
            }

            if (!My.IsAngleBetween(curHeading,LeftEnemyHeading,RightEnemyHeading))
            {
               if (My.absADiffDeg2(curHeading,LeftEnemyHeading) < My.absADiffDeg2(RightEnemyHeading,curHeading))
               {
                  RightSweeping = true;
               }
               else
               {
                  RightSweeping = false;
               }
            }

            if (RightSweeping)
            {
               if (RightSweepEnemy == LeftSweepEnemy)
                  yngwie.control.TurnRight(cRadar,3,My.absADiffDeg2(curHeading,yngwie.NextTurnAngleTo(RightSweepEnemy))+20.0);
               else
               {
                  diff = My.absADiffDeg2(curHeading,yngwie.NextTurnAngleTo(RightSweepEnemy));
                  yngwie.control.TurnRight(cRadar,3,diff+10.0);
               }
            }
            else
            {
               if (RightSweepEnemy == LeftSweepEnemy)
                  yngwie.control.TurnLeft(cRadar,3,My.absADiffDeg2(yngwie.NextTurnAngleTo(LeftSweepEnemy),curHeading)+20.0);
               else
               {
                  diff = My.absADiffDeg2(yngwie.NextTurnAngleTo(LeftSweepEnemy),curHeading);
                  yngwie.control.TurnLeft(cRadar,3,diff+10.0);
               }
            }
         }
      }
      Scans++;
      lastHeading = curHeading;
   }

   private void Clear(){
      ScanRound = false;
      EnemyFinding = false;
      LostEnemy = null;
      Assist = false;
      TaskDone = false;
      Scans = 0;
   }
}