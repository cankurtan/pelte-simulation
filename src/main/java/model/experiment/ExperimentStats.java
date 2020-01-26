package model.experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.network.RelationType;
import utils.Utils;

public class ExperimentStats {
	
	private final Map<RelationType, Stats> relationStats = new HashMap<>();
	private int count = 0;
	
	public ExperimentStats(RelationType[] rTypes, int ndecision) {
		for(RelationType rType : rTypes) {
			relationStats.put(rType, new Stats(ndecision));
		}
	}
	
	public void addStats(Map<RelationType, Stats> rStats) {
		rStats.forEach((rType,stats) -> {
			relationStats.get(rType).addConfusion(stats.getConfusion());
		});
		count++;
	}

	public int getCount() {
		return count;
	}
	
	public Map<RelationType, Stats> getRelationStats() {
		return relationStats;
	}
	
	public Stats getStats(RelationType rType) {
		return relationStats.get(rType);
	}
	
	public String metricResults() {
		StringBuilder sb = new StringBuilder();
		sb.append("After " + getCount() + " simulations, the results are: ");
		this.relationStats.forEach((rType,stats) -> sb.append("\nRelation Type " + rType + ":\n")
				.append("Pri Ratio, Pri Rec, Pub Rec, Acc\n")
				.append(Utils.formatDouble(stats.getPriRatio())).append(", ")
				.append(Utils.formatDouble(stats.getPriRecall())).append(", ")
				.append(Utils.formatDouble(stats.getPubRecall())).append(", ")
				.append(Utils.formatDouble(stats.getAccuracy())).append("\n"));
		return sb.toString();
	}
	
	public String getResultTable() {
		List<List<String>> rows = new ArrayList<>();
		rows.add(getHeaderRow());
		getResultRows().forEach((rType,row) -> rows.add(row));
		return Utils.formatAsTable(rows);
	}
	
	public Map<RelationType, List<String>> getResultRows() {
		Map<RelationType, List<String>> rows = new HashMap<>(); 
		this.relationStats.forEach((rType,stats) -> {
			List<String> temp = new ArrayList<>();
			temp.addAll(stats.getMetricsAsRow());
			temp.add(0, rType.name());
			rows.put(rType,temp);
		});
		return rows;
	}
	
	public List<String> getHeaderRow() {
		List<String> headers = new ArrayList<>();
		headers.addAll(relationStats.values().iterator().next().getMetricNames());
		headers.add(0, "Relation Type");
		return headers;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("After " + getCount() + " simulations, the results are: ");
		relationStats.forEach((rType,stats) -> sb.append("\nRelation Type " + rType + ":").append(stats.toString()));
		return sb.toString();
	}
}
