package shrub;

import java.text.NumberFormat;

/**
 * ShrubMath - alternative useful maths functions.
 */
public class ShrubMath
{
    private static double[] mSinTable = null;
    private static double[] mCosTable = null;
    private static double[] mTanTable = null;

    private static double[] mRobotAngularWidthTable = null;
    private static int mAWMaxIndex = 200;
    private static double mAWMaxDistance = 2000.0;
    private static double mAWDistanceStep = 100.0;
    private static double mRobotRadius = 15.0;

    private static NumberFormat dp2 = NumberFormat.getInstance();

    //??? pass in max distance etc. as parameters here
    public static void Initialise()
    {
//        System.out.println();
//        System.out.println("*** Initialising SM functions ***");

        mSinTable = new double[91];
        mCosTable = new double[91];
        mTanTable = new double[91];

        int angleIndexDegrees = 0;

        mSinTable[angleIndexDegrees] = 0.0;
        mCosTable[angleIndexDegrees] = 1.0;
        mTanTable[angleIndexDegrees] = 0.0;

        angleIndexDegrees++;

        while (angleIndexDegrees < 90)
        {
            mSinTable[angleIndexDegrees] =
                    Math.sin(Math.toRadians((double)angleIndexDegrees));
            mCosTable[angleIndexDegrees] =
                    Math.cos(Math.toRadians((double)angleIndexDegrees));
            mTanTable[angleIndexDegrees] =
                    Math.tan(Math.toRadians((double)angleIndexDegrees));

            angleIndexDegrees++;
        }

        mSinTable[angleIndexDegrees] = 1.0;
        mCosTable[angleIndexDegrees] = 0.0;
        mTanTable[angleIndexDegrees] = 9999.0;
                // tan(90) is actually infinite, but this will do well enough.

        mAWMaxDistance = 2000.0;  //??? set to passed in parameters
        mAWDistanceStep = 100.0;
        mRobotRadius = 15.0;

        mAWMaxIndex = (int)(mAWMaxDistance / mAWDistanceStep);

        int distanceIndex = 0;
        mRobotAngularWidthTable = new double[mAWMaxIndex + 1];
        while (distanceIndex <= mAWMaxIndex)
        {
            double angularWidthRad = Math.atan(mRobotRadius /
                                             (distanceIndex * mAWDistanceStep));
            double angularWidthDeg = Math.toDegrees(angularWidthRad);
            mRobotAngularWidthTable[distanceIndex] = angularWidthDeg;

            distanceIndex++;
        }

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public static void SetRobotRadius(final double newValue)
    {
        mRobotRadius = newValue;
    }

    public static final double Sin(final double angle)
    {
        double answer = 0;

//        if ((angle < 0) || (angle > 90))
//        {
//            System.out.println("ERROR: ShrubMath.sin, " +
//                               "invalid angle " + angle);
//        }
//        else
//        {
            answer = mSinTable[(int)angle];
//        }

//        System.out.println("sin " + dp2.format(angle) +
//                           ": IsInitialised " + mIsInitialised +
//                           ", answer " + dp2.format(answer));

        return answer;
    }

    public static final double Cos(final double angle)
    {
        double answer = 0;

//        if ((angle < 0) || (angle > 90))
//        {
//            System.out.println("ERROR: ShrubMath.cos, " +
//                               "invalid angle " + angle);
//        }
//        else
//        {
            answer = mCosTable[(int)angle];
//        }

//        System.out.println("cos " + dp2.format(angle) +
//                           ": IsInitialised " + mIsInitialised +
//                           ", answer " + dp2.format(answer));

        return answer;
    }

    public static final double Tan(final double angle)
    {
        double answer = 0;

//        if ((angle < 0) || (angle > 90))
//        {
//            System.out.println("ERROR: ShrubMath.tan, " +
//                               "invalid angle " + angle);
//        }
//        else
//        {
            answer = mTanTable[(int)angle];
//        }

//        System.out.println("tan " + dp2.format(angle) +
//                           ": IsInitialised " + mIsInitialised +
//                           ", answer " + dp2.format(answer));

        return answer;
    }

    public static final double RobotAngularWidth(final double distance)
    {
        double answer = 0;
        int index = (int)(distance / mAWDistanceStep);

        // If distance is greater than expected, rather than run off end of
        // array, or do nothing, let's just assume distance is the maximum
        // supported. Unlikely to be a big deal that target appears to be a
        // bit bigger than it really should be at long ranges, hitting is 
        // really down to pure luck anyway. Alternatively, if we make sure
        // that max distance is less than arena diagonal length this can 
        // never arise anyway.
        if (index > mAWMaxIndex)
        {
            index = mAWMaxIndex;
        }
        answer = mRobotAngularWidthTable[index];

        return answer;
    }
}

