package shrub;

import java.math.*;
import robocode.*;

/**
 * Body - basic functionality of a robot body. Knows where it is,
 *        knows where it's going to and how, and returns instructions
 *        for what to do now. Doesn't make any robocode API calls itself.
 */
public class Body
{
    // Mode values are:
    //    0 - stationary
    //    1 - move to single waypoint then stop
    //    2 - series of random absolute waypoints
    //    3 - series of random waypoints, each relative to the last
    //    4 - quantum gravity movement
    private int mMode = 0;

    private Location mWaypointLocn = new Location();
    private Heading mRobotHdng = new Heading();
    private Location mRobotLocn = new Location();
    private long mTimeNow = 0;
    private double mRobotRadius = 0;

    // Constraints on our movement.  //??? add max throttle?
    private boolean mAllowReversal = false;
    private double mMaxTurnRate = 10.0;
    private double mMaxMoveAmount = 25.0;

    //??? move some or all of this stuff to navigator?
    // Distance within which we consider waypoint reached.
    private double mWaypointProximity = 10.0;

    // Should body react to being hit by a bullet?
    private boolean mReactToHitByBullet = false;

    // Max number of turns allowed to reach a waypoint (0 = limit).
    // Time (turn number) when we abandon waypoint if not reached.
    // And number of times this has happened.
    private long mMaxWaypointDuration = 0; // 0 indicates no time limit.
    private long mWaypointTimeoutTime = 999999; 

    // Optional limit to amount of time remain stationary.
    private long mMaxStationaryDuration = 0; // 0 indicates no time limit.
    private long mStationaryTimeoutTime = 999999;

    // Statistics gathered by body.
    private long mNumWaypointTimeouts = 0;
    private long mNumTimesRammer = 0;
    private long mNumTimesRammee = 0;
    private long mNumTimesHitWall = 0;
    private long mNumTimesHitByBullet = 0;

    // Indicate whether we have just set a new waypoint.
    private boolean mWaypointSetThisTurn = false;

    private Navigator mNavigatorRef = null;

    public void Initialise()
    {
        SetMode(0);
        mWaypointLocn = new Location();
        mRobotLocn = new Location();
        mRobotHdng = new Heading();

        mTimeNow = 0;
	// What size does this body think it is, itself? For best
        // results, need to pass in the correct value *after*
        // initialising this body object! This is just a guess...
        mRobotRadius = 20;

        mAllowReversal = false;
        mMaxTurnRate = 10.0;

        mWaypointProximity = 10.0;
        mMaxWaypointDuration = 0;
        mWaypointTimeoutTime = 999999;
        mMaxStationaryDuration = 0;
        mStationaryTimeoutTime = 999999;

        mReactToHitByBullet = false;

        mWaypointSetThisTurn = false;

        // Statistics gathered by body.
        mNumWaypointTimeouts = 0;
        mNumTimesRammer = 0;
        mNumTimesRammee = 0;
        mNumTimesHitWall = 0;
        mNumTimesHitByBullet = 0;

        mNavigatorRef = null;
    }

    public void SetNavigatorRef(Navigator navRef)
    {
        mNavigatorRef = navRef;
    }

    public void SetTimeNow(final long timeNow)
    {
        mTimeNow = timeNow;
    }

    // Consider robot size as if robot is a circle - too hard to worry
    // about actual dimensions, since then orientation must be considered!
    public void SetRobotRadius(final double newValue)
    {
        mRobotRadius = newValue;
    }

    public void SetMaxTurnRate(final double newValue)
    {
        mMaxTurnRate = newValue;
    }

    public void SetAllowReversal(final boolean newValue)
    {
        mAllowReversal = newValue;
    }

    public void SetMaxWaypointDuration(final long newValue)
    {
        mMaxWaypointDuration = newValue;
    }

    //??? method needed for this?
    private void SetWaypointTimeoutTime(final long newValue)
    {
        mWaypointTimeoutTime = newValue;
    }

    public void SetMaxStationaryDuration(final long newValue)
    {
        mMaxStationaryDuration = newValue;
    }

    public void SetWaypointProximity(final double newValue)
    {
        mWaypointProximity = newValue;
    }

    public void SetMode(final int newMode)
    {
        mMode = newMode;
    }

    public void SetRobotLocn(final double newX, final double newY)
    {
        mRobotLocn.SetX(newX);
        mRobotLocn.SetY(newY);
    }

    public void SetRobotLocn(final Location robotLocn)
    {
        mRobotLocn.Set(robotLocn);
    }

