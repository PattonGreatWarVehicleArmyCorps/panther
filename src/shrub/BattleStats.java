package shrub;

import java.text.NumberFormat;

/**
 * BattleStats - gathers a set of stats for the whole battle.
 */
public class BattleStats
{
    private static long mNumTotalTurns = 0;
    private static long mNumTurnsSkipped = 0;
    private static long mNumShotsFired = 0;
    private static long mNumShotsHit = 0;
    private static long mNumShotsMissed = 0;
    private static long mNumBulletCollisions = 0;

    private static NumberFormat dp2 = NumberFormat.getInstance();

    public BattleStats()
    {
        Initialise();
    }

    public void Initialise()
    {
        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);

        mNumTotalTurns = 0;
        mNumTurnsSkipped = 0;
        mNumShotsFired = 0;
        mNumShotsHit = 0;
        mNumShotsMissed = 0;
        mNumBulletCollisions = 0;
    }

    public void EndRound(final long numTotalTurns,
                         final long numTurnsSkipped,
                         final long numShotsFired,
                         final long numShotsHit,
                         final long numShotsMissed,
                         final long numBulletCollisions)
    {
        mNumTotalTurns += numTotalTurns;
        mNumTurnsSkipped += numTurnsSkipped;
        mNumShotsFired += numShotsFired;
        mNumShotsHit += numShotsHit;
        mNumShotsMissed += numShotsMissed;
        mNumBulletCollisions += numBulletCollisions;

        Print();
    }

    public void Print()
    {
        double hitPct = 0.0;
        long totalResolvedShots = mNumShotsHit + mNumShotsMissed;
        if (totalResolvedShots > 0)
        {
            hitPct = 100.0 * mNumShotsHit / totalResolvedShots;
        }

        double skipPct = 100.0 * mNumTurnsSkipped / mNumTotalTurns;

        System.out.println();
        System.out.println("========== Battle stats ==========");
        System.out.println("Shots/HitBlt/Remain: " +
                           mNumShotsFired + " / " +
                           mNumBulletCollisions + " / " +
                           (mNumShotsFired - mNumBulletCollisions));
        System.out.println("Hit/Missed/HitPct:   " +
                           mNumShotsHit + " / " +
                           mNumShotsMissed + " / " +
                           dp2.format(hitPct) + "%");        
        System.out.println("Turns/Skips/SkipPct: " +
                           mNumTotalTurns + " / " +
                           mNumTurnsSkipped + " / " +
                           dp2.format(skipPct) + "%");
        System.out.println();
    }
}
