package shrub;

/**
 * Box - a box represented by 2 corner locations.  ??? TBD
 */
public class Box
{
    private Location mCornerTL = new Location();
    private Location mCornerTR = new Location();
    private Location mCornerBL = new Location();
    private Location mCornerBR = new Location();

    public Box()
    {
        Initialise();
    }

    public Box(final Box otherBox)
    {
        Initialise();
        Set(otherBox);
    }

    public void Initialise()
    {
        mCornerTL = new Location();
        mCornerTR = new Location();
        mCornerBL = new Location();
        mCornerBR = new Location();
    }

    public void Set(final Location corner1, final Location corner2)
    {
        double x1 = corner1.GetX();
        double y1 = corner1.GetY();
        double x2 = corner2.GetX();
        double y2 = corner2.GetY();

        if (x1 < x2)
        {
            mCornerTL.SetX(x1);
            mCornerTR.SetX(x2);
            mCornerBL.SetX(x1);
            mCornerBR.SetX(x2);
        }
        else
        {
            mCornerTL.SetX(x2);
            mCornerTR.SetX(x1);
            mCornerBL.SetX(x2);
            mCornerBR.SetX(x1);
        }

        if (y1 < y2)
        {
            mCornerTL.SetY(y2);
            mCornerTR.SetY(y2);
            mCornerBL.SetY(y1);
            mCornerBR.SetY(y1);
        }
        else
        {
            mCornerTL.SetY(y1);
            mCornerTR.SetY(y1);
            mCornerBL.SetY(y2);
            mCornerBR.SetY(y2);
        }
    }

    public void Set(final Box newBox)
    {
        Set(newBox.GetCornerTL(), newBox.GetCornerBR());
    }

    public void SetMinX(final double minX)
    {
        mCornerTL.SetX(minX);
        mCornerBL.SetX(minX);
    }

    public void SetMinY(final double minY)
    {
        mCornerBR.SetY(minY);
        mCornerBL.SetY(minY);
    }

    public void SetMaxX(final double maxX)
    {
        mCornerTR.SetX(maxX);
        mCornerBR.SetX(maxX);
    }

    public void SetMaxY(final double maxY)
    {
        mCornerTR.SetY(maxY);
        mCornerTL.SetY(maxY);
    }

    public final Location GetCornerTL()
    {
        Location answer = new Location();
        answer.Set(mCornerTL);
        return answer;
    }

    public final Location GetCornerTR()
    {
        Location answer = new Location();
        answer.Set(mCornerTR);
        return answer;
    }

    public final Location GetCornerBL()
    {
        Location answer = new Location();
        answer.Set(mCornerBL);
        return answer;
    }

    public final Location GetCornerBR()
    {
        Location answer = new Location();
        answer.Set(mCornerBR);
        return answer;
    }

    public final double GetMinX()
    {
        return mCornerTL.GetX();
    }

    public final double GetMaxX()
    {
        return mCornerBR.GetX();
    }

    public final double GetMinY()
    {
        return mCornerBL.GetY();
    }

    public final double GetMaxY()
    {
        return mCornerTR.GetY();
    }

    public final boolean IsInside(final Location theLocn)
    {
        boolean answer = false;

        if ((theLocn.GetX() >= this.GetMinX()) &&
            (theLocn.GetY() >= this.GetMinY()) &&
            (theLocn.GetX() <= this.GetMaxX()) &&
            (theLocn.GetY() <= this.GetMaxY()))
        {
            answer = true;
        }

        return answer;
    }

