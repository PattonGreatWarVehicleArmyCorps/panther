package shrub;

import robocode.*;
import java.awt.Color;
import java.text.NumberFormat;

/**
 * Vapour v124 - a robot by Shrubbery.
 */
public class Vapour extends AdvancedRobot
{
    private String mBotName = "Vapour v124";

    private Box mArena = null;

    private Body mBody = null;
    private Turret mTurret = null;
    private Gun mGun = null;
    private Radar mRadar = null;

    private MHTracker mTracker = null;
    private Navigator mNavigator = null;

    private MultiPData mMultiPData = null;

    private CPPredictor mCPPredictor = null;
    private BHPredictor mBHPredictor = null;
    private PHPredictor mPHPredictor = null;

    private Phasor mSweep3x5 = null;
    private Phasor mTrackScan = null;

    private long mNumLoopIterations = 0;

    private long mTimeFirstFireAllowed = 0;
    private long mTimeFirstMoveAllowed = 0;
    private int mInitialNumOpponents = 0;

    private NumberFormat dp2 = NumberFormat.getInstance();

    private static BattleStats mBattleStats = new BattleStats();
    private RoundStats mRoundStats = new RoundStats(mBattleStats, mBody);

    public Vapour()
    {
    }

    public void Initialise()
    {
        setColors(Color.cyan, Color.green, Color.white);

        // Radius of robot is average of half-width and half-height.
        double robotWidth = getWidth();
        double robotHeight = getHeight();
        double robotRadius = (robotWidth + robotHeight) / 4.0;

        // Initialise ShrubMath package, for quick trig lookups etc.
        ShrubMath.SetRobotRadius(robotRadius);
        ShrubMath.Initialise();

        // Make sure all components start fresh each round, i.e.
        // no stale data hanging around to muck up early tracking etc.
        mArena = new Box();
        mBody = new Body();
        mTurret = new Turret();
        mGun = new Gun();
        mRadar = new Radar();
        mTracker = new MHTracker();
        mNavigator = new Navigator();

        mMultiPData = new MultiPData();

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        // Refresh stats accumulator for new round.
        // Note: do not refresh persistent battle statistics object!
        mRoundStats = new RoundStats(mBattleStats, mBody);

        // Create arena information and pass where needed.
        Location corner1 = new Location(0, 0);
        Location corner2 = new Location(getBattleFieldWidth(),
                                        getBattleFieldHeight());
        mArena.Set(corner1, corner2);
        mNavigator.SetArena(mArena);

        // Set permitted movement area. Make slightly larger than actually
        // possible to make sure we err on the side of caution - robots sighted
        // outside what we think are the permitted limits causes problems!
        Location corner3 = new Location(robotRadius - 5, robotRadius - 5);
        Location corner4 = new Location(
                                    getBattleFieldWidth() - robotRadius + 5,
                                    getBattleFieldHeight() - robotRadius + 5);
        Box movementArea = new Box();
        movementArea.Set(corner3, corner4);

        // Parameters controlling behaviour of our tracker module.
        mTracker.SetMaxHistory(160);
        mTracker.SetPhasorResetOnTargetChange(true);

        // Parameters controlling how periodicity is determined.
        mMultiPData.SetNumTargetsPerGo(4);
        mMultiPData.SetMinPeriod(20);
        mMultiPData.SetPeriodStep(1);
        mMultiPData.SetNumPeriods(60);
        mMultiPData.SetValueThreshold(100);

        // Target prediction objects, to be used by gun.
        mCPPredictor = new CPPredictor();
        mBHPredictor = new BHPredictor();
        mBHPredictor.SetInterceptArea(movementArea);
        mBHPredictor.SetMaxInterceptTime(75);
        mBHPredictor.SetMaxAngleOverride(30);
        mPHPredictor = new PHPredictor();
        mPHPredictor.SetInterceptArea(movementArea);
        mPHPredictor.SetMaxInterceptTime(75);
        mPHPredictor.SetPeriodicity(50);
        mPHPredictor.SetMaxAngleOverride(30);

        // Make sure objects that need to can access each other.
        mTracker.SetRadarRef(mRadar);
        mMultiPData.SetTrackerRef(mTracker);
        mNavigator.SetTrackerRef(mTracker);
        mBody.SetNavigatorRef(mNavigator);
        mGun.SetTrackerRef(mTracker);
        mGun.SetCPPredictor(mCPPredictor);
        mGun.SetBHPredictor(mBHPredictor);
        mGun.SetPHPredictor(mPHPredictor);

        // Set up persistent phasor objects for future use.
        mSweep3x5 = new Phasor();
        mTrackScan = new Phasor();
        double[] sweep3x5Array = { 0, -5, -10, -15, -10, -5,
                                   0, +5, +10, +15, +10, +5 };
        double[] tscArray = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                              45, 90, 135, 180, 225, 270, 315 };
        mSweep3x5.SetPhaseArray(sweep3x5Array);
        mTrackScan.SetPhaseArray(tscArray);

