package model.network;

import java.time.LocalDateTime;

import utils.TextUtils;

/**
 * A class to log an agent action
 * @author cankurtan
 *
 */
public class ActionLog {
	
	/** agent id that took the action */
	private int agentId;	
	/** currently all actions are held as a string, 
	 * should be changed to hold it more efficiently */
	private String action;
	/** time the action was taken */
	private LocalDateTime time;
	
	/**
	 * Log constructor
	 * 
	 * @param id agent's id
	 * @param action agent's action
	 * @param time agent's action time
	 */
	public ActionLog(int agentId, String action, LocalDateTime time) {
		super();
		this.agentId = agentId;
		this.action = action;
		this.time = time;
	}

	@Override
	public String toString() {
		return "Log [agentId=" + agentId + ", action=" + action 
				+ ", actionTime=" + time.format(TextUtils.FORMATTER) + "]";
	}
}
