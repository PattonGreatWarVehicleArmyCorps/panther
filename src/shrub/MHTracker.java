package shrub;
import java.util.Vector;

/**
 * MHTracker - handles multiple targets, and maintains a history of each.
 */
public class MHTracker implements TrackerAPI
{
    private Radar mRadarRef = new Radar();

    // Null means no clipping of extrapolated moves.
    private Box mMovementArea = null;

    private Heading mGunHdng = new Heading(); 
    private Location mTrackerLocn = new Location();
    private Vector mTargetList = new Vector();
    private int mCurrentTargetIndex = -1;
    private int mNumTargets = 0;
    private long mTimeNow = 0;

    private long mNumTargetChanges = 0;

    private int mMaxHistory = 100;
    private boolean mChangeTargetAllowed = true;
    private boolean mPhasorResetOnTargetChange = false;
    private long mStalenessLimit = 20;

    private double mEvalStaleCoeff = 5.0;
    private double mEvalSpeedCoeff = 5.0;
    private double mEvalAngleCoeff = 1.0;
    private double mEvalDistanceCoeff = 1.0;
    private double mEvalDisabledThreshold = 0.5;
    private double mEvalDisabledBonus = 100.0;
    private double mEvalLowEnergyThreshold = 5.0;
    private double mEvalLowEnergyBonus = 0.0;

    private int mExtrapolationInterval = 1;  // 0 means never extrapolate.

    public MHTracker()
    {
        Initialise();
    }

    public void Initialise()
    {
        Radar mRadarRef = new Radar();

        mMovementArea = null;

        mGunHdng = new Heading(); 
        mTrackerLocn = new Location();
        mTargetList = new Vector();
        mCurrentTargetIndex = -1;
        mNumTargets = 0;
        mTimeNow = 0;

        mNumTargetChanges = 0;

        mMaxHistory = 100;
        mChangeTargetAllowed = true;
        mPhasorResetOnTargetChange = false;
        mStalenessLimit = 20;

        mEvalStaleCoeff = 5.0;
        mEvalSpeedCoeff = 5.0;
        mEvalAngleCoeff = 1.0;
        mEvalDistanceCoeff = 1.0;
        mEvalDisabledThreshold = 0.5;
        mEvalDisabledBonus = 100.0;
        mEvalLowEnergyThreshold = 5.0;
        mEvalLowEnergyBonus = 0.0;

        mExtrapolationInterval = 1;
    }

    public void SetRadarRef(Radar radarRef)
    {
        mRadarRef = radarRef;
    }

    public void SetMovementArea(final Box movementArea)
    {
        mMovementArea = null;
        if (movementArea != null)
        {
            mMovementArea = new Box();
            mMovementArea.Set(movementArea);
        }
    }

    public void SetTrackerLocn(final Location trackerLocn)
    {
        mTrackerLocn.Set(trackerLocn);
    }

    public void SetTrackerLocn(final double xPos, final double yPos)
    {
        mTrackerLocn.Set(xPos, yPos);
    }

    public void SetGunHdng(final Heading gunHdng)
    {
        mGunHdng.Set(gunHdng);
    }

    public void SetGunHdng(final double gunHdng)
    {
        mGunHdng.Set(gunHdng);
    }

    public void SetPhasorResetOnTargetChange(final boolean newValue)
    {
        mPhasorResetOnTargetChange = newValue;
    }

    public void SetStalenessLimit(long newValue)
    {
        mStalenessLimit = newValue;
    }

    public void SetEvalStaleCoeff(double newValue)
    {
        mEvalStaleCoeff = newValue;
    }

    public void SetEvalAngleCoeff(double newValue)
    {
        mEvalAngleCoeff = newValue;
    }

    public void SetEvalSpeedCoeff(double newValue)
    {
        mEvalSpeedCoeff = newValue;
    }

    public void SetEvalDistanceCoeff(double newValue)
    {
        mEvalDistanceCoeff = newValue;
    }

    public void SetEvalDisabledThreshold(double newValue)
    {
        mEvalDisabledThreshold = newValue;
    }

    public void SetEvalDisabledBonus(double newValue)
    {
        mEvalDisabledBonus = newValue;
    }

    public void SetEvalLowEnergyThreshold(double newValue)
    {
        mEvalLowEnergyThreshold = newValue;
    }

