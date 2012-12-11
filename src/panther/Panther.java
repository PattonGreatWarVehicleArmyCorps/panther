package panther;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Random;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;
import robocode.util.Utils;

public class Panther extends AdvancedRobot implements Cloneable {

	public OtherBotsManager others = new OtherBotsManager();
	public OtherBot lockOn = null;
	public int enemyCount = -1;
	public int scaned = 0;
	public boolean scanToRight = true;

	private static double lateralDirection;
	private static double lastEnemyVelocity;
	private static final double BULLET_POWER = 1;
	private GFTMovement movement;

	// Methods
	public void run() {
		setColors(Color.pink, Color.pink, Color.pink);
		// レーダー、砲塔、車体の回転を独立させる。
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		movement = new GFTMovement(this);
		while (true) {
			waitFor(new TurnCompleteCondition(this));
			enemyCount = getOthers();
			lockOn = others.decideTarget();
			doRadar();
			doMove();
			doFire();
		}
	}

	private void doFire() {
		if (lockOn == null)
			return;
		// TODO 未来位置射撃よりもGuess Factor Aimingの実装したい。
		// TODO 連続で外したら小銃ロジック切り替え

		// XXX たまに数値のおかしいイベントが入ってくる。なんで？
		// ScannedRobotEvent latest = lockOn.getLatestEvent();
		// double latestHeading = latest.getBearingRadians() +
		// getHeadingRadians();
		// latestHeading = normalHeading(latestHeading);
		// // 砲を指向する
		// double bearing = latestHeading - getGunHeadingRadians();
		// bearing = normaliseBearing(bearing);
		// setTurnGunRightRadians(bearing);
		// // 修正角が少なければ発砲する。
		// if (Math.abs(bearing) < 0.1)
		// setFire(1);

		ScannedRobotEvent e = lockOn.getLatestEvent();
		double enemyAbsoluteBearing = getHeadingRadians()
				+ e.getBearingRadians();
		double enemyDistance = e.getDistance();
		double enemyVelocity = e.getVelocity();
		if (enemyVelocity != 0) {
			lateralDirection = GFTUtils.sign(enemyVelocity
					* Math.sin(e.getHeadingRadians() - enemyAbsoluteBearing));
		}
		GFTWave wave = new GFTWave(this);
		wave.gunLocation = new Point2D.Double(getX(), getY());
		GFTWave.targetLocation = GFTUtils.project(wave.gunLocation,
				enemyAbsoluteBearing, enemyDistance);
		wave.lateralDirection = lateralDirection;
		wave.bulletPower = BULLET_POWER;
		wave.setSegmentations(enemyDistance, enemyVelocity, lastEnemyVelocity);
		lastEnemyVelocity = enemyVelocity;
		wave.bearing = enemyAbsoluteBearing;
		setTurnGunRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing
				- getGunHeadingRadians() + wave.mostVisitedBearingOffset()));
		setFire(wave.bulletPower);
		if (getEnergy() >= BULLET_POWER) {
			addCustomEvent(wave);
		}
		// movement.onScannedRobot(lockOn.getLatestEvent());
		// setTurnRadarRightRadians(Utils.normalRelativeAngle(enemyAbsoluteBearing
		// - getRadarHeadingRadians()) * 2);
	}

	private void doRadar() {
		// TODO 攻撃対象を重点的にスキャンする。
		if (scanToRight) {
			setTurnRadarRightRadians(Math.PI);
		} else {
			setTurnRadarLeftRadians(Math.PI);
		}
	}

	/**
	 * 自分を撃った敵を記録する。
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		others.addDamagingBullet(e);
		// TODO 回避アルゴリズム変更。
	}

	/**
	 * 敵を全て記録する
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		// すべての敵を記録したら、レーダの首振り方向を逆にする。
		scaned++;
		if (scaned >= enemyCount) {
			scanToRight = (scanToRight ? false : true);
			scaned = 0;
		}
		// 敵の行動を記録する。
		others.registerEnemyData(e, (Robot) this.clone());
	}

	private void doMove() {
		// TODO 標的が見つからなかったら索敵。
		if (lockOn == null)
			return;

		// TODO 標的がいたら回避運動
		dodge();
	}

	private void dodge() {

		if (!others.areShooting()) {
			// TODO 撃たれてないとき
			return;
		}
		// 砲火にさらされているとき
		// TODO 壁と敵を避ける
		if (new Random().nextBoolean()) {
			setAhead(getRondomDouble() * 300 + getRondomDouble() * 10
					+ getRondomDouble());
		} else {
			setBack(getRondomDouble() * 300 + getRondomDouble() * 10
					+ getRondomDouble());
		}
		if (new Random().nextBoolean()) {
			setTurnRightRadians(getRondomDouble() * Math.PI * 0.75);
		} else {
			setTurnLeft(getRondomDouble() * Math.PI * 0.75);
		}
	}

	private double getRondomDouble() {
		return new Random().nextDouble();
	}

	/**
	 * 死んだ敵を標的候補から除外。
	 */
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		others.remove(event);
	}

	/**
	 * 2地点間の距離
	 */
	public double getTange(double x1, double y1, double x2, double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		double h = Math.sqrt(x * x + y * y);
		return h;
	}

	/**
	 * 真方位を0から2PIにノーマライズ
	 */
	public static double normalHeading(double angle) {
		if (angle < 0)
			return (2 * Math.PI) + (angle % (2 * Math.PI));
		else
			return angle % (2 * Math.PI);
	}

	/**
	 * 相対角を-PIからPIにノーマライズ
	 */
	public static double normaliseBearing(double ang) {
		if (ang > Math.PI)
			ang -= 2 * Math.PI;
		if (ang < -Math.PI)
			ang += 2 * Math.PI;
		return ang;
	}

	@Override
	protected Object clone() {
		return super.clone();
	}
}
