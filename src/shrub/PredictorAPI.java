package shrub;

/**
 * PredictorAPI - common interface for predictor objects.
 */
public interface PredictorAPI
{
    public void Initialise();

    // Area within which we expect target movement to be constrained.
    // (May or may not be supported by specific predictor instance.)
    public void SetMovementArea(final Box movementArea);

    // Area within which we are allowed to intercept target.
    // (May or may not be supported by specific predictor instance.)
    public void SetInterceptArea(final Box interceptArea);

    // Maximum time allowed for interception, anything more is not valid.
    // (May or may not be supported by specific predictor instance.)
    public void SetMaxInterceptTime(final long newValue);

    // Given our location and heading, either indicate the speed to cause
    // an interception of target, or an angle to turn to provide interception
    // opportunity in near future.
    public InterceptSolution Intercept(final double minSpeedAllowed,
                                       final double maxSpeedAllowed,
                                       final Location subLocn,
                                       final Heading subHdng,
                                       final MoveHistory objHistory);
}
