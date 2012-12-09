package shrub;

import java.util.ListIterator;
import java.text.NumberFormat;

/**
 * PCalculator - evaluate periodicity in object movement.
 */
public class PCalculator
{
    private MoveHistory mHistoryRef = null;
    private ListIterator mLoIterator = null;
    private ListIterator mHiIterator = null;
    private int mListSize = -1;
    private int mPeriod = -1;
    private boolean mIsOkay = true;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public PCalculator(final MoveHistory historyRef, final int pVal)
    {
        mIsOkay = false;

        if (historyRef == null)
        {
            System.out.println("ERROR: PCalculator constructor, null ref");
        }
        else if (pVal < 1)
        {
            System.out.println("ERROR: PCalculator constructor, pVal " + pVal);
        }
        else
        {
            mPeriod = pVal;
            mHistoryRef = historyRef;
            mListSize = historyRef.size();
            if (mPeriod > (mListSize/2))
            {
//                System.out.println("ERROR: PCalculator constructor, " +
//                                   "list too small " + mListSize + "/" +
//                                   mPeriod);
            }
            else
            {
                int loStartIndex = mListSize - mPeriod;

//                System.out.println("mListSize: " + mListSize +
//                                   ", loStart: " + loStartIndex);

                mLoIterator = mHistoryRef.listIterator(loStartIndex);
                mHiIterator = mHistoryRef.listIterator(mListSize);

                // Set number of decimal places we will be formatting
                // out display output to.
                dp2.setMaximumFractionDigits(2);
                dp2.setMinimumFractionDigits(2);

                mIsOkay = true;
            }
        }
    }

    public final boolean IsOkay()
    {
        return mIsOkay;
    }

    public final double Evaluate()
    {
        double accEval = 0;
        double avgEval = 0;
        int numIterations = 0;
        int numEvaluations = 0;
        Movement loMove = null;
        Movement hiMove = null;
        Bearing loHdngDelta = null;
        Bearing hiHdngDelta = null;
        Bearing diffHdngDelta = new Bearing();
        double loVelocity = 0;
        double hiVelocity = 0;
        double diffVelocity = 0;
        double thisEval = 0;

//        System.out.println();
//        System.out.println("PCalc.Evaluate() - period " + mPeriod);

        while (mLoIterator.hasPrevious())
        {
            numIterations++;

//            System.out.println("Iteration " + numIterations);

            loMove = (Movement)mLoIterator.previous();
            hiMove = (Movement)mHiIterator.previous();

            numEvaluations++;

	    // for performance within the loop, get reference to internal
	    // data items in the movement objects - be careful not to
	    // corrupt the contents inadvertantly!
            loHdngDelta = loMove.GetRefToHdngDelta();
            hiHdngDelta = hiMove.GetRefToHdngDelta();

            loVelocity = loMove.GetVelocityNow();
            hiVelocity = hiMove.GetVelocityNow();

//            System.out.println("Velocity: " + loVelocity +
//                               " / " + hiVelocity);
//            System.out.println("HdngDelta: " + loHdngDelta.Get() +
//                               " / " + hiHdngDelta.Get());

            diffVelocity = loVelocity - hiVelocity;
            if ((loHdngDelta != null) && (hiHdngDelta != null))
            {
                diffHdngDelta.SetFromTo(loHdngDelta, hiHdngDelta);
            }
            else
            {
                diffHdngDelta.Set(0);
            }

//            System.out.println("diffVel: " + diffVelocity +
//                               ", diffHD: " + diffHdngDelta.Get());

            thisEval = Math.pow(diffVelocity, 2) +
                       Math.pow(diffHdngDelta.Get(), 2);

            accEval += thisEval;
        }

        avgEval = accEval / numEvaluations;

//        System.out.println("period: " + mPeriod +
//                           ", iterations: " + numIterations +
//                           ", avgEval: " + dp2.format(avgEval));

        return avgEval;
    }
}
