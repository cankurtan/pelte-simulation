package model.privacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.network.RelationType;
import utils.Utils;

/**
 * Extended version of a tag table to store
 * support value for each relation type.
 * @author cankurtan
 *
 */
public class ExternalTagTable extends TagTable {

	/**
	 * Because of the trust application, support values may vary for relation types.
	 * Therefore, support values of the tags are stored as a list of arrays
	 */
	private final List<double[]> supportValues = new ArrayList<>();
		
	public ExternalTagTable() {
		super();
	}
	
	/**
	 * Estimate a sharing decision for the given relation type by using external tag table 
	 * @param tagList tags of the content
	 * @param rTypeId relation type id that will be predicted externally
	 * @return estimated sharing decision
	 */
	public int estimateDecisionExternally(List<String> tagList, int rTypeId) {
		
		double[] confidence = calculateConfidence(tagList);
		double[] avg = getAverageConfidence();
		double confVal = confidence[rTypeId];
		double avgConf = avg[rTypeId];
		if(confVal > avgConf){
			return SharingDecision.PERMIT.getId();
		}
		return SharingDecision.DENY.getId();
	}
	
	/**
	 * Updates the table according to trust between them
	 * This will be implemented later
	 * @param tag
	 * @param decisions
	 * @param trust
	 */
	public void updateTable(String tag, int[] decisions, double[] trust) {
		if(!tags.contains(tag)){
			tags.add(tag);
			double[] tempSupportValues = Arrays.copyOf(trust, trust.length);
			double[] effectArr = new double[decisions.length];
			for (int i = 0; i < decisions.length; i++) {
				effectArr[i] = 1.0 * decisions[i] * trust[i];
			}
			supportValues.add(tempSupportValues);
			rValues.add(effectArr);
		}
		else{
			int index = tags.indexOf(tag);
			double[] tempSupports = supportValues.get(index);
			double[] temp = rValues.get(index);
			//add effects of the tag for each relationship type
			for (int i = 0; i < decisions.length; i++) {
				temp[i] += 1.0 * decisions[i] * trust[i];
				tempSupports[i] += trust[i];
			}
			supportValues.set(index, tempSupports);
			rValues.set(index, temp);
		}
	}
	
	public void updateTable(String tag, int[] decisions){
		super.updateTable(tag, decisions);
	}
	
	@Override
	protected double[] calculateConfidence(List<String> tagList){
		if(Utils.isTrustBasedLearningActive()){
			double[] effectVal = new double[RelationType.values().length];
			double[] supportVal = new double[RelationType.values().length];
			int nNotFound = 0;
			double[] avgSupports = getAverageSupports();
			double[] avgEffects = getAverageEffects();
			
			for(String str : tagList){
				//if the tag is in the tag table
				if(tags.contains(str)){
					int index = tags.indexOf(str);
					double[] rebacSupportValues = supportValues.get(index);
					double[] rebacEffectValues = rValues.get(index);
					for(int i = 0; i < RelationType.values().length; i++){
						supportVal[i] += rebacSupportValues[i];
						effectVal[i] += rebacEffectValues[i];
					}
				}
				// tag is not in the tag table
				else{
					nNotFound += 1;
				}
			}
			double[] confidence = new double[RelationType.values().length];
			//iterate over relationship types
			for (int i = 0; i < RelationType.values().length; i++) {
				confidence[i] = (effectVal[i] + avgEffects[i] * nNotFound) / 
						(supportVal[i] + avgSupports[i] * nNotFound); //action value against the relationship type
			}	
			return confidence;	
		}
		else{
			return super.calculateConfidence(tagList);
		}
	}

	
	/**
	 * Calculates average support value of the tags for each relation type
	 * @return array of average support values of the tag table
	 */
	public double[] getAverageSupports(){
		double[] supports = new double[RelationType.values().length];
		if(tags.size() > 0){
			for(int j = 0; j < RelationType.values().length; j++){
				double total = 0;
				for(int i = 0; i < tags.size(); i++){
					total += this.supportValues.get(i)[j];
				}
				supports[j] = total / tags.size();
			}
		}
		return supports;
	}
	
	@Override
	protected double[] getAverageConfidence() {
		//if trust based learning is active then every relation type will have different support values
		if(Utils.isTrustBasedLearningActive()){
			double[] avgSup = getAverageSupports();
			double[] avgConf = getAverageEffects();		
			for (int i = 0; i < avgConf.length; i++) {
				avgConf[i] = avgConf[i] / avgSup[i]; //average effect per support
			}
			return avgConf;
		}
		else {
			return super.getAverageConfidence();
		}
	}

}
