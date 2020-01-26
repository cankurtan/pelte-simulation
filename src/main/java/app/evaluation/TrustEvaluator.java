package app.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import app.Configuration;
import model.experiment.ExperimentStats;
import model.experiment.Parameters;
import model.loader.BulkContentLoader;
import model.loader.SnapDataLoader;
import model.network.ExternalEnvironment;
import model.network.RelationType;
import model.privacy.AgentCharacter;
import model.privacy.SharingDecision;
import utils.Utils;

public class TrustEvaluator extends Evaluator {


	public TrustEvaluator(Configuration simConfig, BulkContentLoader cLoader) {
		super(simConfig, cLoader);
	}
	
	@Override
	protected void simulate(Parameters param) {
		int[] numbers = {1, 2, 3, 4, 5, 7, 10, 15, 20, 25, 30};
		for(int j = 0; j < numbers.length; j++) {
			ExperimentStats internalStats = new ExperimentStats(RelationType.values(), SharingDecision.values().length);
			ExperimentStats externalStats = new ExperimentStats(RelationType.values(), SharingDecision.values().length);
			System.out.print("##Simulation:");
			for (int i = 0; i < simConfig.getNumOfSims(); i++) {
				System.out.print(" " + (i + 1));
				ExternalEnvironment env = runTrust(param, numbers[j]);
				internalStats.addStats(env.getRelationStats());
				externalStats.addStats(env.getExternalStats());
			}
			System.out.println();
			writeLineToCSV(param, internalStats, externalStats);
		}
	}
	
	protected ExternalEnvironment runTrust(Parameters param, int untrusted) {

		Utils.deactivatePrediction();
		Utils.setInternalThreshold(param.threshold);
		//Utils.activateTrustBasedLearning();
		
		RelationType[] rTypes = RelationType.values();
		ExternalEnvironment env = new ExternalEnvironment(Arrays.asList(rTypes));
		//load snap social network
		SnapDataLoader snap = new SnapDataLoader();
		snap.loadData(env, simConfig.getFeatures(), simConfig.getEdges());
		
		List<Integer> untrustedAgents = new ArrayList<>(env.getAgentIds());
		Collections.shuffle(untrustedAgents);
		untrustedAgents = untrustedAgents.subList(0, untrusted);
		
		for(int agentId : untrustedAgents){
			env.getAgent(agentId).setAgentChar(AgentCharacter.OPPOSITE);
		}
		//load contents
		cLoader.loadData(env, param);
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
		eStats.getResultRows().forEach((rType, values)-> {
			List<String> temp = new ArrayList<>(row);
			temp.addAll(values);
			String[] line = new String[temp.size()+1];
			line = temp.toArray(line);
			line[temp.size()] = "EXT";
			this.fo.getCsvWriter().writeNext(line);
		});
		sb.append("Total Results\n" + tStats.getResultTable() + "\n");	//add total results
	}
}
