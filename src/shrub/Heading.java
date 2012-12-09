package shrub;

import java.text.NumberFormat;

/**
 * Heading - an absolute heading (angle from 0 to <360).
 */
public class Heading
{
    private double mHeading = 0;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public Heading()
    {
        Initialise();
    }

    public Heading(final double hdngVal)
    {
        Initialise();
        Set(hdngVal);
    }

    public Heading(final Heading hdngVal)
    {
        Initialise();
        Set(hdngVal);
    }

    public void Initialise()
    {
        mHeading = 0;

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public void Set(final double newValue)
    {
        mHeading = newValue;
        Normalise();
    }

    public void Set(final Heading newValue)
    {
        if (newValue != null)
        {
            mHeading = newValue.Get();
            Normalise();
        }
    }

    public void Adjust(final double delta)
    {
        mHeading += delta;
        Normalise();
    }

    public void Adjust(final Bearing delta)
    {
        if (delta != null)
        {
            mHeading += delta.Get();
            Normalise();
        }
    }

    public void Flip()
    {
        mHeading += 180.0;
        Normalise();
    }

    public double Get()
    {
        return mHeading;
    }

    public boolean IsNormalised()
    {
        boolean answer = false;
        if ((mHeading >= 0) && (mHeading < 360))
        {
            answer = true;
        }
        return answer;
    }

    public void Normalise()
    {
        boolean done = false;
        while (!done)
        {
            if (mHeading >= 360)
            {
                mHeading -= 360;
            }
            else if (mHeading < 0)
            {
                mHeading += 360;
            }
            else
            {
                done = true;
            }
        }
    }

    public void SetFromTo(final Location fromLocn,
                          final Location toLocn)
    {
        double radAngle = 0;
        double degAngle = 0;
        double deltaX = 0;
        double deltaY = 0;
        double absDeltaX = 0;
        double absDeltaY = 0;
        double answer = 0;

//        System.out.println("=============================");
//        System.out.println("Heading::CalcFromTo");
//        System.out.print("fromLocn:");
//        fromLocn.Print();
//        System.out.println();
//        System.out.print("toLocn:");
//        toLocn.Print();
//        System.out.println();

        deltaX = toLocn.GetX() - fromLocn.GetX();
        deltaY = toLocn.GetY() - fromLocn.GetY();

//        System.out.println("deltaX: " + deltaX);
//        System.out.println("deltaY: " + deltaY);

        absDeltaX = Math.abs(deltaX);
        absDeltaY = Math.abs(deltaY);

        if ((absDeltaX <= 0.5) && (absDeltaY <= 0.5))
        {
            // We are effectively on top of the target location, heading
            // ceases to be particularly useful data at this point.
            answer = 0;
        }
        else if ((absDeltaX <= 0.5) && (deltaY <= -0.5))
        {
            answer = 180;
        }
        else if ((absDeltaX <= 0.5) && (deltaY >= 0.5))
        {
            answer = 0;
        }
        else if ((absDeltaY <= 0.5) && (deltaX <= -0.5))
        {
            answer = 270;
        }
        else if ((absDeltaY <= 0.5) && (deltaX >= 0.5))
        {
            answer = 90;
        }
        else
        {
            radAngle = Math.atan(absDeltaX / absDeltaY);
            degAngle = Math.toDegrees(radAngle);

            //??? add x/y sense input to quadrant converter?
            if ((deltaX >= 0.5) && (deltaY >= 0.5))
            {
                answer = degAngle;
            }
            else if ((deltaX >= 0.5) && (deltaY <= -0.5))
            {
                answer = 180 - degAngle;
            }
            else if ((deltaX <= -0.5) && (deltaY <= -0.5))
            {
                answer = 180 + degAngle;
            }
            else if ((deltaX <= -0.5) && (deltaY >= 0.5))
            {
                answer = 360 - degAngle;
            }
        }

        mHeading = answer;

        Normalise();
    }

    // +ve answer is directly away from us, -ve directly towards us.
    public double InlineComponent(final Heading objHdng,
                                  final double objValue)
    {
        double answer = 0.0;
        
//        System.out.println("=== InlineComponent() ===");
//        System.out.println("refHdng:  " + mHeading);
//        System.out.println("objHdng:  " + objHdng.Get());
//        System.out.println("objValue: " + objValue);
        
        // Determine relative bearing of object to our heading.
        Bearing objRelBrng = new Bearing();
        objRelBrng.SetFromTo(this, objHdng);

        // Adjust angle to 0-90 range, and determine how to adjust answer
	// given by the quadrant 1 angle to give our correct answer.
        QuadrantAdjust qAdj = new QuadrantAdjust(objRelBrng.Get());
        double adjThetaDegY = qAdj.GetAdjThetaDegY();
        int adjSenseY = qAdj.GetAdjSenseY();

        // Now calculate the answer, adjusting back to original quadrant.
        answer = adjSenseY * ShrubMath.Cos(adjThetaDegY) * objValue;

        return answer;
    }

    // +ve answer is to the right across us, -ve to the left.
    public double CrossComponent(final Heading objHdng,
                                 final double objValue)
    {
        double answer = 0.0;

//        System.out.println("=== CrossComponent() ===");
//        System.out.println("refHdng:  " + mHeading);
//        System.out.println("objHdng:  " + objHdng.Get());
//        System.out.println("objValue: " + objValue);
        
        // Determine relative bearing of object to our heading.
        Bearing objRelBrng = new Bearing();
        objRelBrng.SetFromTo(this, objHdng);

        // Adjust angle to 0-90 range, and determine how to adjust answer
        // given by the quadrant 1 angle to give our correct answer.
        QuadrantAdjust qAdj = new QuadrantAdjust(objRelBrng.Get());
        double adjThetaDegY = qAdj.GetAdjThetaDegY();
        int adjSenseX = qAdj.GetAdjSenseX();

        // Now calculate the answer, adjusting back to original quadrant.
        answer = adjSenseX * ShrubMath.Sin(adjThetaDegY) * objValue;

        return answer;
    }

    public String toString()
    {
        String answer = new String();
        answer = answer.concat(dp2.format(mHeading));
        return answer;
    }
}
