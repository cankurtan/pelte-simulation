package model.privacy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.network.RelationType;

/**
 * A class describing privacy setting of a post 
 * @author Can Kurtan
 *
 */
public class PrivacySetting {

	/**
	 * a map storing relation based access control (ReBAC) decisions
	 */
	private final Map<RelationType, SharingDecision> rebac = new LinkedHashMap<>();
	/**
	 * list of agent IDs that have exception on privacy setting
	 * If it is empty, all the agents are subject to the ReBAC Rules
	 */
	private final List<Integer> exceptionAgents = new ArrayList<>();
	
	/**
	 * Empty constructor
	 */
	public PrivacySetting() {}
	
	/**
	 * Privacy Setting constructor with ReBAC
	 * 
	 * @param rebac map of relation types and their corresponding sharing decisions
	 */
	public PrivacySetting(Map<RelationType, SharingDecision> rebac) {
		this.rebac.putAll(rebac);
	}
	
	/**
	 * Full constructor
	 * 
	 * @param rebac map of relation types and their corresponding sharing decisions
	 * @param exceptionAgents
	 */
	public PrivacySetting(Map<RelationType, SharingDecision> rebac, 
			List<Integer> exceptionAgents) {
		this.rebac.putAll(rebac);
	}
	
	/**
	 * Applies the given privacy decision to the given relation type
	 * @param rType relation type
	 * @param sharing privacy decision
	 */
	public void addSharingRule(RelationType rType, SharingDecision sharing){
		rebac.put(rType, sharing);
	}
	
	/**
	 * Gets the privacy decision for the given relation type 
	 * @param rType relation type 
	 * @return sharing decision 
	 */
	public SharingDecision getSharingDecision(RelationType rType){
		return rebac.get(rType);
	}

	/**
	 * Get ReBAC decisions of the privacy setting
	 * @return mapping of relation types and privacy decisions
	 */
	public Map<RelationType, SharingDecision> getRebac() {
		return rebac;
	}

	/**
	 * The list is empty if the setting is the same for all agents
	 * @return list of agent ids that are exception for the privacy setting
	 */
	public List<Integer> getExceptionAgents() {
		return exceptionAgents;
	}

	/**
	 * Converts the privacy decisions in ReBAC map into an array in the order of relation types
	 * @return an array of privacy decisions 
	 */
	public int[] getArrayOfSetting(){
		int[] decisions = new int[RelationType.values().length];
		for (int i = 0; i < decisions.length; i++) {
			decisions[i] = rebac.get(RelationType.values()[i]).getId();
		}
		return decisions;
	}
	
	/**
	 * Changes privacy decisions in the setting based on given array.
	 * Array has to be in the order of relation types
	 * @param decisions array of privacy decisions of the privacy setting
	 */
	public void setDecisions(int[] decisions){
		for(int i = 0; i < RelationType.values().length; i++){
			if(decisions[i] == SharingDecision.PERMIT.getId()){
				rebac.put(RelationType.values()[i], SharingDecision.PERMIT);
			}
			else if(decisions[i] == SharingDecision.DENY.getId()) {
				rebac.put(RelationType.values()[i], SharingDecision.DENY);
			}
			else {
				System.out.println("Unkown privacy decison for Relation Type " + RelationType.values()[i].name());
			}
		}
	}
	
	/**
	 * Returns a deep copy of the privacy setting
	 * @return a deep copy of the privacy setting
	 */
	public PrivacySetting copy() {
		PrivacySetting ps = new PrivacySetting(new LinkedHashMap<>(this.rebac),
				new ArrayList<>(this.exceptionAgents));
		return ps;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < RelationType.values().length; i++) {
			sb.append(RelationType.values()[i].name() + ":" + rebac.get(RelationType.values()[i]));
		}
		return sb.toString(); 
	}
}
