package panther;

import java.util.HashMap;
import java.util.Map;

import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * �G�̏����Ǘ�����
 */
public class OtherBotsManager {

	public Map<String, OtherBot> others = new HashMap<String, OtherBot>();

	/**
	 * ���Гx�A�U���w���Ȃǂ���U���ڕW�����߂�B
	 */
	public OtherBot decideTarget() {
		int max = -999999999;
		OtherBot candidate = null;
		for (String key : others.keySet()) {
			candidate = others.get(key);
			// TODO �����T���B
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
