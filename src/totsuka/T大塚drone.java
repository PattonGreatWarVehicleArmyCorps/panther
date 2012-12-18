package totsuka;

import robocode.*;
import robocode.util.Utils;

import java.awt.Color;
import java.awt.geom.*;
import java.io.IOException;

public class T大塚drone extends TeamRobot {
	private static final double BULLET_POWER = 1.6;

	private static double lateralDirection;
	private static double lastEnemyVelocity;
	private static GFTMovement movement;
	private static String target;

	public OtherBotsManager others = new OtherBotsManager();
	public boolean scanToRight = true;
	public int scaned = 0;

	public void run() {
		setColors(Color.BLUE, Color.BLACK, Color.YELLOW);
		lateralDirection = 1;
		lastEnemyVelocity = 0;
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		movement = new GFTMovement(this);
		do {
			// 索敵。見つけたらあとはGFTの中で非同期に連続処理される。
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		} while (true);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		// 味方なら無視
		if (e.getName().startsWith("osaka"))
			return;
		// 敵の行動を記録する。
		others.registerEnemyData(e);
		// すべての敵を記録したら、レーダの首振り方向を逆にする。
		// TODO 攻撃対象を重点的にスキャンする or 乱戦だと全周スキャンで均等に情報を更新する方がベター？
		scaned++;
		if (scaned >= getOthers()) {
			scanToRight = (scanToRight ? false : true);
			scaned = 0;
		}
		if (scanToRight) {
			setTurnRadarRightRadians(Math.PI);
		} else {
			setTurnRadarLeftRadians(Math.PI);
		}

		// その敵が砲撃してたら回避機動を変更
		if (others.areShooting(e.getName()))
			movement.onScannedRobot(e);

		// 攻撃目標が設定されてないときは自己判断
		if (target == null) {
			target = others.decideTarget().getLatestEvent().getName();
		}
		if (target.equals(e.getName())) {
			fcs(e);
		}
	}

	public void assignMission(String target) {
		try {
			broadcastMessage("target:" + target);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onMessageReceived(MessageEvent e) {
		Object msg = e.getMessage();
		if (!(msg instanceof String))
			return;
		String message = (String) msg;
		if (message == null || "".equals(message))
			return;
		if (message.startsWith("target:"))
			target = message.split(":")[1];
	}

	/**
	 * 射撃管制
	 */
	private void fcs(ScannedRobotEvent e) {
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
	}

	/**
	 * 死んだ敵を標的リストから除外。
	 */
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		others.remove(event);
		if (event.getName().equals(target))
			target = null;
	}

}