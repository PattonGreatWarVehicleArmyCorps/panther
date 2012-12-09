package emp;

import java.util.Random;
import java.awt.Point;
import java.awt.Polygon;

public class Predictor implements Consts{
   private class HistoryMatchInfo implements Consts{
      public int Idx;
      public int Length;
      public double Corr;
      public boolean Found;
      public int Type;
      public HistoryMatchInfo(){
         Idx = 0;
         Length = 0;
         Corr = 0;
         Found = false;
      }
   }

   Yngwie yngwie;
   private static Random rand = new Random();
   private Enemy pe;
   private double BulletPowerAtEnemy;
   private double MaxFirePower;
   private double AdvicePower;
   private boolean canKillEnemy;

   public Predictor(Yngwie y){
      yngwie = y;
      BulletPowerAtEnemy = 0.0;
      MaxFirePower = 0.0;
      AdvicePower  = 0.0;
      canKillEnemy = false;
      pe = null;
   }

   private void SortStrategies(){
   // een langzame sort.
      if (pe.Strategies.size() < 2)
         return;
      Strategy S1;
      Strategy S2;
      int j = 0;
      while (j+1 < pe.Strategies.size()) {
         S1 = (Strategy)pe.Strategies.elementAt(j);
         S2 = (Strategy)pe.Strategies.elementAt(j+1);
         if (S2.Faith() > S1.Faith()){
            pe.Strategies.removeElementAt(j);
            pe.Strategies.insertElementAt(S1,j+1);
            j = 0;
         }
         else
            j++;
      }
   }

   private void BlessBullet(){
      if (pe.HitX < 35.0)
         pe.HitX = 35.0;
      else if (pe.HitX > yngwie.BattleFieldWidth - 35.0)
         pe.HitX = yngwie.BattleFieldWidth - 35.0;

      if (pe.HitY < 35.0)
         pe.HitY = 35.0;
      else if (pe.HitY > yngwie.BattleFieldHeight - 35.0)
         pe.HitY = yngwie.BattleFieldHeight - 35.0;
   }

