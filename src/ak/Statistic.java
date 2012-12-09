package ak;

import robocode.*;

/**
 * Statistic - a class by Arun Kishore
 */
public class Statistic {
	int shots;
	int hits;
	double lastHitTime;

	public double getRatio() {
		return ((double) hits) / ((double) shots);
	}
}