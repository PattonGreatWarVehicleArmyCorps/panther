package panther;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TurnCompleteCondition;

public class Panther extends AdvancedRobot {

	public Map<String, OtherBot> others = new HashMap<String, OtherBot>();
	public OtherBot lockOn = null;

	// Methods
	public void run() {
		setColors(Color.pink, Color.pink, Color.pink);
		while (true) {
			waitFor(new TurnCompleteCondition(this));
			lockOn = decideTarget();
			doRadar();
			doMove();
			doFire();
		}
	}

	private void doFire() {
		if (lockOn == null)
			return;
		// TODO �����ʒu��\������B
		ScannedRobotEvent lastEvent = lockOn.getLastEvent();
		double targetBearing = lastEvent.getBearingRadians()
				+ getHeadingRadians();
		targetBearing = normalAbsoluteHeading(targetBearing);
		// �C���w������
		double bearing = targetBearing - getGunHeadingRadians();
		bearing = normalRelativeAngle(bearing);
		setTurnGunRightRadians(bearing);
		// �C�������Ȃ����ɔ��C����B
		if (Math.abs(bearing) < 0.01 && lastEvent.getDistance() < 400)
			fire(1);
	}

	private OtherBot decideTarget() {
		if (!others.isEmpty()) {
			for (String key : others.keySet()) {
				// TODO �œK�ȓG��T���B(�̗͂��Ⴍ�ċ߂��G)
				OtherBot bot = others.get(key);
				return others.get(key);
			}
		}
		return null;
	}

	private void doRadar() {
		// �G��T��������B
		setTurnRadarLeftRadians(Math.PI);
	}

	/**
	 * �G���ǂ�ǂ�L�^����
	 */
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		if (!others.containsKey(event.getName())) {
			others.put(event.getName(), new OtherBot(event));
		} else {
			others.get(event.getName()).addEvent(event);
		}
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
		// TODO ����^��
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		// ���񂾓G�͕W�I���珜�O
		if (others.containsKey(event.getName()))
			others.remove(event.getName());
	}

	public static double normalAbsoluteHeading(double angle) {
		if (angle < 0)
			return (2 * Math.PI) + (angle % (2 * Math.PI));
		else
			return angle % (2 * Math.PI);
	}

	public static double normalRelativeAngle(double angle) {
		if (angle > Math.PI)
			return ((angle + Math.PI) % (2 * Math.PI)) - Math.PI;
		if (angle < -Math.PI)
			return ((angle - Math.PI) % (2 * Math.PI)) + Math.PI;
		return angle;
	}
}