   public void Predict(Enemy en){
      if ((en == null) || (en.Death) || (yngwie.getEnergy() <= 0.1))
         return;

      pe = en;

      if (en.Energy() <= 0.0){ // kill him quick
//         yngwie.out.println("finish him" + en.Energy());
         en.HitStrategy = cFinisherPredictor;
         en.HitX = en.X();
         en.HitY = en.Y();
         en.HitPower = 0.1;
         return;
      }

      // bepalen maximum firepower
      MaxFirePower = cMaxBulletPower;
      if (yngwie.getEnergy() <= cSaveAmmoMode2) // we zijn bijna kapot
         MaxFirePower = 0.1;
      else if (yngwie.getEnergy() <= cSaveAmmoMode1) // we zijn iets minder bijna kapot
         MaxFirePower = 1.0;

      if (pe.Energy() < 16.0)
      {
         canKillEnemy = true;
         double PowerToKill = My.getBulletPowerToKill(pe.Energy());
//         yngwie.out.println("kill "+pe.Name+" power "+PowerToKill);
         if (yngwie.getBulletDamageAimedAt(en) > PowerToKill)
            MaxFirePower = Math.min(MaxFirePower,1.0);
         else
            MaxFirePower = Math.min(MaxFirePower,PowerToKill);
      }
      else
         canKillEnemy = false;

      double dist = yngwie.Distance(pe);

      if ((Yngwie.OneOnOne) && (Yngwie.BattleFieldHeight == 600.0))
      {
         if (dist < 200.0)
            AdvicePower = cMaxBulletPower;
         else if (dist < 400.0)
            AdvicePower = 2.0;
         else AdvicePower = 1.5;
      }
      else if ((Yngwie.OneOnOne) && (Yngwie.BattleFieldHeight != 600.0))
      {
         if (dist < 200.0)
            AdvicePower = cMaxBulletPower;
         else if (dist < 400.0)
            AdvicePower = 2.0;
         else if (dist < 800.0)
            AdvicePower = 1.0;
         else AdvicePower = 0.5;
      }
      else
      {
         if (dist < 150.0)
            AdvicePower = cMaxBulletPower;
         else if (dist < 400.0)
            AdvicePower = 1.0;
         else AdvicePower = 0.5;

         Enemy e; // als er nog een enemy achter zit, schiet wat harder
         for (int i=0; i < yngwie.EC.Enemies.size();i++)
         {
            e = (Enemy) yngwie.EC.Enemies.elementAt(i);
            if ((e.Death) ||
                (e.Time() == -1) ||
                (e == en) ||
                (e.Time() != yngwie.getTime()))
                continue;

            if ((My.absADiffDeg(yngwie.AngleTo(e),yngwie.AngleTo(en)) < 4.0))
            {
               if (yngwie.Distance(e) < 400.0)
                  AdvicePower = Math.min(cMaxBulletPower,AdvicePower + 0.5);
               break;
            }
         }
      }

      en.HitPower = 0.0;
      en.HitX = en.X();
      en.HitY = en.Y();
      SortStrategies();

      Strategy s;
      HistoryMatchInfo h;
      for (int i = 0;i < en.Strategies.size(); i++) {
         s = (Strategy)en.Strategies.elementAt(i);
         if (s.ID == cFinisherPredictor)
            continue;
         if (s.ID == cCurPosPredictor) {
            en.HitStrategy = cCurPosPredictor;
            en.HitX = en.X();
            en.HitY = en.Y();
            en.HitPower = Math.min(MaxFirePower,Math.max(0.1,2.0* s.Faith()/ 100.0));//Math.min(3,Math.max(0.1,s.AvgScore/10));
            return;
         }
         else if (s.ID == cXYPredictor) {
            if (HistoryPredictor(s.ID))
               break;
         }
         else if (s.ID == cCloseRangePredictor) {
            if (CloseRangePredict())
               break;
         }
         else if (s.ID == cSquarePredictor) {
            if (SquarePredict())
               break;
         }
         else if (s.ID == cTurnDispPredictor) {
            if (HistoryPredictor(s.ID))
               break;
         }
         else if (s.ID == cSprayPredictor){
            if (SprayPredict(en))
               break;
         }
         else if (s.ID == cVelocityPredictor) {
            if (HistoryPredictor(s.ID))
               break;
         }
         else if (s.ID == cLinearPredictor) {
            if (LinearPredict())
               break;
         }
         else if ((s.ID == cDodgePredictor) || (s.ID == cDodge2Predictor)){
            if (DodgePredict(s.ID))
               break;
         }
      }
      BlessBullet();
      return;
   }

   private boolean HistoryPredictor(int sID){
      HistoryMatchInfo h = HistoryMatch(sID);
      if (h.Found)
         return GuessHitPoint(h);
      else
         return false;
   }

