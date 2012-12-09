package shrub;

import java.text.NumberFormat;

/**
 * RoundStats - gathers a set of stats for the current round.
 */
public class RoundStats
{
//    private long mTotalLoopTime = 0;
//    private long mMaxLoopTime = 0;
//    private long mMinLoopTime = 999999;
//    private double mAvgLoopTime = 0;
//    private long mTotalExecTime = 0;
//    private long mMaxExecTime = 0;
//    private long mMinExecTime = 999999;
//    private double mAvgExecTime = 0;
    private long mNumTotalTurns = 0;
    private long mNumTurnsSkipped = 0;
    private long mNumShotsFired = 0;
    private long mNumShotsHit = 0;
    private long mNumShotsMissed = 0;
    private long mNumBulletCollisions = 0;
    private long mFirstShotTime = 0;
    private long mLastShotTime = 0;
    private double mAvgeShotPower = 0.0;
    private double mAvgeShotInterval = 0.0;
    private long mNumNoLockOn = 0;

    private BattleStats mBattleStatsRef = null; // Optional battle stats ref.
    private Body mBodyRef = null;               // Optional body component ref.

    private NumberFormat dp2 = NumberFormat.getInstance();

    public RoundStats(final BattleStats battleStatsRef,
                      final Body bodyRef)
    {
        Initialise();
        mBattleStatsRef = battleStatsRef;
        mBodyRef = bodyRef;
    }

