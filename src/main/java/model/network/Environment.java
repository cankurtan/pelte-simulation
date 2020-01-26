package model.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.experiment.Stats;
import model.privacy.PrivacySetting;
import model.privacy.SharingDecision;
import utils.Utils;

/**
 * Environment stores agents and contents.
 * It supports ReBAC (Relation Based Access Control).
 * @author cankurtan
 *
 */
public class Environment {

	/**
	 * Agents in the environment, mapped to agentId
	 */
	protected final Map<Integer, Agent> agents = new HashMap<>();
	/**
	 * Relationships between agents, mapped to agentId
	 */
	protected final Map<Integer, Map<Integer, Relation>> relations = new HashMap<>();
	/**
	 * Shared contents, mapped to contentId
	 */
	protected final Map<Long, Content> contents = new HashMap<>();
	/**
	 * Relationship types in the environment
	 */
	private final List<RelationType> relationTypes = new ArrayList<>();
	/**
	 * Unshared contents
	 */
	private final List<Content> unsharedContents = new ArrayList<>();
	/**
	 * If the relations in the environment are bidirectional, it is true.
	 * Default type for relations is bidirectional, which is true.
	 */
	private boolean isBidirectional = true;
	/**
	 * Statistics of the environment, mapped to relation types 
	 */
	protected final Map<RelationType, Stats> relationStats = new HashMap<>();
	/**
	 * Number of possible sharing decisions
	 */
	protected int nDecision;
	/**
	 * Statistics of each agent, mapped to agentId
	 */
	private final Map<Integer, Map<RelationType, Stats>> agentStats = new HashMap<>();

	
	/**
	 * A constructor with defining only relation types.
	 * Co-ownership is not supported, relations are bidirectional.
	 * @param relationTypes a list of supported relation in the environment
	 */
	public Environment(List<RelationType> relationTypes) {
		super();
		this.relationTypes.addAll(relationTypes);
		this.nDecision = SharingDecision.values().length;
		for(RelationType rType : relationTypes) {
			relationStats.put(rType, new Stats(nDecision));
		}
	}
	
	/**
	 * 
	 * @param relationTypes a list of supported relation in the environment
	 * @param isBidirectional true if the relations are bidirectional
	 */
	public Environment(List<RelationType> relationTypes, boolean isBidirectional) {
		this(relationTypes);
		this.isBidirectional = isBidirectional;
	}

	/**
	 * Gets the agent based on agent id 
	 * @param agentId ID of the agent
	 * @return agent
	 */
	public Agent getAgent(int agentId) {
		return agents.get(agentId);
	}

	/**
	 * Adds the agent to the environment
	 * @param agent agent to be added
	 */
	public void addAgent(Agent agent) {
		agents.put(agent.getId(), agent);
	}

	/**
	 * Gets the ids of all the agents in the environment
	 * @return list of ids
	 */
	public List<Integer> getAgentIds() {
		return new ArrayList<Integer>(agents.keySet());
	}

	/**
	 * Counts the agents in the environment
	 * @return the number of agents
	 */
	public int getAgentCount(){
		return agents.size();
	}

	/**
	 * Gets the relation between two agents
	 * 
	 * @param sAgentId source agent's id
	 * @param tAgentId target agent's id
	 * @return If they have a relation, returns the relation between agents. 
	 * Otherwise, returns null
	 */
	public Relation getRelation(int sAgentId, int tAgentId) {
		if(hasRelation(sAgentId, tAgentId)){
			return relations.get(sAgentId).get(tAgentId);
		}
		return null;
	}

	/**
	 * Checks if there is a relation between agents
	 * 
	 * @param sAgentId source agent's id
	 * @param tAgentId target agent's id
	 * @return true if they have a relation. Otherwise, false
	 */
	public boolean hasRelation(int sAgentId, int tAgentId){
		if(relations.containsKey(sAgentId) && 
				relations.get(sAgentId).containsKey(tAgentId)){
			return true;
		}
		return false;
	}

	/**
	 * Gets all the relations an agent has
	 * 
	 * @param agentId agent's id
	 * @return map of agent's all relations
	 */
	public Map<Integer, Relation> getAgentRelations(int agentId) {
		return relations.get(agentId);
	}

	/**
	 * Adds a new relation from source agent to target agent.
	 * 
	 * @param sAgentId source agent's id
	 * @param tAgentId target agent's id
	 * @param rel relation between agents
	 */
	public void addRelation(int sAgentId, int tAgentId, Relation rel) {
		if(!relations.containsKey(sAgentId)){
			relations.put(sAgentId, new HashMap<Integer, Relation>());
		}
		relations.get(sAgentId).put(tAgentId, rel);
	}

	/**
	 * Gets all the contents in the environment
	 * @return map from content id to content 
	 */
	public Map<Long, Content> getContents(){
		return contents;
	}

