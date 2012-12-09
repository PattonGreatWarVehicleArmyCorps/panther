package emp;

import java.util.Random;
import java.util.Vector;


public class Motor implements Consts{
   private class Energy2D{
      public double OriginX; //x point of origin
      public double OriginY; //y point of origin
      public double Z;
      public Energy2D(double OrX, double OrY){
         OriginX = OrX;
         OriginY = OrY;
         Z = 0.0;
      }

      public void SetNewOrigin(double OrX, double OrY)
      {
         OriginX = OrX;
         OriginY = OrY;
         Z = 0.0;
      }

      public void AddFieldObject(double atX, double atY, double Force, double Power){
         double x1 = OriginX - atX;
         double x2 = OriginY - atY;
         if ((x1 == 0.0) & (x2 == 0.0))
         {
            Z += 10.0 * Force;
         }
         else
         {
            Z += Force / Math.pow(Math.sqrt(x1*x1+x2*x2),Power);
         }
      }
   }

   private class Vector2D{
      public double OriginX; //x point of origin
      public double OriginY; //y point of origin
      public double X;     // field vector towards x from OriginX
      public double Y;     // field vector towards y from OriginY
      public Vector2D(double OrX, double OrY){
         OriginX = OrX;
         OriginY = OrY;
         X = 0.0;
         Y = 0.0;
      }

      public void AddVector(Vector2D other){
         X += other.X;
         Y += other.Y;
      }

      public void AddXY(double vX,double vY){
         X += vX;
         Y += vY;
      }

      public void SetXY(double vX, double vY)
      {
         X = vX;
         Y = vY;
      }

      public void SetNewOrigin(double OrX, double OrY)
      {
         OriginX = OrX;
         OriginY = OrY;
         Y = 0.0;
         X = 0.0;
      }

      public void AddFieldObject(double atX, double atY, double Force, double Power){
         double x1 = OriginX - atX;
         double x2 = OriginY - atY;
         if ((x1 == 0.0) & (x2 == 0.0))
         {
            X += Force;
            Y += Force;
         }
         else
         {
            double resultant = Force / Math.pow(Math.sqrt(x1*x1+x2*x2),Power+1.0);
            X += x1 * resultant;
            Y += x2 * resultant;
         }
      }

      public double getAngle(){
         return My.AngleFromTo(yngwie.X,yngwie.Y,yngwie.X+X,yngwie.Y+Y);
      }

      public void Multiply(double x){
         X *= x;
         Y *= x;
      }

      public double Length(){
         return Math.sqrt(X*X+Y*Y);
      }
   }

   private class BulletThreat{
      public double X;
      public double Y;
      public double Power;
      public double Speed;
      public long TimeFired;
      public String Name;
      public BulletThreat(String vName,double vX, double vY,double vPower,long vTime){
         X = vX;
         Y = vY;
         Power = vPower;
         TimeFired = vTime;
         Speed = My.getBulletVelocity(Power);
         Name = vName;
      }
   }

   private Yngwie yngwie;
   private Vector2D FieldVector;
   private Energy2D FieldEnergy;
   private Vector2D RuisVector;
   private double strafemult;
   private Energy2D[] GlobalEnergies;
   public Vector Aggressors;
   public Vector Threats;
   public boolean Collide;

   // in the array aggressors komen diegene die een hoge target-score hebben
   // en diegene die mij geraakt hebben in de afgelopen 100 turns, en niet dood zijn
   // en diegene die minder dan 400 pixels ver weg zijn

   private static Random rand = new Random();