        Location myLocn = new Location();
        myLocn.Set(getX(), getY());

        mBody.SetRobotRadius(robotRadius);
        mBody.SetWaypointLocn(myLocn);

        mTurret.SetPhasor(mSweep3x5);

        double gunHeat = getGunHeat();
        mGun.SetGunHeat(gunHeat);
        mTimeFirstFireAllowed = mGun.HowLongTillReady();
        mTimeFirstMoveAllowed = mTimeFirstFireAllowed - 15;

        mRadar.SetPhasor(mTrackScan);

        // Control how tracker evaluates targets,
        // e.g. to choose the best one to engage.
        mTracker.SetStalenessLimit(20);
        mTracker.SetEvalDistanceCoeff(1.0);
        mTracker.SetEvalAngleCoeff(1.0);
        mTracker.SetEvalSpeedCoeff(0.0);
        mTracker.SetEvalStaleCoeff(0.0);
        mTracker.SetEvalDisabledThreshold(0.001);
        mTracker.SetEvalDisabledBonus(300.0);
        mTracker.SetEvalLowEnergyThreshold(0.5);
        mTracker.SetEvalLowEnergyBonus(-1000.0); // Ignore low health target
	                                         // in melee completely!

        // Specify how tracker should handle target movement extrapolation.
        mTracker.SetExtrapolationInterval(1);

        // Remember how many opponents we started with, i.e. so we
        // can tell whether this is melee battle or 1v1 and act accordingly.
        mInitialNumOpponents = getOthers();

        // Print out some useful initial info.
        System.out.println();
        System.out.println("-*- " + mBotName + " -*-");
        System.out.println("Starting locn: " + myLocn.toString());
        System.out.println("Robot width:   " + dp2.format(robotWidth));
        System.out.println("Robot height:  " + dp2.format(robotHeight));
        System.out.println("Robot radius:  " + dp2.format(robotRadius));
        System.out.println("Fire allowed:  " + mTimeFirstFireAllowed);
        System.out.println("Move allowed:  " + mTimeFirstMoveAllowed);
        System.out.println("Num opponents: " + mInitialNumOpponents);

