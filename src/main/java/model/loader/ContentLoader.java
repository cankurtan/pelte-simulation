package model.loader;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import model.network.Content;
import model.network.Environment;
import model.network.RelationType;
import model.privacy.PrivacySetting;
import model.privacy.SharingDecision;

public class ContentLoader {
	
	protected Environment env;
	private int[][] imageCount;
	private final Set<String> tagList = new LinkedHashSet<>();
	private static final Logger LOGGER = Logger.getLogger(ContentLoader.class.getName());
	
	public ContentLoader(Environment env) {
		super();
		this.env = env;
		resetCounts();
	}
	
	protected void addContentToEnvironment(Content content) {
		this.env.addContent(content);
		content.getPrivacySetting().getRebac().forEach(
				(rType, decision) -> this.imageCount[rType.getId()][decision.getId()]++);
		this.tagList.addAll(content.getTags());
	}
	
	protected PrivacySetting createFromNumericValues(String[] inputLine, int start) {
		Map<RelationType, SharingDecision> rebac = new HashMap<RelationType, SharingDecision>();	
		double privacyValue = 0;
		for (int i = 0; i < RelationType.values().length; i++) {
			privacyValue = Double.parseDouble(inputLine[start+i]);	
			if(privacyValue > 0.5){
				rebac.put(RelationType.values()[i], SharingDecision.PERMIT);
			}
			else if(privacyValue < 0.5){
				rebac.put(RelationType.values()[i], SharingDecision.DENY);
			}
			else {
				//TODO In case of pVal = 0.5, what is the default action?
				rebac.put(RelationType.values()[i], SharingDecision.DENY);
			}
		}
		PrivacySetting ps = new PrivacySetting(rebac);
		return ps;
	}
	
	protected PrivacySetting createFromTextValues(String[] inputLine, int start) {
		
		Map<RelationType, SharingDecision> rebac = new HashMap<>();
		for (int i = 0; i < RelationType.values().length; i++) {
			String privacyValue = inputLine[start+i].trim();
			if(privacyValue.equals("public")){
				rebac.put(RelationType.values()[i], SharingDecision.PERMIT);
			}
			else if(privacyValue.equals("private")){
				rebac.put(RelationType.values()[i], SharingDecision.DENY);
			}
		}
		if(rebac.size() == 0) {
			LOGGER.log(Level.WARNING, "Couldn't cast privacy setting as string label (public or private)!");
			LOGGER.log(Level.SEVERE, "Privacy settings are could not created. The system halts!");
			System.exit(0);
		}
		PrivacySetting ps = new PrivacySetting(rebac);
		return ps;
	}

	public void printTagInfo(){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < RelationType.values().length; i++) {
			sb.append("For relation type " + RelationType.values()[i] + ":\n");
			sb.append(Arrays.toString(SharingDecision.values()) + "\n");
			sb.append(Arrays.toString(this.imageCount[i]) + "\n");
		}
		sb.append(this.tagList.size() + " different tags appeared.");
		System.out.println(sb.toString());
	}

	public void resetCounts(){
		this.imageCount = new int[RelationType.values().length][SharingDecision.values().length];
	}
	
	protected List<Integer> getAgents() {
		return this.env.getAgentIds();
	}
	
}