   public Motor(Yngwie yn){
      yngwie = yn;
      FieldVector = new Vector2D(yngwie.getX(),yngwie.getY());
      FieldEnergy = new Energy2D(yngwie.getX(),yngwie.getY());

      RuisVector  = new Vector2D(0,0);
      strafemult = 1;
      Collide = false;
      GlobalEnergies = new Energy2D[cGlobalPoints*cGlobalPoints];
      Aggressors = new Vector(yngwie.getOthers());
      Threats = new Vector(10,5);

      double x;
      double y;
      int i;
      int j;
      for (i = 0; i < cGlobalPoints;i++)
      {
         for (j = 0; j < cGlobalPoints;j++)
         {
            x = Yngwie.BattleFieldWidth * (i+1) /(cGlobalPoints+1);
            y = Yngwie.BattleFieldHeight * (j+1) /(cGlobalPoints+1);
            GlobalEnergies[i*cGlobalPoints+j] = new Energy2D(x,y);
         }
      }
   }

   private void UpdateAngryBullets(){
      Aggressors.clear();
      Enemy bestEn = yngwie.EC.getBestEnemy();
      if (bestEn != null){
         Enemy en;
         int Enemycount = 0;
         double a = 0.75;
         for (int i=0; i < yngwie.EC.Enemies.size();i++){
            en = (Enemy) yngwie.EC.Enemies.elementAt(i);

            if (en.TargetScore >= a * bestEn.TargetScore)
               Enemycount++;
         }

         for (int i=0; i < yngwie.EC.Enemies.size();i++){
            en = (Enemy) yngwie.EC.Enemies.elementAt(i);
            if (en.Death)
               continue;

            if ((en.TargetScore >= a * bestEn.TargetScore)||
               ((yngwie.getTime() -  en.LastTimeHitMe) < 30)||
               (yngwie.Distance(en) < 500.0))
            {
               Aggressors.add(en);
               double firePower = en.Fired();
               if ((firePower >= 0.09) && (en.Time() == yngwie.getTime()))
               {

                  double x,y;
                  if (en.Records[en.RC-1][cTime]-en.Records[en.RC-2][cTime]== 1)
                  {
                     //eigenlijk was de bullet al een beurt eerder geschoten
                     x = en.Records[en.RC-2][cX];
                     y = en.Records[en.RC-2][cY];
//                     yngwie.out.println("hij heeft geschoten, maar ik heb hem toen wel gezien");
                  }
                  else // scanevents gemist, dus neem de huidige positie van de aggressor als punt waarvan de bullet geschoten is
                  {
                     x = en.X();
                     y = en.Y();
//                     yngwie.out.println("hij heeft geschoten, maar ik heb hem toen niet gezien");
                  }
                  BulletThreat t = new BulletThreat(en.Name,x,y,firePower,yngwie.getTime()-1);
                  Threats.add(t);
               }
            }
         }
      }

      int i = 0;
      while (i < Threats.size())
      {
         BulletThreat t = (BulletThreat) Threats.elementAt(i);
         double BulletRadius = t.Speed * (yngwie.getTime() - t.TimeFired);
         if (My.Distance(yngwie.X,yngwie.Y,t.X,t.Y)+cRobotRadius < BulletRadius){
            Threats.removeElementAt(i);
         }
         else{
            i++;
         }
      }
   }

