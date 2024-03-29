package totsuka;

import java.util.HashMap;
import java.util.Map;

import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * 敵の情報を管理する
 */
public class FriendBotsManager {

	public Map<String, OtherBot> others = new HashMap<String, OtherBot>();

	/**
	 * 標的との間に味方がいないか
	 * 
	 * @param e
	 */
	public boolean isMidwayOfTarget(ScannedRobotEvent e) {
		for (OtherBot bot : others.values()) {
			ScannedRobotEvent latestEvent = bot.getLatestEvent();
			if (e.getBearingRadians() - 0.3 < latestEvent.getBearingRadians()
					&& latestEvent.getBearingRadians() < e.getBearingRadians() + 0.3
					&& latestEvent.getDistance() < e.getDistance())
				return true;
		}
		return false;
	}

	public void registerFriendData(ScannedRobotEvent event) {
		if (!others.containsKey(event.getName())) {
			others.put(event.getName(), new OtherBot(event));
		} else {
			others.get(event.getName()).addScan(event);
		}
	}

	/**
	 * 除外する
	 */
	public void remove(RobotDeathEvent event) {
		if (others.containsKey(event.getName()))
			others.remove(event.getName());
	}
}