   private boolean DodgePredict(int sID){
      if (pe.BulletList.size() < 10)
         return false;
      double bestScore = 0.0;
      int bestItem = -1;
      double score1;
      double score2;
      double score3;
      double score;
      BulletItem bi;
      double bearingNow = My.absADiffDeg2(yngwie.AngleTo(pe),pe.Heading());
      double dist = yngwie.Distance(pe);
      int lastIndex = 0;
      if (pe.BulletList.size() > 1000)
         lastIndex = (int) 0.5 * pe.BulletList.size();
      else if (pe.BulletList.size() > 100)
         lastIndex = (int) 0.2 * pe.BulletList.size();
      if (sID == cDodgePredictor)
      {
         for (int i = pe.BulletList.size()-1; i >= lastIndex;i--)
         {
            bi = (BulletItem) pe.BulletList.elementAt(i);
            if (bi.OneOnOne != Yngwie.OneOnOne)
               continue;
            score1 = 17.0 - Math.abs(pe.Velocity() - bi.EnemyVelocity);
            score2 = 10.0 - (My.absADiffDeg(bearingNow,bi.EnemyBearing) / 20.0);
            score3 = Math.max(1.0,10.0-Math.abs(dist - bi.Distance)/30.0);
            score = score1 * score2 * score3;
            if (score > bestScore){
               bestScore = score;
               bestItem = i;
            }
         }
      }
      else
      {
         for (int i = pe.BulletList.size()-1; i >= lastIndex;i--)
         {
            bi = (BulletItem) pe.BulletList.elementAt(i);
            if (bi.OneOnOne != Yngwie.OneOnOne)
               continue;
            score = 10.0 - 0.25 * Math.abs(pe.Velocity() - bi.EnemyVelocity);
            score *= Math.max(0.1,2.0 - Math.abs(AdvicePower - bi.firePower));
            score *= Math.max(1.0,10.0-Math.abs(dist - bi.Distance)/20.0);
            if (score > bestScore){
               bestScore = score;
               bestItem = i;
            }
         }
      }

      if (bestItem == -1)
         return false;

      bi = (BulletItem) pe.BulletList.elementAt(bestItem);
      double TargetX;
      double TargetY;
      double Power;

      if (sID == cDodgePredictor)
      {
         double hoek = My.AddDegrees(pe.Heading(),bi.DeviationAngle);
         double snelheid  = bi.Deviation / bi.Duration;

         double Speed;
         double damage;
         long Turns;
         double MaxTurns = bi.Duration+4;
         int i;

         for (int k = 6; k >= 0; k--){
            if ((k == 6) && (canKillEnemy))
               Power = MaxFirePower;
            else
            {
               Power = Math.max(k * 0.5,0.1);
               if ((Power > MaxFirePower) || (Power > AdvicePower))
                  continue;
            }

            Speed = My.getBulletVelocity(Power);
            damage = My.getBulletDamage(Power);

            TargetX = pe.X();
            TargetY = pe.Y();

            Turns = 1;
            while ((Turns < MaxTurns) && PointInBattleField(TargetX,TargetY)){
               if (My.Distance(TargetX,TargetY,yngwie.X,yngwie.Y)-18.0 < Speed * Turns){
                  pe.HitX = TargetX;
                  pe.HitY = TargetY;
                  pe.HitPower = Power;
                  pe.HitStrategy = cDodgePredictor;
                  return true;
               }

               TargetX = TargetX + snelheid * My.sinDeg(hoek);
               TargetY = TargetY + snelheid * My.cosDeg(hoek);
               Turns++;
            }
         }

         return false;
      }
      else if (sID == cDodge2Predictor)
      {
         if (Math.abs(dist - bi.Distance) > 30.0)
            return false;
         TargetX = pe.X() + bi.Deviation * My.sinDeg(My.AddDegrees(pe.Heading(),bi.DeviationAngle));
         TargetY = pe.Y() + bi.Deviation * My.cosDeg(My.AddDegrees(pe.Heading(),bi.DeviationAngle));
         if ((PointInBattleField(TargetX,TargetY)) && (bi.firePower <= MaxFirePower))
         {
            pe.HitX = TargetX;
            pe.HitY = TargetY;
            pe.HitPower = bi.firePower;
            pe.HitStrategy = cDodge2Predictor;
            return true;
         }
      }
      return false;
   }

   private boolean SquarePredict(){
      if (pe.RC < 60)
         return false;

      double CenterX = 0.0;
      double CenterY = 0.0;

      double MaxLeft   = Yngwie.BattleFieldWidth;
      double MaxRight  = 0.0;
      double MaxTop    = 0.0;
      double MaxBottom = Yngwie.BattleFieldHeight;
      double Precision;
      double Power;

      long curTime = yngwie.getTime();
      int count = 0;
      int k = pe.RC-1; // k is de index die langs de observaties loopt waartegen getest wordt, en begint met de laatst waargenomen observatie van deze enemy
      while ((curTime - pe.Records[k][cTime] < 50) && (pe.Records[k][cTime] > 20) && (k > 5)){
         CenterX += pe.Records[k][cX];
         CenterY += pe.Records[k][cY];
         if (pe.Records[k][cX] > MaxRight)
            MaxRight = pe.Records[k][cX];
         if (pe.Records[k][cX] < MaxLeft)
            MaxLeft = pe.Records[k][cX];
         if (pe.Records[k][cY] < MaxBottom)
            MaxBottom = pe.Records[k][cY];
         if (pe.Records[k][cY] > MaxTop)
            MaxTop = pe.Records[k][cY];
         count++;
         k--;
         if (count == 40){
            Precision = Math.max(1.0,(MaxRight - MaxLeft) / 36.0);
            Precision = Precision * Math.max(1.0,(MaxTop - MaxBottom) / 36.0);
            Power = Math.min(MaxFirePower,Math.max(0.0,4.0 - (0.8 * Math.sqrt(Precision))));
            if (Power > 0.0)
            {
               pe.HitX = CenterX / (double) count;
               pe.HitY = CenterY / (double) count;
               pe.HitPower = Power;
               pe.HitStrategy = cSquarePredictor;
               return true;
            }
         }
      }
      return false;
   }


