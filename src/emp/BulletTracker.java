package emp;

import robocode.*;

public class BulletTracker {

   public Bullet bullet;
   public String enemy;
   public int Strategy;
   public double StartX;
   public double StartY;
   public double EnemyX;
   public double EnemyY;
   public double EnemyVelocity;
   public double EnemyHeading;
   public double EnemyBearing;
   public double Distance;
   public double Power;
   public long Counter;
   public boolean bulletDead;
   public boolean completed;
   public boolean OneOnOne;

   public BulletTracker(Bullet b, int Strat,String N,boolean vOneOnOne){
      bullet = b;
      Power = b.getPower();
      Strategy = Strat;
      enemy = N;
      bulletDead = false;
      completed = false;
      OneOnOne = vOneOnOne;
   }

   public void SetPosition(double vStartX, double vStartY, double vEndX, double vEndY)
   {
      StartX   = vStartX;
      StartY   = vStartY;
      EnemyX   = vEndX;
      EnemyY   = vEndY;
      Distance = My.Distance(vStartX, vStartY, vEndX, vEndY);
      Counter  = (long) Math.floor(Distance / My.getBulletVelocity(Power));
   }
}