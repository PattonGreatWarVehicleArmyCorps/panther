package shrub;

import java.text.NumberFormat;

/**
 * Gun - basic functionality of the robot gun.
 */
public class Gun
{
    // 0 - hold fire
    // 1 - adaptive-speed autoshoot with constant linear prediction.
    // 2 - adaptive-speed autoshoot with backward historic prediction.
    // 3 - adaptive-speed autoshoot, at current target position.
    // 4 - adaptive-speed autoshoot, with periodic historic prediction.
    private int mMode = 0;

    private long mTimeNow = 0;    
    private double mGunHeat = 0.0;
    private double mCoolingRate = 0.1;     
    private Location mGunLocn = new Location();
    private Heading mGunHdng = new Heading();

    private double mMaxPower = 3.0;
    private double mMinPower = 0.1;

    private TrackerAPI mTrackerRef = null;

    private PredictorAPI mCLPredictor = null;
    private PredictorAPI mBHPredictor = null;
    private PredictorAPI mCPPredictor = null;
    private PredictorAPI mPHPredictor = null;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public Gun()
    {
        Initialise();
    }

    public void Initialise()
    {
        SetMode(0);

        mTimeNow = 0;
        mGunHeat = 0.0;

        mCoolingRate = 0.1;
        mMaxPower = 3.0;
        mMinPower = 0.1;

        mTrackerRef = null;  //??? pass in in constructor?

        mCLPredictor = null;
        mBHPredictor = null;
        mCPPredictor = null;
        mPHPredictor = null;

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public void SetTrackerRef(final TrackerAPI trackerRef)
    {
        mTrackerRef = trackerRef;
    }

    public void SetCLPredictor(PredictorAPI predictorRef)
    {
        mCLPredictor = predictorRef;
    }

    public void SetBHPredictor(PredictorAPI predictorRef)
    {
        mBHPredictor = predictorRef;
    }

    public void SetCPPredictor(PredictorAPI predictorRef)
    {
        mCPPredictor = predictorRef;
    }

    public void SetPHPredictor(PredictorAPI predictorRef)
    {
        mPHPredictor = predictorRef;
    }

    public void SetMode(final int newMode)
    {
        mMode = newMode;
    }

    public void SetGunHeat(final double newGunHeat)
    {
        mGunHeat = newGunHeat;
    }

    public void SetGunHdng(final double newGunHdng)
    {
        mGunHdng.Set(newGunHdng);
    }

    public void SetGunLocn(final double gunX,
                           final double gunY)
    {
        mGunLocn.SetX(gunX);
        mGunLocn.SetY(gunY);
    }

    public void SetGunLocn(final Location gunLocn)
    {
        mGunLocn.Set(gunLocn);
    }

    public void SetMaxPower(final double newMaxPower)
    {
        mMaxPower = newMaxPower;
    }

    public void SetMinPower(final double newMinPower)
    {
        mMinPower = newMinPower;
    }

    public void SetTime(final long newTime)
    {
        mTimeNow = newTime;
    }

    public void SetCoolingRate(final double newCoolingRate)
    {
        mCoolingRate = newCoolingRate;
    }

    public boolean IsReadyToFire()
    {
        boolean answer = false;
        if (mGunHeat < 0.01)
        {
            answer = true;
        }
        return answer;
    }

    public long HowLongTillReadyAfter(double shotPower)
    {
        double numTurns = (1 + (shotPower/5)) / mCoolingRate;

        // Round up to next integer value.
        numTurns = -1.0 * Math.floor(-1.0 * numTurns);

        return (long)numTurns;
    }

    public long HowLongTillReady()
    {
        double numTurns = (double)mGunHeat / mCoolingRate;

        // Round up to next integer value.
        numTurns = -1.0 * Math.floor(-1.0 * numTurns);

        return (long)numTurns;
    }

    // -ve return value indicates no valid solution.
    static public final double ShotPowerFromSpeed(double speed)
    {
        double power = -1.0;

        // Filter out the really nonsense questions, but
        // don't make assumptions here about what the max
        // and min possible values of speed may be in case
        // game engine changes in future to allow greater range.
        if (speed >= 0.0)
        {
            power = (20.0 - speed) / 3.0;
        }

        return power;
    }

    // -ve return value indicates no valid solution.
    static public final double ShotSpeedFromPower(double power)
    {
        double speed = -1.0;

        // Filter out the really nonsense questions, but
        // don't make assumptions here about what the max
        // and min possible values of power may be in case
        // game engine changes in future to allow greater range.
        if (power >= 0.0)
        {
            speed = 20.0 - (3.0 * power);
        }

        return speed;
    }

    static public final double MinLegalPower()
    {
        return 0.1;
    }

    static public final double MaxLegalPower()
    {
        return 3.0;
    }

    static public final double MinLegalSpeed()
    {
        return (ShotSpeedFromPower(MaxLegalPower()));
    }

    static public final double MaxLegalSpeed()
    {
        return (ShotSpeedFromPower(MinLegalPower()));
    }

    static public final double DamageFromPower(final double power)
    {
        double damage = 4.0 * power;
        if (power > 1.0)
        {
            damage += 2.0 * (power - 1.0);
        }
        return damage;
    }

    static public final double PowerFromDamage(final double damage)
    {
        double power = -1;
        if (damage > 4.0)
        {
            // Power must be > 1.0.
            power = (damage + 2.0) / 6.0;
        }
        else
        {
            // Power must be <= 1.0.
            power = damage / 4.0;
        }
        return power;
    }

    public GunInstruction Process()
    {
//        System.out.println("Gun.Process()");

        GunInstruction instruct = new GunInstruction();
        instruct.SetNothing();

        if (mMode == 0)
        {
            // Do nothing.
        }
        else if (mMode == 1)
        {
            // No point in even thinking about shooting 
            // if gun is not ready to fire.
            if (IsReadyToFire())
            {
                CLAutoshoot(instruct);
            }
        }
        else if (mMode == 2)
        {
            // No point in even thinking about shooting 
            // if gun is not ready to fire.
            if (IsReadyToFire())
            {
                BHAutoshoot(instruct);
            }
        }
        else if (mMode == 3)
        {
            // No point in even thinking about shooting 
            // if gun is not ready to fire.
            if (IsReadyToFire())
            {
                CPAutoshoot(instruct);
            }
        }
        else if (mMode == 4)
        {
            // No point in even thinking about shooting 
            // if gun is not ready to fire.
            if (IsReadyToFire())
            {
                PHAutoshoot(instruct);
            }
        }
        else
        {
            System.out.println("ERROR: Gun.Process(), unexpected mode");
        }

        return instruct;
    }

    public void CLAutoshoot(GunInstruction instruct)
    {
//        System.out.println();
//        System.out.println("===== Gun.CLAutoshoot()=====");
//        Print();

        instruct.SetNothing();

        if ((mTrackerRef == null) || (mCLPredictor == null))
        {
           System.out.println("ERROR : Gun.CLAutoshoot(), " +
                              "tracker/predictor not specified");
        }
        else
        {
            // Ensure our target position is extrapolated up to date if
            // not already done so, or up to date due to recent sighting.
            mTrackerRef.ExtrapolateCurrentTarget(mTimeNow);

            // We are in autoshoot mode. That means examing target(s), and
            // the current heading of the gun, and firing if appropriate.
            // Shot power and hence bullet speed is adjusted to achieve a
            // firing solution, if one is possible.
            final double minSpeedAllowed = ShotSpeedFromPower(mMaxPower);
            final double maxSpeedAllowed = ShotSpeedFromPower(mMinPower);
            MoveHistory targetHist = mTrackerRef.GetCurrentTargetHistory();
            InterceptSolution intSoln = mCLPredictor.Intercept(minSpeedAllowed,
                                                               maxSpeedAllowed,
                                                               mGunLocn,
                                                               mGunHdng,
                                                               targetHist);

            // -ve speed passed back indicates no valid solution.
            if ((intSoln.mSpeed >= MinLegalSpeed()) &&
                (intSoln.mSpeed <= MaxLegalSpeed()))
            {
                double shotPower = ShotPowerFromSpeed(intSoln.mSpeed);
                instruct.SetFire(shotPower);
            }
            // Linear predictor does not support anything other than
            // immediate fire solutions. ???
            else
            {
                // No solution, do nothing.
            }
        }
    }

    //??? make shoot methods into one generic method, operating on a
    //??? specified index in an array of predictor objects.
    public void BHAutoshoot(GunInstruction gunInstruct)
    {
        boolean okSoFar = true;

        gunInstruct.SetNothing();

//        System.out.println();
//        System.out.println("=== Gun.BHAutoshoot(), time " + mTimeNow);

        if ((mTrackerRef == null) || (mBHPredictor == null))
        {
           System.out.println("ERROR : Gun.BHAutoshoot(), " +
                              "tracker/predictor not specified");
        }
        else
        {
            // Ensure our target position is extrapolated up to date if
            // not already done so, or up to date due to recent sighting.
            mTrackerRef.ExtrapolateCurrentTarget(mTimeNow);

            // We are in autoshoot mode. That means examing target(s), and
            // the current heading of the gun, and firing if appropriate.
            // Shot power and hence bullet speed is adjusted to achieve a
            // firing solution, if one is possible.
            final double minSpeedAllowed = ShotSpeedFromPower(mMaxPower);
            final double maxSpeedAllowed = ShotSpeedFromPower(mMinPower);
            MoveHistory targetHist = mTrackerRef.GetCurrentTargetHistory();
            InterceptSolution intSoln = mBHPredictor.Intercept(minSpeedAllowed,
                                                               maxSpeedAllowed,
                                                               mGunLocn,
                                                               mGunHdng,
                                                               targetHist);

            // Immediate solution, fire.
            if ((intSoln.mSpeed >= MinLegalSpeed()) &&
                (intSoln.mSpeed <= MaxLegalSpeed()))
            {
                double bulletPower = ShotPowerFromSpeed(intSoln.mSpeed);
                gunInstruct.SetFire(bulletPower);
            }
            // No immediate solution, turn left for near solution.
            else if (intSoln.mBearing < -0.1)
            {
                gunInstruct.SetTurnLeft(-1.0 * intSoln.mBearing);
            }
            // No immediate solution, turn right for near solution.
            else if (intSoln.mBearing > 0.1)
            {
                gunInstruct.SetTurnRight(intSoln.mBearing);
            }
            else
            {
                // No solution, do nothing.
            }
        }
    }

    public void CPAutoshoot(GunInstruction gunInstruct)
    {
        boolean okSoFar = true;

        gunInstruct.SetNothing();

//        System.out.println();
//        System.out.println("=== Gun.CPAutoshoot(), time " + mTimeNow);

        if ((mTrackerRef == null) || (mCPPredictor == null))
        {
           System.out.println("ERROR : Gun.CPAutoshoot(), " +
                              "tracker/predictor not specified");
        }
        else
        {
            // Ensure our target position is extrapolated up to date if
            // not already done so, or up to date due to recent sighting.
            mTrackerRef.ExtrapolateCurrentTarget(mTimeNow);

            // We are in autoshoot mode. That means examing target(s), and
            // the current heading of the gun, and firing if appropriate.
            // Shot power and hence bullet speed is adjusted to achieve a
            // firing solution, if one is possible.
            final double minSpeedAllowed = ShotSpeedFromPower(mMaxPower);
            final double maxSpeedAllowed = ShotSpeedFromPower(mMinPower);
            MoveHistory targetHist = mTrackerRef.GetCurrentTargetHistory();
            InterceptSolution intSoln = mCPPredictor.Intercept(minSpeedAllowed,
                                                               maxSpeedAllowed,
                                                               mGunLocn,
                                                               mGunHdng,
                                                               targetHist);

            // Immediate solution, fire.
            if ((intSoln.mSpeed >= MinLegalSpeed()) &&
                (intSoln.mSpeed <= MaxLegalSpeed()))
            {
                double bulletPower = ShotPowerFromSpeed(intSoln.mSpeed);
                gunInstruct.SetFire(bulletPower);
            }
            // No immediate solution, turn left for near solution.
            else if (intSoln.mBearing < 0.0)
            {
                gunInstruct.SetTurnLeft(intSoln.mBearing);
            }
            // No immediate solution, turn right for near solution.
            else if (intSoln.mBearing > 0.0)
            {
                gunInstruct.SetTurnRight(intSoln.mBearing);
            }
            else
            {
                // No solution, do nothing.
            }
        }
    }

    //??? make shoot methods into one generic method, operating on a
    //??? specified index in an array of predictor objects.
    public void PHAutoshoot(GunInstruction gunInstruct)
    {
        boolean okSoFar = true;

        gunInstruct.SetNothing();

//        System.out.println();
//        System.out.println("=== Gun.PHAutoshoot(), time " + mTimeNow);

        if ((mTrackerRef == null) || (mPHPredictor == null))
        {
           System.out.println("ERROR : Gun.PHAutoshoot(), " +
                              "tracker/predictor not specified");
        }
        else
        {
            // Ensure our target position is extrapolated up to date if
            // not already done so, or up to date due to recent sighting.
            mTrackerRef.ExtrapolateCurrentTarget(mTimeNow);

            // We are in autoshoot mode. That means examing target(s), and
            // the current heading of the gun, and firing if appropriate.
            // Shot power and hence bullet speed is adjusted to achieve a
            // firing solution, if one is possible.
            final double minSpeedAllowed = ShotSpeedFromPower(mMaxPower);
            final double maxSpeedAllowed = ShotSpeedFromPower(mMinPower);
            MoveHistory targetHist = mTrackerRef.GetCurrentTargetHistory();
            InterceptSolution intSoln = mPHPredictor.Intercept(minSpeedAllowed,
                                                               maxSpeedAllowed,
                                                               mGunLocn,
                                                               mGunHdng,
                                                               targetHist);

            // Immediate solution, fire.
            if ((intSoln.mSpeed >= MinLegalSpeed()) &&
                (intSoln.mSpeed <= MaxLegalSpeed()))
            {
                double bulletPower = ShotPowerFromSpeed(intSoln.mSpeed);
                gunInstruct.SetFire(bulletPower);
            }
            // No immediate solution, turn left for near solution.
            else if (intSoln.mBearing < -0.1)
            {
                gunInstruct.SetTurnLeft(-1.0 * intSoln.mBearing);
            }
            // No immediate solution, turn right for near solution.
            else if (intSoln.mBearing > 0.1)
            {
                gunInstruct.SetTurnRight(intSoln.mBearing);
            }
            else
            {
                // No solution, do nothing.
            }
        }
    }

    public void Print()
    {
        System.out.println("----- Gun -----");
        System.out.println("mMode: " + mMode);
        System.out.println("mGunLocn: " + mGunLocn.toString());
        System.out.println("mGunHdng: " + mGunHdng.Get());
    }
}
