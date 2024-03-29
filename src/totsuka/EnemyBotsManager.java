package totsuka;

import java.util.HashMap;
import java.util.Map;

import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * 敵の情報を管理する
 */
public class EnemyBotsManager {

	public Map<String, OtherBot> others = new HashMap<String, OtherBot>();

	/**
	 * 脅威度、攻撃指示などから攻撃目標を決める。
	 */
	public OtherBot decideTarget() {
		double max = Double.MIN_VALUE;
		OtherBot candidate = null;
		for (OtherBot bot : others.values()) {
			// TODO 賢く探す。
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
	 * 敵が砲撃しているか
	 */
	public boolean areShooting(String name) {
		return others.get(name).firesGun();
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
