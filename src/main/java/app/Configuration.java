package app;

import java.util.Arrays;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import model.experiment.ExperimentType;

/**
 * Configuration class includes all the information required to conduct a simulation experiment.
 * 
 * @author Can Kurtan
 */
public class Configuration {
	
	/**
	 * Type of the experiment that will be conducted
	 */
	private ExperimentType expType;
	/**
	 * file of the nodes in the network
	 * each line is starts with node id and continues with its features
	 */
	private String features;
	/**
	 * file of the edges in the network
	 * each line has two node ids that have a relation in between
	 */
	private String edges;
	/**
	 * name of the training file
	 */
	private String trainingFile;
	/**
	 * name of the test file
	 */
	private String testFile;
	/**
	 * name of the file that has tags
	 */
	private String tagFile;
	/**
	 * number of simulations
	 */
	private int numOfSims;
	/**
	 * array of the training sizes 
	 * if experiment will be done only for the same size, then only array with only one element
	 */
	private int[] trainingSizes;
	/**
	 * array of the test sizes
	 */
	private int[] testSizes;
	/**
	 * threshold for the external learning
	 */
	private double[] threshold;
	
	/**
	 * ID of the agent that will be evaluated as a newcomer
	 */
	private int newcomer;
	
	/**
	 * The number of content will be distributed before the newcomer
	 */
	private int newcomerTurn;
	
	/**
	 * Number of tags will be used in the simulations even if there are more tags
	 */
	private int[] tagNumbers;
	
	private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

	/**
	 * JSON based object creator
	 * @param expType experiment type
	 * @param features nodes and their features
	 * @param edges edges between notes
	 * @param trainingFile training file for the experiment
	 * @param testFile test file for the experiment
	 * @param tagFile file that has tags for the experiment
	 * @param numOfSims number of simulations
	 * @param trainingSizes array of training sizes for the simulations
	 * @param testSize test size
	 * @param threshold threshold values for undecidable internal estimation state
	 * @param newcomer the agent that will join the network in the middle
	 * @param newcomerTurn the turn that the newcomer agent will join
	 * @param tagNumbers number of tags will be evaluated
	 */
	@JsonCreator
	public Configuration(@JsonProperty(value = "expType", required = true) ExperimentType expType,
			@JsonProperty(value = "features", required = true) String features, 
			@JsonProperty(value = "edges", required = true) String edges, 
			@JsonProperty(value = "trainingFile", required = true) String trainingFile, 
			@JsonProperty(value = "testFile", required = true) String testFile,
			@JsonProperty(value = "tagFile", required = false) String tagFile,
			@JsonProperty(value = "numOfSims", required = true) int numOfSims, 
			@JsonProperty(value = "trainingSizes", required = true) int[] trainingSizes, 
			@JsonProperty(value = "testSizes", required = true) int[] testSizes,
			@JsonProperty(value = "threshold", required = false) double[] threshold,
			@JsonProperty(value = "newcomer", required = false) int newcomer,
			@JsonProperty(value = "newcomerTurn", required = false) int newcomerTurn,
			@JsonProperty(value = "tagNumbers", required = false) int[] tagNumbers) {
		super();
		this.expType = expType;
		this.features = features;
		this.edges = edges;
		this.trainingFile = trainingFile;
		this.testFile = testFile;
		this.tagFile = tagFile;
		this.numOfSims = numOfSims;
		this.trainingSizes = trainingSizes;
		this.testSizes = testSizes;
		this.threshold = threshold;
		this.newcomer = newcomer;
		this.newcomerTurn = newcomerTurn;
		this.tagNumbers = tagNumbers;
	}
	
	//TODO Experiment type based field check functions
	public boolean validate() {
		
		switch (expType) {
		case INTERNAL:
			
			break;
		case EXTERNAL:
			if (threshold.length > 0) {
				LOGGER.info("External Estimation experiment will be "
						+ "conducted for threshold:" + Arrays.toString(this.threshold));
			}
			else {
				LOGGER.severe("Threshold data have not been provided. "
						+ "External Estimation experiment cannot be conducted!");
				return false;
			}
			break;
		case SINGLE_AGENT:
			if (this.newcomer != 0 && newcomerTurn != 0) {
				LOGGER.info("Single Agent experiment will be conducted for Agent ID:" +
						this.newcomer + " after " + this.newcomerTurn + "turns");
			}
			else {
				LOGGER.severe("Either newcomer or newcomerTurn has not been provided. "
						+ "Single Agent experiment cannot be conducted!");
				return false;
			}
			break;
		case TAG:
			if(tagNumbers != null) {
				LOGGER.info("Tag number experiment will be conducted for " +
						Arrays.toString(tagNumbers));
			}
			else {
				LOGGER.severe("Array of tag numbers has not been provided. "
						+ "Tag number experiment cannot be conducted!");
			}
			break;
		case TRUST:
			break;

		default:
			return true;
		}
		return true;
	}
	
	public ExperimentType getExpType() {
		return expType;
	}

	public void setExpType(ExperimentType expType) {
		this.expType = expType;
	}

	public String getFeatures() {
		return features;
	}

	public void setFeatures(String features) {
		this.features = features;
	}

	public String getEdges() {
		return edges;
	}

	public void setEdges(String edges) {
		this.edges = edges;
	}

	public String getTrainingFile() {
		return trainingFile;
	}

	public void setTrainingFile(String trainingFile) {
		this.trainingFile = trainingFile;
	}

	public String getTestFile() {
		return testFile;
	}

	public void setTestFile(String testFile) {
		this.testFile = testFile;
	}

	public int getNumOfSims() {
		return numOfSims;
	}

	public void setNumOfSims(int numOfSims) {
		this.numOfSims = numOfSims;
	}

	public int[] getTrainingSizes() {
		return trainingSizes;
	}
	
	/**
	 * If only one size is requested, then either the array has the length of 1 or the first element is expected
	 * @return first element of the training sizes array
	 */
	public int getTrainingSize() {
		return trainingSizes[0];
	}

	public void setTrainingSizes(int[] trainingSizes) {
		this.trainingSizes = trainingSizes;
	}

	public int getTestSize() {
		return testSizes[0];
	}
	
	public int[] getTestSizes() {
		return testSizes;
	}

	public void setTestSize(int[] testSizes) {
		this.testSizes = testSizes;
	}

	public double[] getThreshold() {
		return threshold;
	}

	public void setThreshold(double[] threshold) {
		this.threshold = threshold;
	}

	public int getNewcomer() {
		return newcomer;
	}

	public void setNewcomer(int newcomer) {
		this.newcomer = newcomer;
	}

	public int getNewcomerTurn() {
		return newcomerTurn;
	}

	public void setNewcomerTurn(int newcomerTurn) {
		this.newcomerTurn = newcomerTurn;
	}

	public int[] getTagNumbers() {
		return tagNumbers;
	}

	public void setTagNumbers(int[] tagNumbers) {
		this.tagNumbers = tagNumbers;
	}

	public String getTagFile() {
		return tagFile;
	}

	public void setTagFile(String tagFile) {
		this.tagFile = tagFile;
	}

	@Override
	public String toString() {
		return "Configuration [features=" + features + ", edges=" + edges + ", trainingFile="
				+ trainingFile + ", testFile=" + testFile + ", numOfSims=" + numOfSims + ", trainingSizes="
				+ Arrays.toString(trainingSizes) + ", testSize=" + Arrays.toString(testSizes)
				+ ", threshold=" +  Arrays.toString(threshold) + "]";
	}
	
}