    public void SetEvalLowEnergyBonus(double newValue)
    {
        mEvalLowEnergyBonus = newValue;
    }

    public void SetMaxHistory(int newValue)
    {
        mMaxHistory = newValue;
    }

    public void SetExtrapolationInterval(int newValue)
    {
        mExtrapolationInterval = newValue;
    }

    public int GetCurrentTargetIndex()
    {
        return mCurrentTargetIndex;
    }

    public long GetNumTargetChanges()
    {
        return mNumTargetChanges;
    }

    public final boolean ExtrapolateCurrentTarget(final long toTime)
    {
        boolean extrapolated = false;

        extrapolated = ExtrapolateTarget(mCurrentTargetIndex, toTime);

        return extrapolated;
    }

    public final boolean ExtrapolateTarget(final int index, final long toTime)
    {
        boolean extrapolated = false;

//        System.out.println();
//        System.out.println("===== ExtrapolateTarget " + index +
//                           " to time " + toTime);

        if ((index >= 0) && (index < mNumTargets))
        {
            MoveHistory target = (MoveHistory)mTargetList.get(index);
            if (target != null)
            {
                extrapolated = target.Extrapolate(toTime);
            }
            else
            {
                System.out.println("ERROR! Tracker::ExtrapolateTarget, " +
                                   "null pointer at " + index);
            }
        }
        else
        {
            System.out.println("ERROR! Tracker::ExtrapolateTarget, " +
                               "bad index " + index);
        }

        return extrapolated;
    }

    public final boolean ExtrapolateAll(final long toTime)
    {
        boolean okSoFar = true;
        int index = 0;

//        System.out.println();
//        System.out.println("=== ExtrapolateAll() at time " +  toTime);
//        this.Print();

        if (mExtrapolationInterval > 0)
        {
            while (index < mNumTargets)
            {
                if ((mTimeNow % mExtrapolationInterval) ==
                    (index % mExtrapolationInterval))
                {    
                    okSoFar &= ExtrapolateTarget(index, toTime);
                }
                index++;
            }
        }

        return okSoFar;
    }

    // Report a sighting, without overriding Tracker choice
    // of what is considered the current target.
    public void ReportSighting(Sighting newSighting)
    {
        int posn = -1;
        final String targetName = newSighting.GetName();

//        System.out.println();
//        System.out.println("===== MHTracker::ReportSighting() =====");
//        System.out.println(" Reporting [" + targetName + "]");
//        newSighting.Print();

        posn = FindNamedTarget(targetName);

        if ((posn >= 0) && (posn < mNumTargets))
        {
            // Known target, add new info to its history.
            UpdateTargetAt(posn, newSighting);
        }
        else
        {
            // Unnown target, add to end of list.
            AddNewTarget(newSighting);
        }
    }

    public final int FindNamedTarget(final String searchName)
    {
        int answer = -1;   // Note: -ve indicates not found.
        int index = 0;
        boolean found = false;

        while (!found && (index < mNumTargets))
        {
            final MoveHistory target = (MoveHistory)mTargetList.get(index);
            final String targetName = target.GetName();

            if (targetName.equals(searchName))
            {
                found = true;
                answer = index;
            }

            index++;
        }

        return answer;
    }

    //??? Could maybe optimise this by getting last extrapolation for each
    //??? and comparing (timeNow - timeThen) + estimationTime
    public final int GetOldestTargetIndex()
    {
        int thisIndex = 0;
        double oldestTimeSoFar = 999999.9;
        int oldestIndexSoFar = -1;  // Note: -ve indicates not found.      

        while (thisIndex < mNumTargets)
        {
            final MoveHistory thisTarget =
                                (MoveHistory)mTargetList.get(thisIndex);
            final Movement lastSight = thisTarget.GetLastNonEstimate();

            if (lastSight != null)
            {
                long thisSightTime = lastSight.GetTimeNow();

                // Is this the oldest yet?
                if (thisSightTime < oldestTimeSoFar)
                {
                    oldestTimeSoFar = thisSightTime;
                    oldestIndexSoFar = thisIndex;
                }
            }

            thisIndex++;
        }

        return oldestIndexSoFar;
    }

