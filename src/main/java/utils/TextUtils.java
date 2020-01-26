package utils;

import java.time.format.DateTimeFormatter;

public class TextUtils {
	
	public static final DateTimeFormatter FORMATTER = 
			DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
	
	public static final String[] HEADERS = 
			{"Training Size", "Test size", "Threshold", "nTags", 
			"# Sims", "Relation Type", "Private Ratio", "Private Recall", 
			"Public Recall", "Accuracy", "int/all"};
	
	//ERROR MESSAGES
	/* Configuration loading error */
	public static final String CONFIG_ERROR = "Configuration could not be loaded!";
}
