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
		// TODO 未来位置を予測する。
		ScannedRobotEvent lastEvent = lockOn.getLastEvent();
		double targetBearing = lastEvent.getBearingRadians()
				+ getHeadingRadians();
		targetBearing = normalAbsoluteHeading(targetBearing);
		// 砲を指向する
		double bearing = targetBearing - getGunHeadingRadians();
		bearing = normalRelativeAngle(bearing);
		setTurnGunRightRadians(bearing);
		// 修正が少ない時に発砲する。
		if (Math.abs(bearing) < 0.01 && lastEvent.getDistance() < 400)
			fire(1);
	}

	private OtherBot decideTarget() {
		if (!others.isEmpty()) {
			for (String key : others.keySet()) {
				// TODO 最適な敵を探す。(体力が低くて近い敵)
				OtherBot bot = others.get(key);
				return others.get(key);
			}
		}
		return null;
	}

	private void doRadar() {
		// 敵を探し続ける。
		setTurnRadarLeftRadians(Math.PI);
	}

	/**
	 * 敵をどんどん記録する
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
		// TODO 標的が見つからなかったら索敵。
		if (lockOn == null) {
			setAhead(10);
			return;
		}
		// TODO 標的がいたら回避運動
		dodge();
	}

	private void dodge() {
		// TODO 回避運動
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		// 死んだ敵は標的から除外
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
