package totsuka;

import robocode.*;
import robocode.util.Utils;

import java.awt.Color;
import java.awt.geom.*;
import java.io.IOException;

public class T��� extends TeamRobot {
	private static final double BULLET_POWER = 1.6;

	private static double lateralDirection;
	private static double lastEnemyVelocity;
	private static GFTMovement movement;
	private static String target;

	public EnemyBotsManager enemies = new EnemyBotsManager();
	public FriendBotsManager friends = new FriendBotsManager();
	public boolean scanToRight = true;
	public int scaned = 0;

	public void run() {
		setColors(new Color(255, 000, 051), new Color(255, 000, 051),
				new Color(255, 000, 051));
		lateralDirection = 1;
		lastEnemyVelocity = 0;
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		movement = new GFTMovement(this);
		do {
			// ���G�B�������炠�Ƃ�GFT�̒��Ŕ񓯊��ɘA�����������B
			turnRadarRightRadians(Double.POSITIVE_INFINITY);
		} while (true);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		// �����Ȃ疳��
		if (isFriend(e)) {
			friends.registerFriendData(e);
			return;
		}
		// �G�̍s�����L�^����B
		enemies.registerEnemyData(e);
		// ���ׂĂ̓G���L�^������A���[�_�̎�U��������t�ɂ���B
		// TODO �U���Ώۂ��d�_�I�ɃX�L�������� or ���킾�ƑS���X�L�����ŋϓ��ɏ����X�V��������x�^�[�H
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

		// ���̓G���C�����Ă������@����ύX
		if (enemies.areShooting(e.getName()))
			movement.onScannedRobot(e);

		// �U���ڕW�Ȃ�`�[���ɍU�����w�����Č���
		if (target == null) {
			target = enemies.decideTarget().getLatestEvent().getName();
		}
		if (target.equals(e.getName()) || friends.isMidwayOfTarget(e)) {
			assignMission(target);
			fcs(e);
		}
	}

	private boolean isFriend(ScannedRobotEvent e) {
		return e.getName().contains("Y�h��") || e.getName().contains("K�R��")
				|| e.getName().contains("T���");
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
	 * �ˌ��ǐ�
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
	 * ���񂾓G��W�I���X�g���珜�O�B
	 */
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		enemies.remove(event);
		friends.remove(event);
		if (event.getName().equals(target))
			target = null;
	}

}