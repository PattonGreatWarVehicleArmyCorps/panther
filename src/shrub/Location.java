package shrub;

import java.text.NumberFormat;

/**
 * Location - encapsulates location information and provides some
 *            useful methods for manipulating / deriving locations.
 */
public class Location
{
    private double mX = 0;
    private double mY = 0;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public Location()
    {
        Initialise();
    }

    public Location(final double xPos, final double yPos)
    {
        Initialise();
        mX = xPos;
        mY = yPos;
    }

    public Location(final Location locn)
    {
        Initialise();
        Set(locn);
    }

    public void Initialise()
    {
        mX = 0;
        mY = 0;

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public void Set(final Location newLocn)
    {
        if (newLocn != null)
        {
            mX = newLocn.GetX();
            mY = newLocn.GetY();
        }
        /*???
        else
        {
            System.out.println("ERROR: null location passed to Location.Set()");
        }
        */
    }

    public void Set(final double newX, final double newY)
    {
        mX = newX;
        mY = newY;
    }

    public void SetX(final double newX)
    {
        mX = newX;
    }

    public void SetY(final double newY)
    {
        mY = newY;
    }

    public void ChangeX(final double deltaX)
    {
        mX += deltaX;
    }

    public void ChangeY(final double deltaY)
    {
        mY += deltaY;
    }

    public double GetX()
    {
        return mX;
    }

    public double GetY()
    {
        return mY;
    }

    public double DistanceTo(final Location toLocn)
    {
        double answer = -1;

        double fromX = mX;
        double fromY = mY;
        double toX = toLocn.GetX();
        double toY = toLocn.GetY();

        double deltaX = toX - fromX;
        double deltaY = toY - fromY;

        double targetDist2 = (deltaX*deltaX) + (deltaY*deltaY);

        answer = Math.sqrt(targetDist2);

        return answer;
    }

    // Determine this location based on a heading and distance from
    // a specified source source location. Note heading is from source
    // to us, not from us to source!
    public void SetRelative(final Location sourceLocn,
                            final Heading heading,
                            final double distance)
    {
        boolean okSoFar = true;
        double degAngle = 0.0;
        double xSense = 0.0;
        double ySense = 0.0;

        // Work out what quadrant we are in, and adjust values accordingly.
        //??? do some QuadrantAdjust type thing for this too?
        if (heading.Get() < 90.0)
        {
            degAngle = heading.Get();
            xSense = +1.0;
            ySense = +1.0;
        }
        else if (heading.Get() < 180.0)
        {
            degAngle = 180.0 - heading.Get();
            xSense = +1.0;
            ySense = -1.0;
        }        
        else if (heading.Get() < 270.0)
        {
            degAngle = heading.Get() - 180.0;
            xSense = -1.0;
            ySense = -1.0;
        }
        else
        {
            degAngle = 360.0 - heading.Get();
            xSense = -1.0;
            ySense = +1.0;
        }

        // If all is okay, work out target location from the
        // input data and the quadrant-adjustment stuff.
        if (okSoFar)
        {
            double deltaX = distance * ShrubMath.Sin(degAngle) * xSense;
            double deltaY = distance * ShrubMath.Cos(degAngle) * ySense;

            mX = sourceLocn.GetX() + deltaX;
            mY = sourceLocn.GetY() + deltaY;
        }        
    }

    public String toString()
    {
        //??? find a better way of doing this?
        
        String answer = new String();
        answer = answer.concat("[");
        answer = answer.concat(dp2.format(mX));
        answer = answer.concat(",");
        answer = answer.concat(dp2.format(mY));
        answer = answer.concat("]");

        return answer;
    }

    public void Print()
    {
        System.out.print("[" + dp2.format(mX) + "," + dp2.format(mY) + "]");
    }
}