   private void CalcFieldVector(){
      FieldVector.SetNewOrigin(yngwie.getX(),yngwie.getY());
      FieldVector.AddFieldObject(18.0,yngwie.getY(),cWallsField,2); // wallsleft
      FieldVector.AddFieldObject(yngwie.getX(),Yngwie.BattleFieldHeight-18.0,cWallsField,2); // wallstop
      FieldVector.AddFieldObject(Yngwie.BattleFieldWidth-18.0,yngwie.getY(),cWallsField,2); // wallstop
      FieldVector.AddFieldObject(yngwie.getX(),18.0,cWallsField,2); // wallbottom
      if (!Yngwie.OneOnOne)
         FieldVector.AddFieldObject(Yngwie.BattleFieldWidth/2.0,Yngwie.BattleFieldHeight/2.0,cCenterField,1); // center

      FieldEnergy.SetNewOrigin(yngwie.getX(),yngwie.getY());
      FieldEnergy.AddFieldObject(18.0,FieldEnergy.OriginY,cWallsField,2); // wallsleft
      FieldEnergy.AddFieldObject(FieldEnergy.OriginX,Yngwie.BattleFieldHeight-18.0,cWallsField,2); // wallstop
      FieldEnergy.AddFieldObject(Yngwie.BattleFieldWidth-18.0,FieldEnergy.OriginY,cWallsField,2); // wallstop
      FieldEnergy.AddFieldObject(FieldEnergy.OriginX,18.0,cWallsField,2); // wallbottom
      if (!Yngwie.OneOnOne)
         FieldEnergy.AddFieldObject(Yngwie.BattleFieldWidth/2.0,Yngwie.BattleFieldHeight/2.0,cCenterField,1); // center

      int i;
      int j;
      int k;
      Energy2D e;
      for (i = 0; i < cGlobalPoints;i++)
      {
         for (j = 0; j < cGlobalPoints;j++)
         {
            e = GlobalEnergies[i*cGlobalPoints+j];
            e.Z = 0.0;
            e.AddFieldObject(18.0,e.OriginY,cWallsField,2); // wallsleft
            e.AddFieldObject(e.OriginX,Yngwie.BattleFieldHeight-18.0,cWallsField,2); // wallstop
            e.AddFieldObject(Yngwie.BattleFieldWidth-18.0,e.OriginY,cWallsField,2); // wallstop
            e.AddFieldObject(e.OriginX,18.0,cWallsField,2); // wallbottom
            if (!Yngwie.OneOnOne)
               e.AddFieldObject(Yngwie.BattleFieldWidth/2.0,Yngwie.BattleFieldHeight/2.0,cCenterField,1); // center
         }
      }

      for (i=0; i < yngwie.EC.Enemies.size();i++)
      {
         Enemy en = (Enemy) yngwie.EC.Enemies.elementAt(i);
         if ((en.Death) || (en.Time() == -1))
            continue;

         if (en == yngwie.gunner.Target)
         {
            FieldVector.AddFieldObject(en.X(),en.Y(),yngwie.getTargetField(),1.0);
            for (j = 0; j < cGlobalPoints;j++)
            {
               for (k = 0; k < cGlobalPoints;k++)
               {
                  GlobalEnergies[j*cGlobalPoints+k].AddFieldObject(en.X(),en.Y(),yngwie.getTargetField(),1.0);
               }
            }
            FieldEnergy.AddFieldObject(en.X(),en.Y(),yngwie.getTargetField(),1.0);
         }
         else
         {
            FieldVector.AddFieldObject(en.X(),en.Y(),cEnemyField,1.0);
            for (j = 0; j < cGlobalPoints;j++)
            {
               for (k = 0; k < cGlobalPoints;k++)
               {
                  GlobalEnergies[j*cGlobalPoints+k].AddFieldObject(en.X(),en.Y(),cEnemyField,1.0);
               }
            }
            FieldEnergy.AddFieldObject(en.X(),en.Y(),cEnemyField,1.0);
         }
      }

      Energy2D minE = GlobalEnergies[0];
      for (i = 1; i < cGlobalPoints * cGlobalPoints;i++)
      {
         if (GlobalEnergies[i].Z < minE.Z)
            minE = GlobalEnergies[i];
      }


      if (My.Distance(yngwie.getX(),yngwie.getY(),minE.OriginX,minE.OriginY) >
            yngwie.getBattleFieldWidth()/(cGlobalPoints + 1) && (FieldEnergy.Z > minE.Z))
      {
         FieldVector.AddFieldObject(minE.OriginX,minE.OriginY,minE.Z-FieldEnergy.Z,0);
      }

      double strafeAngle;
      double faktor;
      Vector2D StrafeVector = new Vector2D(0,0);
      StrafeVector.SetXY(rand.nextDouble()-0.5,rand.nextDouble()-0.5);

      for (i=0; i < Threats.size();i++)
      {
         BulletThreat t = (BulletThreat) Threats.elementAt(i);
         strafeAngle = My.AddDegrees(90.0,My.AngleFromTo(yngwie.getX(),yngwie.getY(),t.X,t.Y));
         if (strafeAngle >= 180.0)
            strafeAngle -= 180.0;

         faktor =  (2.0*t.Power+1.0) * Math.max(1.0,0.01*(400.0-My.Distance(yngwie.getX(),yngwie.getX(),t.X,t.Y)));
         StrafeVector.X += faktor * My.sinDeg(strafeAngle);
         StrafeVector.Y += faktor * My.cosDeg(strafeAngle);
      }

      if (yngwie.gunner.Target != null)
      {
         strafeAngle = My.AddDegrees(90.0,yngwie.AngleTo(yngwie.gunner.Target));
         if (strafeAngle >= 180.0)
            strafeAngle -= 180.0;

         StrafeVector.X += My.sinDeg(strafeAngle);
         StrafeVector.Y += My.cosDeg(strafeAngle);
      }

      // voeg 1.0 - 3.0 energie strafe toe
      // ga strafen ten opzichte van alle bullets
      // meer strafen ten opzichte van bullets die van dichtbij geschoten zijn

      if (Collide)
      {
         strafemult *= -1;
         Collide = false;
      }
      else if ((Yngwie.OneOnOne) &&
               (yngwie.gunner.Target != null) &&
               (yngwie.Distance(yngwie.gunner.Target) < 300.0) &&
               (rand.nextDouble() < 0.15))
      {
         if (rand.nextDouble() > 0.5)
            strafemult = -1.0;
         else
            strafemult = 1.0;
      }
      else if (((Yngwie.OneOnOne) && (rand.nextDouble() < 0.10))||
               ((!Yngwie.OneOnOne) && (rand.nextDouble() < 0.15)))
      {
         if (rand.nextDouble() > 0.5)
            strafemult = -1.0;
         else
            strafemult = 1.0;
      }

      if (Threats.size() > 0)
         StrafeVector.Multiply(strafemult * 2.0 / StrafeVector.Length());
      else
         StrafeVector.Multiply(strafemult / StrafeVector.Length());

      if (Yngwie.OneOnOne)
         StrafeVector.Multiply(0.7/FieldVector.Length());
      else
         StrafeVector.Multiply(2.0/StrafeVector.Length());
      FieldVector.AddVector(StrafeVector);
   }