    public void SetRobotHdng(final double newHdng)
    {
        mRobotHdng.Set(newHdng);
    }

    public void SetWaypointLocn(final double newX, final double newY)
    {
        Location newLocn = new Location();
        newLocn.Set(newX, newY);

        SetWaypointLocn(newLocn);
    }

    public void SetWaypointLocn(final Location newLocn)
    {
        mWaypointLocn.Set(newLocn);

        // If waypoint duration limit is being applied,
        // remember the time that this waypoint must be reached by.
        if (mMaxWaypointDuration > 0)
        {
            mWaypointTimeoutTime = mTimeNow + mMaxWaypointDuration;
        }
        else
        {
            // Set time far in the future so we will not reach it.
            mWaypointTimeoutTime = 999998;  //??? 999999?
        }

        // We are now moving for sure - clear stationary time limit if set.
        mStationaryTimeoutTime = 999999;

        // Indicate that we have set a new waypoint this turn, this
        // info may be queried externally by interested parties.
        mWaypointSetThisTurn = true;

//        System.out.println();
//        System.out.println("*** Body.SetWaypointLocn()");
//        System.out.println("Time: " + mTimeNow + ", mode: " + mMode);
//        System.out.println("loc: " + mRobotLocn.toString() +
//                           ", wpt: " + newLocn.toString() +
//                           ", till: " + mWaypointTimeoutTime);
    }

    public void SetReactToHitByBullet(final boolean newValue)
    {
        mReactToHitByBullet = newValue;
    }

    public final double GetRobotRadius()
    {
        return mRobotRadius;
    }

    public final Heading GetRobotHdng()
    {
        return mRobotHdng;
    }

    public final Location GetRobotLocn()
    {
        return mRobotLocn;
    }

    public final long GetNumWaypointTimeouts()
    {
        return mNumWaypointTimeouts;
    }

    public final long GetNumTimesRammer()
    {
        return mNumTimesRammer;
    }

    public final long GetNumTimesRammee()
    {
        return mNumTimesRammee;
    }

    public final long GetNumTimesHitWall()
    {
        return mNumTimesHitWall;
    }

    public final long GetNumTimesHitByBullet()
    {
        return mNumTimesHitByBullet;
    }

    public final boolean IsWaypointSetThisTurn()
    {
        return mWaypointSetThisTurn;
    }

