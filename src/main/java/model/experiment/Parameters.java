package model.experiment;

import java.util.Arrays;
import java.util.List;

public class Parameters {

	public int training;
	public int test;
	public double threshold;
	public int nTags;
	
	public Parameters() {
		nTags = 0;
	}
	
	public List<String> getAsList() {
		return Arrays.asList(Integer.toString(training), Integer.toString(test), Double.toString(threshold), Integer.toString(nTags));
	}
}
