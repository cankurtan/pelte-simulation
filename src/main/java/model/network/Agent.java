package model.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import model.experiment.Stats;
import model.privacy.AgentCharacter;
import model.privacy.ExternalTagTable;
import model.privacy.PrivacySetting;
import model.privacy.SharingDecision;
import model.privacy.TagTable;
import model.privacy.Trust;
import utils.Utils;

public class Agent {

	/**
	 * agent id
	 */
	private int id;
	/**
	 * list of agents' own shared contents
	 */
	private final List<Long> contentIds = new ArrayList<>();
	/**
	 * list of contents that are visible by the agents
	 */
	private final List<Long> visibleContentIds = new ArrayList<>();
	/**
	 * agent name
	 */
	private String name;
	/**
	 * map of agent's relationships to other agents
	 */
	private final Map<Integer, Relation> relationMap = new LinkedHashMap<>();	
	/**
	 * agent character
	 */
	private AgentCharacter agentChar;
	/**
	 * Internal tag table of the agent
	 */
	private final TagTable tagTable = new TagTable();
	/**
	 * External tag table of the agent
	 */
	private final ExternalTagTable extTagTable = new ExternalTagTable();
	/**
	 * Map of trusts towards other agents that the agent has a relation
	 */
	private final Map<Integer, Trust> trusts = new LinkedHashMap<>();
	/**
	 * Statistics of internal estimation mapped to relation type
	 */
	private final Map<RelationType, Stats> internalStats = new HashMap<>();
	/**
	 * Statistics of external estimation mapped to relation type
	 */
	private final Map<RelationType, Stats> externalStats = new HashMap<>();

	/**
	 * Basic Agent constructor
	 * 
	 * @param id agent's id
	 * @param name agent's name
	 */
	public Agent(int id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.agentChar = AgentCharacter.NORMAL;
		initStats(SharingDecision.values().length);
	}

	/**
	 * Agent with character
	 * @param id agent's id
	 * @param name agent's name
	 * @param character agent's character
	 */
	public Agent(int id, String name, AgentCharacter character) {
		this(id, name);
		this.agentChar = character;
	}

	/**
	 * Complete Agent constructor
	 * 
	 * @param id agent's id
	 * @param contentIds agent's contents
	 * @param name agent's name
	 * @param relationIds agent's relations
	 * @param agentChar agent's character
	 */
	public Agent(int id, List<Long> contentIds, String name, 
			Map<Integer, Relation> relationMap, AgentCharacter agentChar) {
		this(id, name);
		this.contentIds.addAll(contentIds);
		this.relationMap.putAll(relationMap);
		this.setAgentChar(agentChar);
	}

	public List<Long> getContentIds() {
		return contentIds;
	}

	/**
	 * adds a new content to the agent's contents
	 * @param contentId
	 */
	public void addContent(Content content) {
		contentIds.add(content.getId());
		//to open/close learning while prediction is active use this condition
		if(!Utils.isPredictionActive() || Utils.isLearningActive()){
			updateTagTable(content);
		}
	}

	public Map<Integer,Relation> getRelationMap() {
		return relationMap;
	}

	/**
	 * Adds a new relation to the agent's relations
	 * 
	 * @param relationId id of the relation
	 */
	public void addRelation(Relation relation) {
		relationMap.put(relation.getDestination(), relation);
		trusts.put(relation.getDestination(), new Trust());
	}

	/**
	 * updates agent's tag table
	 * @param content agent's own shared content
	 */
	protected void updateTagTable(Content content) {
		List<String> tags = content.getTags();	
		int[] effects = content.getPrivacySetting().getArrayOfSetting();
		for(String tag : tags){
			tagTable.updateTable(tag, effects);
		}
	}

	/**
	 * Updates the external tag table of the agent
	 * @param content a content that is shared by another agent that the agent has a relation
	 */
	private void updateExternalTagTable(Content content) {		
		List<String> tags = content.getTags();	
		int[] effects = content.getPrivacySetting().getArrayOfSetting();
		//if the trust is active, then update according to trust values
		if(Utils.isTrustBasedLearningActive()){
			double[] trust = trusts.get(content.getOwnerId()).getValue();
			for(String tag : tags){
				extTagTable.updateTable(tag, effects, trust);
			}
		}
		else{
			for(String tag : tags){
				extTagTable.updateTable(tag, effects);
			}
		}
	}