   private boolean LinearPredict(){
      if (pe.RC < 7)
         return false;

      double snelheid = 0.0;
      double draaihoek = 0.0;
      int histories = 5;

      double LastCorr = 1.0;
      double TestCorr = 1.0;
      double dist = yngwie.Distance(pe);

      int k = pe.RC-histories-1; // k is de index die langs de observaties loopt waartegen getest wordt, en begint met de laatst waargenomen observatie van deze enemy
      while ((TestCorr * LastCorr > 0.9) && // kijk of de laatse correlatie nog wel voldeed
             (pe.Records[k][cTime] == pe.Records[k-1][cTime]+1) && // kijk of de vergelijkende record ook wel opeenvolgend was
             (k < pe.RC)){
         TestCorr *= LastCorr;
         if (k == pe.RC-1){
            snelheid /= (double) histories;
            draaihoek /= (double)  histories;
            double Speed;
            double TargetX;
            double TargetY;
            double TargetH;
            double Power;
            double damage;
            int Turns;
            for (int z = 6; z >= 0; z--){
               if ((z == 6) && (canKillEnemy))
                  Power = MaxFirePower;
               else
               {
                  Power = Math.max(z * 0.5,0.1);
                  if ((Power > MaxFirePower) || (Power > AdvicePower))
                     continue;
               }

               Speed = My.getBulletVelocity(Power);
               damage = My.getBulletDamage(Power);

               TargetX = pe.X();
               TargetY = pe.Y();
               TargetH = pe.Heading();

               Turns = 1; // the gunner needs also 1 turn
               while ((Turns < 25) && PointInBattleField(TargetX,TargetY)){
                  if (My.Distance(TargetX,TargetY,yngwie.X,yngwie.Y)-18.0 < Speed * Turns){
                     pe.HitX = TargetX;
                     pe.HitY = TargetY;
                     pe.HitPower = Power;
                     pe.HitStrategy = cLinearPredictor;
                     return true;
                  }

                  TargetH = My.AddDegrees(TargetH,draaihoek);
                  TargetX = TargetX + snelheid * My.sinDeg(TargetH);
                  TargetY = TargetY + snelheid * My.cosDeg(TargetH);
                  Turns++;
               }
            }
         }

         LastCorr = 1.0-0.02*My.abs(pe.Records[k-1][cVelocity]-pe.Records[k][cVelocity]);
         snelheid += pe.Records[k][cVelocity];
         if (LastCorr < 0)
            break; // zorg ervoor dat er geen rare dingen gebeurpe..
         LastCorr *= 1.0-0.003*My.abs(pe.Records[k-1][cTurned]-pe.Records[k][cTurned]);
         draaihoek += pe.Records[k][cTurned];

         k++;
      }
      return false;
   }