    // Examines current state of body, and determines what action
    // we should be taking as a result.
    // Returns indication of whether state has been achieved, i.e.
    // we have reached our target destination and can start
    // worrying about what comes after. Otherwise, indicates what
    // action we should be taking.
    public BodyInstruction[] Process()
    {
        boolean waypointReached = false;
        BodyInstruction[] instructArray = new BodyInstruction[3];

        mWaypointSetThisTurn = false;

//        System.out.println();
//        System.out.println("*** Body.Process, at time " + mTimeNow);
//        System.out.println("mMode: " + mMode);
//        System.out.print("myLocn: ");
//        mRobotLocn.Print();
//        System.out.println();
//        System.out.print("waypt: ");
//        mWaypointLocn.Print();
//        System.out.println();

        // Initialise array elements   //??? loop here?
        instructArray[0] = new BodyInstruction();
        instructArray[1] = new BodyInstruction();
        instructArray[2] = new BodyInstruction();
        instructArray[0].SetNothing();
        instructArray[1].SetNothing();
        instructArray[2].SetNothing();

        if (mMode == 0)
        {
            // All array elements already set to "nothing", leave them.
        }
        else if (mMode == 1)
        {
            // Continue to head for waypoint. If reached, we do
            // nothing, await new instructions from above.
            waypointReached = ProcessWaypointMove(instructArray);

            // If appropriate, apply limit to time we can remain
            // stationary after reaching waypoint.
            if (waypointReached && (mMaxStationaryDuration > 0))
            {
                if (mTimeNow >= mStationaryTimeoutTime)
                {
//                    System.out.println("Exceeded max stationary time!!!");

                    Location newWp = mNavigatorRef.RandomWaypointRel(
                                                                 mRobotLocn);
                    SetWaypointLocn(newWp);
                }
                else if (mStationaryTimeoutTime >= 999999) //???
                {
//                    System.out.println("Setting stationary timeout time");
                  
                    // This must be first turn stationary, set next limit time.
                    mStationaryTimeoutTime = mTimeNow + mMaxStationaryDuration;
                }
//                else
//                {
//                    System.out.println("Already stationary, waiting...");
//                }
            }
        }
        else if (mMode == 2)
        {
            // Continue to head for waypoint. If reached, we
            // autonomously set a new waypoint and will continue
            // moving toward that in future.
            waypointReached = ProcessWaypointMove(instructArray);
            if (waypointReached)
            {
                Location newWp = mNavigatorRef.RandomWaypointAbs();
                SetWaypointLocn(newWp);
            } 
        }
        else if (mMode == 3)
        {
            // Continue to head for waypoint. If reached, we
            // autonomously set a new waypoint and will continue
            // moving toward that in future.
            waypointReached = ProcessWaypointMove(instructArray);
            if (waypointReached)
            {
                Location newWp = mNavigatorRef.RandomWaypointRel(mRobotLocn);
                SetWaypointLocn(newWp);
            }
        }
        else if (mMode == 4)
        {
            // Continue to head for waypoint. If reached, we
            // autonomously set a new waypoint and will continue
            // moving toward that in future.
            waypointReached = ProcessWaypointMove(instructArray);
            if (waypointReached)
            {
                final Location newWp =
                         mNavigatorRef.QuantumGravWaypoint(mRobotLocn);
                SetWaypointLocn(newWp);
            }
        }
        else
        {
            System.out.println("ERROR: Body.Process, " + "unexpected mode");
        }

        return instructArray;
    }
    // Move towards the defined waypoint. What happens after we get there
    // is determined at a higher level.
    private boolean ProcessWaypointMove(BodyInstruction[] instructArray)
    {
        boolean achieved = false;
        double targetDist = 0;
        int index = 0;
        Heading waypointHdng = new Heading();
        Bearing relBearing = new Bearing();
        boolean reversing = false;

//        System.out.println();
//        System.out.println("Body.ProcessWaypointMove()");

        targetDist = mRobotLocn.DistanceTo(mWaypointLocn);
        waypointHdng.SetFromTo(mRobotLocn, mWaypointLocn);

//        System.out.print("mRobotLocn: ");
//        mRobotLocn.Print();
//        System.out.println();
//        System.out.print("mWaypointLocn: ");
//        mWaypointLocn.Print();
//        System.out.println();

//        System.out.println("targetDist: " + targetDist);
//        System.out.println("waypointHdng: " + waypointHdng.Get());

        // Determine relative bearing of target to our heading,
        // range -180 to +180.
        relBearing.SetFromTo(mRobotHdng, waypointHdng);

//        System.out.println("relBearing: " + relBearing.Get());

//        System.out.println("Doing any turning...");

        // For a simple robot, we want to ensure turn happens first.
        // Thus put any turn instruction into array first, move after.
        // (Advanced robot will deal with both in one turn, and won't
        // worry about the ordering.)
        index = 0;
        if (relBearing.IsAligned())
        {
            // No turn needed, and we'll be driving forward.
            reversing = false;
        }
        else if (relBearing.IsOpposed() && (mAllowReversal == true))
        {
            // No turn needed, and we'll be driving backward.
            reversing = true;
        }
        else if (relBearing.IsForward()
                   ||
                 (relBearing.IsBackward() && (mAllowReversal == false)))
        {
            // If it is behind us and we are not allowed to reverse,
            // we'll have to turn right round. If it is in front, 
            // no problem. Either way, we will be driving ahead.
            reversing = false;

            double turnAmount = mMaxTurnRate;
            if (relBearing.GetAbs() < turnAmount)
            {
                turnAmount = relBearing.GetAbs();
            }

            if (relBearing.IsLeftward())
            {
                instructArray[index].SetTurnLeft(turnAmount);
            }
            else
            {
                instructArray[index].SetTurnRight(turnAmount);
            }

            // Ensure any further instructions go into next slot.
            index += 1;
        }
        else if (relBearing.IsBackward() && (mAllowReversal == true))
        {
            // We will be reversing, so the relative bearing from
            // our actual travel direction to target heading should
            // be flipped 180 degrees accordingly.
            reversing = true;
            Bearing flippedBearing = new Bearing();
            flippedBearing.Set(relBearing);
            flippedBearing.Flip();

            double turnAmount = mMaxTurnRate;
            if (flippedBearing.GetAbs() < turnAmount)
            {
                turnAmount = flippedBearing.GetAbs();
            }

            if (flippedBearing.IsLeftward())
            {
                instructArray[index].SetTurnLeft(turnAmount);
            }
            else
            {
                instructArray[index].SetTurnRight(turnAmount);
            }

            // Ensure any further instructions go into next slot.
            index += 1;
        }
        else
        {
            System.out.println("ERROR: Body.ProcessWaypointMove(), " +
                               "unexpected mode");
        }

        // Drive instructions.
        // Have we reached waypoint yet?
        if (targetDist < mWaypointProximity)
        {
            achieved = true;
            instructArray[index].SetAchieved();
        }
        else if (WaypointTimeout() == true)
        {
            achieved = true;
            instructArray[index].SetAchieved();
        }
        else
        {
            double moveAmount = mMaxMoveAmount;
            if (moveAmount > targetDist)
            {
                moveAmount = targetDist;
            }

            if (reversing == true)
            {
                instructArray[index].SetMoveBack(moveAmount);
            }
            else
            {
                instructArray[index].SetMoveAhead(moveAmount);
            }
        }

//        System.out.println("End of ProcessWaypointMove()");
//        System.out.println();

        return achieved;
    }

