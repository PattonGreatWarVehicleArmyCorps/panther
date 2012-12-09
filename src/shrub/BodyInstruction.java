package shrub;

public class BodyInstruction extends Instruction
{
    public final boolean IsTurnLeft()
    {
        boolean answer = false;
        if (mType == 1)
        {
            answer = true;
        }
        return answer;
    }

    public final boolean IsTurnRight()
    {
        boolean answer = false;
        if (mType == 2)
        {
            answer = true;
        }
        return answer;
    }

    public final boolean IsMoveAhead()
    {
        boolean answer = false;
        if (mType == 3)
        {
            answer = true;
        }
        return answer;
    }

    public final boolean IsMoveBack()
    {
        boolean answer = false;
        if (mType == 4)
        {
            answer = true;
        }
        return answer;
    }

    public final boolean IsSpeedLimit()
    {
        boolean answer = false;
        if (mType == 5)
        {
            answer = true;
        }
        return answer;
    }

    public void SetTurnLeft(final double amount)
    {
        mType = 1;
        mDouble = amount;
    }

    public void SetTurnRight(final double amount)
    { 
        mType = 2;
        mDouble = amount;
    }

    public void SetMoveAhead(final double amount)
    {
        mType = 3;
        mDouble = amount;
    }

    public void SetMoveBack(final double amount)
    {
        mType = 4;
        mDouble = amount;
    }

    public void SetSpeedLimit(final double amount)
    {
        mType = 5;
        mDouble = amount;
    }
}
