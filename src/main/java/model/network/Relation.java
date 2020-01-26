package model.network;

/**
 * Class representing relation between two agents
 * in a directed way.
 * @author cankurtan
 *
 */
public class Relation {

	private int id;
	private RelationType type;
	private double trust;
	private int source;
	private int destination;
	
	/**
	 * Full constructor
	 * @param id relation id
	 * @param type relation type
	 * @param trust trust value of source to destination
	 * @param source id of the source
	 * @param destination id of the destination
	 */
	public Relation(int id, RelationType type, double trust, int source, int destination) {
		this.id = id;
		this.type = type;
		this.trust = trust;
		this.source = source;
		this.destination = destination;
	}
	
	/**
	 * Constructor without trust value
	 * 
	 * @param id relation id
	 * @param type relation type
	 * @param source id of the source
	 * @param destination id of the destination
	 */
	public Relation(int id, RelationType type, int source, int destination) {
		super();
		this.id = id;
		this.type = type;
		this.source = source;
		this.destination = destination;
	}

	public int getId() {
		return id;
	}

	public RelationType getType() {
		return type;
	}

	public void setRelType(RelationType type) {
		this.type = type;
	}

	public double getTrust() {
		return trust;
	}

	public void setTrust(double trust) {
		this.trust = trust;
	}

	public int getSource() {
		return source;
	}

	public int getDestination() {
		return destination;
	}

}
