package model.privacy;

/**
 * An enum class of the set of predefined agent characters
 * that determines how an agent acts while specifying privacy settings
 * @author cankurtan
 *
 */
public enum AgentCharacter {	
	/* Agent acts as expected*/
	NORMAL(0),
	/* Agent acts randomly */ 
	RANDOM(1),
	/* Agent specifies the privacy setting as the opposite */
	OPPOSITE(2),
	/* Agent always permits */
	PERMIT(3);
	
	private int id;	
	private AgentCharacter(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
}
