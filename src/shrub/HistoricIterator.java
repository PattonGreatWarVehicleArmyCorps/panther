package shrub;

import java.util.ListIterator;

/**
 * HistoricIterator - an iterative movement predictor based on past history.
 */
public class HistoricIterator
{
    private MoveHistory mHistoryRef = null;
    private int mDirection = -1;
    private int mStartIndex = -1;
    private ListIterator mListIterator = null;
    private boolean mIsOkay = true;
    private Movement mPredictionRef = null;
    private Box mAllowedArea = null;

    // Pass in references to a history list, and a sighting record
    // that we will manipulate to move forward in prediction time.
    public HistoricIterator(final MoveHistory historyRef, 
                            Movement startMoveRef,
                            final Box allowedArea)
    {
//        System.out.println();
//        System.out.println("*** HistoricIterator, backward mode");

        mIsOkay = true;
        if ((historyRef != null) && (startMoveRef != null))
        {
            mHistoryRef = historyRef;

            // Default to backward iteration from end of history provided.
            // Remember that the cursor position for an iterator lies between
            // elements, and can range 0 - listSize, not just (listSize - 1).
            mStartIndex = mHistoryRef.GetNumMoves();
            mDirection = -1;
            mListIterator = mHistoryRef.listIterator(mStartIndex);

            // And store reference to our working target information -
            // this forms starting point for our prediction, and we
            // update it as we iterate into the future.
            mPredictionRef = startMoveRef;

            // If we pass in a non-null Box ref, extrapolated target
            // movements will be clipped to within the allowed area.
            mAllowedArea = allowedArea;
        }
        else
        {
            System.out.println("ERROR: HistoricIterator::Constructor, " +
                               "null ref");
            mIsOkay = false;
        }
    }

    //??? merge the two constructors together at some point
    public HistoricIterator(final MoveHistory historyRef, 
                            Movement startMoveRef,
                            final Box allowedArea,
                            final int periodicity)
    {
//        System.out.println();
//        System.out.println("*** HistoricIterator, periodic mode: " +
//                           periodicity);

        mIsOkay = true;
        if ((historyRef != null) && (startMoveRef != null))
        {
            mHistoryRef = historyRef;

            // Periodicity specified, so will be traversing list forwards.
            // Remember that the cursor position for an iterator lies between
            // elements, and can range 0 - listSize, not just (listSize - 1).
            int listSize = mHistoryRef.GetNumMoves();
            mStartIndex = listSize - periodicity;
            mDirection = +1;

            // If list not big enough for periodicity specified, start at
            // zero and accept we will wrap more frequently than desired!
            if (mStartIndex < 0)
            {
                mStartIndex = 0;
            }

            mListIterator = mHistoryRef.listIterator(mStartIndex);

//            System.out.println("listSize: " + listSize +
//                               ", startIndex:" + mStartIndex);

            // And store reference to our working target information -
            // this forms starting point for our prediction, and we
            // update it as we iterate into the future.
            mPredictionRef = startMoveRef;

            // If we pass in a non-null Box ref, extrapolated target
            // movements will be clipped to within the allowed area.
            mAllowedArea = allowedArea;
        }
        else
        {
            System.out.println("ERROR: HistoricIterator::Constructor, " +
                               "null ref");
            mIsOkay = false;
        }
    }

    public final boolean IsOkay()
    {
        return mIsOkay;
    }

    // Do next iteration, generate next predicted target location etc.
    public final boolean Iterate()
    {
//        System.out.println("======= HistoricIterator::Iterate() =======");

        boolean okSoFar = true;
        int listSize = mHistoryRef.GetNumMoves();
        Movement histMove = null;

        if ((mIsOkay == true) && (listSize > 0))
        {
            // Go to next move history in our chosen direction of traversal.
            // If period reached, or list end reached, wrap iterator round.
            if (mDirection < 0)
            {
                if (!mListIterator.hasPrevious())
                {
//                    System.out.println("Wrapping index to: " + mStartIndex);

                    mListIterator = mHistoryRef.listIterator(mStartIndex);
                }

                histMove = (Movement)mListIterator.previous();
            }
            else
            {
                if (!mListIterator.hasNext())
                {
//                    System.out.println("Wrapping index to: " + mStartIndex);

                    mListIterator = mHistoryRef.listIterator(mStartIndex);
                }

                histMove = (Movement)mListIterator.next();
            }

            // Use the retrieved movement to extrapolate our prediction
            // further into the future.
            long deltaTime = histMove.GetTimeDelta();
            long predictTime = mPredictionRef.GetTimeNow() + deltaTime;
            okSoFar &= mPredictionRef.ExtrapolateSelf(predictTime,
                                                      mAllowedArea,
                                                      histMove);
        }
        else
        {
            System.out.println("ERROR: HistoricIterator::Iterate(), " +
                               "bad state or list empty");

            okSoFar = false;
        }
      
        return okSoFar;
    }
}
