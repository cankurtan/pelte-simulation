package model.privacy;

/**
 * Sharing decision specifies either the privacy decision
 * permits or denies accesses
 * @author cankurtan
 *
 */
public enum SharingDecision{
	DENY(0), PERMIT(1);
	
	private int id;
	private SharingDecision(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
}