    // Have we exceeded the allowed time to reach the waypoint?
    private boolean WaypointTimeout()
    {
        boolean answer = false;

//        System.out.println("Checking for waypoint timeout [" + mTimeNow +
//                           "/" + mWaypointTimeoutTime + "]");

        // If max duration is zero, it means there is no limit
        // to the amount of time allowed to reach waypoint.
        if (mMaxWaypointDuration > 0)
        {
            if (mTimeNow >= mWaypointTimeoutTime)
            {
                answer = true;

//                System.out.println("*** Waypoint timout [" + mTimeNow +
//                                   "/" + mWaypointTimeoutTime + "]");

                mNumWaypointTimeouts++;
            }
        }

        return answer;
    } 

    // In some operating modes, body may react autonomously to some events.
    public void ActOnRobotHitRobot(final HitRobotEvent event)
    {
        if (event.isMyFault())
        {
            // Count how many collisions were my fault.
            mNumTimesRammer++;

//            System.out.println("***** My fault hit robot *****");

            if ((mMode == 2) || (mMode == 3) || (mMode == 4))
            {
                // We know bearing to contact, so lets set a waypoint
                // away from it to try to break free. Note that the
                // bearing is RELATIVE to our current heading.

                // Determine bearing to move away.
                Bearing brng = new Bearing();
                brng.Set(event.getBearing());
                brng.Flip();

                // Determine actual heading to move in, i.e. our
                // current heading adjusted by the relative bearing.
                Heading hdng = new Heading();
                hdng.Set(mRobotHdng);
                hdng.Adjust(brng);

                // Derive a location to be set as our next waypoint.
                // When we hit that, next one will again be determined
                // as normal for the particualr mode we are in.
                Location newWp = new Location();
                newWp.SetRelative(mRobotLocn, hdng, 100);
                SetWaypointLocn(newWp);
            }
        }
        else
        {
            // Count number of collisions that were not my fault.
            mNumTimesRammee++;
        }
    }

    // In some operating modes, body may react autonomously to some events.
    public void ActOnRobotHitWall(final HitWallEvent event)
    {
        mNumTimesHitWall++;

//        System.out.println("***** Body : Robot hit wall!!! *****");

        if (mMode == 2)
        {
            Location newWp = mNavigatorRef.RandomWaypointAbs();
            SetWaypointLocn(newWp);
        }
        else if (mMode == 3)
        {
            Location newWp = mNavigatorRef.RandomWaypointRel(mRobotLocn);
            SetWaypointLocn(newWp);
        }
        else if (mMode == 4)
        {
            final Location newWp =
                       mNavigatorRef.QuantumGravWaypoint(mRobotLocn);
            SetWaypointLocn(newWp);
        }
    }

    public void ActOnHitByBullet(final HitByBulletEvent event)
    {
        mNumTimesHitByBullet++;

        if (mReactToHitByBullet == true)
        {
            System.out.println("*** Body : react to hit by bullet, " +
                               "time " + mTimeNow);

            Location newWp = mNavigatorRef.RandomWaypointRel(mRobotLocn);
            SetWaypointLocn(newWp);
        }
    }

    public void ActOnBulletFireDetection()
    {
//        System.out.println();
//        System.out.println("*** Bullet detect at time " + mTimeNow);

        Location newWp = mNavigatorRef.RandomWaypointRel(mRobotLocn);
        SetWaypointLocn(newWp);
    }

    public void Print()
    {
        System.out.println("----- Body,Print() ------");
        System.out.println("mMode: " + mMode);
        System.out.print("mWaypoint:");
        mWaypointLocn.Print();
        System.out.println();
        System.out.print("mRobotLocn:");
        mRobotLocn.Print();
        System.out.println();
        System.out.println("-------------------------");
    }
}
