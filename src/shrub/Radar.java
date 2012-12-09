package shrub;

public class Radar
{
    // 0 - stopped
    // 1 - goto target heading
    // 2 - continuous scan left
    // 3 - continuous scan right
    // 4 - track centred on heading
    // 5 - track centred on a location
    // 6 - obsolete mode, code removed
    // 7 - obsolete mode, code removed
    // 8 - phased angle offset to track on heading
    // 9 - phased angle offset to track on location
    private int mMode = 0;
    
    private Location mRadarLocn = null;
    private Heading mRadarHdng = null;
    private Location mTargetLocn = null;
    private Heading mTargetHdng = null;

    private double mScanSpeed = 45;
    private double mScanPastAngle = 15;
    private long mTime = 0;

    private Phasor mPhasor = new Phasor();

    public Radar()
    {
        Initialise();
    }

    public void Initialise()
    {
        SetMode(0);

        mRadarLocn = new Location();
        mTargetLocn = new Location();
        mRadarHdng = new Heading();
        mTargetHdng = new Heading();

        mScanSpeed = 45;
        mScanPastAngle = 15;
        mTime = 0;
        mPhasor.Initialise();
    }

    public void SetMode(final int newMode)
    {
        mMode = newMode;
    }

    public void SetTime(final long newTime)
    {
        mTime = newTime;
    }

    public void SetPhasor(Phasor newPhasor)
    {
        mPhasor = newPhasor;
    }

    public void SetScanSpeed(final double newValue)
    {
        mScanSpeed = newValue;
    }

    public void SetScanPastAngle(final double newValue)
    {
        mScanPastAngle = newValue;
    }

    public void SetRadarHdng(final double newValue)
    {
        mRadarHdng.Set(newValue);
    }

    public void SetRadarLocn(final double radarX,
                             final double radarY)
    {
        mRadarLocn.SetX(radarX);
        mRadarLocn.SetY(radarY);
    }

    public void SetRadarLocn(final Location radarLocn)
    {
        mRadarLocn.Set(radarLocn);
    }

    public void SetTargetHdng(final double newValue)
    {
        mTargetHdng.Set(newValue);
    }

    public void SetTargetLocn(final double targetX,
                              final double targetY)
    {
        mTargetLocn.SetX(targetX);
        mTargetLocn.SetY(targetY);
    }

    public void SetTargetLocn(final Location targetLocn)
    {
        mTargetLocn.Set(targetLocn);
    }

    public final Location GetRadarLocn()
    {
        return mRadarLocn;
    }

    public final Heading GetRadarHdng()
    {
        return mRadarHdng;
    }

    public final Phasor GetPhasor()
    {
        return mPhasor;
    }

    public RadarInstruction Process()
    {
//        System.out.println("Radar::Process()");

        RadarInstruction instruct = new RadarInstruction();

        if (mMode == 0)
        {
            // Do nothing.
            instruct.SetNothing();
        }
        else if (mMode == 1)
        {
            //??? TBD - rotate to target heading
            instruct.SetNothing();
        }
        else if (mMode == 2)
        {
            instruct.SetRotateLeft(mScanSpeed);
        }
        else if (mMode == 3)
        {
            instruct.SetRotateRight(mScanSpeed);
        }
        else if (mMode == 4)
        {
            ProcessTrackOnHeading(instruct, mTargetHdng);
        }
        else if (mMode == 5)
        {
            // First work out current heading of target location.
            // Then this mode operates just like the track heading mode.
            mTargetHdng.SetFromTo(mRadarLocn, mTargetLocn);
            ProcessTrackOnHeading(instruct, mTargetHdng);
        }
        else if (mMode == 6)
        {
            System.out.println("ERROR : Radar::Process, obsolete mode");
        }
        else if (mMode == 7)
        {
            System.out.println("ERROR : Radar::Process, obsolete mode");
        }
        else if (mMode == 8)
        {
            ProcessPhasedAroundHeading(instruct, mTargetHdng);
        }
        else if (mMode == 9)
        {
            // First work out current heading of target location, then
            // this mode operates just like the heading equivalent.
            mTargetHdng.SetFromTo(mRadarLocn, mTargetLocn);
            ProcessPhasedAroundHeading(instruct, mTargetHdng);
        }
        else
        {
            System.out.println("ERROR : Radar::Process, unexpected mode");
        }

        return instruct;
    }

    public void ProcessRotateToHeading(RadarInstruction instruct,
                                       final Heading rawHdng)
    {
        // Is target hdng currently to left or right of us?
        // We want to set endpoint of scan sweep a short distance
        // to the other side of the target.
        Bearing targetBearing = new Bearing();
        targetBearing.SetFromTo(mRadarHdng, rawHdng);
        double amount = targetBearing.GetAbs(); //??? adjust width

        //??? TBD
    }

    // Track radar around a known heading - therefore we assume that
    // the target heading has been supplied or calculated somehow.
    public void ProcessTrackOnHeading(RadarInstruction instruct,
                                      final Heading rawHdng)
    {
        // Is target hdng currently to left or right of us?
        // We want to set endpoint of scan sweep a short distance
        // to the other side of the target.
        Bearing targetBearing = new Bearing();
        targetBearing.SetFromTo(mRadarHdng, rawHdng);
        double amount = targetBearing.GetAbs() + mScanPastAngle;

        // Work out how far we are going to rotate this turn.
        if (mScanSpeed < amount)
        {
            amount = mScanSpeed;
        }
   
        // Prepare instruction to move the appropriate amount in the 
        // correct direction.
        if (targetBearing.IsLeftward())
        {
            instruct.SetRotateLeft(amount);
        }
        else
        {
            instruct.SetRotateRight(amount);
        }
    }

    public void ProcessPhasedAroundHeading(RadarInstruction instruct,
                                           final Heading rawHdng)
    {
        // Work out the current phased offset to nominal target heading.
        mPhasor.SetTimeNow(mTime);
        double phaseValue = mPhasor.GetPhaseValue();

        // Drive radar towards the offset target heading.
        Heading adjHdng = new Heading();
        adjHdng.Set(rawHdng);
        adjHdng.Adjust(phaseValue);
        ProcessTrackOnHeading(instruct, adjHdng);
    }
}
