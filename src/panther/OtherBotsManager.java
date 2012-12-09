package panther;

import java.util.HashMap;
import java.util.Map;

import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * 敵の情報を管理する
 */
public class OtherBotsManager {

	public Map<String, OtherBot> others = new HashMap<String, OtherBot>();

	/**
	 * 脅威度、攻撃指示などから攻撃目標を決める。
	 */
	public OtherBot decideTarget() {
		int max = -999999999;
		OtherBot candidate = null;
		for (String key : others.keySet()) {
			candidate = others.get(key);
			// TODO 賢く探す。(体力が低くて近くてこちらを撃ってきた敵)
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
	 * 砲撃してる敵がいるか
	 */
	public boolean areShooting() {
		for (OtherBot bot : others.values())
			if (bot.firesGun())
				return true;
		return false;
	}

	/**
	 * 標的から除外する
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
