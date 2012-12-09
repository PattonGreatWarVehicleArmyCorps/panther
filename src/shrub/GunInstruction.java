package shrub;

public class GunInstruction extends Instruction
{
    public final boolean IsFire()
    {
        boolean answer = false;
        if (mType == 1)
        {
            answer = true;
        }
        return answer;
    }

    public void SetFire(final double power)
    {
        mType = 1;
        mDouble = power;
    }

    public final boolean IsTurnLeft()
    {
        boolean answer = false;
        if (mType == 2)
        {
            answer = true;
        }
        return answer;
    }

    public void SetTurnLeft(final double amount)
    {
        mType = 2;
        mDouble = amount;
    }

    public final boolean IsTurnRight()
    {
        boolean answer = false;
        if (mType == 3)
        {
            answer = true;
        }
        return answer;
    }

    public void SetTurnRight(final double amount)
    {
        mType = 3;
        mDouble = amount;
    }
}