	/**
	 * Gets the content based on given id 
	 * @param contentId id of the requested content
	 * @return content
	 */
	public Content getContent(long contentId) {
		return contents.get(contentId);
	}
	
	/**
	 * Adds the shared content to the environment.
	 * Gets the estimation for the privacy setting from the content owner's tag table.
	 * Updates the tag tables according to the actual privacy setting.
	 * Updates the confusion matrix by comparing the estimation and the actual privacy setting.
	 * 
	 * @param content shared content
	 */
	public void addContent(Content content) {
		
		contents.put(content.getId(), content);
		Agent agent = agents.get(content.getOwnerId());
		agent.addContent(content);
		
		if(Utils.isPredictionActive()){
			PrivacySetting ps = content.getPrivacySetting();
			int[] estimation = agent.estimate(content);
			
			for(RelationType rType : this.relationTypes) {
				relationStats.get(rType).update(
						ps.getSharingDecision(rType).getId(), estimation[rType.getId()]);
				//To count how many different times the estimation for the content is wrong
				if(ps.getSharingDecision(rType).getId() != estimation[rType.getId()]){
					Utils.addFalsePredicted(content.getId());
				}
			}
			updateAgentStats(content.getOwnerId(), ps, estimation);
		}
	}

	/**
	 * Updates agent's statistics based on the actual privacy setting and the estimation
	 * 
	 * @param agentId agent id
	 * @param ps privacy setting (the truth)
	 * @param estimation estimations for the sharing decision
	 */
	public void updateAgentStats(int agentId, PrivacySetting ps, int[] estimation){
		if(!agentStats.containsKey(agentId)){
			Map<RelationType, Stats> temp = new HashMap<>();
			for(RelationType rType : relationTypes) {
				Stats stats = new Stats(nDecision);
				stats.update(ps.getSharingDecision(rType).getId(), estimation[rType.getId()]);
				temp.put(rType, stats);
			}
			agentStats.put(agentId, temp);
		}
		else {
			for(RelationType rType : relationTypes) {
				agentStats.get(agentId).get(rType).update(
						ps.getSharingDecision(rType).getId(), estimation[rType.getId()]);
			}
		}
	}

	/**
	 * Returns {@code true} if relations are bidirectional
	 * @return {@code true} if relations are bidirectional
	 */
	public boolean isBidirectional() {
		return isBidirectional;
	}

	/**
	 * Removes the first content in the list of unshared contents
	 * @return a new content
	 */
	public Content shareNewContent() {
		Content content = null;
		if(!unsharedContents.isEmpty()){
			content = unsharedContents.remove(0);
		}
		return content;		
	}

	/**
	 * Adds a content to the list of unshared contents
	 * @param content
	 */
	public void addUnsharedContent(Content content) {
		unsharedContents.add(content);
	}

	/**
	 * Returns the supported relation types
	 * @return the list of relation types
	 */
	public List<RelationType> getRelationTypes() {
		return relationTypes;
	}

	/**
	 * Returns the statistics of an agent 
	 * @param agentId ID of the agent
	 * @return statistics of the given agent
	 */
	public Map<RelationType, Stats> getAgentStats(int agentId) {
		return agentStats.get(agentId);
	}

	/**
	 * Returns statistics mapped to relation types
	 * @return statistics mapped to relation types
	 */
	public Map<RelationType, Stats> getRelationStats() {
		return relationStats;
	}

	/**
	 * Returns the {@code String} version of the tag tables of all agents
	 * @return the {@code String} version of the tag tables of all agents
	 */
	public String printTagTables(){
		List<Integer> agentIds = getAgentIds();
		StringBuilder sb = new StringBuilder();
		for (int agentId : agentIds) {
			Agent agent = agents.get(agentId);
			sb.append("Agent " + agentId + "\n");
			sb.append(agent.getTagTableStr(agentId));
		}
		return sb.toString();
	}

	/**
	 * Returns {@code String} of the confusion matrix, recalls, and accuracy
	 * @return {@code String} of the confusion matrix, recalls, and accuracy
	 */
	public String printConfusion(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nInternal Estimation Statistics:");
		for(RelationType rType : relationTypes){
			sb.append("\n" + rType + relationStats.get(rType).toString());
		}
		return sb.toString();
	}
	
	/**
	 * Returns {@code String} of the trust values of all the agents 
	 * @return {@code String} of the trust values of all the agents 
	 */
	public String printTrusts() {
		StringBuilder sb = new StringBuilder();
		for(Agent agent : agents.values()){
			sb.append("Agent #" + agent.getId() + " -> " + agent.getTrusts());
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Environment [number of agents=" + agents.size() + ", number of agents who have relations=" + 
				relations.size() + ",number of contents=" + contents.size() + ", relationTypes=" + 
				relationTypes.toString() + ",number of unsharedContents=" + unsharedContents.size() + 
				", isBidirectional=" + isBidirectional + "]";
	}

}
