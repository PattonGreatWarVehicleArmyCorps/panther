package panther;

import java.util.List;
import java.util.Vector;

import robocode.HitByBulletEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

/**
 * ���̃��{�b�g�̏����L�^����B
 * 
 * @author halt55
 */
public class OtherBot {
	// ����
	List<ScannedRobotEvent> scans = new Vector<ScannedRobotEvent>();
	List<Robot> historyMySelf = new Vector<Robot>();

	List<HitByBulletEvent> bullets = new Vector<HitByBulletEvent>();
	String name;
	boolean answerdFire = false;

	public OtherBot(ScannedRobotEvent event, Robot mySelf) {
		name = event.getName();
		scans.add(event);
		historyMySelf.add(mySelf);
	}

	public void addScan(ScannedRobotEvent event, Robot mySelf) {
		scans.add(event);
		historyMySelf.add(mySelf);
		answerdFire = false;
		if (scans.size() > 10)
			scans.remove(0);
		if (historyMySelf.size() > 10)
			historyMySelf.remove(0);
	}

	public ScannedRobotEvent getLatestEvent() {
		return scans.get(scans.size() - 1);
	}

	/**
	 * �T�m�������_�̎����̏��
	 */
	public Robot getLatestMySelf() {
		return historyMySelf.get(historyMySelf.size() - 1);
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
	public int getRisk() {
		// TODO ���ʓI�Ȍv�Z
		int risk = 0;
		// risk += bullets.size() * 5;
		// risk += 5000 - getLatestEvent().getDistance();
		risk += 200 - getLatestEvent().getEnergy();
		return risk;
	}

}
