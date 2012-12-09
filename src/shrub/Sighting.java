package shrub;

import java.text.NumberFormat;

/**
 * Sighting - details of a target sighting.
 */
public class Sighting
{
    private Location mLocation = new Location();
    private Heading mHeading = new Heading();
    private double mVelocity = 0.0;
    private double mEnergy = 0.0;
    private int mInaccuracy = 0;
    private long mTimestamp = 0;
    private String mName = new String();

    private NumberFormat dp2 = NumberFormat.getInstance();

    public Sighting()
    {
        Initialise();
    }

    public void Initialise()
    {
        mLocation.Initialise();
        mHeading.Initialise();
        mVelocity = 0.0;
        mEnergy = 0.0;
        mInaccuracy = 0;
        mTimestamp = 0;
        mName = "";

        // Set number of decimal places we will be formatting
        // out display output to.
        dp2.setMaximumFractionDigits(2);
        dp2.setMinimumFractionDigits(2);
    }

    public void SetLocation(final double posX, final double posY)
    {
        mLocation.Set(posX, posY);
    }

    public void SetLocation(final Location locn)
    {
        mLocation.Set(locn);
    }

    public void SetVelocity(final double velocity)
    {
        mVelocity = velocity;
    }

    public void SetEnergy(final double energy)
    {
        mEnergy = energy;
    }

    public void SetInaccuracy(final int inaccuracy)
    {
        mInaccuracy = inaccuracy;
    }

    public void SetHeading(final double heading)
    {
        mHeading.Set(heading);
    }

    public void SetHeading(final Heading heading)
    {
        mHeading.Set(heading.Get());
    }

    public void SetTimestamp(final long timestamp)
    {
        mTimestamp = timestamp;
    }

    public void SetName(String name)
    {
        mName = name;
    }

    // Allocates memory for answer, so caller can do what it likes with it.
    public final Location GetLocation()
    {
        Location answer = new Location(mLocation);
        return answer;
    }

    // Allocates memory for answer, so caller can do what it likes with it.
    public final Heading GetHeading()
    {
        Heading answer = new Heading(mHeading);
        return answer;
    }

    public final double GetVelocity()
    {
        return mVelocity;
    }

    public final double GetEnergy()
    {
        return mEnergy;
    }

    public final int GetInaccuracy()
    {
        return mInaccuracy;
    }

    public final long GetTimestamp()
    {
        return mTimestamp;
    }

    // Allocates memory for answer, so caller can do what it likes with it.
    public final String GetName()
    {
        String answer = new String(mName);
        return answer;
    }

    public void Print()
    {
//        System.out.println("----- Sighting -----");
//        System.out.println(" Name: [" + mName + "]");
        System.out.print(" Time: " + mTimestamp); 
        System.out.print(" Locn: " + mLocation.toString());
        System.out.print(" Hdng: " + mHeading.toString());
        System.out.println();
//        System.out.print(" Vel: " + dp2.format(mVelocity));
//        System.out.print(" Energy: " + dp2.format(mEnergy));
//        System.out.print(" Inacc: " + dp2.format(mInaccuracy));
//        System.out.println();
    }
}
