package yadoriyohei01;

import java.util.List;
import java.util.Vector;

import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;

/**
 * ���̃��{�b�g�̏����L�^����B
 * 
 * @author yadoran
 */
public class OtherBot {
	// ����
	List<ScannedRobotEvent> scans = new Vector<ScannedRobotEvent>();
	List<HitByBulletEvent> bullets = new Vector<HitByBulletEvent>();
	String name;
	boolean answerdFire = false;

	public OtherBot(ScannedRobotEvent event) {
		name = event.getName();
		scans.add(event);
	}

	public void addScan(ScannedRobotEvent event) {
		scans.add(event);
		answerdFire = false;
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
		// ���łɔ��˂�ʒm�ς݂Ȃ�false��Ԃ��B
		if (answerdFire)
			return false;
		double energyDiff = getPreviousEvent().getEnergy()
				- getLatestEvent().getEnergy();
		if (0 < energyDiff && energyDiff <= 3.0)
			answerdFire = true;
		return true;
	}

	public void addBullet(HitByBulletEvent event) {
		bullets.add(event);
		if (bullets.size() > 10)
			bullets.remove(0);
	}

	/**
	 * ���Гx�A�U���D��x�𔻒�B
	 */
	public double calcRisk() {
		// TODO ���ʓI�Ȍv�Z
		// �Ƃ肠��������Ă�G�̓��A��ԋ߂��G
		return (200.0 - getLatestEvent().getEnergy()) + (getLatestEvent().getDistance() / 10000);
	}

}
