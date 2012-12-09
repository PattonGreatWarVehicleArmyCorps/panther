package emp;

public interface Consts {
     public static final int cRobot     = 1;
     public static final int cTurret    = 2;
     public static final int cRadar     = 3;

     public static final int cTime      = 0;
     public static final int cX         = 1;
     public static final int cY         = 2;
     public static final int cVelocity  = 3;
     public static final int cHeading   = 4;
     public static final int cTurned    = 5;
     public static final int cDisplace  = 6;
     public static final int cEnergy    = 7;
     public static final int cFired     = 8;
     public static final int cOthers    = 9;

     public static final int cVariables = 10;

     public static final int cFinisherPredictor   = 300;
     public static final int cCurPosPredictor     = 301;
     public static final int cXYPredictor         = 302;
     public static final int cTurnDispPredictor   = 303;
     public static final int cLinearPredictor     = 304;
     public static final int cVelocityPredictor   = 305;
     public static final int cSprayPredictor      = 306;
     public static final int cCloseRangePredictor = 307;
     public static final int cSquarePredictor     = 308;
     public static final int cDodgePredictor      = 309;
     public static final int cDodge2Predictor     = 310;

     public static final int cGlobalPoints        = 4;

     public static final double cWallsField              = 2000.0;
     public static final double cEnemyField              = 200.0;
//     public static final double cTargetField             = 50.0;
     public static final double cCenterField             = 150.0;
     public static final double cRobotMaxAcceleration    = 1.0;
     public static final double cRobotMaxDeceleration    = 2.0;
     public static final double cRobotMaxVelocity        = 8.0;

     public static final double cMaxBulletPower          = 3.0;

     public static final double cMaxScannerRange         = 1200.0;

     public static final double cSaveAmmoMode1 = 8.0;  // if yngwie has this energy or lower, he shoots max 1.0
     public static final double cSaveAmmoMode2 = 3.0;  // if yngwie has this energy or lower, he shoots max 0.1

     public static final int cRecordCount      = 4096;  // starting enemy record length, is increased when too small
     public static final int cMaxHistoryLength = 30;
     public static final int cMaxInterpolatie  = 8;

     public static final int cRobotRadius      = 18;
}