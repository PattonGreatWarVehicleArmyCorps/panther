package panther;

import java.util.ArrayList;
import java.util.List;

import robocode.ScannedRobotEvent;

/**
 * �O�̃��{�b�g
 * @author halt55
 */
public class OtherBot {
	// ����
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
