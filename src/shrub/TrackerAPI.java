package shrub;

/**
 * TrackerAPI - the common public interface for tracker
 *              objects of various flavours.
 */
public interface TrackerAPI
{
    public int GetNumTargets();
    public Sighting GetCurrentTarget();
    public Sighting GetTargetByIndex(final int index);
    public MoveHistory GetCurrentTargetHistory(); //??? safer method of access?
    public long LockOnStaleness();
    public boolean HasLockOn();
    public void ChooseBestTarget();
    public void ClearCurrentTarget();
    public boolean ExtrapolateCurrentTarget(final long toTime);
    public void Print();
}
