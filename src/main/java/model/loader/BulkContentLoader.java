package model.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import app.App;
import app.CsvOperator;
import model.experiment.Parameters;
import model.network.Content;
import model.network.Environment;
import model.network.RelationType;
import model.privacy.PrivacySetting;
import model.privacy.SharingDecision;
import utils.Utils;

public class BulkContentLoader extends ContentLoader {

	private Map<Long, List<String>> tagMap = new HashMap<>();
	private Map<Long, PrivacySetting> settings = new HashMap<>();
	private List<Content> contents = new ArrayList<>();
	private static final Logger LOGGER = Logger.getLogger(App.class.getName());
	private Set<String> priTags = new HashSet<>();
	private Set<String> pubTags = new HashSet<>();

	public BulkContentLoader(Environment env, String tagFile, String psFile) {
		super(env);
		loadTags(tagFile);
		loadPrivacySettings(psFile);
	}

	public void loadData(Environment env, Parameters param) {
		super.env = env;
		this.contents = prepareContents(param);
		Collections.shuffle(contents);
		distributeContents(contents.subList(0, param.training));
		//make new contents predictable
		Utils.activatePrediction();
		distributeContents(contents.subList(param.training, param.training+param.test));
	}

	public List<Content> prepareContents(Parameters param) {
		List<Content> contents = new ArrayList<>();
		for(long id : settings.keySet()) {
			if(tagMap.containsKey(id)) {
				Content content = new Content(id, 0);
				if(addTagsFromMap(content, param.nTags)) {
					content.setPrivacySetting(settings.get(id));
					contents.add(content);
				}
			}			
		}
		return contents;
	}

	public void distributeContents(List<Content> contents){		
		List<Integer> agentList = super.getAgents();
		if(!agentList.isEmpty()){
			for(int i = 0; i < contents.size(); i++) {
				int next = i % agentList.size();
				if(next == 0) {
					Collections.shuffle(agentList);
				}
				Content content = contents.get(i).copy();
				content.setOwnerId(agentList.get(next));
				if(!Utils.isPredictionActive()){
					env.getAgent(agentList.get(next)).changeDecision(content.getPrivacySetting());
				}
				addContentToEnvironment(content);
			}
		}
		else{
			LOGGER.severe("There is not any agent in the environment");
			System.exit(0);
		}
		printTagInfo();
		resetCounts();
	}

	/**
	 * Adds tags to content from the map, which stores tags mapped to content ids
	 * @param content that tags will be added
	 * @param nTags The number of tags to be added to content
	 * @return {@code true} if the content has tags, otherwise {@code false}
	 */
	private boolean addTagsFromMap(Content content, int nTags) {
		if(tagMap.containsKey(content.getId())) {
			List<String> tags = new ArrayList<>(tagMap.get(content.getId()));
			if(nTags != 0 && nTags < tags.size()) {
				tags = tags.subList(0, nTags);
			}
			content.addTags(tags);
			return true;
		}
		else {
			//System.out.println(content.getId() + " is not in the tag map");
			return false;
		}
	}

	/**
	 * Reads tags from a file where first column of a row is content id 
	 * and the rest is tags. Creates a map from content id to set of tags.
	 * @param tagFile file of tags
	 */
	public void loadTags(String tagFile) {
		CsvOperator reader = new CsvOperator(tagFile);
		String[] line = reader.readLine();
		long contentId = 0L;
		while (line != null) {
			contentId = Long.parseLong(line[0]);
			List<String> tags = new ArrayList<>();
			for (int i = 1; i < line.length; i++) {
				tags.add(line[i]);
			}
			this.tagMap.put(contentId, tags);
			line = reader.readLine();
		}
		LOGGER.info("TagMap size " + tagMap.size());
	}

	/**
	 * Reads privacy settings from a file where first column is content id 
	 * and the second one is privacy label. Creates a map from content id to privacy setting.
	 * @param psFile file of the privacy settings
	 */
	private void loadPrivacySettings(String psFile) {
		CsvOperator reader = new CsvOperator(psFile);
		String[] line = reader.readLine();
		boolean numeric = true;
		try {
			Integer.parseInt(line[1]);
		} catch(NumberFormatException e) {
			numeric = false;
		}
		if(numeric) {
			while (line != null) {
				long contentId = Long.parseLong(line[0]);
				PrivacySetting ps = createFromNumericValues(line, 1);
				if(settings.containsKey(contentId)) {
					System.out.println(contentId + " " + settings.get(contentId) + ps);
				}
				this.settings.put(contentId, ps);
				line = reader.readLine();
			}
		}
		else {
			while (line != null) {
				long contentId = Long.parseLong(line[0]);
				PrivacySetting ps = createFromTextValues(line, 1);
				if(settings.containsKey(contentId)) {
					System.out.println(contentId + " " + settings.get(contentId) + ps);
				}
				this.settings.put(contentId, ps);
				line = reader.readLine();
			}
		}
		LOGGER.info("PrivacySettings size: " + settings.size());
	}
	
	public void tagInfo() {
		int prin = 0, pubn = 0;
		for(Content c : contents) {
			if(c.getPrivacySetting().getSharingDecision(RelationType.FRIEND) 
					== SharingDecision.PERMIT) {
				pubTags.addAll(c.getTags());
				pubn++;
			}
			else {
				priTags.addAll(c.getTags());
				prin++;
			}
		}
		System.out.println(String.format("Public images %d, tags %d", pubn, pubTags.size()));
		System.out.println(String.format("Private images %d, tags %d", prin, priTags.size()));
	}

	public void setTagMap(Map<Long, List<String>> tagMap) {
		this.tagMap = tagMap;
	}
}