   private void ComputeEnemyRange(Enemy en,Polygon range, boolean ahead,double Degrees,double bSpeed)
   {
      int Turns = 1;
      double TargetX = en.X();
      double TargetY = en.Y();
      double TargetH = en.Heading();
      double TargetSpeed = en.Velocity();
      double Turned = 0.0;
      double MaxTurning = 0.0;

      while (My.Distance(yngwie.X,yngwie.Y,TargetX,TargetY)-18.0 > Turns * bSpeed)
      {
         MaxTurning = My.getRobotMaxTurning(TargetSpeed);
         if ((Degrees >= 0.0) && (Turned < Degrees))
         {
            if ((Degrees - Turned) >= MaxTurning)
            {
               TargetH = TargetH + MaxTurning;
               Turned += MaxTurning;
            }
            else
            {
               TargetH = TargetH + (Degrees - Turned);
               Turned += (Degrees - Turned);
            }
         }
         else if ((Degrees < 0.0) && (Turned < -Degrees))
         {
            if ((-Degrees - Turned) >= MaxTurning)
            {
               TargetH = TargetH - MaxTurning;
               Turned += MaxTurning;
            }
            else
            {
               TargetH = TargetH + Degrees + Turned;
               Turned -= (Degrees + Turned);
            }
         }

         if (TargetH >= 360.0)
            TargetH -= 360.0;
         else if (TargetH < 0.0)
            TargetH += 360.0;

         TargetSpeed = My.getRobotAccVelocity(TargetSpeed,ahead);

         TargetX = TargetX + TargetSpeed * My.sinDeg(TargetH);
         TargetY = TargetY + TargetSpeed * My.cosDeg(TargetH);

         if (TargetX < 18.0){
            if (TargetY < 18.0)
               TargetY = 18.0;
            TargetX = 18.0;
            break;
         }
         if (TargetY < 18.0){
            TargetY = 18.0;
            break;
         }
         if (TargetX > Yngwie.BattleFieldWidth-18.0){
            if (TargetY > Yngwie.BattleFieldHeight-18.0)
               TargetY = Yngwie.BattleFieldHeight-18.0;
            TargetX = Yngwie.BattleFieldWidth-18.0;
            break;
         }
         if (TargetY > Yngwie.BattleFieldHeight-18.0){
            TargetY = Yngwie.BattleFieldHeight-18.0;
            break;
         }
         Turns++;
      }
      range.addPoint((int)TargetX,(int)TargetY);
   }

   private boolean CloseRangePredict(){
      double Power;
      double Speed;
      Polygon EnemyRange;

      if (yngwie.Distance(pe) < 150.0)
         Power = cMaxBulletPower;
      else return false;

      Power = Math.min(Power,MaxFirePower);

      Speed = My.getBulletVelocity(Power);

      EnemyRange = new Polygon();

      if (pe.Velocity() >= 0)
      {
         ComputeEnemyRange(pe,EnemyRange,true,-90.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,true,-45.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,true,0.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,true,45.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,true,90.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,false,-45.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,false,0.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,false,45.0,Speed);
      }
      else
      {
         ComputeEnemyRange(pe,EnemyRange,false,90.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,true,-45.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,true,0.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,true,45.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,false,-90.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,false,-45.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,false,0.0,Speed);
         ComputeEnemyRange(pe,EnemyRange,false,45.0,Speed);
      }
      int[] x = EnemyRange.xpoints;
      int[] y = EnemyRange.ypoints;
      double avg_x = 0.0;
      double avg_y = 0.0;
      for (int i = 0;i < 8;i++)
      {
          if (x[i] < 18)
             x[i] = 18;
          else if (x[i] > Yngwie.BattleFieldWidth-18)
             x[i] = (int)Yngwie.BattleFieldWidth - 18;
          if (y[i] < 18)
             y[i] = 18;
          else if (y[i] > Yngwie.BattleFieldHeight-18)
             y[i] = (int) Yngwie.BattleFieldHeight - 18;
          avg_x += x[i];
          avg_y += y[i];
      }
      avg_x /= 8.0;
      avg_y /= 8.0;

      pe.HitX = avg_x;
      pe.HitY = avg_y;
      pe.HitPower = Power;
      pe.HitStrategy = cCloseRangePredictor;
      return true;
   }