    // Find point where a line crosses edge of this box, starting
    // from inside and heading out.
    public final Location EdgeIntersectOutward(final Line theLine)
    {
//        System.out.println("=== Box::EdgeIntersectOutward() ===");
//        this.Print();
//        theLine.Print();

        Location answer = null;
        Location lineStart = theLine.GetStartLocn();
        Heading lineHdng = theLine.GetHeading();

        if (!IsInside(lineStart))
        {
            // Do nothing. Null pointer will be passed out, it is
            // up to the caller to handle this gracefully.
            System.out.println("Box::EdgeIntersectOutward(), locn not inside");
        }
        else
        {
//            System.out.println("Locn inside box, calculating line eqn...");

            // Ensure that equation has been calculated for line of interest.
            if (!theLine.IsEquationSet())
            {
                theLine.CalcEquation();

//                System.out.println("Calculating equation of the line");
//                theLine.Print();
            }

            //??? store calculated top, left, right, bottom edges
            //??? as class data for future reference?
            Line edge1 = new Line();
            Line edge2 = new Line();

            // Determine from line heading the two candidate intersect edges.
            // The nearest intersection point will be the correct answer - the
            // furthest will actually be intersecting a line extending the other
            // edge outside the bounds of the box.
            if (lineHdng.Get() < 0.1)
            {
                // Bit of a fudge, set two candidate edges to be the same.
                edge1.SetStartEnd(mCornerTL, mCornerTR);
                edge2.SetStartEnd(mCornerTL, mCornerTR);
            }
            else if (lineHdng.Get() < 89.9)
            {
                edge1.SetStartEnd(mCornerTL, mCornerTR);
                edge2.SetStartEnd(mCornerBR, mCornerTR);
            }
            else if (lineHdng.Get() < 90.1)
            {
                // Bit of a fudge, set two candidate edges to be the same.
                edge1.SetStartEnd(mCornerBR, mCornerTR);
                edge2.SetStartEnd(mCornerBR, mCornerTR);
            }
            else if (lineHdng.Get() < 179.9)
            {
                edge1.SetStartEnd(mCornerBR, mCornerTR);
                edge2.SetStartEnd(mCornerBL, mCornerBR);
            }
            else if (lineHdng.Get() < 180.1)
            {
                // Bit of a fudge, set two candidate edges to be the same.
                edge1.SetStartEnd(mCornerBL, mCornerBR);
                edge2.SetStartEnd(mCornerBL, mCornerBR);
            }
            else if (lineHdng.Get() < 269.9)
            {
                edge1.SetStartEnd(mCornerBL, mCornerTL);
                edge2.SetStartEnd(mCornerBL, mCornerBR);
            }
            else if (lineHdng.Get() < 270.1)
            {
                // Bit of a fudge, set two candidate edges to be the same.
                edge1.SetStartEnd(mCornerBL, mCornerTL);
                edge2.SetStartEnd(mCornerBL, mCornerTL);
            }
            else if (lineHdng.Get() < 359.9)
            {
                edge1.SetStartEnd(mCornerBL, mCornerTL);
                edge2.SetStartEnd(mCornerTL, mCornerTR);
            }
            else
            {
                // Bit of a fudge, set two candidate edges to be the same.
                edge1.SetStartEnd(mCornerTL, mCornerTR);
                edge2.SetStartEnd(mCornerTL, mCornerTR);
            }

            // Derive the line equations for which the two edges are line
            // segments, and determine the intersection points accordingly.
            edge1.CalcEquation();
            edge2.CalcEquation();

//            System.out.println("Edge line objects:");
//            edge1.Print();
//            edge2.Print();

            Location intPoint1 = edge1.IntersectionLocn(theLine);
            Location intPoint2 = edge2.IntersectionLocn(theLine);

//            System.out.println("intPoint1: " + intPoint1.toString());
//            System.out.println("intPoint2: " + intPoint2.toString());

            double intDist1 = intPoint1.DistanceTo(lineStart);
            double intDist2 = intPoint2.DistanceTo(lineStart);
            if (intDist1 < intDist2)
            {
                answer = new Location(intPoint1);
            }
            else
            {
                answer = new Location(intPoint2);
            }

//            if (answer == null)
//            {
//                System.out.println("answer is null!");
//            }
//            else
//            {
//                System.out.println("edge intersect at: " + answer.toString());
//            }
        }

        return answer;
    }

    public final Location EdgeIntersectOutward(final Location lineStart,
                                               final Heading lineHdng)
    {
        Location answer = null;

        Line theLine = new Line();
        theLine.SetStartHdng(lineStart, lineHdng);
        answer = EdgeIntersectOutward(theLine);

        return answer;
    }

    public void Print()
    {
        System.out.println("========== Box ==========");
        System.out.println(" BL: " + mCornerBL);
        System.out.println(" TL: " + mCornerTL);
        System.out.println(" TR: " + mCornerTR);
        System.out.println(" BR: " + mCornerBR);
        System.out.println();
    }

    //??? method to pass in an array of locations, and determine bounding box
    // perhaps including additional margin parameters?
}
