package app;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.evaluation.*;
import model.loader.BulkContentLoader;
import utils.TextUtils;
import utils.Utils;

public class App {
	
	private static final Logger LOGGER = Logger.getLogger(App.class.getName());
	private static final String LOG_FILE = "logs.txt";
	private static final File CONFIG_FILE = new File("src\\main\\resources\\config.json");

	public static void main(String[] args) {
		
		setupLogger();
		Configuration simConfig = loadConfigFromFile();
		if(simConfig == null || !simConfig.validate()) {
			LOGGER.log(Level.SEVERE, TextUtils.CONFIG_ERROR);
			System.exit(0);
		}
		conductExperiment(simConfig);
	}
	
	/**
	 * Loads configuration from a file and creates the configuration object
	 * @return loaded configuration
	 */
	public static Configuration loadConfigFromFile() {
		
		ObjectMapper mapper = new ObjectMapper();
		Configuration simConfig = null;
		try {
			simConfig = mapper.readValue(CONFIG_FILE, Configuration.class);
		} catch (JsonParseException | JsonMappingException e1) {
			LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, TextUtils.CONFIG_ERROR);
			LOGGER.log(Level.WARNING, "File chooser will be used.");
			try {
				simConfig = mapper.readValue(runFileChooser(), Configuration.class);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, TextUtils.CONFIG_ERROR);
				System.exit(0);
			}
		}
		try {
			LOGGER.log(Level.INFO, "The loaded configuration is " 
						+ mapper.writeValueAsString(simConfig));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return simConfig;
		
	}
	
	/**
	 * To choose configuration file from computer by using JFileChooser
	 * @return selected file
	 */
	private static File runFileChooser() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		JFileChooser jfc = new JFileChooser();

		int returnValue = jfc.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jfc.getSelectedFile();
			LOGGER.log(Level.FINE, selectedFile.getAbsolutePath());
			return selectedFile;
		}
		else {
			LOGGER.log(Level.SEVERE, "No file chosen!");
		}
		return null;
	}
	
	/**
	 * Runs the experiment based on selected experiment type
	 * @param simConfig configuration for the experiment
	 */
	public static void conductExperiment(Configuration simConfig) {
		
		Utils.setLearningActive(false);
		
		switch (simConfig.getExpType()) {
		case INTERNAL://intentionally cascades 
			double[] threshold = {0};
			simConfig.setThreshold(threshold);	//set threshold to 0, then do external experiment
		case EXTERNAL:
			BulkContentLoader cLoader = new BulkContentLoader(
					null, simConfig.getTagFile(), simConfig.getTrainingFile());
			Evaluator evaluator = new Evaluator(simConfig, cLoader);
			evaluator.evaluate();
			break;
		/*case TAG:
			TagEvaluator tEvaluator = new TagEvaluator(simConfig);
			tEvaluator.numberOfTagsEvaluation();
			break;*/
		case SINGLE_AGENT:
			//evaluator.singleAgentEvaluation();
			break;
		case TRUST:
			BulkContentLoader cl = new BulkContentLoader(
					null, simConfig.getTagFile(), simConfig.getTrainingFile());
			Evaluator trustEvaluator = new TrustEvaluator(simConfig, cl);
			trustEvaluator.evaluate();
			break;
		default:
			
		}
	}
	
	public static void setupLogger() {
		LOGGER.setLevel(Level.ALL);
		try {
			FileHandler fHandler = new FileHandler(LOG_FILE, true);
			fHandler.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fHandler);
		} catch (SecurityException | IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		
	}
}
