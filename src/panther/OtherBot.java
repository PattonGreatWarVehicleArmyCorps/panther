package panther;

import java.util.List;
import java.util.Vector;

import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;

/**
 * ���̃��{�b�g�̏����L�^����B
 * 
 * @author halt55
 */
public class OtherBot {
	// ����
	List<ScannedRobotEvent> scans = new Vector<ScannedRobotEvent>();
	List<HitByBulletEvent> bullets = new Vector<HitByBulletEvent>();
	String name;

	public OtherBot(ScannedRobotEvent event) {
		name = event.getName();
		scans.add(event);
	}

	public void addScan(ScannedRobotEvent event) {
		scans.add(event);
		if (scans.size() > 10)
			scans.remove(0);
	}

	public ScannedRobotEvent getLatestEvent() {
		return scans.get(scans.size() - 1);
	}

	public ScannedRobotEvent getPreviousEvent() {
		// ����f�[�^���������Ƃ��͂����Ԃ��B
		if (scans.size() < 2)
			return getLatestEvent();
		return scans.get(scans.size() - 2);
	}

	public boolean firesGun() {
		double energyDiff = getPreviousEvent().getEnergy()
				- getLatestEvent().getEnergy();
		return (0 < energyDiff && energyDiff <= 3.0);
	}

	public void addBullet(HitByBulletEvent event) {
		bullets.add(event);
		if (bullets.size() > 10)
			bullets.remove(0);
	}

	/**
	 * ���Гx�A�U���D��x�𔻒�B
	 */
	public int getRisk() {
		// TODO ���ʓI�Ȍv�Z
		int risk = 0;
		risk += bullets.size() * 5;
//		risk += 5000 - getLatestEvent().getDistance();
		risk += 100 - getLatestEvent().getEnergy();
		return risk;
	}

}
