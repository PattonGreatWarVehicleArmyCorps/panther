package emp;

import robocode.*;
import java.util.Vector;


public class EnemyCollection implements Consts{

   private Yngwie yngwie;
   private long ScoresRecalculated;
   public Vector Enemies;


   public EnemyCollection(Yngwie y){
      yngwie = y;
      Enemies = new Vector(10,5);
      ScoresRecalculated = -1;
   }

   public void printStrategies(){
      Enemy en = (Enemy) Enemies.elementAt(0);
      Strategy strat;
      double[] Scores = new double[en.Strategies.size()];
      double[] Fired = new double[en.Strategies.size()];
      double[] Successes = new double[en.Strategies.size()];
      double[] IDs = new double[en.Strategies.size()];

      for(int j=0; j < en.Strategies.size();j++){
         IDs[j] = ((Strategy) en.Strategies.elementAt(j)).ID;
      }


      for (int i=0; i < Enemies.size();i++){
         en = (Enemy) Enemies.elementAt(i);
         for(int j=0; j < en.Strategies.size();j++){
            strat = (Strategy) en.Strategies.elementAt(j);
            for(int k=0; k < en.Strategies.size();k++)
            {
               if (IDs[k] == strat.ID)
               {
                   Scores[k] += strat.Score;
                   Fired[k] += strat.Fired;
                   Successes[k] += strat.successes;
               }
            }
         }
      }

      en = (Enemy) Enemies.elementAt(0);
/*      for(int i=0; i < Scores.length;i++){
         strat = (Strategy) en.Strategies.elementAt(i);
         yngwie.out.println("Strat "+strat.ID+" score "+Scores[i]+" fired " +Fired[i]+" succes " +Successes[i]);
      }*/

   }

   public void EnemyDeath(String Name){
      int index = IndexOf(Name);
      if (index > -1){
         Enemy en = (Enemy) Enemies.elementAt(index);
         en.Death = true;
         en.Scanned = true;
//         for (int i = 0; i < en.Strategies.size(); i++){
//            Strategy strat = (Strategy) en.Strategies.elementAt(i);
//            yngwie.out.println("strategy "+i+" name : "+strat.ID+" faith : "+strat.Faith()+" succes "+strat.SuccessRatio());
//         }
      }
   }

   public void ResetDeaths()
   {
      ScoresRecalculated = -1;
      for (int i = 0; i < Enemies.size(); i++)
         ((Enemy) Enemies.elementAt(i)).Clear();
   }

   private double InterpolateVelocity(Enemy en, int start, int last)
   {
      // nu is start de eerste onbekende
      // maar last is de laatste onbekende snelheid (de x en y zijn al wel bekend)
      double Dist = My.Distance(en.Records[start-1][cX],en.Records[start-1][cY],en.Records[last][cX],en.Records[last][cY]);
      double Turns = (last - start + 1);
      double vel = Dist / Turns;
      double nextvel = en.Records[last+1][cVelocity];
      // kijk hoeveel hoek er gedraaien is, en bepaal aan de hand daarvan af de robot
      // achteruit heeft moeten rijden, of niet.
      if (My.absADiffDeg(My.AngleFromTo(en.Records[start-1][cX],en.Records[start-1][cY],en.Records[last][cX],en.Records[last][cY]),
                         en.Records[last][cHeading]) / 15.0 > Turns){
         vel *= -1.0;
      }

      // hou rekening met de max acceleration / deceleration
      // echter omgekeerd, aangezien naar de velocity van de volgende beurt bekend is
      if (nextvel >= 0.0)
      {
         if (vel >= nextvel) // als de huidige snelheid hoger is dan de volgende snelheid, mag niet meer dan maxdeceleration erboven zitten
            vel = Math.min(vel,nextvel+cRobotMaxDeceleration);
         else // als zij lager ligt, mag zij niet meer dan max acceleration eronder liggen
            vel = Math.max(vel,nextvel-cRobotMaxAcceleration);
      }
      else // next velocity dus negatief
      {
         if (vel >= nextvel) // als zij lager ligt, mag zij niet meer dan max acceleration eronder liggen
            vel = Math.min(vel,nextvel+cRobotMaxAcceleration);
         else // als de huidige snelheid hoger is dan de volgende snelheid, mag niet meer dan maxdeceleration erboven zitten
            vel = Math.max(vel,nextvel-cRobotMaxDeceleration);
      }

      // snelheden kunnen niet groter zijn dan cRobotMaxVelocity
      if (vel > cRobotMaxVelocity)
         vel = cRobotMaxVelocity;
      else if (vel < - cRobotMaxVelocity)
         vel = - cRobotMaxVelocity;

      return vel;
   }


