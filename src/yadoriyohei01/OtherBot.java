package yadoriyohei01;

import java.util.List;
import java.util.Vector;

import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;

/**
 * 他のロボットの情報を記録する。
 * 
 * @author yadoran
 */
public class OtherBot {
	// 履歴
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
		// 初回データしか無いときはそれを返す。
		if (scans.size() < 2)
			return getLatestEvent();
		return scans.get(scans.size() - 2);
	}

	public boolean firesGun() {
		// すでに発射を通知済みならfalseを返す。
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
	 * 脅威度、攻撃優先度を判定。
	 */
	public double calcRisk() {
		// TODO 効果的な計算
		// とりあえず弱ってる敵の内、一番近い敵
		return (200.0 - getLatestEvent().getEnergy()) + (getLatestEvent().getDistance() / 10000);
	}

}
