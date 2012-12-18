package totsuka;

import java.util.HashMap;
import java.util.Map;

import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * “G‚Ìî•ñ‚ğŠÇ—‚·‚é
 */
public class EnemyBotsManager {

	public Map<String, OtherBot> others = new HashMap<String, OtherBot>();

	/**
	 * ‹ºˆĞ“xAUŒ‚w¦‚È‚Ç‚©‚çUŒ‚–Ú•W‚ğŒˆ‚ß‚éB
	 */
	public OtherBot decideTarget() {
		double max = Double.MIN_VALUE;
		OtherBot candidate = null;
		for (OtherBot bot : others.values()) {
			// TODO Œ«‚­’T‚·B
			if (bot.calcRisk() > max) {
				max = bot.calcRisk();
				candidate = bot;
			}
		}
		return candidate;
	}

	public void registerEnemyData(ScannedRobotEvent event) {
		if (!others.containsKey(event.getName())) {
			others.put(event.getName(), new OtherBot(event));
		} else {
			others.get(event.getName()).addScan(event);
		}
	}

	
	/**
	 * “G‚ª–CŒ‚‚µ‚Ä‚¢‚é‚©
	 */
	public boolean areShooting(String name) {
		return others.get(name).firesGun();
	}

	/**
	 * •W“I‚©‚çœŠO‚·‚é
	 */
	public void remove(RobotDeathEvent event) {
		if (others.containsKey(event.getName()))
			others.remove(event.getName());
	}

	public void addDamagingBullet(HitByBulletEvent event) {
		if (others.containsKey(event.getName())) {
			others.get(event.getName()).addBullet(event);
		}
	}

}
