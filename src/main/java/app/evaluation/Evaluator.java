package app.evaluation;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import app.CsvOperator;
import app.Configuration;
import model.experiment.ExperimentStats;
import model.experiment.Parameters;
import model.loader.BulkContentLoader;
import model.loader.IterativeContentLoader;
import model.loader.SnapDataLoader;
import model.network.Environment;
import model.network.ExternalEnvironment;
import model.network.RelationType;
import model.privacy.SharingDecision;
import utils.TextUtils;
import utils.Utils;

public class Evaluator {

	protected Configuration simConfig;
	protected StringBuilder sb = new StringBuilder();
	protected BulkContentLoader cLoader;
	protected FileOperator fo;

	public Evaluator(Configuration simConfig, BulkContentLoader cLoader) {
		this.simConfig = simConfig;
		this.cLoader = cLoader;
		File tra = new File(simConfig.getTrainingFile());
		File tag = new File(simConfig.getTagFile());
		String filename = LocalDateTime.now().format(TextUtils.FORMATTER).toString() 
				+ "_" + tra.getName().replaceFirst("[.][^.]+$", "") 
				+ "_" + tag.getName().replaceFirst("[.][^.]+$", "") + "_results.csv";
		this.fo = new FileOperator(filename);
	}

	public void evaluate(){
		this.fo.getCsvWriter().writeNext(TextUtils.HEADERS);
		
		changeNumberOfTags();
		
		System.out.println(sb.toString());
		try {
			this.fo.getCsvWriter().flush();
			this.fo.getCsvWriter().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void changeNumberOfTags() {
		Parameters param = new Parameters();
		for(int i = 0; i < simConfig.getTagNumbers().length; i++) {
			param.nTags = simConfig.getTagNumbers()[i];
			changeSetSize(param);
		}
	}

	private void changeSetSize(Parameters param) {
		for (int i = 0; i < simConfig.getTrainingSizes().length; i++) {
			for (int j = 0; j < simConfig.getTestSizes().length; j++) {
				param.training = simConfig.getTrainingSizes()[i];
				param.test = simConfig.getTestSizes()[j];
				sb.append("Training = " + param.training + ", Test = " + param.test);
				changeThreshold(param);
			}
		}
	}

	private void changeThreshold(Parameters param) {

		for (int i = 0; i < simConfig.getThreshold().length; i++) {
			param.threshold = simConfig.getThreshold()[i];
			sb.append("\nThreshold = " + this.simConfig.getThreshold()[i] + "\n");	//add threshold info
			simulate(param);
			System.out.println(sb.toString());
			sb = new StringBuilder();
		}
	}

	protected void simulate(Parameters param) {
		ExperimentStats internalStats = new ExperimentStats(RelationType.values(), SharingDecision.values().length);
		ExperimentStats externalStats = new ExperimentStats(RelationType.values(), SharingDecision.values().length);
		System.out.print("##Simulation:");
		for (int i = 0; i < simConfig.getNumOfSims() ; i++) {
			System.out.print(" " + (i + 1));
			ExternalEnvironment env = runExternal(param);
			internalStats.addStats(env.getRelationStats());
			externalStats.addStats(env.getExternalStats());
		}
		System.out.println();
		writeLineToCSV(param, internalStats, externalStats);
	}
	
	private ExternalEnvironment runExternal(Parameters param) {
		if(this.cLoader != null) {
			return runExternalFromLoader(param);
		}
		return runExternalByParsing(param);
	}

	private ExternalEnvironment runExternalByParsing(Parameters param) {
		Utils.deactivatePrediction();
		Utils.setInternalThreshold(param.threshold);

		RelationType[] rTypes = RelationType.values();
		ExternalEnvironment env = new ExternalEnvironment(Arrays.asList(rTypes));

		//load snap social network
		SnapDataLoader snap = new SnapDataLoader();
		snap.loadData(env, simConfig.getFeatures(), simConfig.getEdges());
		//create csv file organizer
		CsvOperator csv = new CsvOperator(simConfig.getTrainingFile());
		
		//load training data from picAlert image data set
		IterativeContentLoader cLoader = new IterativeContentLoader(csv, env);	
		cLoader.loadData(param.training, param.nTags);
		cLoader.printTagInfo();
		cLoader.resetCounts();

		//make new contents predictable
		Utils.activatePrediction();
		csv = new CsvOperator(simConfig.getTestFile());
		cLoader.setCSV(csv);
		
		//load data from picAlert image data set for predictions
		cLoader.loadData(param.test, param.nTags);
		cLoader.printTagInfo();

		System.out.println(env.printConfusion());
		//write(env);
		return env;
	}

	private ExternalEnvironment runExternalFromLoader(Parameters param) {

		Utils.deactivatePrediction();
		Utils.setInternalThreshold(param.threshold);
		
		RelationType[] rTypes = RelationType.values();
		ExternalEnvironment env = new ExternalEnvironment(Arrays.asList(rTypes));
		//load snap social network
		SnapDataLoader snap = new SnapDataLoader();
		snap.loadData(env, simConfig.getFeatures(), simConfig.getEdges());		
		//load contents
		cLoader.loadData(env, param);
		return env;
	}

	public void singleAgentEvaluation(){

		Utils.deactivatePrediction();
		Environment env = null;
		StringBuilder sb = new StringBuilder();
		ExperimentStats expStats = new ExperimentStats(RelationType.values(), SharingDecision.values().length);
		ExperimentStats agentStats = new ExperimentStats(RelationType.values(), SharingDecision.values().length);
		for (int j = 0; j < simConfig.getThreshold().length; j++) {
			for (int i = 0; i < simConfig.getNumOfSims() ; i++) {
				System.out.println("\n##Simulation " + (i+1));

				env = runSingleAgentConfusionSimulation(simConfig.getThreshold()[j]);
				expStats.addStats(env.getRelationStats());
				agentStats.addStats(env.getAgentStats(simConfig.getNewcomer()));
				System.out.println(env.getAgentStats(simConfig.getNewcomer()));
			}
		}
		sb.append("Overall System, " + expStats.toString() + "\n");
		System.out.println("Threshold, [Private Recall, Public Recall, Accuracy, Private Ratio]");
		System.out.println(sb.toString());
		System.out.println(agentStats.toString());
	}

	private Environment runSingleAgentConfusionSimulation(double threshold){

		Utils.deactivatePrediction();
		Utils.setInternalThreshold(threshold);
		
		RelationType[] rTypes = RelationType.values();
		Environment env = new ExternalEnvironment(Arrays.asList(rTypes));
		//load snap social network
		SnapDataLoader snap = new SnapDataLoader();
		snap.loadData(env, simConfig.getFeatures(), simConfig.getEdges());

		//create csv file organizer
		CsvOperator csv = new CsvOperator(simConfig.getTrainingFile());
		//load training data from picAlert image data set
		IterativeContentLoader cLoader = new IterativeContentLoader(csv, env);
		cLoader.sequentiallyJoinedAgentsDataLoader(simConfig.getTrainingSize(), 0, simConfig.getNewcomer());
		cLoader.printTagInfo();
		cLoader.resetCounts();
		//make new contents predictable
		Utils.activatePrediction();
		csv = new CsvOperator(simConfig.getTestFile());
		cLoader.setCSV(csv);
		//load data from picAlert image data set for predictions
		cLoader.sequentiallyJoinedAgentsDataLoader(simConfig.getTestSize(), 
				simConfig.getNewcomerTurn(), simConfig.getNewcomer());
		cLoader.printTagInfo();

		System.out.println(env.printConfusion());
		this.fo.write(env);
		return env;
	}
	
	protected void writeLineToCSV(Parameters param, ExperimentStats iStats, ExperimentStats eStats) {
		
		sb.append("Internal Results\n" + iStats.getResultTable() + "\n");	//print internal results
		sb.append("External Results\n" + eStats.getResultTable() + "\n");	//print external results
		ExperimentStats tStats = new ExperimentStats(RelationType.values(), 
				SharingDecision.values().length);
		tStats.addStats(iStats.getRelationStats());	//add internal results
		tStats.addStats(eStats.getRelationStats());	//add external results

		ArrayList<String> row = new ArrayList<>(param.getAsList());
		row.add(Integer.toString(simConfig.getNumOfSims()));
		tStats.getResultRows().forEach((rType, values)-> {
			List<String> temp = new ArrayList<>(row);
			temp.addAll(values);
			String[] line = new String[temp.size()+1];
			line = temp.toArray(line);
			int totalSize = tStats.getRelationStats().get(rType).getConfusionSize();
			int intSize = iStats.getRelationStats().get(rType).getConfusionSize();
			line[temp.size()] = Utils.formatDouble(1.0 * intSize / totalSize);
			this.fo.getCsvWriter().writeNext(line);
		});
		sb.append("Total Results\n" + tStats.getResultTable() + "\n");	//add total results
	}


}
