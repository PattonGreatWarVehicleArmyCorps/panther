package shrub;

import java.util.Vector;
import java.text.NumberFormat;

/**
 * Navigator - Determination of movement strategies.
 */
public class Navigator
{
    private TrackerAPI mTrackerRef = null;
    private Box mArena = new Box();
    private double mWallAvoidDistance = 100.0;
    private Box mAllowedArea = new Box();
    private double mStepLength = 100.0;
    private double mStepLengthVar = 0.0;
    private int mNumPaths = 16;
    private double mPathHdngBase = 0.0;
    private double mPathHdngVar = 0.0;
    private double mEvalPowerLaw = 1.0;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public void Initialise()
    {
        mTrackerRef = null;   //??? pass in in constructor?

        mArena = new Box();
        mWallAvoidDistance = 100.0;
        mAllowedArea = new Box();

        mStepLength = 100.0;
        mStepLengthVar = 0.0;
        mNumPaths = 16;
        mPathHdngBase = 0.0;
        mPathHdngVar = 0.0;
        mEvalPowerLaw = 1.0;

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public void SetTrackerRef(final TrackerAPI trackerRef)
    {
        mTrackerRef = trackerRef;
    }

    public void SetArena(final Box arena)
    {
        mArena.Set(arena);
    }

    public void SetWallAvoidDistance(final double newValue)
    {
        mWallAvoidDistance = newValue;

        mAllowedArea.SetMinX(mArena.GetMinX() + mWallAvoidDistance);
        mAllowedArea.SetMinY(mArena.GetMinY() + mWallAvoidDistance);
        mAllowedArea.SetMaxX(mArena.GetMaxX() - mWallAvoidDistance);
        mAllowedArea.SetMaxY(mArena.GetMaxY() - mWallAvoidDistance);
    }

    public void SetStepLength(final double newValue)
    {
        mStepLength = newValue;
    }

    public void SetStepLengthVar(final double newValue)
    {
        mStepLengthVar = newValue;
    }

    public void SetNumPaths(final int newValue)
    {
        mNumPaths = newValue;
    }

    public void SetPathHdngBase(final double newValue)
    {
        mPathHdngBase = newValue;
    }

    public void SetPathHdngVar(final double newValue)
    {
        mPathHdngVar = newValue;
    }

    public void SetEvalPowerLaw(final double newValue)
    {
        mEvalPowerLaw = newValue;
    }

    // Choose random waypoint, with no regard to current situation.
    public final Location RandomWaypointAbs()
    {
        double maxX = mAllowedArea.GetMaxX();
        double maxY = mAllowedArea.GetMaxY();
        double minX = mAllowedArea.GetMinX();
        double minY = mAllowedArea.GetMinY();

        double deltaX = maxX - minX;
        double deltaY = maxY - minY;

        double randomX = minX + (Math.random() * deltaX);
        double randomY = minY + (Math.random() * deltaY);

        Location answer = new Location();
        answer.Set(randomX, randomY);
        return answer;
    }

    // Choose random waypoint, based on a relative offset to known point.
    public final Location RandomWaypointRel(final Location sourceLocn)
    {
        Location answer = new Location();
        boolean done = false;
        Heading adjPathHdng = new Heading();
        Location pathEnd = new Location();
	int numAttempts = 0;

//        System.out.println("RandomWaypointRel() from: " +
//                           sourceLocn.toString());

        if ((mNumPaths <= 0) || (mStepLength < 1.0))
        {
            System.out.println("ERROR: Navigator.RandomWaypointRel(), " +
                               "input error " + mNumPaths + "/" + mStepLength);
        }
        else
        {
            /*???
            // May be some randomness in path length.
            double adjPathLength = mStepLength;
            if (mStepLengthVar > 0.5)
            {
                adjPathLength = mStepLength - mStepLengthVar +
                                (2.0 * Math.random() * mStepLengthVar);
            }

            // May be a random offset to the permitted angles of movement.
            double hdngOffset = 0.0;
            if (mPathHdngVar > 0.5)
            {
                hdngOffset = (2.0 * Math.random() * mPathHdngVar) -
                                                             mPathHdngVar;
            }
            */

//            System.out.println("numPaths: " + mNumPaths);
//            System.out.println("pathHdngBase: " + dp2.format(mPathHdngBase));
//            System.out.println("hdngOffset: " + dp2.format(hdngOffset));
//            System.out.println("adjPathLength: " + dp2.format(adjPathLength));

            while (!done)
            {
                numAttempts++;

//                System.out.println("Iterating, numAttempts: " + numAttempts);

                // May be some randomness in path length.
                double adjPathLength = mStepLength;
                if (mStepLengthVar > 0.5)
                {
                    adjPathLength = mStepLength - mStepLengthVar +
                                    (2.0 * Math.random() * mStepLengthVar);
                }

                // May be a random offset to the permitted angles of movement.
                // This will be used to adjust the randomly chosen base angle
                // below...
                double hdngOffset = 0.0;
                if (mPathHdngVar > 0.5)
                {
                    hdngOffset = (2.0 * Math.random() * mPathHdngVar) -
                                                             mPathHdngVar;
                }

                // Number of paths in this case controls how many valid angles
                // there are to turn through, equally spaced through 360 deg.
                // So pick a random number to generate correct base angle.
                int randomIndex = (int)(Math.random() * mNumPaths);
                adjPathHdng.Set((360.0 * randomIndex / mNumPaths) +
                                                        mPathHdngBase);
                adjPathHdng.Adjust(hdngOffset);

                pathEnd.SetRelative(sourceLocn, adjPathHdng, adjPathLength);

//                System.out.println(" randomIndex: " + randomIndex);
//                System.out.println(" adjPathHdng: " +
//                                   adjPathHdng.toString());
//                System.out.println(" pathEnd: " + pathEnd.toString());
//                System.out.println(" box: ");
//                mAllowedArea.Print();

                // Is this path allowed?
                if (mAllowedArea.IsInside(pathEnd))
                {
                    // This path is valid, use it.
                    done = true;
                    answer.Set(pathEnd);
                }
                else if (numAttempts > 20)  //??? parameterise
                {
//                    System.out.println("RelWp, too many attempts, aborting!");
               
                    // If we are having trouble, wimp out!
                    done = true;
                    answer = RandomWaypointAbs();
                }
            }
        }

        return answer;
    }

    // Slightly optimised version - not sure if it works as well though!
    public Location QuantumGravWaypoint(final Location sourceLocn)
    {
        Location answer = new Location();
        Heading pathHdng = new Heading();
        Heading adjPathHdng = new Heading();
        int pathIndex = 0;
        Vector locnArray = new Vector();

//        System.out.println();
//        System.out.println("===== QuantumGravWaypoint() =====");
//        System.out.println("sourceLocn: " + sourceLocn.toString());
//        System.out.println("Try " + mNumPaths + " paths, length " +
//                           mStepLength + ", var " + mStepLength);

        if ((mNumPaths <= 0) || (mStepLength < 1.0))
        {
            System.out.println("ERROR: Navigator.QuantumGravWaypoint, " +
                               "input error " + mNumPaths + "/" + mStepLength);
        }
        else
        {
            // May be some randomness in candidate path length, and angle.
            double adjPathLength = mStepLength;
            double hdngOffset = 0.0;
            if (mStepLengthVar > 0.5)
            {
                adjPathLength = mStepLength - mStepLengthVar +
                                (2.0 * Math.random() * mStepLengthVar);
            }
            if (mPathHdngVar > 0.5)
            {
                hdngOffset = (2.0 * Math.random() * mPathHdngVar) -
                                                             mPathHdngVar;
            }
         
            // Starting from base heading, determine location of
            // a number of candidate destinations.
            pathIndex = 0;
            pathHdng.Set(mPathHdngBase);
            while (pathIndex < mNumPaths)
            {
                // Must allocate memory inside this loop, as we wish to store
                // in array.
                //??? may still be slightly more efficient to copy when we are
                //??? not discarding the path?
                Location pathEnd = new Location();
              
                // Determine the vector being evaluated.
                // May be applying some randomness on top of the base.
                adjPathHdng.Set(pathHdng);
                adjPathHdng.Adjust(hdngOffset);
                pathEnd.SetRelative(sourceLocn, adjPathHdng, adjPathLength);

                // Is this path allowed? If so store for later consideration.
                if (mAllowedArea.IsInside(pathEnd))
                {
                    locnArray.add(pathEnd);
                }

                // Move on to next vector around the circle.
                pathHdng.Adjust(360.0 / mNumPaths);
                pathIndex++;
            }

            boolean success = EvaluatePaths(locnArray, answer);
            if (!success)
            {
                // If couldn't find best path, choose at random.
                int numValidPaths = locnArray.size();
                if (numValidPaths <= 0)
                {
                    System.out.println("ERROR - valid paths array empty");
                }
                else
                {
                    int randomIndex = (int)(Math.random() * numValidPaths);
                    Location randomEnd = (Location)locnArray.get(randomIndex);
                    answer.Set(randomEnd);
                }
            }
        }

        return answer;
    }

    private final boolean EvaluatePaths(final Vector locnArray,
                                       Location answer)
    {
        boolean okSoFar = true;
        double biggestEvalSoFar = 0.0;
        int numLocns = 0;
        int numTargets = 0;

//        System.out.println();
//        System.out.println("*** EvaluatePaths ***");

        numTargets = mTrackerRef.GetNumTargets();
        numLocns = locnArray.size();
        if (numTargets <= 0)
        {
            // This isn't a serious error, but here is no way to choose
            // between locns, caller must decide what to do instead.
            okSoFar = false;
        }
        else if (numLocns <= 0)
        {
            System.out.println("ERROR: Navigator.EvaluatePaths() - none!");
            okSoFar = false;
        }
        else
        {
//            System.out.println("numTargets " + numTargets);
//            System.out.println("numLocns " + numLocns);

            // Try each candidate in turn to see which is furthest
            // from all the other robots.

            int locnIndex = 0;
            while (locnIndex < numLocns)
            {
                final Location thisLocn = (Location)locnArray.get(locnIndex);
                double thisEval = EvalDistanceFunc(thisLocn, mEvalPowerLaw);

                if (thisEval > biggestEvalSoFar)
                {
                    biggestEvalSoFar = thisEval;
                    answer.Set(thisLocn);
                }

                locnIndex++;
            }
        }

        return okSoFar;
    }

    private double EvalDistanceFunc(final double xPos,
                                    final double yPos,
                                    final double powerLaw)
    {
        double sumEval = 0.0;
	Location refLocn = new Location(xPos, yPos);
        sumEval = EvalDistanceFunc(refLocn, powerLaw);
        return sumEval;
    }

    private double EvalDistanceFunc(final Location refLocn,
                                    final double powerLaw)
    {
        double thisEval = 0;
        double sumEval = 0;
        int thisIndex = 0;
        int numTargets = 0;

        // Iterate through all the targets currently known to us.
        //??? take account of freshness?
        sumEval = 0.0;
        thisIndex = 0;
        numTargets = mTrackerRef.GetNumTargets();
        while (thisIndex < numTargets)
        {
            final Sighting thisTarget =
                          mTrackerRef.GetTargetByIndex(thisIndex);
            final Location thisLocn = thisTarget.GetLocation();
            double thisDist = thisLocn.DistanceTo(refLocn);

            thisEval = Math.pow(thisDist, powerLaw);

//            System.out.println("    Target name: [" +
//                               thisTarget.GetName() + "]");
//            System.out.println("    Distance to target " + thisIndex +
//                               " is " + thisDist);
//            System.out.println("    Evaluation result " + thisIndex +
//                               " is " + thisEval);

            sumEval += thisEval;
            thisIndex++;
        }

//        System.out.println("  sumEval = " + sumEval);

        return sumEval;
    }
}
