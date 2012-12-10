package panther;

import java.awt.Color;
import java.util.Random;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;

public class Panther extends AdvancedRobot {

	public OtherBotsManager others = new OtherBotsManager();
	public OtherBot lockOn = null;
	public int enemyCount = -1;
	public int scaned = 0;
	public boolean scanToRight = true;

	// Methods
	public void run() {
		setColors(Color.pink, Color.pink, Color.pink);
		enemyCount = getOthers();
		// ���[�_�[�A�C���A�ԑ̂̉�]��Ɨ�������B
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		while (true) {
			waitFor(new TurnCompleteCondition(this));
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
		ScannedRobotEvent latest = lockOn.getLatestEvent();
		double latestHeading = latest.getBearingRadians() + getHeadingRadians();
		latestHeading = normalHeading(latestHeading);
		// �C���w������
		double bearing = latestHeading - getGunHeadingRadians();
		bearing = normaliseBearing(bearing);
		setTurnGunRightRadians(bearing);
		// �C���p�����Ȃ���Δ��C����B
		if (Math.abs(bearing) < 0.1)
			setFire(1);
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
	public void onScannedRobot(ScannedRobotEvent event) {
		// ���ׂĂ̓G���L�^������A���[�_�̎�U��������t�ɂ���B
		scaned++;
		if (scaned >= enemyCount)
			scanToRight = (scanToRight ? false : true);

		// �G�̍s�����L�^����B
		others.registerEnemyData(event);
	}

	private void doMove() {
		// TODO �W�I��������Ȃ���������G�B
		if (lockOn == null) {
			setAhead(10);
			return;
		}
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
		if (new Random(getTime()).nextBoolean()) {
			setAhead(getRondomDouble() * 100 + getRondomDouble() * 10
					+ getRondomDouble());
		} else {
			setBack(getRondomDouble() * 100 + getRondomDouble() * 10
					+ getRondomDouble());
		}

		if (new Random(getTime()).nextBoolean()) {
			setTurnRightRadians(getRondomDouble() * Math.PI);
		} else {
			setTurnLeft(getRondomDouble() * Math.PI);
		}
	}

	private double getRondomDouble() {
		return new Random(getTime()).nextDouble();
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

}
