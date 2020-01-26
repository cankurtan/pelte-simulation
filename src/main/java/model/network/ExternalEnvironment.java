package model.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.experiment.Stats;
import model.privacy.AgentCharacter;
import model.privacy.PrivacySetting;
import model.privacy.SharingDecision;
import utils.Utils;

public class ExternalEnvironment extends Environment {

	/**
	 * Statistics of external estimation, mapped to relation types 
	 */
	protected final Map<RelationType, Stats> externalStats = new HashMap<>();

	public ExternalEnvironment(List<RelationType> relationTypes) {
		super(relationTypes);
		for(RelationType rType : relationTypes) {
			externalStats.put(rType, new Stats(super.nDecision));
		}
	}
	
	public ExternalEnvironment(List<RelationType> relationTypes, boolean isBidirectional) {
		super(relationTypes, isBidirectional);
		for(RelationType rType : relationTypes) {
			externalStats.put(rType, new Stats(super.nDecision));
		}
	}

	@Override
	public void addContent(Content content) {
		contents.put(content.getId(), content);
		Agent agent = agents.get(content.getOwnerId());
		agent.addContent(content);
		Map<Integer, Relation> userRelations = relations.get(content.getOwnerId());
		//sends the shared content to other agents that have relationship
		if(userRelations != null){
			for(int id : userRelations.keySet()){
				/*
				 * In case of ReBAC is used and more than one relation type,
				 * If the relation type between agents is permitted, then the agent can view the content
				 */
				if(RelationType.values().length > 1) {
					if(content.getPrivacySetting().getSharingDecision(userRelations.get(id).getType()) == SharingDecision.PERMIT){
						agents.get(id).addVisibleContent(content);
					}
				}
				/*
				 * If there is only one relation type, privacy decision is considered as public or not
				 * Therefore, all the agents having a relation can see each others all contents
				 */
				else {
					agents.get(id).addVisibleContent(content);
				}
			}
		}
		if(Utils.isPredictionActive() && agent.getAgentChar() == AgentCharacter.NORMAL){
			PrivacySetting ps = content.getPrivacySetting();
			int[] estimation = agent.estimate(content);
			for(RelationType rType : getRelationTypes()){
				//if the estimation is internally decidable, assume that will be the action
				if(estimation[rType.getId()] != Utils.INTERNALLY_UNDECIDABLE_STATE){
					relationStats.get(rType).update(ps.getSharingDecision(rType).getId(), estimation[rType.getId()]);
					agent.updateInternalStats(ps, estimation);
				}
				//if it is internally undecidable, get an estimation from external
				else{
					estimation[rType.getId()] = agent.estimateExternallyForRelation(content, rType);
					//if the external action is not undecidable state, take that action
					if(estimation[rType.getId()] != Utils.EXTERNALLY_UNDECIDABLE_STATE){
						externalStats.get(rType).update(ps.getSharingDecision(rType).getId(), estimation[rType.getId()]);
					}
					else{
						// TODO This part will be implemented
						System.out.println("Externally undecidable");
					}
				}
				if(ps.getSharingDecision(rType).getId() != estimation[rType.getId()]){
					Utils.addFalsePredicted(content.getId());
				}
			}
			updateAgentStats(content.getOwnerId(), ps, estimation);
		}
	}

	public Map<RelationType, Stats> getExternalStats() {
		return externalStats;
	}

	@Override
	public String printConfusion(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nExternal Estimation Statistics: ");
		for(RelationType rType : getRelationTypes()){
			sb.append("\n" + rType + externalStats.get(rType).toString());
		}
		sb.append("\n" + super.printConfusion());
		return sb.toString();
	}

}
