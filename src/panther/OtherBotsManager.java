package panther;

import java.util.HashMap;
import java.util.Map;

import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * “G‚Ìî•ñ‚ğŠÇ—‚·‚é
 */
public class OtherBotsManager {

	public Map<String, OtherBot> others = new HashMap<String, OtherBot>();

	/**
	 * ‹ºˆĞ“xAUŒ‚w¦‚È‚Ç‚©‚çUŒ‚–Ú•W‚ğŒˆ‚ß‚éB
	 */
	public OtherBot decideTarget() {
		int max = -999999999;
		OtherBot candidate = null;
		for (String key : others.keySet()) {
			candidate = others.get(key);
			// TODO Œ«‚­’T‚·B
			if (others.get(key).getRisk() > max) {
				max = others.get(key).getRisk();
				candidate = others.get(key);
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
