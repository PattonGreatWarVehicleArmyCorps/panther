package shrub;

/**
 * QuadrantAdjust - handles quadrant adjustment for trig calcs.
 *     Construct one of these, pass in an angle from 0 to 360, or a
 *     bearing in the range -180 to +180.
 *     This class will then contain the angle adjusted to 0 to 90 range,
 *     and x and y sense factors, +/- 1 in each case. A trig function
 *     can then be applied to the resultant angle, and multiplied by
 *     the appropriate sense to give the correct answer for the original
 *     angle.
 */
public class QuadrantAdjust
{
    private double mAdjThetaDegY = 0;
    private int mAdjSenseX = 0;
    private int mAdjSenseY = 0;

    public final double GetAdjThetaDegY()
    {
        return mAdjThetaDegY;
    }

    public final int GetAdjSenseX()
    {
        return mAdjSenseX;
    }

    public final int GetAdjSenseY()
    {
        return mAdjSenseY;
    }

    public QuadrantAdjust(final double origThetaDegY)
    {
        mAdjThetaDegY = 0.0;
        mAdjSenseX = 0;
        mAdjSenseY = 0;

//        System.out.println("--- QA construct, origThetaDegY: " +
//                           origThetaDegY);

        if (origThetaDegY < -180.0)
        {
            System.out.println("ERROR: QuadrantAdjust, invalid angle " +
                               origThetaDegY);
        }
        else if (origThetaDegY < -90.0)
        {
//            System.out.println("--- (-ve) Bottom left quadrant");

            mAdjThetaDegY = 180.0 + origThetaDegY;
            mAdjSenseX = -1;
            mAdjSenseY = -1;
        }
        else if (origThetaDegY < 0.0)
        {
//            System.out.println("--- (-ve) Top left quadrant");

            mAdjThetaDegY = -origThetaDegY;
            mAdjSenseX = -1;
            mAdjSenseY = +1;
        }
        else if (origThetaDegY < 90.0)
        {
//            System.out.println("--- (+ve) Top right quadrant");

            mAdjThetaDegY = origThetaDegY;
            mAdjSenseX = +1;
            mAdjSenseY = +1;
        }
        else if (origThetaDegY < 180.0)
        {
//            System.out.println("--- (+ve) Bottom right quadrant");

            mAdjThetaDegY = 180.0 - origThetaDegY;
            mAdjSenseX = +1;
            mAdjSenseY = -1;
        }
        else if (origThetaDegY < 270.0)
        {
//            System.out.println("--- (+ve) Bottom left quadrant");

            mAdjThetaDegY = origThetaDegY - 180.0;
            mAdjSenseX = -1;
            mAdjSenseY = -1;
        }
        else if (origThetaDegY < 360.0)
        {
//            System.out.println("--- (+ve) Top left quadrant");

            mAdjThetaDegY = 360.0 - origThetaDegY;
            mAdjSenseX = -1;
            mAdjSenseY = +1;
        }
        else
        {
            System.out.println("ERROR: QuadrantAdjust, invalid angle " +
                               origThetaDegY);
        }

//        System.out.println("--- adjThetaDegY: " + mAdjThetaDegY);
//        System.out.println("--- adjSenseX: " + mAdjSenseX);
//        System.out.println("--- adjSenseY: " + mAdjSenseY);
    }
}
