package yadoriyohei01;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;

import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;
import robocode.util.Utils;

public class Y�h�� extends TeamRobot {
	// For AssultMode
	private static final int ASSULT_TURN_SPEED = 5;
	private static boolean isAssultMode = false;
	private static int assultTurnDirection = 1;
	private static int enemyNum = 3;

	private static double BULLET_POWER = 1.0f;

	private static double lateralDirection;
	private static double lastEnemyVelocity;
	private static GFTMovement movement;
	private static String targetName;

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
			if (isAssultMode) {
				turnRight(ASSULT_TURN_SPEED * assultTurnDirection);
			} else {
				// ���G�B�������炠�Ƃ�GFT�̒��Ŕ񓯊��ɘA�����������B
				turnRadarRightRadians(Double.POSITIVE_INFINITY);
			}
		} while (true);
	}

	/**
	 * ���[�_�[����Ԃ𔭌�
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// �����Ȃ疳��
		if (isFriend(e.getName())) {
			return;
		}
		
		// �G�̍s�����L�^����B
		others.registerEnemyData(e);

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
		
		// �ˌ����[�h�p�A�������l�߂܂��B
		if (isAssultMode) {
			if (e.getBearing() >= 0) {
				assultTurnDirection = 1;
			} else {
				assultTurnDirection = -1;
			}
			turnRight(e.getBearing());
			ahead(e.getDistance() + 5);
			scan();
			return;
		}
		

		// ���̓G���C�����Ă������@����ύX
		if (others.areShooting(e.getName())) {
			movement.onScannedRobot(e);
		}

		// �U���ڕW�Ȃ�`�[���ɍU�����w�����Č���
		if (targetName == null) {
			targetName = others.decideTarget().getLatestEvent().getName();
		}
		if (targetName.equals(e.getName())) {
			assignMission(targetName);
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
			targetName = message.split(":")[1];
	}

	/**
	 * �ˌ��ǐ�
	 */
	private void fcs(ScannedRobotEvent e) {
		// TODO �F�R��ˉ���B
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
		
		if(e.getDistance() > 600){
			return;
		}
		
		setFire(((600 / e.getDistance()) * 3));
		if (getEnergy() >= BULLET_POWER) {
			addCustomEvent(wave);
		}
	}

	/**
	 * ���񂾓G��W�I���X�g���珜�O�B
	 */
	@Override
	public void onRobotDeath(RobotDeathEvent e) {
		others.remove(e);
		String name = e.getName();
		if (name.equals(targetName)) {
			targetName = null;
		}

		// �^�[�Q�b�g����l�ɂȂ�����A�ǂ������郂�[�h�˓�
		if (!isFriend(name)) {
			enemyNum--;
			if (enemyNum == 1) {
				isAssultMode = true;
			}
		}
	}

	/**
	 * �G���{�Ɍ��˂��ɂ�������̍s��
	 */
	public void onHitRobot(HitRobotEvent e) {
		if (isAssultMode) {
			if (e.getBearing() >= 0) {
				assultTurnDirection = 1;
			} else {
				assultTurnDirection = -1;
			}
			turnRight(e.getBearing());
			if (e.getEnergy() > 16) {
				fire(3);
			} else if (e.getEnergy() > 10) {
				fire(2);
			} else if (e.getEnergy() > 4) {
				fire(1);
			} else if (e.getEnergy() > 2) {
				fire(.5);
			} else if (e.getEnergy() > .4) {
				fire(.1);
			}
			ahead(40); // �܂����˂��ɂ����B
		}
	}

	/**
	 * �������ǂ����𔻕ʂ���
	 * 
	 * @param targetName
	 * @return
	 */
	private boolean isFriend(String targetName) {
		if (targetName.startsWith("yadoriyohei01") || targetName.startsWith("totsuka") || targetName.startsWith("santoken01"))
		{
			return true;
		}
		return false;
	}
}