	public void addVisibleContent(Content content) {
		visibleContentIds.add(content.getId());
		//to open/close learning while prediction is active use this condition
		if(!Utils.isPredictionActive() || Utils.isLearningActive()){
			updateExternalTagTable(content);
		}
		updateTrust(content);
	}

	private void updateTrust(Content content) {
		int[] imageOwnerAction = content.getPrivacySetting().getArrayOfSetting();
		int[] trustOwnerAction = tagTable.getEstimatedDecisions(content.getTags());
		Trust trust = trusts.get(content.getOwnerId());
		trust.updateTrust(trustOwnerAction, imageOwnerAction);
	}

	public String getTagTableStr(int agentId){
		return tagTable.toString();
	}
	/**
	 * Agent estimates the privacy setting of a given content
	 * @param content
	 * @return
	 */
	public int[] estimate(Content content) {
		int[] estimation = tagTable.getEstimatedDecisions(content.getTags());
		return estimation;
	}

	/**
	 * Estimates externally a sharing decision of the content for given relation type
	 * 
	 * @param content shared content
	 * @param rType relation type that will be predicted
	 * @return estimated sharing decision 
	 */
	public int estimateExternallyForRelation(Content content, RelationType rType) {
		return extTagTable.estimateDecisionExternally(content.getTags(), rType.getId());
	}

	/**
	 * Initializes both internal and external statistics
	 * 
	 * @param nDecisions number of decisions that can be produced
	 */
	private void initStats(int nDecisions) {
		//TODO Agent should get relation types from the environment instead of class itself 
		for(RelationType rType : RelationType.values()) {
			internalStats.put(rType, new Stats(nDecisions));
			externalStats.put(rType, new Stats(nDecisions));
		}
	}

	public AgentCharacter getAgentChar() {
		return agentChar;
	}

	public void setAgentChar(AgentCharacter agentChar) {
		this.agentChar = agentChar;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public TagTable getTagTable() {
		return tagTable;
	}

	public List<Long> getVisibleContentIds() {
		return visibleContentIds;
	}

	public TagTable getExternalTagTable() {
		return extTagTable;
	}

	public Stats getInternalStats(RelationType rType) {
		return internalStats.get(rType);
	}

	public void updateInternalStats(PrivacySetting ps, int[] estimation) {
		ps.getRebac().forEach((k,v) -> 
			internalStats.get(k).update(v.getId(), estimation[k.getId()]));
	}

	public Stats getExternalStats(RelationType rType) {
		return externalStats.get(rType);
	}

	public void updateExternalConfusion(PrivacySetting ps, int[] estimation) {
		ps.getRebac().forEach((k,v) -> 
			externalStats.get(k).update(v.getId(), estimation[k.getId()]));
	}

	public void changeDecision(PrivacySetting sp){
		int[] decisions = sp.getArrayOfSetting();
		for (int i = 0; i < decisions.length; i++) {
			if(getAgentChar() == AgentCharacter.OPPOSITE){
				decisions[i] = (decisions[i] + 1) % 2;
			}
			else if(getAgentChar() == AgentCharacter.RANDOM){
				decisions[i] = (int) (Math.random()*2);
			}
			else if(getAgentChar() == AgentCharacter.PERMIT){
				decisions[i] = 1;
			}
		}
		sp.setDecisions(decisions);
	}

	public String getTrusts() {
		StringBuilder sb = new StringBuilder();
		if(trusts != null && !trusts.isEmpty()){
			for(int agentId : trusts.keySet()){
				sb.append(agentId + ": ");
				double[] trustValues = trusts.get(agentId).getValue();
				for (int i = 0; i < trustValues.length; i++) {
					sb.append(Utils.formatDouble(trustValues[i]) + ", ");
				}
			}
		}
		return sb.toString();
	}

}