        mNumLoopIterations = 0;
    }

    public void run() 
    {
        long currentTime = 0;
        long numOpponents = 0;
        Location myLocn = new Location();
        double currentEnergy = 0.0;
        double gunHeat = 0.0;
        double bodyHeading = 0.0;
        double gunHeading = 0.0;
        double radarHeading = 0.0;
        Heading hdngToTarget = new Heading();
        Heading perpToTarget = new Heading();
        Heading leadTarget = new Heading();
        Sighting currentTarget = new Sighting();
        MoveHistory targetHist = null;

        int dodgeDir = 1;
        if (Math.random() >= 0.5)
        {
            dodgeDir = -1;
        }
        long dodgeDirChangeTime = 0;
        long dodgeDirIntervalMin = 100;
        long dodgeDirIntervalVar = 100;

        long wpDurationMin = 10;
        long wpDurationMax = 25;
        long wpDuration = wpDurationMin;

        double wallAvoidDist = 50.0;

        boolean haveLockOn = false;
        double closestTargetDist = 999999;
        int numFreshTargets = 0;
        double targetEnergy = -1;
        double shotAtPower = -1;

        Initialise();

        while (true) 
        {
            mNumLoopIterations++;

            currentTime = (long)getTime();
            myLocn.Set(getX(), getY());
            bodyHeading = getHeading();
            gunHeading = getGunHeading();
            gunHeat = getGunHeat();
            radarHeading = getRadarHeading();
            currentEnergy = getEnergy();
            numOpponents = getOthers();

//            System.out.println();
//            System.out.println("======= Iteration " + mNumLoopIterations +
//                               ", time " + currentTime);
//            System.out.println();

            // Prime the components with current situation data.
            mBody.SetRobotLocn(myLocn);
            mBody.SetRobotHdng(bodyHeading);
            mBody.SetTimeNow(currentTime);
            mTurret.SetTurretHdng(gunHeading);
            mTurret.SetTurretLocn(myLocn);
            mTurret.SetTime(currentTime);
            mGun.SetGunHdng(gunHeading);
            mGun.SetGunLocn(myLocn);
            mGun.SetGunHeat(gunHeat);
            mGun.SetTime(currentTime);
            mRadar.SetRadarHdng(radarHeading);
            mRadar.SetRadarLocn(myLocn);
            mRadar.SetTime(currentTime);
            mTracker.SetCurrentTime(currentTime);
            mTracker.SetTrackerLocn(myLocn);
            mTracker.SetGunHdng(gunHeading);

            boolean gunReady = mGun.IsReadyToFire();

            // Extrapolate data for targets not sighted recently.
            // This gives us the most up-to-date world view we can
            // achieve.
            mTracker.ExtrapolateAll(currentTime);

            // Apply the previously-specified logic to choosing the
            // "best" target to shoot at.
            mTracker.ChooseBestTarget();

            // Determine some useful info re. target that we will be using...
            haveLockOn = mTracker.HasLockOn();
            currentTarget = mTracker.GetCurrentTarget();
            closestTargetDist = mTracker.ClosestTargetDistance();
            numFreshTargets = mTracker.GetNumFreshTargets();
            targetEnergy = -1;
            if (currentTarget != null)
            {
                hdngToTarget.SetFromTo(myLocn, currentTarget.GetLocation());
                targetEnergy = currentTarget.GetEnergy();
                targetHist = mTracker.GetCurrentTargetHistory();
            }
            wpDuration = (long)(closestTargetDist / 25);
            if (wpDuration < wpDurationMin)
            {
                wpDuration = wpDurationMin;
            }
            else if (wpDuration > wpDurationMax)
            {
                wpDuration = wpDurationMax;
            }

            // Try to spot if current target has fired a shot at us.
            // Only bother trying to do this if we are in 1v1 situation.
            shotAtPower = -1;
            if (currentTime > mTimeFirstFireAllowed)
            {
                if (numFreshTargets == 1)
                {
                    shotAtPower = mTracker.BulletFireDetection();
                }
            }

            // Gather data and see if we can spot any significant
            // periodicity in movement of other targets.
            int bestPeriod = -1;
            if ((numOpponents <= 10) &&
                (targetHist != null) &&
                (currentTime > 50))
            {
                // Note: we calculate periodicity values when gun is hot,
                // need to determine the best value only when gun is cool.
                if (gunReady == false)
                {
                    // The periodicity threshold we use is higher for melee
                    // than 1v1, as additional noise is introduced into the
                    // calculations by the presence of estimated data as well
                    // as firm sightings.
                    if (numFreshTargets == 1)
                    {
                        mMultiPData.SetValueThreshold(50);
                    }
                    else
                    {
                        mMultiPData.SetValueThreshold(100);
                    }
                
                    // Calculate periodicity value for next period
                    // in sequence.
                    mMultiPData.DoNextPeriodicityCalcs();
                }
                else
                {
                    // Determine best period for this target, from
                    // periodicity calculations performed in previous turns.
                    bestPeriod = mMultiPData.GetBestPeriodForTarget(
                                                    targetHist.GetName());

//                    System.out.println("*** GetBestPeriod(), " +
//                                       bestPeriod);
                }
            }

            // Radar strategy.
            mRadar.SetScanSpeed(45);
            mRadar.SetScanPastAngle(15);
            if (numOpponents == 0)
            {
                // Radar component of victory dance.
                mRadar.SetMode(2);
            }
            else if (haveLockOn == false)
            {
                // Do complete scan to pick up a valid target ASAP.
                mRadar.SetMode(2);
            }
            else if (numFreshTargets < numOpponents)
            {
                // We have at least one valid target, but there are some
                // targets we have no current knowledge of. Best to mix
                // tracking the current target (to give accurate shooting)
                // with occasional scan to give us a chance to locate others
                // sooner or later.
                mRadar.SetMode(9);
                mRadar.SetTargetLocn(currentTarget.GetLocation());
            }
            else   // All targets known to us, and locked on to one.
            {
                // The aim here is to sweep back and forth across the
                // narrowest angle to keep all targets updated as
                // frequently as possible whilst gun is hot, then steady
                // track of current target as we prepare to fire.

                // Note that this case also covers only one opponent remaining,
                // as it will inevitably track the only target.

                if (mGun.HowLongTillReady() >= 5)
                {
                    int index = mTracker.GetOldestTargetIndex();
                    Sighting oldestTarget =
                                    mTracker.GetTargetByIndex(index);
                    mRadar.SetMode(5);
                    mRadar.SetTargetLocn(oldestTarget.GetLocation());
                }
                else
                {
                    mRadar.SetMode(5);
                    mRadar.SetTargetLocn(currentTarget.GetLocation());
                }
            }

            // Turret strategy.
            if (numOpponents == 0)
            {
                // Turret component of victory dance.
                mTurret.SetMode(3);
            }
            else if (haveLockOn == false)
            {
                mTurret.SetMode(0);
            }
            else
            {
                // Determine target's velocity perpendicular to us.
                double perpVel = hdngToTarget.CrossComponent(
                                               currentTarget.GetHeading(),
                                               currentTarget.GetVelocity());

                // Lead target slightly in correct direction. And waggle!
                // Rather low-tech solution, but as yet I have no code to
                // solve gun heading to intercept target. This approach does
                // have the benefit of pseudo-randomising bullet power,
                // which I intuitively think is useful.
                int turretMode = 0;
                leadTarget.Set(hdngToTarget);
                if (perpVel <= -6.0)
                {
                    leadTarget.Adjust(-20.0);
                    turretMode = 7;
                }
                else if (perpVel <= -4.5)
                {
                    leadTarget.Adjust(-15.0);
                    turretMode = 7;
                }
                else if (perpVel <= -3.0)
                {
                    leadTarget.Adjust(-10.0);
                    turretMode = 7;
                }
                else if (perpVel <= -1.5)
                {
                    leadTarget.Adjust(-05.0);
                    turretMode = 7;
                }
                else if (perpVel < +1.5)
                {
                    // No adjustment, point straight at target.
                    // Do not waggle in this case.
                    turretMode = 6;
                }
                else if (perpVel < +3.0)
                {
                    leadTarget.Adjust(+05.0);
                    turretMode = 7;
                }
                else if (perpVel < +4.5)
                {
                    leadTarget.Adjust(+10.0);
                    turretMode = 7;
                }
                else if (perpVel < +6.0)
                {
                    leadTarget.Adjust(+15.0);
                    turretMode = 7;
                }
                else
                {
                    leadTarget.Adjust(+20.0);
                    turretMode = 7;
                }

                mTurret.SetMode(turretMode);
                mTurret.SetTargetHdng(leadTarget);
                mTurret.SetRotateSpeed(20);
            }

            // Gun strategy.
            if (numOpponents == 0)
            {
                // Gun component of victory dance.
                mGun.SetMode(0);
            }
            else if (haveLockOn == false)
            {
                mGun.SetMode(0);
            }
            else if (gunReady == true)
            {
                // Use periodic historic prediction if target has strong
                // enough periodicity, otherwise use backward historic.
                int gunMode = 0;
                if (targetEnergy < 0.001)
                {
                    gunMode = 3; // No point in any clever prediction!
                }
                else if (bestPeriod > 0)
                {
                    mPHPredictor.SetPeriodicity(bestPeriod);
                    gunMode = 4;
                }
                else
                {
                    gunMode = 2;
                }

                // Energy management - normal power range for shooting.
                double maxPower = 0;
                double minPower = 0;

                // Our base power values depend on range to target - no
                // point in wasting time firing wimpy shots at close targets.
                if (closestTargetDist < 200)
                {
                    maxPower = Gun.MaxLegalPower();
                    minPower = 2.0;
                }
                else if (closestTargetDist < 400)
                {
                    maxPower = Gun.MaxLegalPower();
                    minPower = 1.0;
                }
                else if (closestTargetDist < 600)
                {
                    maxPower = Gun.MaxLegalPower();
                    minPower = 0.5;
                }
                else
                {
                    maxPower = Gun.MaxLegalPower();
                    minPower = Gun.MinLegalPower();
                }

                // Now consider things which may reduce the power
                // we want to shoot at...

                // If target disabled, go for quick coup de gras.
                if (targetEnergy < 0.001)
                {
                    maxPower = Gun.MinLegalPower() + 0.01;
                    minPower = Gun.MinLegalPower();
                }
                else
                {
                    // Target not disabled, but may still be low on power.
                    // If so, no point in firing meaty bullets at him.
                    double powerNeeded = mGun.PowerFromDamage(targetEnergy);
                    if ((powerNeeded > 0)  && (powerNeeded < maxPower))
                    {
                        maxPower = powerNeeded;
                    }

                    // Make sure we don't disable ourself unnecessarily.
                    if (currentEnergy <= maxPower)
                    {
                        maxPower = currentEnergy - 0.01;
                    }

                    // But after all of the above, ensure minimum is
                    // less than maximum, or we wont be able to fire at all!
                    if (maxPower <= minPower)
                    {
                        minPower = Gun.MinLegalPower();
                    }
                }

                mGun.SetMode(gunMode);
                mGun.SetMinPower(minPower);
                mGun.SetMaxPower(maxPower);
            }
            else
            {
                mGun.SetMode(0);
            }

            // See if it is time to switch our dodge direction.
            if (currentTime >= dodgeDirChangeTime)
            {
                // Work out when we will next be changing...
                dodgeDirChangeTime += dodgeDirIntervalMin +
                                      (Math.random() * dodgeDirIntervalVar);

                dodgeDir = dodgeDir * -1;
            }

            // If low on energy, be a bit more careful about hitting wall.
            if (currentEnergy < 3.05)
            {
                wallAvoidDist = 100.0;
            }
            else
            {
                wallAvoidDist = 50.0;
            }

            // Movement strategy. See earlier comment re. reason for using
            // number of fresh targets known to us, rather than actual number
            // of opponents.
            mNavigator.SetWallAvoidDistance(wallAvoidDist);
            mNavigator.SetEvalPowerLaw(0.5);
            mBody.SetAllowReversal(true);
            mBody.SetMaxWaypointDuration(wpDuration);
            if (numOpponents == 0)
            {
                // Body component of victory dance.
                mBody.SetMode(0);
            }
            else if (currentTime < mTimeFirstMoveAllowed)
            {
                // Stationary for a while at the start, whilst we
                // gather initial radar data.
                mBody.SetMode(0);
            }
            else if (numFreshTargets == 1)
            {
                // Only one opponent known about, use 1v1 strategy.

                if (closestTargetDist < 300.0)
                {
                    // Quantum grav waypoints will take us away from enemy.
                    mNavigator.SetStepLength(100);
                    mNavigator.SetStepLengthVar(25);
                    mNavigator.SetNumPaths(5);
                    mNavigator.SetPathHdngBase(hdngToTarget.Get());
                    mNavigator.SetPathHdngVar(20);
                    mBody.SetMode(4);
                }
                else if (closestTargetDist > 500.0)
                {
                    // Absolute random tends us toward centre.
                    mBody.SetMode(2);
                }
                else
                {
                    // We are within bullet-dodging band.
                    if (shotAtPower > 0)
                    {
                        double pathBaseAngle = 90.0;

                        perpToTarget.Set(hdngToTarget);
                        perpToTarget.Adjust(dodgeDir * pathBaseAngle);

                        mNavigator.SetStepLength(100);
                        mNavigator.SetStepLengthVar(50);
                        mNavigator.SetNumPaths(1);
                        mNavigator.SetPathHdngBase(perpToTarget.Get());
                        mNavigator.SetPathHdngVar(60);

                        mBody.ActOnBulletFireDetection();
                    }
                    else
                    {
                        // Use simple move to wp and stop mode here. Sometimes
                        // means we will be stationary for a short while until
                        // target fires again, which helps to confuse its aim.
                        mBody.SetMode(1);
                    }
                }
            }
            else
            {
                // More than one opponent known about.
                // Use normal melee movement strategy.
                mNavigator.SetStepLength(100);
                mNavigator.SetStepLengthVar(25);
                mNavigator.SetNumPaths(7);
                mNavigator.SetPathHdngBase(hdngToTarget.Get());
                mNavigator.SetPathHdngVar(20);
                mBody.SetMode(4);
            }

            // Follow the defined strategies for this turn, act on results.
            BodyInstruction[] bodyInstructArray = mBody.Process();
            TurretInstruction turretInstruct = mTurret.Process();
            RadarInstruction radarInstruct = mRadar.Process();
            GunInstruction gunInstruct = mGun.Process();
            int index = 0;
            int numElements = bodyInstructArray.length;
            while (index < numElements)
            {
                ResolveBodyInstruct(bodyInstructArray[index]);
                index++;
            }
            ResolveTurretInstruct(turretInstruct);
            ResolveRadarInstruct(radarInstruct);

            if (gunInstruct.IsFire())
            {
                // Keep track of firing stats.
                double power = gunInstruct.GetDouble();
                mRoundStats.ShotFired(currentTime, power);

                System.out.println("FIRE! power: " + dp2.format(power) +
                                   ", time: " + currentTime);
            }

            // Note that fire executes, so don't need to do so
            // again if we have fired this turn (will waste a turn
            // if we do!).
	    // Note also we resolve gun instruction after turret instruction.
            // This allows gun override of turret motion to occur.
            ResolveGunInstruct(gunInstruct);
            if (!gunInstruct.IsFire())
            {
                execute();
            }    
        }
    }

    public void onSkippedTurn(SkippedTurnEvent event)
    {
//        System.out.println("*** Turn skipped at time : " +
//                           getTime() + " ***");

        mRoundStats.SkippedTurn();
    }

    public void onBulletHit(BulletHitEvent event)
    {
        //??? can deduce something about target location and add reported
        // sighting to tracker - although since we extrapolate target
        // movement and scan radar frequently it may not be too useful. TBD.
      
        mRoundStats.ShotHit();
    }

    public void onBulletMissed(BulletMissedEvent event)
    {
        mRoundStats.ShotMissed();
    }

    public void onBulletHitBullet(BulletHitBulletEvent event)
    {
        //??? could deduce something about enemy position from heading of
        //??? the bullet we hit - don't think such uncertain data is much
        //??? use with this bot but worth considering in future.

        mRoundStats.BulletCollision();
    }

    public void onDeath(DeathEvent event)
    {
        System.out.println();
        System.out.println("Dead, " + getOthers() + " still alive");

        // Attempt to force garbage collection now, before
        // we get into the hustle of the next round.
        mTracker.RemoveAll();
        System.gc();

        // Print round stats, update and print overall battle stats.
        mRoundStats.EndRound(getTime());
    }

    public void onWin(WinEvent event)
    {
        System.out.println();
        System.out.println("Rah! Rah! 2-4-6-8 Rah! etc.");
        System.out.println();
   
        // Attempt to force garbage collection now, before
        // we get into the hustle of the next round.
        mTracker.RemoveAll();
        System.gc();

        // Print round stats, update and print overall battle stats.
        mRoundStats.EndRound(getTime());
    }

    public void onScannedRobot(ScannedRobotEvent event) 
    {
        long contactTime = (long)event.getTime();
        String contactName = event.getName();
        Heading robotHdng = new Heading();
        robotHdng.Set(getHeading());
        Bearing bearingTo = new Bearing();
        bearingTo.Set(event.getBearing());
        double distanceTo = event.getDistance();
        Heading contactHdng = new Heading();
        contactHdng.Set(event.getHeading());
        double contactEnergy = event.getEnergy();
        double contactVelocity = event.getVelocity();

//        System.out.println();
//        System.out.println("======= ScanEvent, time " + contactTime);
//        System.out.println();

        // We know our own heading and the relative bearing to contact,
        // work out absolute heading to contact from our current location.
        Heading headingTo = new Heading();
        headingTo.Set(robotHdng);
        headingTo.Adjust(bearingTo);

        // Want to know location of sighting, much more useful to us!
        Location robotLocn = new Location();
        robotLocn.SetX(getX());
        robotLocn.SetY(getY());
        Location contactLocn = new Location();
        contactLocn.SetRelative(robotLocn, headingTo, distanceTo);

        // This is a radar sighting, so we are confident of its accuracy.
        int contactInaccuracy = 0;

        // Create a sighting object and pass to the tracking module.
        Sighting sighting = new Sighting();
        sighting.SetName(contactName);
        sighting.SetLocation(contactLocn);
        sighting.SetHeading(contactHdng);
        sighting.SetVelocity(contactVelocity);
        sighting.SetEnergy(contactEnergy);
        sighting.SetInaccuracy(contactInaccuracy);
        sighting.SetTimestamp(contactTime);
        mTracker.ReportSighting(sighting);

        // Also report name of sighting to periodicity data object, so
        // it can add a new target to its list if necessary.
        // Note: must make sure that we call this after sighting has been
        // reported to tracker, since this object internally calls upon
        // the tracker to get some additional data relating to the target.
        mMultiPData.ReportSighting(contactName);
    }

    public void onHitWall(HitWallEvent event) 
    {
        mBody.ActOnRobotHitWall(event);
    }

    public void onHitRobot(HitRobotEvent event)
    {
        mBody.ActOnRobotHitRobot(event);

        String contactName = event.getName();
        Bearing brngToContact = new Bearing();
        brngToContact.Set(event.getBearing());
        long contactTime = event.getTime();

//        System.out.println("*** Robot collision! ***");
//        System.out.println("contactTime: " + contactTime);
//        System.out.println("contactName: [" + contactName +"]");
//        System.out.println("brngToContact: " + brngToContact.Get());

/*??? don't bother with this since we are extrapolating all targets

        // Know bearing and our heading, work out heading to contact.
        Heading myHdng = new Heading();
        myHdng.Set(mBody.GetRobotHdng());
        Heading hdngToContact = new Heading();
        hdngToContact.Set(myHdng);
        hdngToContact.Adjust(brngToContact);

//        System.out.println("myHdng: " + myHdng.Get());
//        System.out.println("hdngToContact: " + hdngToContact.Get());

        // Work out location of collided robot from heading,
        // and our knowledge of how big each robot is.
        double centreDistance = 2.0 * mBody.GetRobotRadius();
        Location myLocn = new Location();
        myLocn.Set(mBody.GetRobotLocn());
        Location contactLocn = new Location();
        contactLocn.SetRelative(myLocn, hdngToContact, centreDistance);

//        System.out.println("myLocn: " + myLocn.toString());
//        System.out.println("contactLocn: " + contactLocn.toString());

        // Must be pretty much stationary right now.
        // Best guess of its heading is directly towards us.
        // Energy is no more than a wild guess.
        double contactVelocity = 0.0;
        Heading contactHdng = new Heading();
        contactHdng.Set(hdngToContact);
        contactHdng.Flip();
        double contactEnergy = 50.0; 

        // Accuracy of this sighting is a little less than ideal.
        int contactInaccuracy = 1;

        // Construct a sighting object, and report to tracker.
        Sighting sighting = new Sighting();
        sighting.SetName(contactName);
        sighting.SetLocation(contactLocn);
        sighting.SetHeading(contactHdng);
        sighting.SetVelocity(contactVelocity);
        sighting.SetEnergy(contactEnergy);
        sighting.SetInaccuracy(contactInaccuracy);
        sighting.SetTimestamp(contactTime);

        mTracker.ReportSighting(sighting);
*/
    }

    public void onHitByBullet(HitByBulletEvent event)
    {
        //??? what can we guess about firer from this event? TBD.
     
        mBody.ActOnHitByBullet(event);
    }

    public void onRobotDeath(RobotDeathEvent event)
    {
        mTracker.ReportRobotDeath(event.getName());
        mMultiPData.ReportRobotDeath(event.getName());
    }

    /**
     * ResolveBodyInstruct - turns instructions into actual action.
     *
     * Returns true if we have achieved current task, can be used
     * to stop loop and move on to next task, etc.
     */
    public boolean ResolveBodyInstruct(BodyInstruction instruct) 
    {
        boolean achieved = false;

        if (instruct.IsNothing())
        {
            // Do nothing.
        }
        else if (instruct.IsAchieved())
        {
            // Do nothing, but indicate we have achieved allotted task,
            // which may be used by caller to trigger setting next task.
            achieved = true;
        }
        else if (instruct.IsTurnLeft())
        {
            setTurnLeft(instruct.GetDouble());
        }
        else if (instruct.IsTurnRight())
        {
            setTurnRight(instruct.GetDouble());
        }
        else if (instruct.IsMoveAhead())
        {
            setAhead(instruct.GetDouble());
        }
        else if (instruct.IsMoveBack())
        {
            setBack(instruct.GetDouble());
        }
        else if (instruct.IsSpeedLimit())
        {
            System.out.println("Speed limit: " + instruct.GetDouble());

            setMaxVelocity(instruct.GetDouble());
        }
        else
        {
            System.out.println("ERROR : Robot.ResolveBodyInstruct, " +
                               "unexpected mode");
        }

        return achieved;
    }
	
    /**
     * ResolveTurretInstruct - turns instructions into actual action.
     *
     * Returns true if we have achieved current task, can be used
     * to stop loop and move on to next task, etc.
     */
    public boolean ResolveTurretInstruct(TurretInstruction instruct) 
    {
        boolean achieved = false;

        if (instruct.IsNothing())
        {
            // Do nothing.
        }
        else if (instruct.IsAchieved())
        {
            // Do nothing, but indicate we have achieved allotted task,
            achieved = true;
        }
        else if (instruct.IsRotateLeft())
        {
            setTurnGunLeft(instruct.GetDouble());
        }
        else if (instruct.IsRotateRight())
        {
            setTurnGunRight(instruct.GetDouble());
        }
        else
        {
            System.out.println("ERROR : Robot.ResolveTurretInstruct, " +
                               "unexpected mode");
        }

        return achieved;
    }

    /**
     * ResolveRadarInstruct - turns instructions into actual action.
     *
     * Returns true if we have achieved current task, can be used
     * to stop loop and move on to next task, etc.
     */
    public boolean ResolveRadarInstruct(RadarInstruction instruct) 
    {
        boolean achieved = false;

        if (instruct.IsNothing())
        {
            // Do nothing.
        }
        else if (instruct.IsAchieved())
        {
            // Do nothing, but indicate we have achieved allotted task,
            achieved = true;
        }
        else if (instruct.IsRotateLeft())
        {
            setTurnRadarLeft(instruct.GetDouble());
        }
        else if (instruct.IsRotateRight())
        {
            setTurnRadarRight(instruct.GetDouble());
        }
        else
        {
            System.out.println("ERROR : Robot.ResolveRadarInstruct, " +
                               "unexpected mode");
        }

        return achieved;
    }

    /**
     * ResolveGunInstruct - turns instructions into actual action.
     */
    public boolean ResolveGunInstruct(GunInstruction instruct) 
    {
        boolean achieved = false;

        if (instruct.IsNothing())
        {
            // Do nothing.
        }
        else if (instruct.IsAchieved())
        {
            // Do nothing, but indicate we have achieved allotted task,
            achieved = true;
        }
        else if (instruct.IsFire())
        {
            fire(instruct.GetDouble());
        }
        else if (instruct.IsTurnLeft())
        {
            // Gun overriding turret motion for shooting opportunity.
            setTurnGunLeft(instruct.GetDouble());
        }
        else if (instruct.IsTurnRight())
        {
            // Gun overriding turret motion for shooting opportunity.
            setTurnGunRight(instruct.GetDouble());
        }
        else
        {
            System.out.println("ERROR : Robot.ResolveGunInstruct, " +
                               "unexpected mode");
        }

        return achieved;
    }
}

