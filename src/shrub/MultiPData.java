package shrub;
import java.util.Vector;

/**
 * MultiPData - handles periodicity checking for multiple targets.
 */
public class MultiPData
{
    private Vector mTargetList = new Vector();
    private int mNumTargets = 0;
    private int mCurrentTargetIndex = -1;
    private int mNumTargetsPerGo = 1;

    private int mMinPeriod = 20;
    private int mPeriodStep = 1;
    private int mNumPeriods = 60;
    private double mValueThreshold = 50;

    private MHTracker mTrackerRef = null;

    public MultiPData()
    {
        Initialise();
    }

    public void Initialise()
    {
        mTargetList = new Vector();
        mNumTargets = 0;
        mCurrentTargetIndex = -1;
    }

    public void SetTrackerRef(MHTracker trackerRef)
    {
        mTrackerRef = trackerRef;
    }

    // Note - changing this value does not automatically ripple down
    // to individual target data objects, they must be recreated to
    // pick up the new value of this setting (internal array size changes).
    public void SetMinPeriod(final int newValue)
    {
        mMinPeriod = newValue;
    }

    // Note - changing this value does not automatically ripple down
    // to individual target data objects, they must be recreated to
    // pick up the new value of this setting (internal array size changes).
    public void SetPeriodStep(final int newValue)
    {
        mPeriodStep = newValue;
    }

    // Note - changing this value does not automatically ripple down
    // to individual target data objects, they must be recreated to
    // pick up the new value of this setting (internal array size changes).
    public void SetNumPeriods(final int newValue)
    {
        mNumPeriods = newValue;
    }

    // Changing this value immediately ripples through to all
    // individual target data objects for immediate use.
    public void SetValueThreshold(final double newValue)
    {
        mValueThreshold = newValue;

        int index = 0;
        while (index < mNumTargets)
        {
            TargetPData thisTarget = (TargetPData)mTargetList.get(index);
            thisTarget.SetValueThreshold(mValueThreshold);
            index++;
        }
    }

    // This setting controls how many periodicity values we evaluate
    // in one call. If fewer than that number of targets in the list
    // we will evaluate multiple periods for one or more of our targets.
    public void SetNumTargetsPerGo(final int newValue)
    {
        mNumTargetsPerGo = newValue;
    }

    // Report a sighting of a target, create new list entry
    // if we haven't already got one for this target.
    public void ReportSighting(final String targetName)
    {
        int posn = -1;

//        System.out.println();
//        System.out.println("===== MultiPData::ReportSighting() =====");
//        System.out.println(" Reporting [" + targetName + "]");

        posn = FindNamedTarget(targetName);

        if ((posn >= 0) && (posn < mNumTargets))
        {
            // Known target, no action required.
        }
        else
        {
            // Unnown target, add to end of list.
            AddNewTarget(targetName);
        }
    }

    private final int FindNamedTarget(final String searchName)
    {
        int answer = -1;   // Note: -ve indicates not found.
        int index = 0;
        boolean found = false;

        while (!found && (index < mNumTargets))
        {
            final TargetPData target = (TargetPData)mTargetList.get(index);
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

    private void AddNewTarget(final String targetName)
    {
//        System.out.println();
//        System.out.println("MultiPData.AddNewTarget() - " + targetName);

        // Create new PData object for use with this target.
        TargetPData newPData = new TargetPData();
        newPData.SetName(targetName);

        // Set parameters for how periodicity is determined.
        newPData.SetMinPeriod(mMinPeriod);
        newPData.SetPeriodStep(mPeriodStep);
        newPData.SetNumPeriods(mNumPeriods);
        newPData.SetValueThreshold(mValueThreshold);
        newPData.InitialiseData();
	// Pass in reference to the associated move history for later use.
        int index = mTrackerRef.FindNamedTarget(targetName);
        MoveHistory targetHistory = mTrackerRef.GetHistoryByIndex(index);
        newPData.SetHistoryRef(targetHistory);

        // Now add our newly created object to our list of targets.
        mTargetList.add(newPData);
        mNumTargets = mTargetList.size();
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
        mTargetList.remove(index);
        mNumTargets = mTargetList.size();
    }

    public final int GetNumTargets()
    {
        return mNumTargets;
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

    public final int GetBestPeriodForTarget(final String targetName)
    {
//        System.out.println("*** MultiPData.GetBestPeriodForTarget() - " +
//                           targetName);
      
        // Returning negative index indicates no valid period found.
        int bestPeriod = -1;

        int index = FindNamedTarget(targetName);
        if ((index >= 0) && (index < mNumTargets))
        {
            // Known target, no action required.
            TargetPData pData = (TargetPData)mTargetList.get(index);
            bestPeriod = pData.GetBestPeriod();

            /*
            if (bestPeriod > 0)
            {
                System.out.println("[" + targetName + "], best period: " +
                                   bestPeriod + ", pValue: " +
                                   pData.GetValueForPeriod(bestPeriod));
            }
            else
            {
                System.out.println("[" + targetName + "], no periodicity");
            }
            */
        }

        return bestPeriod;
    }

    public void DoNextPeriodicityCalcs()
    {
//        System.out.println("*** MultiPData.DoNextPeriodicityCalcs()");

        int count = 0;
        while (count < mNumTargetsPerGo)
        {
            // Move on to next target *before* we do the calculations, as if
            // we do it after we run the risk of the array shrinking due to a
            // robot death before we do the calculation next time, and hence
            // risk array bounds overrun.
            mCurrentTargetIndex++;
            if ((mCurrentTargetIndex >= mNumTargets) ||
                (mCurrentTargetIndex < 0))
            {
                mCurrentTargetIndex = 0;
            }

            // Calculate the next periodicity value in sequence, for the next
            // target in sequence.
            if (mNumTargets > 0)
            {
                if ((mCurrentTargetIndex >= 0) &&
                    (mCurrentTargetIndex < mNumTargets))
                {
                    TargetPData pData = (TargetPData)mTargetList.get(
                                                         mCurrentTargetIndex);
                    pData.CalcNextPeriod();
                }
                else
                {
//                    System.out.println("ERROR: MultiPData.DoNextPCalc, " +
//                                       "index: " + mCurrentTargetIndex +
//                                       ", size: " + mNumTargets);
                }
            }

            count++;
        }
    }

    public void Print()
    {
        System.out.println("===== MultiPData =====");
        System.out.println("numTargets: " + mNumTargets);
    }
}
