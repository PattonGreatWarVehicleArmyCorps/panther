package shrub;

/**
 * CPPredictor - "prediction" based on aiming at current position of target.
 */
public class CPPredictor implements PredictorAPI
{
    public CPPredictor()
    {
//        System.out.println();
//        System.out.println("CPPredictor constructor");

        Initialise();
    }

    public void Initialise()
    {
    }

    // Area in which we predict target will be constrained to move.
    public void SetMovementArea(final Box newValue)
    {
        // Don't support allowed areas in this predictor.
    }

    // Area within which we are allowed to engage target.
    public void SetInterceptArea(final Box newValue)
    {
        // Don't support allowed areas in this predictor.
    }

    // Maximum time for intercept, anything longer is invalid.
    public void SetMaxInterceptTime(final long newValue)
    {
        // Don't support max intercept time in this predictor.
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
//        System.out.println();
//        System.out.println("===== CPPredictor::Intercept()=====");
//        System.out.println("minSpeedAllowed: " + minSpeedAllowed);
//        System.out.println("maxSpeedAllowed: " + maxSpeedAllowed);

        Movement objLastMove = objHistory.GetLastMove();
        Location objLocn = objLastMove.GetLocnNow();

        Heading hdngToObj = new Heading();
        hdngToObj.SetFromTo(subLocn, objLocn);
        double distanceToObj = subLocn.DistanceTo(objLocn);

        // Angular tolerance to allow us to intercept object of finite size,
        // at this distance.
        //??? pass in robot dimensions
        //??? use ShrubMath lookup method
        double angularTolRad = Math.atan(15.0 / distanceToObj);
        double angularTolDeg = Math.toDegrees(angularTolRad);
        
//        System.out.println("distance : " + distanceToObj);
//        System.out.println("angWidth : " + angularWidthObjDeg);
//        System.out.println("subHdng  : " + subHdng.Get());
//        System.out.println("hdngToObj: " + hdngToObj.Get());

        Bearing angleOff =  new Bearing();
        angleOff.SetFromTo(subHdng, hdngToObj);
                 
        // Is it close enough to intercept? Or do we need to rotate further...
        InterceptSolution intSoln = new InterceptSolution();
        if (angleOff.GetAbs() < angularTolDeg)
        {
            // Slowest (highest power bullet) interception allowed.
            intSoln.mSpeed = minSpeedAllowed;
        }
        else if (angleOff.GetAbs() < 10.0)  //??? parameterise
        {
            // Indicate we need to rotate to intercept next turn.
            intSoln.mBearing = angleOff.Get();
        }

        return intSoln;        
    }
}
