package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
	/**
	 * A counter to produce agent id systematically
	 */
	private static int agentId = 0;
	/**
	 * A counter to produce content id systematically
	 */
	private static int contentId = 0;
	/**
	 * A counter to produce relation id systematically
	 */
	private static int relationId = 0;
	private static double internalThreshold = 0;
	private static boolean isPredictionActive = false;
	private static boolean isLearningActive = false;
	private static boolean isTrustBasedLearningActive = false;

	private static Map<Long, Integer> falsePredictions = new HashMap<Long, Integer>();
	private static List<String> forbiddenTags = new ArrayList<String>(
			Arrays.asList("people","one","two","three","four","five"));
	public static final int INTERNALLY_UNDECIDABLE_STATE = -1;
	public static final int EXTERNALLY_UNDECIDABLE_STATE = -1;
	public static final int STATS_SIZE = 4;

	/**
	 * 
	 * @return unique agent id for the environment
	 */
	public static int getAgentId(){
		agentId++;
		return agentId;
	}
	/**
	 * 
	 * @return unique content id for the environment
	 */
	public static int getContentId(){
		contentId++;
		return contentId;
	}
	/**
	 * 
	 * @return unique relation id for the environment
	 */
	public static int getRelationId(){
		relationId++;
		return relationId;
	}

	public static String getInfo(){
		return "agent number:" + agentId + " content number:" + contentId + 
				" relation number:" + relationId;
	}
	/**
	 * Checks whether a tag is in the forbidden list
	 * @param tag string of content tag
	 * @return true if it is in the list, false otherwise
	 */
	public static boolean isForbiddenTag(String tag) {
		return forbiddenTags.contains(tag);
	}

	public static String formatFloat(float floatValue){
		return String.format("%.2f", floatValue);
	}
	
	public static String formatDouble(double value){
		return String.format("%.2f", value);
	}

	public static boolean isExternalEnabled() {
		return internalThreshold != 0;
	}

	public static double getInternalThreshold() {
		return internalThreshold;
	}

	public static void setInternalThreshold(double threshold) {
		internalThreshold = threshold;
	}

	public static boolean isPredictionActive() {
		return isPredictionActive;
	}
	public static void activatePrediction() {
		Utils.isPredictionActive = true;
	}
	public static void deactivatePrediction(){
		Utils.isPredictionActive = false;
	}	
	public static boolean isLearningActive() {
		return isLearningActive;
	}
	public static void setLearningActive(boolean active) {
		isLearningActive = active;
	}

	public static boolean isTrustBasedLearningActive() {
		return isTrustBasedLearningActive;
	}
	
	public static void activateTrustBasedLearning() {
		isTrustBasedLearningActive = true;
	}

	public static void addFalsePredicted(Long id) {
		if(falsePredictions.containsKey(id)){
			falsePredictions.put(id, falsePredictions.get(id)+1);
		}
		else{
			falsePredictions.put(id, 1);
		}
	}
	
	public static void printFalsePredictions(){
		List<Long> fpList = new ArrayList<>(falsePredictions.keySet());

		Collections.sort(fpList, new Comparator<Long>() {
			@Override
			public int compare(Long arg0, Long arg1) {
				return falsePredictions.get(arg1)
						.compareTo(falsePredictions.get(arg0));
			}
		});
		for (int i = 0; i < fpList.size(); i++) {
			System.out.println(fpList.get(i) + " " + falsePredictions.get(fpList.get(i)));
		}
	}

	public static float[] sumArrays(float[] a, float[] b){
		float[] sum = new float[a.length];
		for (int i = 0; i < sum.length; i++) {
			sum[i] = a[i] + b[i];
		}
		return sum;
	}

	public static float[][] sum2DArrays(float[][] a, float[][] b){
		float[][] sum = new float[a.length][a[0].length];
		for (int i = 0; i < sum.length; i++) {
			for (int j = 0; j < sum[0].length; j++) {
				sum[i][j] = a[i][j] + b[i][j];
			}
		}
		return sum;
	}

	public static float[][] divideArray(float[][] a, float b){
		float[][] result = new float[a.length][a[0].length];
		float shift = 100;
		for (int i = 0; i < result.length; i++) {
			for(int j = 0; j < result[0].length; j++){
				result[i][j] = Math.round((a[i][j] / b) * shift) / shift; //precision value is 2
			}
		}
		return result;
	}

	public static int[][] sum2DArrays(int[][] a, int[][] b){
		int[][] sum = new int[a[0].length][a.length];
		for (int i = 0; i < sum[0].length; i++) {
			for (int j = 0; j < sum.length; j++) {
				sum[i][j] = a[i][j] + b[i][j];
			}
		}
		return sum;
	}

	public static String arrayToString(int[][] a) {
		StringBuilder sb = new StringBuilder();
		String lineSeparator = System.lineSeparator();
		for (int[] row : a) {
			sb.append(Arrays.toString(row)).append(lineSeparator);
		}
		return sb.toString();
	}
	
	/**
	 * Prepares a string from list of lists as a formatted table.
	 * @param rows list of lists, which are rows of the table 
	 * @return formatted string
	 */
	public static String formatAsTable(List<List<String>> rows){
	    int[] maxLengths = new int[rows.get(0).size()];
	    for (List<String> row : rows) {
	        for (int i = 0; i < row.size(); i++)
	        {
	            maxLengths[i] = Math.max(maxLengths[i], row.get(i).length());
	        }
	    }

	    StringBuilder formatBuilder = new StringBuilder();
	    for (int maxLength : maxLengths) {
	        formatBuilder.append("%-").append(maxLength + 2).append("s");
	    }
	    String format = formatBuilder.toString();

	    StringBuilder result = new StringBuilder();
	    for (List<String> row : rows){
	        result.append(String.format(format, row.toArray(new Object[0]))).append("\n");
	    }
	    return result.toString();
	}
}
