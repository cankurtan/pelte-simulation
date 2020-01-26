package model.privacy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import model.network.RelationType;
import utils.Utils;

/**
 * Tag table stores the tags and their indicated privacy values.
 * @author cankurtan
 *
 */
public class TagTable {

	/**
	 * List of tags
	 */
	protected final List<String> tags = new ArrayList<>();
	/**
	 * List of the effect values for each relation type
	 */
	protected final List<double[]> rValues = new ArrayList<>();

	/**
	 * List of the support values of each tags
	 */
	private final List<Double> supportValues = new ArrayList<>();

	/**
	 * Empty constructor
	 */
	public TagTable() {}

	/**
	 * Updates tag table.
	 * If the tag is already stored in the table, the only updates values.
	 * Otherwise adds the tag to the table and initiates the support and effect values.
	 * 
	 * @param tag tag name 
	 * @param decisions effect of the policy
	 */
	public void updateTable(String tag, int[] decisions){
		if(!tags.contains(tag)){
			tags.add(tag);
			supportValues.add(1.0);
			double[] temp = new double[decisions.length];
			for (int i = 0; i < decisions.length; i++) {
				temp[i] = 1.0 * decisions[i];
			}
			rValues.add(temp);
		}
		else{
			//find the index of the given tag
			final int index = tags.indexOf(tag);
			//increase the support value of the tag by one
			supportValues.set(index, supportValues.get(index) + 1);
			//get effect values of the tag
			double[] temp = rValues.get(index);
			//add new decisions for each relation type
			for (int i = 0; i < decisions.length; i++) {
				temp[i] += decisions[i];
			}
		}
	}

	/**
	 * Estimates the sharing action
	 * @param tagList list of the tags of the content
	 * @return predicted action value
	 */
	public int[] getEstimatedDecisions(List<String> tagList){
		double[] confidence = calculateConfidence(tagList);
		double[] avg = getAverageConfidence();
		int[] estimations = new int[RelationType.values().length];
		for(int i = 0 ; i < RelationType.values().length; i++){
			double confValue = confidence[i];
			double avgConf = avg[i];
			//This part is for internally undecidable state
			if(Utils.isExternalEnabled() && (confValue >= avgConf - Utils.getInternalThreshold())
					&& (confValue <= avgConf + Utils.getInternalThreshold())){
				estimations[i] = Utils.INTERNALLY_UNDECIDABLE_STATE;
			}
			else if(confValue > avgConf){
				estimations[i] = SharingDecision.PERMIT.getId();
			}
			else{
				estimations[i] = SharingDecision.DENY.getId();
			}
		}
		return estimations;
	}


	/**
	 * Calculates average support value of the tags
	 * @return average support value of the tag table
	 */
	protected double getAverageSupport(){
		double avgSupport = 0;
		if(supportValues.size() > 0){
			double total = 0;
			for(int i = 0; i < tags.size(); i++){
				total += supportValues.get(i);
			}
			avgSupport = total / tags.size();
		}
		return avgSupport;
	}

	/**
	 * Calculates average effect value of the tags for each relation type
	 * @return array of average effects
	 */
	public double[] getAverageEffects(){
		double[] avg = new double[RelationType.values().length];
		if(tags.size() > 0){
			for(int i = 0; i < tags.size(); i++){
				for (int j = 0; j < avg.length; j++) {
					avg[j] += rValues.get(i)[j];
				}
			}
			for (int j = 0; j < avg.length; j++) {
				avg[j] /= tags.size();
			}
		}
		return avg;
	}

	/**
	 * Calculates observed privacy value for each relation type.
	 * That is the average effect per support value of each relation type.
	 * @return aobserved privacy values in the order of relation types
	 */
	protected double[] getAverageConfidence() {
		double avgSup = getAverageSupport();
		double[] avgConf = getAverageEffects();
		if(avgSup > 0) {
			for (int i = 0; i < avgConf.length; i++) {
				avgConf[i] /= avgSup; //average effect per support
			}
		}
		return avgConf;
	}

	/**
	 * Calculates privacy value indicator for each relation type.
	 * @param tagList tags of the content
	 * @return privacy values in the order of relationship types
	 */
	protected double[] calculateConfidence(List<String> tagList){
		double[] effectVal = new double[RelationType.values().length];
		double supVal = 0;
		int nNotFound = 0;
		double avgSup = getAverageSupport();
		double[] avgEffects = getAverageEffects();

		for(String str : tagList) {
			//if the tag is in the tag table
			if(tags.contains(str)) {
				int index = tags.indexOf(str);
				supVal += supportValues.get(index);
				double[] rebacValues = rValues.get(index);
				for(int i = 0; i < RelationType.values().length; i++){
					effectVal[i] += rebacValues[i];
				}
			}
			// tag is not in the tag table
			else {
				nNotFound += 1;
			}
		}
		double[] confidence = new double[RelationType.values().length];
		if(avgSup > 0) {
			//iterate over relationship types
			for (int i = 0; i < RelationType.values().length; i++) {
				/* calculates confidence value for the relationship type 
				 * average values are used for the tags not in the tag table
				 */
				confidence[i] = (effectVal[i] + avgEffects[i] * nNotFound) / 
						(supVal + avgSup * nNotFound); 
			}
		}
		return confidence;
	}

	/**
	 * Returns the number of occurrences of the tag
	 * @param tag tag
	 * @return the number of occurrences of the tag
	 */
	public double getSupportOfTag(String tag){
		double support = 0;
		if(tags.contains(tag)){
			int index = tags.indexOf(tag);
			support = supportValues.get(index);
		}
		return support;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Tag name, Support Value, Effect Values:" );
		RelationType[] rTypes = RelationType.values();
		double totalSupport = 0;
		double[] totalEffect = new double[rTypes.length];
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		for (int i = 0; i < rTypes.length; i++) {
			sb.append(rTypes[i] + " ");
		}
		sb.append("\n");
		for(int i = 0; i < tags.size(); i++){
			sb.append(tags.get(i) + ", " + supportValues.get(i));
			for (int j = 0; j < rTypes.length; j++) {
				sb.append(", " + rValues.get(i)[j]);
				totalEffect[j] += rValues.get(i)[j];
			}
			sb.append("\n");
			totalSupport += supportValues.get(i);
		}	
		sb.append("Average, " + df.format(totalSupport / 
				tags.size()).replace(",", "."));
		for (int i = 0; i < rTypes.length; i++) {
			sb.append(", " + df.format(totalEffect[i] / 
					tags.size()).replace(",", "."));
		}
		sb.append("\n");
		return sb.toString();
	}

}
