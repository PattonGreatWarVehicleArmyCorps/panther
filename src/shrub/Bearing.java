package shrub;

import java.text.NumberFormat;

/**
 * Bearing - contains relative bearing in range -180 to <180
 */
public class Bearing
{
    private double mBearing = 0;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public Bearing()
    {
        Initialise();
    }

    public Bearing(final double brngVal)
    {
        Initialise();
        Set(brngVal);
    }

    public Bearing(final Bearing brngVal)
    {
        Initialise();
        Set(brngVal);
    }

    public void Initialise()
    {
        mBearing = 0;

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public void Set(final double newValue)
    {
        mBearing = newValue;
        Normalise();
    }

    public void Set(final Bearing newValue)
    {
        if (newValue != null)
        {
            mBearing = newValue.Get();
            Normalise();
        }
/*???
        else
        {
            System.out.println("ERROR: null bearing passed to Bearing.Set()");
        }
*/
    }

    public void Set(final Heading newValue)
    {
        if (newValue != null)
        {
            mBearing = newValue.Get();
            Normalise();
        }
/*???
        else
        {
            System.out.println("ERROR: null heading passed to Bearing.Set()");
        }
*/
    }

    public void Flip()
    {
        mBearing += 180.0;
        Normalise();
    }

    public final double Get()
    {
        return mBearing;
    }

    public final double GetAbs()
    {
        return Math.abs(mBearing);
    }

    public final boolean IsNormalised()
    {
        boolean answer = false;
        if ((mBearing >= -180) && (mBearing < 180))
        {
            answer = true;
        }
        return answer;
    }

    // Make sure the value falls within the permitted range -180 to +180.
    public void Normalise()
    {
        boolean done = false;
        while (!done)
        {
            if (mBearing >= 180)
            {
                mBearing -= 360;
            }
            else if (mBearing < -180)
            {
                mBearing += 360;
            }
            else
            {
                done = true;
            }
        }
    }	
    public void SetFromTo(Heading fromHdng, Heading toHdng)
    {
        mBearing = toHdng.Get() - fromHdng.Get();
        Normalise();
    }

    public void SetFromTo(Bearing fromBrng, Bearing toBrng)
    {
        mBearing = toBrng.Get() - fromBrng.Get();
        Normalise();
    }

    public final boolean IsAligned()
    {
        boolean answer = false;

        if ((mBearing >= -0.5) && (mBearing <= 0.5))
        {
            answer = true;
        }

        return answer;
    }

    public final boolean IsOpposed()
    {
        boolean answer = false;

        if ((mBearing >= 179.5) || (mBearing <= -179.5))
        {
            answer = true;
        }

        return answer;
    }

    public final boolean IsLeft90()
    {
        boolean answer = false;

        if ((mBearing >= -90.5) && (mBearing <= -89.5))
        {
            answer = true;
        }

        return answer;
    }

    public final boolean IsRight90()
    {
        boolean answer = false;

        if ((mBearing >= 89.5) && (mBearing <= 90.5))
        {
            answer = true;
        }

        return answer;
    }

    public final boolean IsLeftward()
    {
        boolean answer = false;

        if ((mBearing < -0.5) && (mBearing > -179.5))
        {
            answer = true;
        }

        return answer;
    }

    public final boolean IsRightward()
    {
        boolean answer = false;

        if ((mBearing > 0.5) && (mBearing < 179.5))
        {
            answer = true;
        }

        return answer;
    }

    public final boolean IsForward()
    {
        boolean answer = false;

        if ((mBearing >= -90.0) && (mBearing <= 90.0))
        {
            answer = true;
        }

        return answer;
    }

    public final boolean IsBackward()
    {
        boolean answer = false;

        if ((mBearing < -90.0) || (mBearing > 90.0))
        {
            answer = true;
        }

        return answer;
    }

    public String toString()
    {
        String answer = new String();
        answer = answer.concat(dp2.format(mBearing));
        return answer;
    }
}