   private void Interpolate(Enemy en, int start, int last){
      // start is de eerste niet ingevulde record index
      // last is de laatste ingevulde record index
      // dus de interpolatie loopt tussen Start en Last -1, van boven naar beneden
      // aangezien terug rekenen makkelijker is.
      double Turns  = last - start + 1.0;
      double Turned = My.ADiffDeg(en.Records[start-1][cHeading],en.Records[last][cHeading]) / Turns;

      // vul nog turned in voor de net gescande time
      en.Records[last][cTurned] = Turned;
//      yngwie.out.println("pol     time "+ en.Records[last][cTime] +" x "+en.Records[last][cX]+" y "+en.Records[last][cY]+" head "+en.Records[last][cHeading]+ " vel "+en.Records[last][cVelocity]+ " turned " + en.Records[last][cTurned]);

      // Interpoleer elke missende waarde
      for (int i=last-1; i >= start; i--){
         en.Records[i][cTime] = en.Records[i+1][cTime]-1;
         en.Records[i][cX] = en.Records[i+1][cX]-en.Records[i+1][cVelocity]*My.sinDeg(en.Records[i+1][cHeading]);
         en.Records[i][cY] = en.Records[i+1][cY]-en.Records[i+1][cVelocity]*My.cosDeg(en.Records[i+1][cHeading]);
         en.Records[i][cEnergy] = en.Records[i+1][cEnergy];
         en.Records[i][cHeading] = My.AddDegrees(en.Records[i+1][cHeading],-Turned);
         en.Records[i][cTurned] = Turned;
         en.Records[i][cVelocity] = InterpolateVelocity(en,start,i);
         en.Records[i][cDisplace] = My.abs(en.Records[i][cVelocity]);
//         yngwie.out.println("interpol time "+ en.Records[i][cTime] +" x "+en.Records[i][cX]+" y "+en.Records[i][cY]+" head "+en.Records[i][cHeading]+ " vel "+en.Records[i][cVelocity]+ " turned " + en.Records[i][cTurned]);
      }
   }

   public void StoreScanEvent(ScannedRobotEvent e){
      int Start; // eerste onbekende tijdstip
      int Last;  // laatste onbekende tijdstip
      Enemy en;

      int i = IndexOf(e.getName());
      if (i==-1){ // Create new enemy
         en = new Enemy(e.getName());
         Enemies.add(0,en);
         i = 0;
      }
      else
         en = ((Enemy) Enemies.elementAt(i));


      // Het volgende gedeelte gaat de bekende gegevens over de gescande enemy
      // in de history van de enemy opslaan. Er wordt geprobeert gegevens van
      // gemiste scans te interpoleren, om de predictors zo goed mogelijk te laten
      // werken.

      Start = en.RC;
      if ((en.Time() == -1) || ((long)(en.Time())+1+cMaxInterpolatie < yngwie.getTime()))
         Last = Start;
      else
         Last = (int) (Start + yngwie.getTime() - en.Time() -1);

      if (Last+1 >= en.Records.length) // array te klein, vergroten
         en.IncreaseRecord();

      en.Records[Last][cTime] = (double) yngwie.getTime();
      en.Records[Last][cX] = yngwie.getX() + e.getDistance() * Math.sin(Math.toRadians(e.getBearing()+yngwie.getHeading()));
      en.Records[Last][cY] = yngwie.getY() + e.getDistance() * Math.cos(Math.toRadians(e.getBearing()+yngwie.getHeading()));
//      yngwie.out.println(yngwie.getTime()+" scanned "+en.Name+ " at X "+en.Records[Last][cX]+" Y "+en.Records[Last][cY]);
      en.Records[Last][cVelocity] = e.getVelocity();
      en.Records[Last][cHeading] = e.getHeading();
      en.Records[Last][cEnergy] = e.getEnergy();
      en.Records[Last][cDisplace] = My.abs(e.getVelocity());

      double firePower = 0.0;
      if ((Start > 0) && (yngwie.getTime() >= yngwie.PeaceTime))
         // assume enemy fired previous time (not always true with missed enemy-scans)
         firePower = en.Energy() + en.EnergyAdjust - e.getEnergy();

      if ((firePower >= 0.09) && (firePower <= cMaxBulletPower) && ((yngwie.getTime()-en.LastTimeFired) > 4)){
//         yngwie.out.println(yngwie.getTime()+"   "+en.Name+" fired "+firePower);
         en.Records[Last][cFired] = firePower;
         en.LastTimeFired = yngwie.getTime();
      }
      else
         en.Records[Last][cFired] = 0.0;

      if (Last == 0)
         en.Records[Last][cTurned] = 0;
      else if (en.Records[Last][cTime] == en.Records[Last-1][cTime]+1)
         en.Records[Last][cTurned] = My.ADiffDeg(en.Records[Last-1][cHeading],en.Records[Last][cHeading]);

      if (Start != Last)
         Interpolate(en,Start,Last);

      en.EnergyAdjust = 0.0;
      en.RC = Last + 1;
      en.Scanned = true;
   }