    public void Initialise()
    {
        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);

//        mTotalLoopTime = 0;
//        mMaxLoopTime = 0;
//        mMinLoopTime = 999999;
//        mAvgLoopTime = 0;
//        mTotalExecTime = 0;
//        mMaxExecTime = 0;
//        mMinExecTime = 999999;
//        mAvgExecTime = 0;
        mNumTotalTurns = 0;
        mNumTurnsSkipped = 0;
        mNumShotsFired = 0;
        mNumShotsHit = 0;
        mNumShotsMissed = 0;
        mNumBulletCollisions = 0;
        mFirstShotTime = 0;
        mLastShotTime = 0;
        mAvgeShotPower = 0.0;
        mAvgeShotInterval = 0.0;
        mNumNoLockOn = 0;
    }

    public void ShotHit()
    {
        mNumShotsHit++;
    }

    public void ShotMissed()
    {
        mNumShotsMissed++;
    }

    public void BulletCollision()
    {
        mNumBulletCollisions++;
    }

    public void SkippedTurn()
    {
        mNumTurnsSkipped++;
    }

    public void NoLockOn()
    {
        mNumNoLockOn++;
    }

    public void ShotFired(final long thisTime, final double thisPower)
    {
        // mNumShotsFired holds number fired PRIOR to this event.

        // Note also that average interval is calculated starting from
        // the first bullet fire time, to eliminate effect of relatively
        // long time before first allowed to fire.
        
        if (mNumShotsFired <= 0)
        {
            mAvgeShotInterval = thisTime;
            mAvgeShotPower = thisPower;
            mNumShotsFired = 1;
            mFirstShotTime = thisTime;
            mLastShotTime = thisTime;
        }
        else if (mNumShotsFired == 1)
        {
            mNumShotsFired++;

            mAvgeShotPower = (mAvgeShotPower + thisPower) / 2.0;
            mAvgeShotInterval = thisTime - mLastShotTime;

            mLastShotTime = thisTime;                      
        }
        else
        {
            long thisInterval = thisTime - mLastShotTime;

            double totalPowerSoFar =
                (mAvgeShotPower * mNumShotsFired) + thisPower;
            double totalIntervalSoFar =
                (mAvgeShotInterval * (mNumShotsFired - 1)) + thisInterval;

            mNumShotsFired++;

            mAvgeShotPower = totalPowerSoFar / mNumShotsFired;
            mAvgeShotInterval = totalIntervalSoFar / (mNumShotsFired - 1);

            mLastShotTime = thisTime;                      
        }
    }

    /*???
    private void UpdateLoopTimeStats(final long numMs, long iterationNum)
    {
        mTotalLoopTime += numMs;

        if (numMs > mMaxLoopTime)
        {
            mMaxLoopTime = numMs;
        }
        if (numMs < mMinLoopTime)
        {
            mMinLoopTime = numMs;
        }

        mAvgLoopTime = mTotalLoopTime / iterationNum;

//        System.out.println("LoopStats: ms " + numMs +
//                           ", num " + iterationNum +
//                           ", total " + mTotalLoopTime +
//                           ", max " + mMaxLoopTime +
//                           ", min " + mMinLoopTime +
//                           ", avg " + dp2.format(mAvgLoopTime));
    }
    */

    /*???
    private void UpdateExecTimeStats(final long numMs, long iterationNum)
    {
        mTotalExecTime += numMs;

        if (numMs > mMaxExecTime)
        {
            mMaxExecTime = numMs;
        }
        if (numMs < mMinExecTime)
        {
            mMinExecTime = numMs;
        }

        mAvgExecTime = mTotalExecTime / iterationNum;

//        System.out.println("ExecStats: ms " + numMs +
//                           ", num " + iterationNum +
//                           ", total " + mTotalExecTime +
//                           ", max " + mMaxExecTime +
//                           ", min " + mMinExecTime +
//                           ", avg " + dp2.format(mAvgExecTime));
    }
    */

    public void EndRound(final long timeNow)
    {
        mNumTotalTurns = timeNow + 1;
        Print();

        if (mBattleStatsRef != null)
        {
            mBattleStatsRef.EndRound(mNumTotalTurns,
                                     mNumTurnsSkipped,
                                     mNumShotsFired,
                                     mNumShotsHit,
                                     mNumShotsMissed,
                                     mNumBulletCollisions);
        }
    }

    public void Print()
    {
        double hitPct = 0.0;
        long totalResolvedShots = mNumShotsHit + mNumShotsMissed;
        if (totalResolvedShots > 0)
        {
            hitPct = 100.0 * mNumShotsHit / totalResolvedShots;
        }

        double skipPct = 0.0;
        if (mNumTotalTurns > 0)
        {
            skipPct = 100.0 * mNumTurnsSkipped / mNumTotalTurns;
        }

        long numWpTimeouts = 0;
        long numTimesRammer = 0;
        long numTimesRammee = 0;
        long numTimesHitWall = 0;
        long numTimesHitByBullet = 0;

        if (mBodyRef != null)
        {
            numWpTimeouts = mBodyRef.GetNumWaypointTimeouts();
            numTimesRammer = mBodyRef.GetNumTimesRammer();
            numTimesRammee = mBodyRef.GetNumTimesRammee();
            numTimesHitWall = mBodyRef.GetNumTimesHitWall();
            numTimesHitByBullet = mBodyRef.GetNumTimesHitByBullet();
        }

        System.out.println();
        System.out.println("========== Round stats ==========");
        System.out.println("---My shooting---");
        System.out.println("Shots/HitBlt/Remain: " +
                           mNumShotsFired + " / " +
                           mNumBulletCollisions + " / " +
                           (mNumShotsFired - mNumBulletCollisions));
        System.out.println("Hit/Missed/HitPct:   " +
                           mNumShotsHit + " / " +
                           mNumShotsMissed + " / " +
                           dp2.format(hitPct) + "%");
        System.out.println("AvgTime/AvgPower:    " +
                           dp2.format(mAvgeShotInterval) + " / " +
                           dp2.format(mAvgeShotPower));
        System.out.println("FirstShot/LastShot:  " +
                           mFirstShotTime + " / " +
                           mLastShotTime);
        if (mBodyRef != null)
        {
            System.out.println("---Collisions---");
            System.out.println("Blt/Ram/Rammed/Wall: " +
                               numTimesHitByBullet + " / " +
                               numTimesRammer + " / " +
                               numTimesRammee + " / " +
                               numTimesHitWall);
        }
        System.out.println("---Other stuff---");
        System.out.println("Timeouts/NoLock:     " +
                           numWpTimeouts + " / " +
                           mNumNoLockOn);
        System.out.println("Turns/Skips/SkipPct: " +
                           mNumTotalTurns + " / " +
                           mNumTurnsSkipped + " / " +
                           dp2.format(skipPct) + "%");
        /*???
        System.out.println("Avg/Min/Max in:    " +
                           dp2.format(mAvgLoopTime) + " / " +
                           mMinLoopTime + " / " +
                           mMaxLoopTime);
        System.out.println("Avg/Min/Max out:   " +
                           dp2.format(mAvgExecTime) + " / " +
                           mMinExecTime + " / " +
                           mMaxExecTime);
        */
    }
}
