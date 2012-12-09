package panther;

import java.util.ArrayList;
import java.util.List;

import robocode.ScannedRobotEvent;

/**
 * 外のロボット
 * @author halt55
 */
public class OtherBot {
	// 履歴
	List<ScannedRobotEvent> events = new ArrayList<ScannedRobotEvent>();

	public OtherBot(ScannedRobotEvent event) {
		events.add(event);
	}
	
	public void addEvent(ScannedRobotEvent event) {
		events.add(event);
	}
	
	public ScannedRobotEvent getLastEvent() {
		return events.get(events.size() - 1);
	}
}
