package shrub;

public class RadarInstruction extends Instruction
{
    public final boolean IsRotateLeft()
    {
        boolean answer = false;
        if (mType == 1)
        {
            answer = true;
        }
        return answer;
    }

    public final boolean IsRotateRight()
    {
        boolean answer = false;
        if (mType == 2)
        {
            answer = true;
        }
        return answer;
    }

    public void SetRotateLeft(final double amount)
    {
        mType = 1;
        mDouble = amount;
    }

    public void SetRotateRight(final double amount)
    {
        mType = 2;
        mDouble = amount;
    }
}
