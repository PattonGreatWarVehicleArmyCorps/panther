package shrub;

import java.text.NumberFormat;

/**
 * TargetPData - periodicity data for a single target.
 */
public class TargetPData
{
    private MoveHistory mHistoryRef = null;
    private String mName = new String("{unnamed}");
    private int mMinPeriod = 20;
    private int mPeriodStep = 1;
    private int mNumPeriods = 60;
    private double mValueThreshold = 100;
    private double[] mPeriodData = null;
    private int mCurrentIndex = 0;

    private NumberFormat dp2 = NumberFormat.getInstance();

    public TargetPData()
    {
        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);

        // Must setup up appropriate class data values, then call
        // InitialiseData() method before this object is ready for use.

        // Target move history reference may be set at any time, and
        // freely changed, but must exist by the time periodic
        // evaluation is called.
    }

    public void InitialiseData()
    {
        mPeriodData = new double[mNumPeriods];
        int index = 0;
        while (index < mNumPeriods)
        {
            mPeriodData[index] = 999999;   //??? max double
            index++;
        }

        mCurrentIndex = 0;
    }

    public void SetName(final String name)
    {
        mName = new String(name);
    }

    public void SetHistoryRef(final MoveHistory historyRef)
    {
        mHistoryRef = historyRef;
    }

    public void SetMinPeriod(final int newValue)
    {
        mMinPeriod = newValue;
    }

    public void SetPeriodStep(final int newValue)
    {
        mPeriodStep = newValue;
    }

    public void SetNumPeriods(final int newValue)
    {
        mNumPeriods = newValue;
    }

    public void SetValueThreshold(final double newValue)
    {
        mValueThreshold = newValue;
    }

    public final String GetName()
    {
        return mName;  //??? should really alloc new memory and copy name in?
    }

    public void CalcNextPeriod()
    {
        mCurrentIndex++;
        if (mCurrentIndex >= mNumPeriods)
        {
            mCurrentIndex = 0;
        }

        int period = PeriodFromIndex(mCurrentIndex);

        double pValue = 999999;  //???
        PCalculator pCalc = new PCalculator(mHistoryRef, period);
        if (pCalc.IsOkay())
        {
            pValue = pCalc.Evaluate();
        }

//        System.out.println("[" + mName + "], index: " + mCurrentIndex +
//                           ", period: " + period +
//                           ", value " + dp2.format(pValue));

        mPeriodData[mCurrentIndex] = pValue;
    }

    public final int GetBestPeriod()
    {
        // Iterate through array looking for index with smallest data value
        // (i.e. period which fits the historic data the best). But only
        // consider it a valid period if value is less than specified max.
        
        // Returning negative index indicates no valid period found.
        int answer = -1;

        if (mPeriodData != null)
        {
            double thisValue = 999999;
            double minValueSoFar = 999999;
            int minIndexSoFar = -1;

            int index = mNumPeriods - 1;
            while (index >= 0)
            {
                thisValue = mPeriodData[index];  //??? remove temp var

//                System.out.println("index: " + index +
//                                   ", value: " + thisValue);

                if (thisValue <= minValueSoFar)
                {
                    minValueSoFar = thisValue;
                    minIndexSoFar = index;
                }

                index--;
            }

//            System.out.println("minIndex: " + minIndexSoFar +
//                               ", minValue: " + minValueSoFar);

            if (minValueSoFar < mValueThreshold)
            {
                answer = PeriodFromIndex(minIndexSoFar);
            }
        }
        else
        {
            System.out.println("ERROR: TargetPData.GetBestPeriod(), " +
                               "data array does not exist");
        }

        return answer;
    }

    public final int PeriodFromIndex(final int index)
    {
        int period = mMinPeriod + (mPeriodStep * index);
        return period;
    }

    public final int IndexFromPeriod(final int period)
    {
        int index = (period - mMinPeriod) / mPeriodStep;
        return index;
    }

    public final double GetValueForPeriod(final int period)
    {
        int index = IndexFromPeriod(period);
        double pVal = GetValueAtIndex(index);
        return pVal;
    }

    public final double GetValueAtIndex(final int index)
    {
        double pVal = mPeriodData[index];
        return pVal;
    }
}