   public int IndexOf(String EnemyName){
      for (int i=0; i < Enemies.size();i++){
         if (((Enemy) Enemies.elementAt(i)).Death)
            continue;
         if (((Enemy) Enemies.elementAt(i)).Name == EnemyName){
            return i;
         }
      }
      return -1;
   }

   private void CalcEnemyTargetScore(Enemy en){
      double score = 0.0;

      if ((!en.Death) && (en.Time() != -1)){
         // huidige target bonus + 100 (draaien turret, minder uitlokken, 1 probleem tegelijkertijd)
         if (en == yngwie.gunner.Target)
            score += Math.max(50.0,10.0 * (15.0 - yngwie.GetCoolingDownTurns()));

         score -= Math.max(200.0,yngwie.getTime()-en.Time());

         double dist = yngwie.Distance(en);
         // distance bonus 0 tot 1200 (makkelijker te raken, gevaarlijker)
         score += (cMaxScannerRange - yngwie.Distance(en));
         // very close range bonus
         score += Math.max(0.0,600.0 - (2.0*dist));

         // hoe minder energy hoe beter (bonus, ruimte)
         score += (1.5 * (100.0 - en.Energy()));
         // hoe minder energy hoe beter (bonus, ruimte)
         score += My.sqr(Math.max(0.0,15.0 - en.Energy()));
         // als enemy disabled is, snel afmaken voor de bonus
         if (en.Energy() <= 0.1) score += 100.0;
         // hoe easy de enemy is bonus -? tot 200 (makkelijker te raken, bonus punten, extra energy)
         //      score += Math.min(100.0,en.EasyLevel());
         // oog-om-oog (uitschakelijken agressor)
         score += (4.0 * en.BulletDamage);
         score += Math.max(0.0,200.0 - 2.0*(yngwie.getTime() - en.LastTimeHitMe));
         score = Math.max(0.0,score); // voor de zekerheid
//         yngwie.out.println(yngwie.getTime()+" target "+en.Name+" score : "+score);
      }
      en.TargetScore = score;
      return;
   }

   public Enemy getBestEnemy(){
      Enemy result = null;
      double MaxScore = 0.0;
      double Score;
      Enemy en;
      for (int i=0; i < Enemies.size();i++){
         en = (Enemy) Enemies.elementAt(i);
         if (ScoresRecalculated != yngwie.getTime())
            CalcEnemyTargetScore(en);
         Score = en.TargetScore;

         if (Score > MaxScore)
         {
            MaxScore = Score;
            result = en;
         }
      }
      ScoresRecalculated = yngwie.getTime();
      return result;
   }
}