   private boolean SprayPredict(Enemy en){
      double Speed;
      double TargetX;
      double TargetY;
      double TargetH;
      double Power;
      double damage;
      int Turns;

      double dist = yngwie.Distance(en);

      if (canKillEnemy)
         Power = MaxFirePower;
      else
         Power = Math.min(AdvicePower,MaxFirePower);

      Speed = My.getBulletVelocity(Power);
      damage = My.getBulletDamage(Power);

      // schat een punt iets in de toekomst
      double tijd = (dist-18.0) / Speed + 1.0; // the gunner needs also 1 turn
      TargetX = en.X() + Math.sqrt(tijd) * en.Velocity() * My.sinDeg(en.Heading());
      TargetY = en.Y() + Math.sqrt(tijd) * en.Velocity() * My.cosDeg(en.Heading());
      double targethoek = yngwie.NextTurnAngleTo(en);
      // hoe meer hij strafed, hoe groter sindraaihoek (tussen 0 en 1 gescaled)
      double sindraaihoek = My.abs(My.sinDeg((targethoek - en.Heading()+360.0)%360.0));
      double myr = rand.nextGaussian();
      en.HitX = TargetX + 0.5*(sindraaihoek+0.5) * tijd * myr * My.sinDeg((targethoek+90)%360);
      en.HitY = TargetY + 0.5*(sindraaihoek+0.5) * tijd * myr * My.cosDeg((targethoek+90)%360);
      en.HitPower = Power;
      en.HitStrategy = cSprayPredictor;
      return true;
   }

   private boolean PointInBattleField(double X, double Y){
      if ((X >= 18.0) & (Y >= 18.0) & (X <= yngwie.BattleFieldWidth-18.0) & (Y <= yngwie.BattleFieldHeight-18.0))
         return true;
      else
         return false;
   }

   private boolean GuessHitPoint(HistoryMatchInfo h){
      double Speed;
      double TargetX;
      double TargetY;
      double TargetH;
      double Power;
      double damage;
      int Turns;
      int i;

      for (int k = 6; k >= 0; k--){
         if ((k == 6) && (canKillEnemy))
            Power = MaxFirePower;
         else
         {
            Power = Math.max(k * 0.5,0.1);
            if ((Power > MaxFirePower) || (Power > AdvicePower))
               continue;
         }

         Speed = My.getBulletVelocity(Power);
         damage = My.getBulletDamage(Power);

         TargetX = pe.X();
         TargetY = pe.Y();
         TargetH = pe.Heading();
         Turns = 0;
         i = h.Idx + 1;

         if (h.Type == cVelocityPredictor){
            while ((Turns < 30) && PointInBattleField(TargetX,TargetY)){
               if (My.Distance(TargetX,TargetY,yngwie.X,yngwie.Y)-18.0 < Speed * Turns){
                  pe.HitX = TargetX;
                  pe.HitY = TargetY;
                  pe.HitPower = Power;
                  pe.HitStrategy = h.Type;
                  return true;
               }

               if ((i < pe.RC-1) && (pe.Records[i][cTime] - pe.Records[i-1][cTime] == 1)){
                  TargetX = TargetX + pe.Records[i][cVelocity] * My.sinDeg(TargetH);
                  TargetY = TargetY + pe.Records[i][cVelocity] * My.cosDeg(TargetH);
               }
               else if (i == pe.RC-1){
                  TargetX = TargetX + pe.Velocity() * My.sinDeg(TargetH);
                  TargetY = TargetY + pe.Velocity() * My.cosDeg(TargetH);
               }
               else
                  break;
               Turns++;
               i++;
            }
         }
         else{
            while ((Turns < 40) && (i < pe.RC-1) && (pe.Records[i][cTime] - pe.Records[i-1][cTime] == 1) && PointInBattleField(TargetX,TargetY)){
               if (My.Distance(TargetX,TargetY,yngwie.X,yngwie.Y) < Speed * Turns){
                  pe.HitX = TargetX;
                  pe.HitY = TargetY;
                  pe.HitPower = Power;
                  pe.HitStrategy = h.Type;
                  return true;
               }
               if (h.Type == cXYPredictor){
                  TargetX = pe.Records[i][cX];
                  TargetY = pe.Records[i][cY];
               }
               else if (h.Type == cTurnDispPredictor){
                  TargetH = My.AddDegrees(TargetH,pe.Records[i][cTurned]);
                  TargetX = TargetX + pe.Records[i][cVelocity] * My.sinDeg(TargetH);
                  TargetY = TargetY + pe.Records[i][cVelocity] * My.cosDeg(TargetH);
               }

               Turns++;
               i++;
            }
         }
      }
      return false;
   }

