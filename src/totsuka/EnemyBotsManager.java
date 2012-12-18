package totsuka;

import java.util.HashMap;
import java.util.Map;

import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * �G�̏����Ǘ�����
 */
public class EnemyBotsManager {

	public Map<String, OtherBot> others = new HashMap<String, OtherBot>();

	/**
	 * ���Гx�A�U���w���Ȃǂ���U���ڕW�����߂�B
	 */
	public OtherBot decideTarget() {
		double max = Double.MIN_VALUE;
		OtherBot candidate = null;
		for (OtherBot bot : others.values()) {
			// TODO �����T���B
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
	 * �G���C�����Ă��邩
	 */
	public boolean areShooting(String name) {
		return others.get(name).firesGun();
	}

	/**
	 * �W�I���珜�O����
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
