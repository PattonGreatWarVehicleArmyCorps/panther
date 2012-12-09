package shrub;

import java.text.NumberFormat;

/**
 * PHPredictor - prediction based on periodic motion in target history.
 */
public class PHPredictor implements PredictorAPI
{
    private long mMinTimeDelta = 1;
    private long mMaxInterceptTime = 9999;
    private Box mMovementArea = null;
    private Box mInterceptArea = null;
    private double mMaxAngleOverride = 10;
    private int mPeriodicity = 50;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public PHPredictor()
    {
        Initialise();
    }

    public void Initialise()
    {
        mMinTimeDelta = 1;
        mMaxInterceptTime = 9999;
        mMovementArea = null;
        mInterceptArea = null;
        mMaxAngleOverride = 10;
        mPeriodicity = 50;

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    // This is not a general predictor method, specific to PHPredictor.
    public void SetMinTimeDelta(final long newValue)
    {
        mMinTimeDelta = newValue;
    }
    
    // Area within which we expect target movement to be constrained.
    public void SetMovementArea(final Box newValue)
    {
        mMovementArea = new Box(newValue);
    }
    
    // Area within which we are allowed to intercept the target.
    public void SetInterceptArea(final Box newValue)
    {
        mInterceptArea = new Box(newValue);
    }
    
    // Maximum time for interception, anything more is invalid.
    public void SetMaxInterceptTime(final long newValue)
    {
        mMaxInterceptTime = newValue;
    }

    // Maximum angle for which we can send back a turret turn request.
    public void SetMaxAngleOverride(final long newValue)
    {
        mMaxAngleOverride = newValue;
    }
  
    // Maximum time for interception, anything more is invalid.
    public void SetPeriodicity(final int newValue)
    {
        mPeriodicity = newValue;
    }
  
    // For a given subject (i.e. "us") location and heading, determine
    // speed to intercept a given object (i.e. "target"), assuming
    // linear motion at constant velocity for both parties.
    public final InterceptSolution Intercept(final double minSpeedAllowed,
                                             final double maxSpeedAllowed,
                                             final Location subLocn,
                                             final Heading subHdng,
                                             final MoveHistory objHistory)
    {
        boolean okSoFar = true;
        InterceptSolution intSoln = new InterceptSolution();

        // Get up-to-date target info.
        Movement objStartMove = objHistory.GetLastMove();
        long startTime = objStartMove.GetTimeNow();
        Location objStartLocn = new Location();
        objStartLocn.Set(objStartMove.GetLocnNow());
        double objStartDist = objStartLocn.DistanceTo(subLocn);

//        System.out.println();
//        System.out.println("===== PHPredictor::Intercept(), startTime: " +
//                           startTime);
//        System.out.println("minSpeedAllowed:  " +
//                           dp2.format(minSpeedAllowed));
//        System.out.println("maxSpeedAllowed:  " +
//                           dp2.format(maxSpeedAllowed));
//        System.out.println("maxAngleOverride: " +
//                           dp2.format(mMaxAngleOverride));
//        System.out.println("periodicity:      " + mPeriodicity);

//        System.out.println("--- target start info: ---");
//        objStartMove.Print();
//        System.out.println("starting distance to target: " +
//                           dp2.format(objStartDist));

        // The simplest approach to determining theoretical window
        // of opportunity for hits is to consider the earliest and 
        // latest times we could possibly hit, i.e. fastest bullet
        // fired at target moving fast towards us, and slowest bullet
        // fired at target moving fast away from us.
        long earliestHit = (long)(objStartDist / (maxSpeedAllowed + 8.0));
        long latestHit = (long)(objStartDist / (minSpeedAllowed - 8.0));

        // Derive equation for line of attempted interception.
        Line subLine = new Line();
        subLine.SetStartHdng(subLocn, subHdng);
        subLine.CalcEquation();

        // Check for time for bullet to reach arena limits, if specified.
        if (mInterceptArea != null)
        {
            Location wallIntLocn = mInterceptArea.EdgeIntersectOutward(subLine);
            double distToWall = wallIntLocn.DistanceTo(subLocn);
            int maxTimeToWall = (int)(distToWall / minSpeedAllowed);

//            System.out.println("  distToWall = " + dp2.format(distToWall));
//            System.out.println("  maxTime = " + maxTimeToWall);

            if (latestHit > maxTimeToWall)
            {
                latestHit = maxTimeToWall;
            }
        }

        // Apply upper limit to bullet flight time.
        if (latestHit > mMaxInterceptTime)
        {
            latestHit = mMaxInterceptTime;
        }

        // Have worked out bullet time in flight, add on current time
        // to give the earliest and latest hit actual times, which we need.
        earliestHit += startTime;
        latestHit += startTime;

//        System.out.println("earliest possible hit: " + earliestHit);
//        System.out.println("latest possible hit:   " + latestHit);
          
        // Create a movement record to hold ongoing prediction.
        Movement predictMove = new Movement(objStartMove);

        HistoricIterator histIterator = new HistoricIterator(objHistory,
                                                             predictMove,
                                                             mMovementArea,
                                                             mPeriodicity);
        okSoFar &= histIterator.IsOkay();

        // Iterate steadily into the future, looking for firing solutions.
        boolean finished = false;
        boolean validSolutionFound = false;
        double lastValidSpeed = -1.0;
        boolean angleOverrideFound = false;
        double bestAngleOverride = 999999;
        long priorPredictTime = startTime;
        Heading hdngToPredict = new Heading();
        Bearing angleOff = new Bearing();
        while (okSoFar && !finished)
        {
            okSoFar &= histIterator.Iterate();
            if (okSoFar == false)
            {
                System.out.println("PHPredictor, iterator gone bad");
            }

            long predictTime = predictMove.GetTimeNow();

//            System.out.println("Iterate: predictTime = " + predictTime);
//            predictMove.Print();

//            System.out.println("priorPredictTime = " + priorPredictTime);
//            System.out.println("mMinTimeDelta = " + mMinTimeDelta);

            if (predictTime > latestHit)
            {
                finished = true;
            }
            else if (predictTime < earliestHit)
            {
                // Do nothing yet, but continue iterating.
            }
            else if ((predictTime - priorPredictTime) < mMinTimeDelta)
            {
                // Too close to previously considered prediction, do
                // nothing for the moment but continue iterating.

//                System.out.println(predictTime + " too soon after " +
//                                   priorPredictTime);
            }
            else
            {
//                System.out.println("Prediction at " + predictTime +
//                                   " being considered");

                priorPredictTime = predictTime;

                // Angular width from object centre to object extremity
                // at this distance.
                final Location predictLocn = predictMove.GetRefToLocnNow();
                double predictDist = predictLocn.DistanceTo(subLocn);
                double angularTolDeg = ShrubMath.RobotAngularWidth(predictDist);

                // Work out if we are pointing close enough to the
                // predicted location to have a potential intercept.
                hdngToPredict.SetFromTo(subLocn, predictLocn);
                angleOff.SetFromTo(subHdng, hdngToPredict);

                // Work out bullet speed to make this intercept.
                // Allow margin for interceoting either front or back, does
                // not have to arrive at centre of target at the due time.
                long deltaTime = predictTime - startTime;
                double bulletSpeed = predictDist / deltaTime;
                double loSpeedToHit = (predictDist - 20) / deltaTime; //???
                double hiSpeedToHit = (predictDist + 30) / deltaTime; //???
                if ((loSpeedToHit <= maxSpeedAllowed) &&
                    (hiSpeedToHit >= minSpeedAllowed))

                {
                    // Is it close enough in direction for a hit?
                    double angleOff_Abs = angleOff.GetAbs();
                    if (angleOff_Abs <= angularTolDeg)
                    {
                        // Remember last (max power) valid solution.
                        // Note that because of the flexible speed range
                        // above, our bullet speed could be invalid. If so,
                        // cap it to the appropriate limit.
                        if (bulletSpeed > maxSpeedAllowed)
                        {
                            lastValidSpeed = maxSpeedAllowed;
                        }
                        else if (bulletSpeed < minSpeedAllowed)
                        {
                            lastValidSpeed = minSpeedAllowed;
                        }
                        else
                        {
                            lastValidSpeed = bulletSpeed;
                        }
                        validSolutionFound = true;
                    }
                    // If not, it may be a valid solution in future turns
                    // if we turn the gun accordingly.
                    else if (angleOff_Abs <= mMaxAngleOverride)
                    {
                        if (angleOff_Abs < Math.abs(bestAngleOverride))
                        {
                            bestAngleOverride = angleOff.Get();
                            angleOverrideFound = true;
                        }
                    }
                }
            }
        }

        // Now pass back best intercept solution to caller to act on.
        if (validSolutionFound)
        {
//            System.out.println("VALID SOLUTION (PH): " +
//                               dp2.format(lastValidSpeed));

            intSoln.mSpeed = lastValidSpeed;
        }
        else if (angleOverrideFound)
        {
//            System.out.println("ANGLE OVERRIDE (PH): " +
//                               dp2.format(bestAngleOverride));

            // Send back a request to turn turret with the idea
            // of having a valid solutionnext turn. Note: it is up to
            // the higher level brain whether this request is
            // honoured!
            intSoln.mBearing = bestAngleOverride;
        }

        return intSoln;        
    }
}
