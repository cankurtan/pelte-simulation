package model.loader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import app.CsvOperator;
import model.network.Agent;
import model.network.Content;
import model.network.Environment;
import model.network.RelationType;
import model.privacy.PrivacySetting;
import utils.Utils;

public class IterativeContentLoader extends ContentLoader {

	protected CsvOperator csv;

	public IterativeContentLoader(CsvOperator csv, Environment env) {
		super(env);
		this.csv = csv;
	}

	/**
	 * Loads content data from a file
	 * 
	 * @param count number of images will be loaded from file.
	 * If you do not know, give a huge number. It also stops at the end of a file.
	 */
	public void loadData(int count, int numberOfTags){		
		List<Integer> agentList = super.getAgents();
		if(!agentList.isEmpty()){
			csv.readLine();//TODO HACK for header line
			String[] inputLine = csv.readLine();
			while(inputLine != null && count > 0){
				Collections.shuffle(agentList);
				Content content = createContent(inputLine, agentList.get(0), numberOfTags);
				//Checks if the created has tags
				if(!content.getTags().isEmpty()) {
					addContentToEnvironment(content);
				}
				inputLine = csv.readLine();
				count--;
			}
		}
		else{
			System.err.println("There is not any agent in the environment");
			System.exit(0);
		}
	}

	/**
	 * Loads content data from a file.
	 * This methods excludes some agent until the given count.
	 * Thus, these agents can be thought as they joined to the system in that count moment.
	 * 
	 * @param count number of images will be loaded from file.
	 * @param excludeCount is the count limit which excluded agents will join
	 * @param excludedAgents is the list of agents excluded from environment
	 */
	public void sequentiallyJoinedAgentsDataLoader(int count, int excludeCount, int excludedAgent){
		List<Integer> agentList = super.getAgents();
		if(!agentList.isEmpty()){
			agentList.remove(excludedAgent);
		}
		if(!agentList.isEmpty()){
			String[] inputLine = csv.readLine();
			while(inputLine != null && count > 0){
				//The key difference of this function is to exclude given list of agents until the given count is reached
				if(excludeCount == count){
					agentList.add(excludedAgent);
				}
				Collections.shuffle(agentList);
				Content content = createContent(inputLine, agentList.get(0), 0);
				//Checks if the created has tags
				if(!content.getTags().isEmpty()) {
					addContentToEnvironment(content);
				}
				inputLine = csv.readLine();
				count--;
			}
		}
		else{
			System.err.println("There is not any agent in the environment");
			System.exit(0);
		}
	}

	public void agentCharacterBasedDataLoader(int count){
		List<Integer> agentList = super.getAgents();

		Agent agent = null;
		if(!agentList.isEmpty()){
			String[] inputLine = csv.readLine();
			while(inputLine != null && count > 0){
				Collections.shuffle(agentList);
				agent = env.getAgent(agentList.get(0));
				Content content = createContent(inputLine, agentList.get(0), 0);
				/*
				 * The key difference of this function is the following line. 
				 * It changes sharing policy of the content according to agent character
				 */
				if(!Utils.isPredictionActive()){
					agent.changeDecision(content.getPrivacySetting());
				}
				//Checks if the created has tags
				if(!content.getTags().isEmpty()) {
					addContentToEnvironment(content);
				}
				inputLine = csv.readLine();
				count--;
			}
		}
		else{
			System.err.println("There is not any agent in the environment");
			System.exit(0);
		}
	}

	private Content createContent(String[] inputLine, int agentId, int nTags){
		
		long contentId = Long.parseLong(inputLine[0]);
		Content content = new Content(contentId, agentId);
		int nRelTypes = RelationType.values().length;
		
		/*
		 * 0: photo id,
		 * 1: privacy value in text format, which is either "private" or "public"
		 */
		/*if(inputLine.length == (nRelTypes + 1)) {
			PrivacySetting ps = createFromTextValues(inputLine, 1);
			content.setPrivacySetting(ps);
			addTagsFromMap(content);
		}*/
		/*
		 * PicAlert based Clarifai input data:
		 * 0: photo id,
		 * 1: user id,
		 * 2: privacy value in numeric format, which is btw 0 (private) and 1 (public)
		 * 3: source link
		 * 4: tags (key1:value1; key2:value2)
		 */
		if(inputLine.length == (nRelTypes + 4)) {
			PrivacySetting ps = createFromNumericValues(inputLine, 2);
			content.setPrivacySetting(ps);
			String source = inputLine[2 + nRelTypes];
			content.setSource(source);
			String[] tagValuePairs = inputLine[3 + nRelTypes].split(";");
			addTagsFromPairs(content, tagValuePairs, nTags);
		}
		else {
			System.err.println("Unknown input type for contents: " + Arrays.toString(inputLine));
			System.exit(0);
		}
		return content;
	}

	private boolean addTagsFromPairs(Content content, String[] tagValuePairs, int nTags) {
		if(nTags < 1 && tagValuePairs.length > 0) {
			nTags = tagValuePairs.length;
		}
		for (int i = 0; i < nTags; i++) {
			String str = tagValuePairs[i];
			String[] pair = str.split(":");
			String tag = pair[0];
			if(!Utils.isForbiddenTag(tag)){
				content.addTag(tag);
			}
		}
		return nTags > 0;
	}

	public void setCSV(CsvOperator csv){
		this.csv = csv;
	}
}
