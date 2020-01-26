package model.network;

public enum RelationType {
	FRIEND(0);// COLLEAGUE(1), FAMILY(2);
	
	private int id;

	private RelationType(int id) {
		this.id = id;
	}
	public int getId(){
		return id;
	}
}
