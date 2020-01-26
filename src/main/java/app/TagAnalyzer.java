package app;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TagAnalyzer {

	private String fileName;
	
	public TagAnalyzer(String fileName) {
		super();
		this.fileName = fileName;
	}

	public void countTags(){
		CsvOperator csv = new CsvOperator(fileName);
		String[] line = csv.readLine();
		String tag = null;
		int index = 0;
		List<String> privateTags = new ArrayList<String>();
		List<Integer> privateCounts = new ArrayList<Integer>();
		List<String> publicTags = new ArrayList<String>();
		List<Integer> publicCounts = new ArrayList<Integer>();
		int publicImageCount = 0;
		int privateImageCount = 0;
		int temp = 0;
		while(line != null){
			double privacyVal = Double.parseDouble(line[2]);
			String[] tagValuePairs = line[4].split(";");
			if(privacyVal > 0.5){
				publicImageCount++;
				for (int i = 0; i < tagValuePairs.length; i++) {
					tag = tagValuePairs[i].split(":")[0];
					if(publicTags.contains(tag)){
						index = publicTags.indexOf(tag);
						temp = publicCounts.get(index);
						publicCounts.set(index, (temp+1));
					}
					else{
						publicTags.add(tag);
						publicCounts.add(1);					
					}
				}
			}
			else{
				privateImageCount++;
				for (int i = 0; i < tagValuePairs.length; i++) {
					tag = tagValuePairs[i].split(":")[0];
					if(privateTags.contains(tag)){
						index = privateTags.indexOf(tag);
						temp = privateCounts.get(index);
						privateCounts.set(index, (temp+1));
					}
					else{
						privateTags.add(tag);
						privateCounts.add(1);					
					}
				}
			}
			line = csv.readLine();
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("private_tags.txt"));
			writer.write("Number of Private Images: " + privateImageCount);
			writer.newLine();
			for(int i = 0; i < privateTags.size(); i++){
				writer.write(privateTags.get(i) + ":" + privateCounts.get(i));
				writer.newLine();
			}
			writer.close();
			writer = new BufferedWriter(new FileWriter("public_tags.txt"));
			writer.write("Number of Public Images: " + publicImageCount);
			writer.newLine();
			for(int i = 0; i < publicTags.size(); i++){
				writer.write(publicTags.get(i) + ":" + publicCounts.get(i));
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
