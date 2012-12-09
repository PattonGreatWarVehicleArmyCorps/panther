package shrub;

/**
 * Phasor - generates periodic phase values that can be used e.g.
 *          to overlay complex scan paaterns around target heading.
 *          Rather than generating phase values mathematically, which
 *          is easy for simple periodic motions, the user is expected
 *          to determine them for himself and set up the appropriate
 *          phase array - cruder, but much more flexible.
 */
public class Phasor
{
    private long mTimeNow = 0;
    private long mTimeStarted = 0;
    private double[] mPhaseArray = new double[1];
    private long mCycleLength = 1;

    public Phasor()
    {
        Initialise();
    }

    public void Initialise()
    {
        mTimeNow = 0;
        mTimeStarted = 0;
        mPhaseArray = new double[1];
        mCycleLength = 1;
    }

    public void SetTimeNow(final long newValue)
    {
        mTimeNow = newValue;
    }

    public void SetTimeStarted(final long newValue)
    {
        mTimeStarted = newValue;
    }

    public void SetPhaseArray(double[] newArray)
    {
        mPhaseArray = newArray;
        mCycleLength = mPhaseArray.length;
    }

    public void ClearPhaseArray()
    {
        mPhaseArray = new double[1];
        mCycleLength = 1;
    }

    public final long GetTimeNow()
    {
        return mTimeNow;
    }

    public final long GetTimeStarted()
    {
        return mTimeStarted;
    }


    public final int GetPhaseIndex()
    {
        int index = 0;
        index = (int)((mTimeNow - mTimeStarted) % mCycleLength);
        return index;
    }

    public final double GetPhaseValueAt(final int index)
    {
        double value = 0.0;
        value = mPhaseArray[index];
        return value;    
    }

    public final double GetPhaseValue()
    {
        double value = 0.0;
        int index = 0;

        index = GetPhaseIndex();
        value = GetPhaseValueAt(index);        

        return value;    
    }

    public void Print()
    {
        System.out.println("Phasor[" + mCycleLength +
                           "]: time " + mTimeNow +
                           ", started " + mTimeStarted +
                           ", index " + GetPhaseIndex() +
                           ", value " + GetPhaseValue());    
    }
}
