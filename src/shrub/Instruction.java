package shrub;

/**
 * Instruction - holds returned instructions to tell robot what to do.
 */
public class Instruction
{
    protected int mType = 0;
    protected double mDouble = 0;

    public Instruction()
    {
        Initialise();
    }

    public void Initialise()
    {
        mType = 0;
        mDouble = 0;
    }

    public final double GetDouble()
    {
        return mDouble;
    }

    public final boolean IsAchieved()
    {
        boolean answer = false;
        if (mType == -1)
        {
            answer = true;
        }
        return answer;
    }

    public final boolean IsNothing()
    {
        boolean answer = false;
        if (mType == 0)
        {
            answer = true;
        }
        return answer;
    }

    public void SetAchieved()
    {
        mType = -1;
        mDouble = 0;
    }

    public void SetNothing()
    {
        mType = 0;
        mDouble = 0;
    }

    public void Print()
    {
        System.out.println("-*- Instruction: type " + mType);
    }
}
