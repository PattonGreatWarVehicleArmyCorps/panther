package ak;

/**
 * PatternAnalyzer - a class by Arun Kishore
 */
public class PatternAnalyzer
{
	public int patterns=2;
	public double avgReverseTime;
	public double avgReverseTimeSqr;
	public long timeLastReverse;
	public boolean accelerating;
	public double acceleration,lastAcceleration,lastVelocity,stddev;
	public double avgMovingTime,startMovingTime,avgMovingTimeSqr,movestddev;
	int readings,moves;
	
	PatternAnalyzer()
	{
		acceleration = lastAcceleration = lastVelocity = 0;
		timeLastReverse = 0;
		avgReverseTime = -1;
		avgMovingTime = startMovingTime = -1;
		stddev = movestddev = -1;
	}
	
	void analyzePatterns(double velocity,long scanTime)
	{
		//move-stop pattern recognition
		if(velocity==0 && Math.abs(lastVelocity) > 0)
		{
			if(startMovingTime != -1)
			{
				if(avgMovingTime == -1)
				{
					moves = 1;
					avgMovingTime = (scanTime - startMovingTime);
					avgMovingTimeSqr = Math.pow(scanTime - startMovingTime,2);
				}
				else
				{
					moves++;
					avgMovingTime = (avgMovingTime*(moves-1)+scanTime-startMovingTime)/moves;
					avgMovingTimeSqr = (avgMovingTimeSqr*(moves-1)+Math.pow(scanTime-startMovingTime,2))/moves;
					movestddev = Math.sqrt(avgMovingTimeSqr - Math.pow(avgMovingTime, 2));
					if(movestddev > 6)
					{
						//start analysing again
						moves = 1;
						avgMovingTime = (scanTime - startMovingTime);
						avgMovingTimeSqr = Math.pow(scanTime - startMovingTime,2);
						movestddev = -1;
					}
				}	
				startMovingTime = -1;
			}
		}
		
		if(Math.abs(velocity) > 0 && lastVelocity==0)
			startMovingTime = scanTime;
		
		//strafing pattern recognition
		acceleration = velocity - lastVelocity;		
		if(acceleration > 1)
			acceleration = 1;
		else if(acceleration < -1)
			acceleration = -1;
		if(acceleration == 1)
			accelerating = true;
		else if(acceleration == -1)
			accelerating = false;
		if((acceleration != lastAcceleration) && (acceleration != 0))
		{			
			if(avgReverseTime == -1)
			{
				readings = 1;
                avgReverseTime = scanTime;
                avgReverseTimeSqr = Math.pow(scanTime,2);
            } 
			else 
			{
				readings++;
				avgReverseTime = ((avgReverseTime * (readings-1)) + (scanTime - timeLastReverse))/readings;
                avgReverseTimeSqr = ((avgReverseTimeSqr * (readings-1)) + (Math.pow(scanTime - timeLastReverse,2)))/readings;
                stddev = Math.sqrt(avgReverseTimeSqr - Math.pow(avgReverseTime, 2));
				if(stddev > 6)
				{
					readings = 1;
	                avgReverseTime = scanTime;
    	            avgReverseTimeSqr = Math.pow(scanTime,2);
					stddev = -1;
				}
            }
            timeLastReverse = scanTime;
        }
			
		lastVelocity = velocity;
		lastAcceleration = acceleration;
	}
}
