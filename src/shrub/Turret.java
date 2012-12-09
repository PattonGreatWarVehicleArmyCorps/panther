package shrub;

/**
 * Turret - basic functionality of the robot turret.
 */
public class Turret
{
    // 0 - stopped
    // 1 - rotate to target location, shortest way
    // 2 - continuously rotate left
    // 3 - continuously rotate right
    // 4 - *removed*
    // 5 - *removed*
    // 6 - rotate to target heading, shortest way
    // 7 - phased-offset track on target heading
    // 8 - phased-offset track on target location
    private int mMode = 0;
    
    private Location mTurretLocn = new Location();
    private Heading mTurretHdng = new Heading();
    private Heading mTargetHdng = new Heading();
    private Location mTargetLocn = new Location();
    private double mRotateSpeed = 0.0;
    private long mTime = 0;
    private Phasor mPhasor = new Phasor();

    public Turret()
    {
        Initialise();
    }
 
    public void Initialise()
    {
        SetMode(0);

        mTurretLocn.Initialise();
        mTurretHdng.Initialise();
        mTargetHdng.Initialise();
        mTargetLocn.Initialise();

        mRotateSpeed = 0;
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

    public void SetRotateSpeed(final double newRotateSpeed)
    {
        mRotateSpeed = newRotateSpeed;
    }

    public void SetPhasor(Phasor newPhasor)
    {
        mPhasor = newPhasor;
    }

    public void SetTurretHdng(final double newTurretHdng)
    {
        mTurretHdng.Set(newTurretHdng);
    }

    public void SetTurretHdng(final Heading newTurretHdng)
    {
        mTurretHdng.Set(newTurretHdng.Get());
    }

    public void SetTurretLocn(final double turretX,
                              final double turretY)
    {
        mTurretLocn.SetX(turretX);
        mTurretLocn.SetY(turretY);
    }

    public void SetTurretLocn(final Location turretLocn)
    {
        mTurretLocn.Set(turretLocn);
    }

    public void SetTargetHdng(final double newTargetHdng)
    {
        mTargetHdng.Set(newTargetHdng);
    }

    public void SetTargetHdng(final Heading newTargetHdng)
    {
        mTargetHdng.Set(newTargetHdng.Get());
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

    public TurretInstruction Process()
    {
        TurretInstruction instruct = new TurretInstruction();

        if (mMode == 0)
        {
            // Do nothing.
            instruct.SetNothing();
        }
        else if (mMode == 1)
        {
            // We know target location, work out its heading from us.
            // Once that is known, rotate to it.
            mTargetHdng.SetFromTo(mTurretLocn, mTargetLocn);
            ProcessRotateToHeading(instruct, mTargetHdng);
        }
        else if (mMode == 2)
        {
            instruct.SetRotateLeft(mRotateSpeed);
        }
        else if (mMode == 3)
        {
            instruct.SetRotateRight(mRotateSpeed);
        }
        else if (mMode == 4)
        {
            System.out.println("ERROR : Turret::Process, obsolete mode");
        }
        else if (mMode == 5)
        {
            System.out.println("ERROR : Turret::Process, obsolete mode");
        }
        else if (mMode == 6)
        {
            ProcessRotateToHeading(instruct, mTargetHdng);
        }
        else if (mMode == 7)
        {
            ProcessPhasedAroundHeading(instruct, mTargetHdng);
        }
        else if (mMode == 8)
        {
            // We know current target location, determine target 
            // heading from us and thence the current phased
            // offset heading to target this turn.
            mTargetHdng.SetFromTo(mTurretLocn, mTargetLocn);
            ProcessPhasedAroundHeading(instruct, mTargetHdng);
        }
        else
        {
            System.out.println("ERROR : Turret::Process, unexpected mode");
        }

        return instruct;
    }

    public void ProcessRotateToHeading(TurretInstruction instruct, 
                                       final Heading toHdng)
    {
        // Determine relative bearing of target to our own heading,
        // range -180 to +180.
        Bearing relBearing = new Bearing();
        relBearing.SetFromTo(mTurretHdng, toHdng);

        // Work out how far we are going to rotate.
        double amount = mRotateSpeed;
        if (relBearing.GetAbs() < amount)
        {
            amount = relBearing.GetAbs();
        }

        if (relBearing.IsLeftward())
        {
            instruct.SetRotateLeft(amount);
        }
        else if (relBearing.IsRightward())
        {
            instruct.SetRotateRight(amount);
        }
        else
        {
            // Must be correctly aligned already.
            instruct.SetNothing();
        }
    }

    public void ProcessPhasedAroundHeading(TurretInstruction instruct,
                                           final Heading rawHdng)
    {
        // Work out the current phased offset to nominal target heading.
        mPhasor.SetTimeNow(mTime);
        double phaseValue = mPhasor.GetPhaseValue();

        // Drive turret towards the offset target heading.
        Heading adjHdng = new Heading();
        adjHdng.Set(rawHdng);
        adjHdng.Adjust(phaseValue);
        ProcessRotateToHeading(instruct, adjHdng);
    }
}