   public void Update(){
      UpdateAngryBullets();
      CalcFieldVector();

      double Vel = 5.0 + 3.0 * rand.nextDouble();

      if (yngwie.getVelocity() >= 0.0) {
         if (My.absADiffDeg(FieldVector.getAngle(),yngwie.getHeading()) > 110.0){
            yngwie.control.setRobotVelocity(-Vel);
            yngwie.control.TurnTo(cRobot,3,My.AddDegrees(FieldVector.getAngle(),180.0),false);
         }
         else{
            yngwie.control.setRobotVelocity(Vel);
            yngwie.control.TurnTo(cRobot,3,FieldVector.getAngle(),false);
         }
      }
      else {
         if (My.absADiffDeg(FieldVector.getAngle(),yngwie.getHeading()) < 70.0){
            yngwie.control.setRobotVelocity(Vel);
            yngwie.control.TurnTo(cRobot,3,FieldVector.getAngle(),false);
         }
         else{
            yngwie.control.setRobotVelocity(-Vel);
            yngwie.control.TurnTo(cRobot,3,My.AddDegrees(FieldVector.getAngle(),180.0),false);
         }
      }
   }


   public boolean Between(double X,double Y1,double Y2){
      return (((X >= Y1) && (X <= Y2)) || ((X >= Y2) && (X <= Y1)));
   }
}