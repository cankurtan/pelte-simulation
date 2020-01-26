package model.privacy;

import java.util.Arrays;

import model.network.RelationType;
import utils.Utils;

/**
 * Trust class stores the statistical information of 
 * image sharing experience and calculates the trust
 * based on the experience
 * 
 * @author cankurtan
 *
 */
public class Trust {
	
	/** 
	 * the first row is the number of images would be shared with the same decision (the agent is agree), 
	 * the second row is the number of images would be shared with different sharing decision (the agent is disagree)
	 */
	private int[][] imageStats;
	
	private double[] value;
	
	public Trust() {
		value = new double[RelationType.values().length];
		//TODO Why 0.5 instead of 1?
		Arrays.fill(value, 0.5);
		imageStats = new int[2][RelationType.values().length];
	}
	
	/**
	 * Updates the image states by comparing the sharing decision of the content owner and
	 * the possible sharing action of the trust owner.
	 * @param trustOwnerAction trust owner's most probable action for the image
	 * @param imageOwnerAction actual sharing action defined by the image owner
	 * @param i index of the relation type
	 */
	public void updateForRelation(int trustOwnerAction, int imageOwnerAction, int i){	
		if(trustOwnerAction == imageOwnerAction){
			imageStats[0][i]++;
		}
		else if((trustOwnerAction != imageOwnerAction) && 
				(trustOwnerAction != Utils.INTERNALLY_UNDECIDABLE_STATE)){
			imageStats[1][i]++;
		}
		else {
			//TODO In the case of internally undecidable state, there should be another operation. 
		}
	}
	
	/**
	 * more than one relationship types are supported
	 * @param trustOwnerAction trust owner's most probable action for the image
	 * @param imageOwnerAction actual sharing action defined by the image owner
	 */
	public void updateTrust(int[] trustOwnerAction, int[] imageOwnerAction){	
		for(int i = 0; i < trustOwnerAction.length; i++){
			updateForRelation(trustOwnerAction[i], imageOwnerAction[i], i);
		}
	}
	
	/**
	 * Whenever trust value is requested, its value will be updated
	 * according to shared image stats.
	 * @return the array of trust values corresponding each relation type
	 */
	public double[] getValue() {
		for(int i = 0; i < value.length; i++){
			int total = imageStats[0][i] + imageStats[1][i];
			if(total > 0 ) {
				value[i] = 1.0 * imageStats[0][i] / total;
			}
		}
		return value;
	}	

}
