package model.experiment;

import java.util.Arrays;
import java.util.List;

import utils.Utils;

public class Stats {
	
	/**
	 * Confusion matrix
	 */
	private int[][] confusion;
	/**
	 * Private Recall
	 */
	private double priRecall;
	/**
	 * Public Recall
	 */
	private double pubRecall;
	/**
	 * Accuracy 
	 */
	private double accuracy;
	/**
	 * Private Ration
	 */
	private double priRatio;
	/**
	 * List of metric names
	 */
	private final List<String> METRIC_NAMES = Arrays.asList("Private Ratio", "Private Recall", "Public Recall", "Accuracy");

	/**
	 * Default constructor
	 * @param ndecision the number of possible sharing decisions
	 */
	public Stats(int ndecision) {
		this.confusion = new int[ndecision][ndecision];
	}
	
	/**
	 * Updates the confusion matrix
	 * @param decision sharing decision
	 * @param estimation sharing estimation
	 */
	public void update(int decision, int estimation) {
		confusion[decision][estimation]++;
		
	}
	
	/**
	 * Adds given confusion to the confusion it has
	 * @param confusion 2D confusion array
	 */
	public void addConfusion (int[][] confusion) {
		this.confusion = Utils.sum2DArrays(this.confusion, confusion);
	}

	/**
	 * Calculates statistics of the confusion matrix
	 */
	public void calculate() {
		getPriRecall();
		getPubRecall();
		getAccuracy();
		getPriRatio();
	}

	/**
	 * Returns 2D confusion array
	 * @return 2D array
	 */
	public int[][] getConfusion() {
		return confusion;
	}

	/**
	 * Calculates private recall, which is the ratio of correct private (deny) estimations 
	 * to the total number of actual private decisions
	 * @return private recall value
	 */
	public double getPriRecall() {
		this.priRecall = 1.0 * confusion[0][0] / (confusion[0][0] + confusion[0][1]);
		return this.priRecall;
	}

	/**
	 * Calculates public recall, which is the ratio of correct public (permit) estimations 
	 * to the total number of actual public decisions
	 * @return public recall value
	 */
	public double getPubRecall() {
		this.pubRecall = 1.0 * confusion[1][1] / (confusion[1][1] + confusion[1][0]);
		return this.pubRecall;
	}

	/**
	 * Calculates accuracy value, which is the ratio of correct estimations 
	 * to the total number of decisions
	 * @return accuracy value
	 */
	public double getAccuracy() {
		this.accuracy = 1.0 * (confusion[0][0] + confusion[1][1]) / (confusion[0][0] + confusion[0][1] 
				+ confusion[1][0] + confusion[1][1]);
		return this.accuracy;
	}

	/**
	 * Calculates private decision ratio, which is the ratio of private decisions 
	 * to the total number of decisions
	 * @return private ratio value
	 */
	public double getPriRatio() {
		this.priRatio = 1.0 * (confusion[0][0] + confusion[0][1]) / (confusion[0][0] + confusion[0][1]
				+ confusion[1][0] + confusion[1][1]);
		return this.priRatio;
	}
	
	/**
	 * Calculates the size of the confusion matrix.
	 * This equals to the total number of estimations.
	 * @return the size of the confusion matrix
	 */
	public int getConfusionSize() {
		int size = 0;
		for(int[] row : confusion) {
			for(int cell : row) {
				size += cell;
			}
		}
		return size;
	}
	
	/**
	 * Returns the list of metric names
	 * @return the list of metric names
	 */
	public List<String> getMetricNames() {
		return this.METRIC_NAMES;
	}
	
	/**
	 * Returns a list of all the calculated metrics in the following order:
	 * private ratio, private recall, public recall, accuracy
	 * @return a list of the calculated metrics
	 */
	public List<String> getMetricsAsRow() {
		List<String> row = Arrays.asList(Utils.formatDouble(getPriRatio()),Utils.formatDouble(getPriRecall()),
				Utils.formatDouble(getPubRecall()), Utils.formatDouble(getAccuracy()));
		return row;
	}

	@Override
	public String toString() {
		calculate();
		return "\nConfusion Matrix:\nPredicted\n" + Arrays.toString(getConfusion()[0]) + 
				"\n" + Arrays.toString(getConfusion()[1]) + 
				"\nPrivate Ratio: " + Utils.formatDouble(getPriRatio()) + 
				"\nPrivate Recall: " + Utils.formatDouble(getPriRecall()) +
				"\nPublic Recall: " + Utils.formatDouble(getPubRecall()) + 
				"\nAccuracy: " +  Utils.formatDouble(getAccuracy());
	}
	
}
