package model.experiment;

public enum ExperimentType {
	
	INTERNAL(0), EXTERNAL(1), TRUST(2), TAG(3), SINGLE_AGENT(4);
	
	private int id;
	
	private ExperimentType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
}