    public final int GetNumFreshTargets()
    {
        int thisIndex = 0;
        int numFresh = 0; 

        while (thisIndex < mNumTargets)
        {
            final MoveHistory thisTarget =
                                (MoveHistory)mTargetList.get(thisIndex);
            final Movement lastMove = thisTarget.GetLastMove();

            if (lastMove != null)
            {
                long thisTime = lastMove.GetTimeNow();
                long thisEstimationTime = lastMove.GetEstimationTime();

                long staleness = (mTimeNow - thisTime) + thisEstimationTime;

                if (staleness <= mStalenessLimit)
                {
                    numFresh++;
                }
            }

            thisIndex++;
        }

        return numFresh;
    }

    private final int FindClosestFreshTarget()
    {
        int thisIndex = 0;
        double closestDistanceSoFar = 999999.9;
        int closestIndexSoFar = -1;  // Note: -ve indicates not found.      

        while (thisIndex < mNumTargets)
        {
            final MoveHistory thisTarget =
                                (MoveHistory)mTargetList.get(thisIndex);
            final Movement lastMove = thisTarget.GetLastMove();

            // Check how fresh the data is, if too old ignore.
            long staleness = lastMove.GetEstimationTime();

            if ((staleness < mStalenessLimit) && (staleness >= 0))
            { 
                final Location thisLocn = lastMove.GetLocnNow();
                double thisDistance = thisLocn.DistanceTo(mTrackerLocn);

                // Is this the closest yet?
                if (thisDistance < closestDistanceSoFar)
                {
                    closestDistanceSoFar = thisDistance;
                    closestIndexSoFar = thisIndex;
                }
            }
            else if (staleness < 0)
            {
                System.out.println("Tracker error!!! - target in future");
            } 

            thisIndex++;
        }

        return closestIndexSoFar;
    }

    public double ClosestTargetDistance()
    {
        int index = 0;
        double answer = 999999.0;

        if (mNumTargets > 0)
        {
            index = FindClosestFreshTarget();
            if ((index >= 0) && (index < mNumTargets))
            {
                MoveHistory target = (MoveHistory)mTargetList.get(index);
                Movement lastMove = target.GetLastMove();
                Location locn = lastMove.GetLocnNow();
                answer = locn.DistanceTo(mTrackerLocn);
            }
        }

        return answer;
    }

    private void AddNewTarget(Sighting newSighting)
    {
        MoveHistory newTarget = new MoveHistory();
        newTarget.SetName(newSighting.GetName());
        newTarget.SetMaxMoves(mMaxHistory);
        newTarget.SetMovementArea(mMovementArea);
        newTarget.ReportSighting(newSighting);
        mTargetList.add(newTarget);
        mNumTargets = mTargetList.size();
    }

    private void UpdateTargetAt(final int index, 
                                Sighting newSighting)
    {
        if ((index >= 0) && (index < mNumTargets))
        {
            MoveHistory thisTarget = (MoveHistory)mTargetList.get(index);
            thisTarget.ReportSighting(newSighting);
        }
    }

    public void RemoveAll()
    {
        int index = 0;
        while (index < mNumTargets)
        {
            RemoveTargetAt(index);
            index++;
        }
    }

    private void RemoveTargetAt(final int index)
    {
        // If target being removed is current target,
        // clear current target.
        if (index == mCurrentTargetIndex)
        {
            mCurrentTargetIndex = -1;
        }

        mTargetList.remove(index);
        mNumTargets = mTargetList.size();
    }

    public void SetCurrentTargetIndex(final int index)
    {
        if ((index >= 0) && (index < mNumTargets))
        {
            mCurrentTargetIndex = index;

            mNumTargetChanges++;
        }
        else
        {
            System.out.println("ERROR! Tracker::SetCurrentTargetIndex, " +
                               "bad index " + index);
        }
    }

    public void ClearCurrentTarget()
    {
        mCurrentTargetIndex = -1;
    }

    public void SetCurrentTime(final long newTime)
    {
        mTimeNow = newTime;
    }

    public final int GetNumTargets()
    {
        return mNumTargets;
    }

