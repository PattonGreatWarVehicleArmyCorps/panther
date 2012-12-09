package shrub;

import java.text.NumberFormat;
//???import java.awt.geom.Line2D;

/**
 * Line - things to do with and on a geometric line.
 */
public class Line
{
    private boolean mIsOkay = false;

    private Location mStartLocn = null;
    private Location mEndLocn = null;
    private Heading mHeading = null;
    private double mLength = 0.0;        // Note: can be negative!

    // Equation of the line in the form y = mx + c.
    private boolean mIsEquationSet = false;
    private double mGradient = 0.0;
    private double mOffsetY = 0.0;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public Line()
    {
        Initialise();
    }

    public void Initialise()
    {
        mIsOkay = false;

        mStartLocn = null;
        mEndLocn = null;
        mHeading = null;
        mLength = 0.0;

        mIsEquationSet = false;
        mGradient = 0.0;
        mOffsetY = 0.0;
       
        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public final boolean IsOkay()
    {
        return mIsOkay;
    }

    public final boolean IsEquationSet()
    {
        return mIsEquationSet;
    }

    public final boolean SetStartEnd(final Location start,
                                     final Location end)
    {
        Initialise();

        mStartLocn = new Location();
        mStartLocn.Set(start);

        mEndLocn = new Location();
        mEndLocn.Set(end);

        mHeading = new Heading();
        mHeading.SetFromTo(mStartLocn, mEndLocn);

        mLength = mStartLocn.DistanceTo(mEndLocn);

        mIsOkay = true;

        return mIsOkay;
    }

    public final boolean SetStartHdng(final Location start,
                                      final Heading hdng)
    {
        Initialise();

        mStartLocn = new Location();
        mStartLocn.Set(start);

        mHeading = new Heading();
        mHeading.Set(hdng);

        // Not enough information to set length or end point.

        mIsOkay = true;

//        Print();

        return mIsOkay;
    }

    public final boolean SetStartHdngLength(final Location start,
                                            final Heading hdng,
                                            final double length)
    {
        Initialise();

        mStartLocn = new Location();
        mStartLocn.Set(start);

        mHeading = new Heading();
        mHeading.Set(hdng);

        mLength = length;

        mIsOkay = true;

        mEndLocn = new Location();
        mEndLocn.SetRelative(mStartLocn, mHeading, mLength);

//        Print();

        return mIsOkay;
    }

    public final Location GetStartLocn()
    {
        Location answer = new Location(mStartLocn);
        return answer;
    }

    public final Heading GetHeading()
    {
        Heading answer = new Heading(mHeading);
        return answer;
    }

    public final double GetGradient()
    {
        return mGradient;
    }

    public final double GetOffsetY()
    {
        return mOffsetY;
    }

    public final boolean CalcEquation()
    {
//        System.out.println();
//        System.out.println("===== Line::CalcEquation() =====");

        if (mIsOkay)
        {
            // Use bearing relative to Y-axis for the following...
            Bearing brng = new Bearing();
            brng.Set(mHeading);

            // Determine the gradient of the line first.
            // If close to vertical or horizontal, let's cut out some
            // of the expensive calculations.
            if (brng.IsAligned() || brng.IsOpposed())
            {
                // Actually gradient is or is close to infinite here,
                // but a big enough number will do for our purposes.
                mGradient = 9999;  //??? big enough?
            }
            else if (brng.IsLeft90() || brng.IsRight90())
            {
                // Gradient is very small here, let's just call it 0.
                //??? any places where we divide by gradient? 
                //??? might want to make it a small but non-zero number
                mGradient = 0;
            }
            else
            {
                QuadrantAdjust qAdj = new QuadrantAdjust(mHeading.Get());
                int adjSenseX = qAdj.GetAdjSenseX();
                int adjSenseY = qAdj.GetAdjSenseY();
                double adjThetaDegY = qAdj.GetAdjThetaDegY();

//                System.out.println(" mHeading: " + mHeading.toString());
//                System.out.println(" adjThetaDegY: " +
//                                   dp2.format(adjThetaDegY));
//                System.out.println(" adjSenseX: " + adjSenseX);
//                System.out.println(" adjSenseY: " + adjSenseY);

                // Absolute value of gradient is tangent of Y step over X step,
                // but we have an angle made with the Y axis so need to invert
                // the answer. However it is cheaper to do tan on the angle
                // made with the X axis, as it is subtraction not division.
                double adjThetaDegX = 90.0 - adjThetaDegY;
//???                double adjThetaRadX = Math.toRadians(adjThetaDegX);
//???                double absGradient = Math.tan(adjThetaRadX);
                double absGradient = ShrubMath.Tan(adjThetaDegX);

//                System.out.println(" adjThetaDegX: " +
//                                   dp2.format(adjThetaDegX));
//                System.out.println(" adjThetaRadX: " +
//                                   dp2.format(adjThetaRadX));
//                System.out.println(" absGradient: " +
//                                   dp2.format(absGradient));

                // Now the actual value of the gradient is +ve or -ve
                // depending on which quadrant the original heading was in.
                if (adjSenseX == adjSenseY)
                {
                    mGradient = absGradient;
                }
                else
                {
                    mGradient = -absGradient;
                }
            }

//            System.out.println(" mGradient: " + dp2.format(mGradient));

            // Having worked out gradient we can now work out the Y-offset.
            // We know the m in y = mx + c, and we know a point (a, b) on
            // the line, so b = ma + c, hence c = b - ma.
            mOffsetY = mStartLocn.GetY() - (mStartLocn.GetX() * mGradient);

//            System.out.println(" mOffsetY: " + dp2.format(mOffsetY));

            mIsEquationSet = true;
        }
        else
        {
            System.out.println("ERROR: Line::CalcEquation(), bad line");
        }

//        Print();

        return mIsEquationSet;
    }

    //??? hmmm, don't want to repeat calcs to find intersection point
    // should wrap the two things up in one method
    public final Line PerpendicularLine(final Location otherLocn)
    {
        Line perpLine = new Line();

//        System.out.println();
//        System.out.println("===== Line::PerpendicularLine() =====");
//        System.out.println(" otherLocn: " + otherLocn.toString());
//        System.out.println("=== Our own line");
//        Print();

        if (mIsOkay)
        {
            // Closest distance is at the intersection point of a
            // perpendicular line to us, through the point.

            Heading perpHdng = new Heading();
            perpHdng.Set(mHeading);
            perpHdng.Adjust(90.0);
            
            perpLine.SetStartHdng(otherLocn, perpHdng);
            perpLine.CalcEquation();  //??? should we leave this to caller?
                                      //??? or check if it is done in the
                                      //??? intersection method and do if not

//            System.out.println("=== Perpendicular line");
//            perpLine.Print();
        }
        else
        {
            System.out.println("ERROR: Line::PerpendicularLine(), bad line");
        }

        return perpLine;
    }

    // Allocates memory for the answer and returns it.
    public final Location IntersectionLocn(final Line otherLine)
    {
        Location intLocn = new Location();

//        System.out.println();
//        System.out.println("===== Line::PerpendicularLine() =====");
//        System.out.println("=== Our own line");
//        Print();
//        System.out.println("=== Other line");
//        otherLine.Print();

        if ((mIsOkay == true) && (otherLine.IsOkay() == true))
        {
            //??? check if either line needs equation calc

            double intX = 0.0;
            double intY = 0.0;
            double offsetY1 = mOffsetY;
            double offsetY2 = otherLine.GetOffsetY();
            double gradient1 = mGradient;
            double gradient2 = otherLine.GetGradient();

            intX = (offsetY1 - offsetY2) / (gradient2 - gradient1);
            intY = (gradient1 * intX) + offsetY1;
            intLocn.Set(intX, intY);

//            System.out.println("Intersection: " + intLocn.toString());
        }
        else
        {
            System.out.println("ERROR: Line::IntersectionLocn(), bad line");
        }

        return intLocn;
    }

    public void Print()
    {
        System.out.println("===== Line =====");
        if (mStartLocn != null)
        {
            System.out.print(" StartLocn: " + mStartLocn.toString());
        }
        if (mEndLocn != null)
        {
            System.out.print(" EndLocn: " + mEndLocn.toString());
        }
        System.out.println();
        if (mHeading != null)
        {
            System.out.print(" Hdng: " + mHeading.toString());
        }
        if (mLength != 0)
        {
            System.out.print(" Length: " + dp2.format(mLength));
        }
        System.out.println();
        if (mIsEquationSet == true)
        {
            System.out.print(" Gradient: " + dp2.format(mGradient));
            System.out.print(" OffsetY: " + dp2.format(mOffsetY));
            System.out.println();
        }
    }
}
