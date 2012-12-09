package shrub;

import java.util.LinkedList;
import java.util.ListIterator;
import java.text.NumberFormat;

/**
 * MoveHistory - a fixed-size list of time-ordered movement records for
 *               a given object in the game. Some may be firm sightings,
 *               others may be estimates.
 */
public class MoveHistory extends LinkedList
{
    private int mMaxSize = 0;
    private String mName = new String("unnamed");

    // null indicates we do not wish to clip extrapolations.
    private Box mMovementArea = null;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public MoveHistory()
    {
        Initialise();
    }

    public void Initialise()
    {
        mName = new String("unnamed");
        mMaxSize = 100;

        mMovementArea = null;

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public void SetName(final String newValue)
    {
        mName = new String(newValue);
    }

    public final String GetName()
    {
        return mName;  //??? should really alloc new memory and copy name in?
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

    public void SetMaxMoves(final int newValue)
    {
        mMaxSize = newValue;
    }

    public final int GetMaxMoves()
    {
        return mMaxSize;
    }

    public final int GetNumMoves()
    {
        return this.size();
    }

    // Search back for last firm sighting (i.e. non-estimate).
    // Allocates memory for answer and passes it out, so maintains internal
    // data integrity.
    // Null reference indicates no non-estimated movement found.
    public final Movement GetLastNonEstimate()
    {
//        System.out.println();
//        System.out.println("MoveHistory.GetLastNonEstimate()");

        Movement answer = null;
        boolean found = false;
        int listSize = this.size();

        // Use an iterator for performance - this is a linked list.
        ListIterator iterator = this.listIterator(listSize);
        while (!found && iterator.hasPrevious())
        {
            Movement thisMove = (Movement)iterator.previous();
            if (thisMove.GetIsEstimate() == false)
            {
                answer = thisMove;
                found = true;
            }
        }

        return answer;
    }

    // Should be used only to add the very first sighting of the
    // target, i.e. to an empty list.
    private final boolean AddFirstSighting(final Sighting newSighting)
    {
//        System.out.println("MoveHistory.AddFirstSighting() of [" +
//                           mName + "]");

        boolean okSoFar = true;
        long newMoveTime = newSighting.GetTimestamp();
        Movement newMove = new Movement();

        // Setup new movement from sighting data,
        // as there is no previous info.
        //??? use Movement.FromSighting() method here?
        newMove.SetTimeNow(newMoveTime);
        newMove.SetTimeDelta(1);
        newMove.SetEnergyNow(newSighting.GetEnergy());
        newMove.SetVelocityNow(newSighting.GetVelocity());
        newMove.SetHdngNow(newSighting.GetHeading());
        Bearing hdngDelta = new Bearing();
        hdngDelta.Set(0);
        newMove.SetLocnNow(newSighting.GetLocation());

//        newMove.Print();

        // Now add it to the list.
        AppendMovement(newMove);

        return okSoFar;
    }

    // Should be used only to add a subsequent sighting of the
    // target, i.e. not first sighting, and hence not to empty list.
    private final boolean AddSubsequentSighting(final Sighting newSighting)
    {
//        System.out.println("MoveHistory.AddSubsequentSighting() of [" +
//                           mName + "]");

        boolean okSoFar = true;
        
        Movement oldSightingMove = GetLastNonEstimate();
        if (oldSightingMove == null)
        {
            System.out.println("ERROR: MoveHistory.AddSubsequentSighting, " +
                               "no previous sighting of [" + mName + "]");
            okSoFar = false;
        }
        else
        {
            long newSightTime = newSighting.GetTimestamp();
            long oldSightTime = oldSightingMove.GetTimeNow();
            long sightTimeDelta = newSightTime - oldSightTime;

            Movement lastMove = (Movement)this.getLast();
            long lastMoveTime = lastMove.GetTimeNow();
            long moveTimeDelta = newSightTime - lastMoveTime;

            if (sightTimeDelta <= 0)
            {
//                System.out.println("ERROR: MoveHistory, " +
//                                   "new sighting prior to old sighting!");
                okSoFar = false;
            }
            else if (moveTimeDelta <= 0)
            {
//                System.out.println("ERROR: MoveHistory, " +
//                                   "new sighting prior to estimate!");
                okSoFar = false;
            }
            else
            {
/*???
                //??? probably only useful for debugging
                // Do another extrapolation, just so we can see how far
                // off we would have been from the real sighting.
                Movement extrapolation = new Movement(lastMove);
                boolean extrapolated = extrapolation.ExtrapolateSelf(
                                                            newSightTime,
                                                            mMovementArea,
                                                            null);
                if (extrapolated == true)
                {
                    Location estLocn = extrapolation.GetLocnNow();
                    Heading estHdng = extrapolation.GetHdngNow();
                    Location trueLocn = newSighting.GetLocation();
                    Heading trueHdng = newSighting.GetHeading();
                    double distance = trueLocn.DistanceTo(estLocn);
                    Bearing hdngDiff = new Bearing();
                    hdngDiff.SetFromTo(trueHdng, estHdng);

//                    System.out.println("=== Extrapolation drift =====");
//                    System.out.print(" HdngDiff: " +
//                                     hdngDiff.toString());
//                    System.out.print(" Distance: " +
//                                     dp2.format(distance));
//                    System.out.println();
                }
*/

                //??? if too far out, interpolate over the bad extrapolation?

                // Setup new movement from sighting data and last movement.
                Movement newMove = new Movement();
                newMove.FromSighting(newSighting);
                newMove.SetTimeDelta(moveTimeDelta);
                Bearing hdngDelta = new Bearing();
                hdngDelta.SetFromTo(oldSightingMove.GetHdngNow(),
                                    newMove.GetHdngNow());
                double avgeHdngDelta = hdngDelta.Get() / sightTimeDelta;
                hdngDelta.Set(avgeHdngDelta);
                newMove.SetHdngDelta(hdngDelta);

                // Now add it to the list.
                AppendMovement(newMove);
            }
        }

        return okSoFar;
    }

    public final boolean ReportSighting(final Sighting newSighting)
    {
        boolean success = false;
        long newMoveTime = newSighting.GetTimestamp();

//        System.out.println();
//        System.out.println("MoveHistory.ReportSighting() at time " +
//                           newMoveTime);
//        newSighting.Print();

        // Determine whether this is first sighting of this target.
        if (this.size() <= 0)
        {
            AddFirstSighting(newSighting);
        }
        else
        {
            success = AddSubsequentSighting(newSighting);
        }

        return success;
    }

    public final boolean Extrapolate(final long timeNow)
    {
        boolean extrapolated = false;

//        System.out.println();
//        System.out.println("MoveHistory.Extrapolate() to time " +
//                           timeNow);
//        System.out.println("allowed area is:");
//        mMovementArea.Print();

        if (this.size() <= 0)
        {
            System.out.println("Cannot extrapolate, no sighting yet");
        }
        else
        {
//            System.out.println("Can extrapolate, list not zero size");

            final Movement lastMove = (Movement)this.getLast();
            Movement extrapolation = new Movement(lastMove);
            extrapolated = extrapolation.ExtrapolateSelf(timeNow,
                                                         mMovementArea,
                                                         null);
            if (extrapolated == true)
            {
//                extrapolation.Print();

                AppendMovement(extrapolation);
            }
        }

        return extrapolated;
    }

    public void AppendMovement(Movement newMove)
    {
        this.addLast(newMove);
        TruncateListStart();

//        newMove.Print();
//        this.Print();
    }

    private void TruncateListStart()
    {
        int sizeNow = this.size();

        // Drop early entries until within permitted list size.
        while (sizeNow > mMaxSize)
        {
            this.removeFirst();
            sizeNow = this.size();
        }
    }

    // Allocates memory and returns it, so no access to internal data.
    public final Movement GetLastMove()
    {
        Movement lastMove = null;

        if (this.size() > 0)
        {
            lastMove = new Movement();
            lastMove.Set((Movement)this.getLast());
        }

        return lastMove;
    }

    public boolean IsEmpty()
    {
        boolean answer = false;
        if (this.size() <= 0)
        {
            answer = true;
        }
        return answer;
    }

    // Determine energy drop between last two sightings of target. If not
    // up to date, or estimated, or too far apart, will not succeed.
    public double GetEnergyDelta(final long toTime)
    {
        double energyDelta = 0;

//        System.out.println();
//        System.out.println("GetEnergyDelta() to time " + toTime);

        int listSize = this.size();

//        System.out.println("List size: " + listSize);

        if (listSize < 2)
        {
            System.out.println("Need >=2 sightings to determine energy delta!");
        }
        else
        {
            // Get last movement record from list.
            ListIterator iterator = this.listIterator(listSize);
            Movement lastMove = (Movement)iterator.previous();

            // If an estimation, we can't determine energy drop.
            // If not up-to-date, no use to us.
            boolean lastIsEst = lastMove.GetIsEstimate();
            long lastTime = lastMove.GetTimeNow();
            if (lastTime != toTime)
            {
//                System.out.println("MoveHistory.GetEnergyDrop(), " +
//                                   "last move time invalid, aborting");
            }
            else if (lastIsEst == true)
            {
//                System.out.println("MoveHistory.GetEnergyDrop(), " +
//                                   "last move is estimation, aborting");
            }
            else
            {
                // Ok so far, let's get the previous movement for comparison.
                Movement prevMove = (Movement)iterator.previous();
                boolean prevIsEst = prevMove.GetIsEstimate();
                long prevTime = prevMove.GetTimeNow();

                long deltaTime = lastTime - prevTime;
                if (deltaTime != 1)
                {
//                    System.out.println("MoveHistory.GetEnergyDrop(), " +
//                                       "time delta invalid, aborting");
                }
                else if (prevIsEst == true)
                {
//                    System.out.println("MoveHistory.GetEnergyDrop(), " +
//                                       "prev move is estimation, aborting");
                }
                else
                {
                    double lastEnergy = lastMove.GetEnergyNow();
                    double prevEnergy = prevMove.GetEnergyNow();
                    energyDelta = lastEnergy - prevEnergy;

//                    System.out.println("Okay, energyDelta: " + energyDelta);
                }
            }
        }

        return energyDelta;
    }

    public void Print()
    {
        System.out.println("===== MoveHistory =====");
        System.out.println(" mName: " + mName);
        System.out.print(" mMaxSize: " + mMaxSize);
        System.out.print(" mSizeNow: " + this.size());
        System.out.println();

        /*???
        if (this.size() > 0)
        {
            Movement lastMove = (Movement)this.getLast();
            lastMove.Print();
        }
        */
    }
}

