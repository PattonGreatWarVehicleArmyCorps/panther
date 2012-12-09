package shrub;

import java.text.NumberFormat;

/**
 * Movement - data about an object's movement at an instant in time.
 */
public class Movement
{
    private long mTimeNow = 0;
    private long mTimeDelta = 0;
    private double mVelocityNow = 0.0;
    private Heading mHdngNow = null;
    private Bearing mHdngDelta = null;
    private Location mLocnNow = null;
    private boolean mIsEstimate = false;
    private long mEstimationTime = 0;
    private double mEnergyNow = 0.0;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public Movement()
    {
//        Initialise();
    }

    public Movement(final Movement otherMove)
    {
//        Initialise();
	Set(otherMove);
    }

    public void Initialise()
    {
        mTimeNow = 0;
        mTimeDelta = 0;
        mVelocityNow = 0.0;
        mHdngNow = new Heading();
        mHdngDelta = new Bearing();
        mLocnNow = new Location();
        mIsEstimate = false;
        mEstimationTime = 0;
        mEnergyNow = 0.0;

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public void Set(final Movement otherMove)
    {
        if (otherMove != null)
        {
            mTimeNow = otherMove.GetTimeNow();
            mTimeDelta = otherMove.GetTimeDelta();
            mVelocityNow = otherMove.GetVelocityNow();
            mHdngNow = otherMove.GetHdngNow();
            mHdngDelta = otherMove.GetHdngDelta();
            mLocnNow = otherMove.GetLocnNow();
            mIsEstimate = otherMove.GetIsEstimate();
            mEstimationTime = otherMove.GetEstimationTime();
            mEnergyNow = otherMove.GetEnergyNow();
        }
    }

    public void SetTimeNow(final long newValue)
    {
        mTimeNow = newValue;
    }

    public final long GetTimeNow()
    {
        return mTimeNow;
    }

    public void SetTimeDelta(final long newValue)
    {
        mTimeDelta = newValue;
    }

    public final long GetTimeDelta()
    {
        return mTimeDelta;
    }

    public void SetVelocityNow(final double newValue)
    {
        mVelocityNow = newValue;
    }

    public final double GetVelocityNow()
    {
        return mVelocityNow;
    }

    public void SetLocnNow(final Location newValue)
    {
        if (mLocnNow == null)
        {
            mLocnNow = new Location(newValue);
        }
        else
        {
            mLocnNow.Set(newValue);
        }
    }

    public final Location GetLocnNow()
    {
        Location answer = new Location(mLocnNow);
        return answer;
    }

    // Note: returns a reference to internal data, so use with caution!
    public final Location GetRefToLocnNow()
    {
        return mLocnNow;
    }

    public void SetHdngNow(final Heading newValue)
    {
        if (mHdngNow == null)
        {
            mHdngNow = new Heading(newValue);
        }
        else
        {
            mHdngNow.Set(newValue);
        }
    }

    // Note: returns a reference to internal data, so use with caution!
    public final Heading GetRefToHdngNow()
    {
        return mHdngNow;
    }

    public final Heading GetHdngNow()
    {
        Heading answer = new Heading(mHdngNow);
        return answer;
    }

    public void SetHdngDelta(final Bearing newValue)
    {
        if (mHdngDelta == null)
        {
            mHdngDelta = new Bearing(newValue);
        }
        else
        {
            mHdngDelta.Set(newValue);
        }
    }

    // Note: returns a reference to internal data, so use with caution!
    public final Bearing GetRefToHdngDelta()
    {
        return mHdngDelta;
    }

    public final Bearing GetHdngDelta()
    {
        Bearing answer = new Bearing(mHdngDelta);
        return answer;
    }

    public void SetIsEstimate(final boolean newValue)
    {
        mIsEstimate = newValue;
    }

    public final boolean GetIsEstimate()
    {
        return mIsEstimate;
    }

    public void SetEstimationTime(final long newValue)
    {
        mEstimationTime = newValue;
    }

    public final long GetEstimationTime()
    {
        return mEstimationTime;
    }

    public void SetEnergyNow(final double newValue)
    {
        mEnergyNow = newValue;
    }

    public final double GetEnergyNow()
    {
        return mEnergyNow;
    }

    // Extrapolate this movement into the future, to specified time.
    // If allowedArea is non-null, extrapolation will be clipped to the
    // specified boundary.
    // If deltaMove is null, delta is derived from this movement record, if
    // non-null then the delta is derived from supplied record.
    public final boolean ExtrapolateSelf(final long toTime,
                                         Box allowedArea,
                                         final Movement deltaMoveSupplied)
    {
//        System.out.println();
//        System.out.println("+++ Movement::ExtrapolateSelf() to " + toTime);

        boolean extrapolated = false;
        long oldTime = this.GetTimeNow();
        long timeDelta = toTime - oldTime;

        double delta_velocity = 0.0;
        Bearing delta_hdngDelta = null;
        if (deltaMoveSupplied == null)
        {
//            System.out.println("no delta movement supplied, use this");

            delta_velocity = this.GetVelocityNow();
            delta_hdngDelta = this.GetHdngDelta();
        }
        else
        {
//            System.out.println("alternative delta movement supplied");

            delta_velocity = deltaMoveSupplied.GetVelocityNow();
            delta_hdngDelta = deltaMoveSupplied.GetHdngDelta();
        }

        // Derive new values based on constant angular velocity, and
        // constant linear velocity.
        // ??? constant linear acceleration assumption later

        if (timeDelta < 0)
        {
            System.out.println("ERROR: Movement::ExtrapolateSelf, " +
                               "time delta < 0");
            extrapolated = false;
        }
        else if (timeDelta == 0)
        {
//            System.out.println("Movement: up to date, no extrapolation");

            // No need to extrapolate, we are already up to date.
            extrapolated = false;
        }
        else
        {
//            System.out.println("...doing extrapolation of this move...");

            long estimationTime = this.GetEstimationTime() + timeDelta;

            this.SetTimeNow(toTime);
            this.SetTimeDelta(timeDelta);
            this.SetIsEstimate(true);
            this.SetEstimationTime(estimationTime);

            // No energy extrapolation at the moment, leave unchanged.

            // Any clipping does not affect our estimate of heading change.
            //??? optimise this to reduce memory alloc + copy impact?
            Heading newHdng = this.GetHdngNow();
            newHdng.Adjust(delta_hdngDelta.Get() * timeDelta);
            this.SetHdngNow(newHdng);
            this.SetHdngDelta(delta_hdngDelta);

            //??? better extrapolation than pure constant speed linear TBD.
            Location oldLocn = this.GetLocnNow();
            Location newLocn = new Location();
            newLocn.SetRelative(oldLocn,
                                newHdng,
                                (delta_velocity * timeDelta));

            // If allowedArea details passed in, clip movement on hitting wall.
            if (allowedArea == null)
            {
//                System.out.println("Extrapolation: allowedArea not set");

                this.SetLocnNow(newLocn);
                this.SetVelocityNow(delta_velocity);
            }
            else if (allowedArea.IsInside(oldLocn))
            {
//                System.out.println("Currently inside allowed area");

                if (allowedArea.IsInside(newLocn))
                {
//                    System.out.println("Extrapolation: unclipped");

                    this.SetLocnNow(newLocn);
                    this.SetVelocityNow(delta_velocity);
                }
                else
                {
//                    System.out.println("Extrapolation: hit wall!");

                    // Determine point at which we estimate wall will be hit.
                    // Note that -ve velocity means travel in the opposite
                    // direction, so heading must be flipped!
                    Location intLocn = null;
                    if (delta_velocity > 0)
                    {
                        intLocn = allowedArea.EdgeIntersectOutward(
                                                         oldLocn,
                                                         this.GetHdngNow());
                    }
                    else
                    {
                        Heading flippedHdng = this.GetHdngNow();
                        flippedHdng.Flip();
                        intLocn = allowedArea.EdgeIntersectOutward(
                                                         oldLocn,
                                                         flippedHdng);
                    }

                    if (intLocn == null)
                    {
                        System.out.println("ERROR: Movement::Extrapolate(), " +
                                           "intersect locn null");

                        // What to do? Just stop it dead where it is.
                        this.SetLocnNow(oldLocn);
                        this.SetVelocityNow(0.0);
                    }
                    else
                    {
//                        System.out.println("Hit wall at: " +
//                                           intLocn.toString());

                        // Assume it stops dead at point where it hits wall.
                        this.SetLocnNow(intLocn);
                        this.SetVelocityNow(0.0);
                    }
                }
            }
            else
            {
//                System.out.println("ERROR: Target outside expected area");
//                System.out.println("oldMove: ");
//                this.Print();
//                System.out.println("allowed area:");
//                allowedArea.Print();

                // Already outside supposed allowed area, assume stands
                // still at location it is already at.
                this.SetLocnNow(oldLocn);
                this.SetVelocityNow(0.0);
            }

            extrapolated = true;

//            System.out.println("Resultant extrapolation:");
//            extrapolation.Print();  //???
        }

        return extrapolated;
    }

    public void FromSighting(final Sighting fromSight)
    {
        Initialise();

        mTimeNow = fromSight.GetTimestamp();
//???        mLocnNow.Set(fromSight.GetLocation());  //??? = rather than set?
//???        mHdngNow.Set(fromSight.GetHeading());
        mLocnNow = fromSight.GetLocation();
        mHdngNow = fromSight.GetHeading();
        mVelocityNow = fromSight.GetVelocity();
        mEstimationTime = fromSight.GetInaccuracy();
        mEnergyNow = fromSight.GetEnergy();
    }

    // Create a new Sighting record and pass it back out.
    public Sighting ToSighting()
    {
        Sighting answer = new Sighting();

	/*???
        answer.SetTimestamp(this.GetTimeNow());
        answer.SetLocation(this.GetLocnNow());
        answer.SetHeading(this.GetHdngNow());       //??? = not Set?
        answer.SetVelocity(this.GetVelocityNow());
        answer.SetInaccuracy((int)this.GetEstimationTime());
        answer.SetEnergy(this.GetEnergyNow());
	*/
        answer.SetTimestamp(mTimeNow);
        answer.SetLocation(mLocnNow);
        answer.SetHeading(mHdngNow);
        answer.SetVelocity(mVelocityNow);
        answer.SetInaccuracy((int)mEstimationTime);
        answer.SetEnergy(mEnergyNow);

        return answer;
    }

    public void Print()
    {
        System.out.println("========== Movement ==========");
        System.out.print(" Time: " + mTimeNow);
        System.out.print(" Delta: " + mTimeDelta);
        System.out.println();
//        System.out.print(" IsEst: " + mIsEstimate);
//        System.out.print(" EstTime: " + mEstimationTime);
//        System.out.println();
//        System.out.print(" Locn: " + mLocnNow.toString());
//        System.out.print(" Vel: " + dp2.format(mVelocityNow));
//        System.out.println();
//        System.out.print(" Hdng: " + mHdngNow.toString());
//        System.out.print(" Delta: " + mHdngDelta.toString());
//        System.out.println();
    }
}
