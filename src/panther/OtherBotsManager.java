package panther;

import java.util.HashMap;
import java.util.Map;

import robocode.HitByBulletEvent;
import robocode.Robot;
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
			// TODO �����T���B(�̗͂��Ⴍ�ċ߂��Ă�����������Ă����G)
			if (others.get(key).getRisk() > max) {
				max = others.get(key).getRisk();
				candidate = others.get(key);
			}
		}
		return candidate;
	}

	public void registerEnemyData(ScannedRobotEvent event, Robot me) {
		if (!others.containsKey(event.getName())) {
			others.put(event.getName(), new OtherBot(event, me));
		} else {
			others.get(event.getName()).addScan(event, me);
		}
	}

	/**
	 * �C�����Ă�G�����邩
	 */
	public boolean areShooting() {
		for (OtherBot bot : others.values())
			if (bot.firesGun())
				return true;
		return false;
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
