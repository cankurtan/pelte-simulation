package model.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.privacy.PrivacySetting;

/**
 * Contents are like the posts shared by users in online social networks
 * @author Can Kurtan
 *
 */
public class Content {

	/** content id */
	private long id;
	/** source link of the content*/
	private String source;
	/** content owner's agent id */
	private int ownerId;
	/** list of content tags */
	private final List<String> tags = new ArrayList<>();
	/** privacy setting of the content */
	private PrivacySetting privacySetting;
	
	/**
	 * Basic Content constructor
	 * 
	 * @param id	content id
	 * @param ownerId	owner agent's id
	 */
	public Content(long id, int ownerId) {
		super();
		this.id = id;
		this.ownerId = ownerId;
	}
	
	/**
	 * Complete Content constructor
	 * 
	 * @param id	content id
	 * @param source	source link of the content
	 * @param ownerId	owner agent's id
	 * @param tags	list of tags
	 * @param privacySetting	privacySetting of the content
	 */
	public Content(long id, String source, int ownerId, List<String> tags, 
			PrivacySetting ps) {
		this(id, ownerId);
		this.source = source;
		this.addTags(tags);
		this.setPrivacySetting(ps);
	}

	/**
	 * Returns the owner id of the content
	 * @return owner id of the content
	 */
	public int getOwnerId() {
		return ownerId;
	}
	
	/**
	 * Sets the owner of the content
	 * this can be necessary because
	 * unpublished contents may be constructed without owner
	 * 
	 * @param ownerId owner agent's id
	 */
	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	/**
	 * Returns the list of tags
	 * @return list of tags
	 */
	public List<String> getTags() {
		return tags;
	}
	
	/**
	 * Adds tags as a whole
	 * @param tags collection of tags
	 */
	public void addTags(Collection<String> tags) {
		this.tags.addAll(tags);
	}
	
	/**
	 * Adds a given tag to the tag list
	 * @param tag tag
	 */
	public void addTag(String tag){
		tags.add(tag);
	}

	/**
	 * Returns the privacy setting of the content
	 * @return privacy setting of the content
	 */
	public PrivacySetting getPrivacySetting() {
		return privacySetting;
	}

	/**
	 * Sets the privacy setting of the content
	 * @param privacySetting desired privacy setting for the content
	 */
	public void setPrivacySetting(PrivacySetting privacySetting) {
		this.privacySetting = privacySetting;
	}

	/**
	 * Return the id of the content
	 * @return id of the content
	 */
	public long getId() {
		return id;
	}
	/**
	 * Sets the source of the content
	 * @param source source link of the content
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Returns the source link of the content
	 * @return the source link of the content
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * Creates a deep copy of the content
	 * @return a deep copy of the content
	 */
	public Content copy() {
		Content c = new Content(this.id, this.source, this.ownerId, 
				new ArrayList<>(this.tags), this.privacySetting.copy());
		return c;
	}
	
	@Override
	public String toString() {
		return "Content [id=" + id + ", source=" + source + ", "
				+ "ownerId=" + ownerId + ", tags=" + tags.toString()
				+ ", privacyPolicy=" + privacySetting.toString() + "]";
	}
}
