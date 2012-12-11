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
		// ���[�_�[�A�C���A�ԑ̂̉�]��Ɨ�������B
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
		// TODO �����ʒu�ˌ�����Guess Factor Aiming�̎����������B
		// TODO �A���ŊO�����珬�e���W�b�N�؂�ւ�

		// XXX ���܂ɐ��l�̂��������C�x���g�������Ă���B�Ȃ�ŁH
		// ScannedRobotEvent latest = lockOn.getLatestEvent();
		// double latestHeading = latest.getBearingRadians() +
		// getHeadingRadians();
		// latestHeading = normalHeading(latestHeading);
		// // �C���w������
		// double bearing = latestHeading - getGunHeadingRadians();
		// bearing = normaliseBearing(bearing);
		// setTurnGunRightRadians(bearing);
		// // �C���p�����Ȃ���Δ��C����B
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
		// TODO �U���Ώۂ��d�_�I�ɃX�L��������B
		if (scanToRight) {
			setTurnRadarRightRadians(Math.PI);
		} else {
			setTurnRadarLeftRadians(Math.PI);
		}
	}

	/**
	 * �������������G���L�^����B
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		others.addDamagingBullet(e);
		// TODO ����A���S���Y���ύX�B
	}

	/**
	 * �G��S�ċL�^����
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		// ���ׂĂ̓G���L�^������A���[�_�̎�U��������t�ɂ���B
		scaned++;
		if (scaned >= enemyCount) {
			scanToRight = (scanToRight ? false : true);
			scaned = 0;
		}
		// �G�̍s�����L�^����B
		others.registerEnemyData(e, (Robot) this.clone());
	}

	private void doMove() {
		// TODO �W�I��������Ȃ���������G�B
		if (lockOn == null)
			return;

		// TODO �W�I�����������^��
		dodge();
	}

	private void dodge() {

		if (!others.areShooting()) {
			// TODO ������ĂȂ��Ƃ�
			return;
		}
		// �C�΂ɂ��炳��Ă���Ƃ�
		// TODO �ǂƓG�������
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
	 * ���񂾓G��W�I��₩�珜�O�B
	 */
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		others.remove(event);
	}

	/**
	 * 2�n�_�Ԃ̋���
	 */
	public double getTange(double x1, double y1, double x2, double y2) {
		double x = x2 - x1;
		double y = y2 - y1;
		double h = Math.sqrt(x * x + y * y);
		return h;
	}

	/**
	 * �^���ʂ�0����2PI�Ƀm�[�}���C�Y
	 */
	public static double normalHeading(double angle) {
		if (angle < 0)
			return (2 * Math.PI) + (angle % (2 * Math.PI));
		else
			return angle % (2 * Math.PI);
	}

	/**
	 * ���Ίp��-PI����PI�Ƀm�[�}���C�Y
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