   private HistoryMatchInfo HistoryMatch(int pred){
      int BestIdx = 0;
      int BestLength = 0;
      double BestCorr = 0;
      int TestLength; // TestLength is de huidige periode waar succesvol tegen getest is
      double TestCorr;  // TestCorr is de correlatie van de gehele periode waartegen getest is
      double LastCorr;  // LastCorr is de correlatie van de laatste tijdstap
      int j;
      int k;
      for (int TestIdx = pe.RC-10;TestIdx > 10;TestIdx--){ // ga minstens 5 ticks terug om te zoeken naar een match
         LastCorr = 1;
         TestCorr = 1;
         TestLength = -1;
         j = TestIdx; // j is de index die langs de te testen observaties loopt
         k = pe.RC-1; // k is de index die langs de observaties loopt waartegen getest wordt, en begint met de laatst waargenomen observatie van deze enemy
         while ((j > 1) && //neem de allereerste observatie nooit mee
                (TestCorr * LastCorr > 0.9) && // kijk of de laatse correlatie nog wel voldeed
                (pe.Records[j][cTime] == pe.Records[j-1][cTime]+1) && // kijk of de test-idx's wel opelkaareenvolgend zijn
                (pe.Records[k][cTime] == pe.Records[k-1][cTime]+1) && // kijk of de vergelijkende record ook wel opeenvolgend was
                (TestLength < (pe.RC - TestIdx)) &&  // belangrijke! zorg ervoor dat er nog genoeg datapunten overblijven om nog iets uit te kunnen schatten (zie guessHitpoint)
                (TestLength <= cMaxHistoryLength)) { //erg optimistisch, maar de periode hoeft niet meer tijdstappen te bevatten
            TestCorr *= LastCorr;
            if (pred == cXYPredictor){
               LastCorr = 1-0.03*My.abs(pe.Records[j][cX]-pe.Records[k][cX]);
               if (LastCorr < 0) break; // zorg ervoor dat er geen rare dingen gebeurpe..
               LastCorr *= 1-0.03*My.abs(pe.Records[j][cY]-pe.Records[k][cY]);
            }
            else if (pred == cTurnDispPredictor){
               LastCorr = 1-0.03*My.abs(pe.Records[j][cVelocity]-pe.Records[k][cVelocity]);
               if (LastCorr < 0) break; // zorg ervoor dat er geen rare dingen gebeurpe..
               LastCorr *= 1-0.01*My.abs(pe.Records[j][cTurned]-pe.Records[k][cTurned]);
            }
            else if (pred == cVelocityPredictor){
               LastCorr = 1-0.02*My.abs(pe.Records[j][cVelocity]-pe.Records[k][cVelocity]);
            }

            TestLength++;
            j--;
            k--;
         }

         // nu is hij eruit gefloeperd..kijken of het iets heeft opgeleverd:
         // vergelijk de gemiddelde corr met de gemiddelde beste corr
         // that is : Power(TestCorr,1/Testlength) > Power(BestCorr,1/BestLength)
//         if ((TestLength > 5) && (TestCorr > 0.9) && ((BestLength == 0) ||(Math.pow(TestCorr,(double)(1/TestLength)) > Math.pow(BestCorr,(double)(1/BestLength))) && (TestLength >= BestLength))){
         if ((TestLength > 5) && (TestCorr > 0.9) && (((TestCorr > BestCorr) && (TestLength >= BestLength)) || ((TestCorr > (BestCorr-1)) && (TestLength > BestLength)   ))){
            BestCorr   = TestCorr;
            BestLength = TestLength;
            BestIdx    = TestIdx;
            if (BestLength >= cMaxHistoryLength)
               break;
         }
      }
      HistoryMatchInfo result = new HistoryMatchInfo();
      result.Found  = (BestLength > 0);
      result.Idx    = BestIdx;
      result.Length = BestLength;
      result.Corr   = BestCorr;
      result.Type   = pred;
      return result;
   }
}