    // Return a Sighting record containing last known movement details of
    // current target. This is NOT a reference into internal class data,
    // so caller is free to do with it what it will.
    public final Sighting GetCurrentTarget()
    {
        MoveHistory target = null;
        Movement lastMove = null;
        Sighting sightingRef = null;

        if (HasLockOn())
        {
            // Get last movement record for current target.
            target = (MoveHistory)mTargetList.get(mCurrentTargetIndex);
            lastMove = target.GetLastMove();
            
            // It is safe to pass a Sighting record out, as this is new
            // memory allocated and populated by the ToSighting method.
            // Thus this is not giving access to internal class data.
            sightingRef = lastMove.ToSighting();

            // We need to add the name into the sighting at this level,
            // because it is not stored inside the movement history.
            sightingRef.SetName(target.GetName());
        }

        return sightingRef;
    }

    // Return a Sighting record containing last known movement details of
    // current target. This is NOT a reference into internal class data,
    // so caller is free to do with it what it will.
    public final Sighting GetTargetByIndex(final int index)
    {
        MoveHistory target = null;
        Movement lastMove = null;
        Sighting sightingRef = null;

        if ((index >= 0) && (index < mNumTargets))
        {
            // Get last movement record for specified target.
            target = (MoveHistory)mTargetList.get(index);
            lastMove = target.GetLastMove();
    
            // It is safe to pass a Sighting record out, as this is new
            // memory allocated and populated by the ToSighting method.
            // Thus this is not giving access to internal class data.
            sightingRef = lastMove.ToSighting();
     
            // We need to add the name into the sighting at this level,
            // because it is not stored inside the movement history.
            sightingRef.SetName(target.GetName());
        }

        return sightingRef;
    }

    // Note: this returns a reference into internal data, use with caution.
    public final MoveHistory GetCurrentTargetHistory()
    {
        MoveHistory targetHist = null;
        if (HasLockOn())
        {
            targetHist = (MoveHistory)mTargetList.get(mCurrentTargetIndex);
        }
        return targetHist;
    }

    // Note: this returns a reference into internal data, use with caution.
    public final MoveHistory GetHistoryByIndex(final int index)
    {
        MoveHistory targetHist = null;

        if ((index >= 0) && (index < mNumTargets))
        {
            targetHist = (MoveHistory)mTargetList.get(index);
        }
        else
        {
            System.out.println("ERROR: MHTracker.GetHistoryByIndex(), " +
                               " index " + index + " out of range");
        }

        return targetHist;
    }

    public final long LockOnStaleness()
    {
        long answer = 0;

        if (HasLockOn())
        {
            final Sighting targetRef = GetCurrentTarget();
            long lockOnTimestamp = targetRef.GetTimestamp();            
            answer = mTimeNow - lockOnTimestamp;
        }
        else
        {
            // If not currently locked on, effectively extremely stale!
            answer = 999999;
        }

        return answer;
    }

    public final boolean HasLockOn()
    {
        boolean answer = false;

        if ((mCurrentTargetIndex >= 0) &&
            (mCurrentTargetIndex < mNumTargets))
        {
            MoveHistory target = (MoveHistory)mTargetList.get(
                                                    mCurrentTargetIndex);
            if (target != null) 
            {
                if (target.IsEmpty() == false)
                {
                    answer = true;
                }
                else
                {
                    System.out.println("ERROR! Tracker::HasLockOn, " +
                                       "empty list at " + mCurrentTargetIndex);
                }
            }
            else
            {
                System.out.println("ERROR! Tracker::HasLockOn, " +
                                   "null pointer at " + mCurrentTargetIndex);
            }
        }

        return answer;
    }

    public void ReportRobotDeath(final String deathName)
    {
        // Remove this target from our list, if we have it on record.
        int posn = FindNamedTarget(deathName);
        if (posn >= 0)
        {
            RemoveTargetAt(posn);
        }
    }

    // Choose "best" target by evaluating a number of weighted criteria.
    public void ChooseBestTarget()
    {
        if (mChangeTargetAllowed == true)
        {
            int index = EvaluateBestTarget();

            // If chosen target differs from the current one, change.
            if ((index != mCurrentTargetIndex) &&
                (index >= 0) &&
                (index < mNumTargets))
            {
                // Change current target as indicated.
                SetCurrentTargetIndex(index);

                if (mPhasorResetOnTargetChange)
                {
                    // Reset phasor, to attune it with new target.
                    mRadarRef.GetPhasor().SetTimeStarted(mTimeNow);
                }
            }
            else
            {
                // No suitable target - best stick with current one,
                // if we have one, unless it is really out of date!
                // No change needed...
                if (HasLockOn() &&
                    (LockOnStaleness() > (mStalenessLimit)))
                {

                    final Sighting target = GetCurrentTarget();

                    System.out.println("Stale lock on " + target.GetName() +
                                       ", at time " + mTimeNow);

                    mCurrentTargetIndex = -1;
                }
            }
        }
    }

    private final int EvaluateBestTarget()
    {
        int thisIndex = 0;
        double bestEvalSoFar = 999999;  // lower indicates better.
        int bestIndexSoFar = -1;  // Note: -ve indicates not found.      

        while (thisIndex < mNumTargets)
        {
            MoveHistory target = (MoveHistory)mTargetList.get(thisIndex);
            final Movement lastMove = target.GetLastMove();

            // Check how fresh the data is, if too old ignore.
            // also disregard anything with a future timestamp!
            long staleness = lastMove.GetEstimationTime();

            //??? do we need to worry much about stale target?
            //??? lowers evaluation anyway, see equation below...
            if ((staleness < mStalenessLimit) && (staleness >= 0))
            { 
                final Location targetLocn = lastMove.GetLocnNow();
                double targetDist = targetLocn.DistanceTo(mTrackerLocn);
                double targetEnergy = lastMove.GetEnergyNow();
                double targetSpeed = Math.abs(lastMove.GetVelocityNow());

                Heading targetHdng = new Heading();
                targetHdng.SetFromTo(mTrackerLocn, targetLocn);

                Bearing angleOffGun = new Bearing();
                angleOffGun.SetFromTo(mGunHdng, targetHdng);

                double energyFactor = 0;
                if (targetEnergy < mEvalDisabledThreshold)
                {
                    energyFactor = mEvalDisabledBonus;
                }
                else if (targetEnergy < mEvalLowEnergyThreshold)
                {
                    energyFactor = mEvalLowEnergyBonus;
                }

                // Evaluate this target according to the "goodness" function.
                double thisEval = (mEvalDistanceCoeff * targetDist) +
                                  (mEvalAngleCoeff * angleOffGun.GetAbs()) +
                                  (mEvalSpeedCoeff * targetSpeed) +
                                  (mEvalStaleCoeff * staleness) -
                                  energyFactor;

                // Is this the best yet?
                if (thisEval < bestEvalSoFar)
                {
                    bestEvalSoFar = thisEval;
                    bestIndexSoFar = thisIndex;
                }
            }
            else if (staleness < 0)
            {
                System.out.println("Tracker error!!! - target in future");
            } 

            thisIndex++;
        }

        return bestIndexSoFar;
    }

    // Determine whether we think an enemy has fired at us this turn.
    public double BulletFireDetection()
    {
        double answer = -1;

        if (HasLockOn())
        {
            MoveHistory target =
                         (MoveHistory)mTargetList.get(mCurrentTargetIndex);

            // Reverse sign of energy delta, i.e. we are interested in a drop
            // in energy level, let's think about that as being +ve.
            double energyDrop = -1.0 * target.GetEnergyDelta(mTimeNow);

            double minBulletDrop = Gun.MinLegalPower();
            double maxBulletDrop = Gun.MaxLegalPower();

            // Rather simplistic approach, can complicate it later,
            // i.e. by accounting for our bullets hitting target, hitting
            // walls, etc.
            if ((energyDrop > (minBulletDrop - 0.05)) &&
                (energyDrop < (maxBulletDrop + 0.05)))
            {
                answer = energyDrop;
            }
        }

        return answer;
    }

    public void Print()
    {
        String lockedOnName = "{none}";

        System.out.println("===== MHTracker =====");
        System.out.println("timeNow: " + mTimeNow);
        System.out.println("numTargets: " + mNumTargets);
        if (HasLockOn() == true)
        {
            /*???
            MoveHistory target = (MoveHistory)mTargetList.get(
                                                     mCurrentTargetIndex);
            lockedOnName = target.GetName();
            */

            lockedOnName = GetCurrentTarget().GetName();
        }
        System.out.println("lockedOn: [" + lockedOnName + "]");
    }
}
