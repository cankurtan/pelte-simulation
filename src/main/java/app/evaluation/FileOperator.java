package app.evaluation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriter;

import model.network.Content;
import model.network.Environment;

public class FileOperator {
	
	private CSVWriter csvWriter;
	
	public FileOperator(String filename) {
		try {
			this.csvWriter = new CSVWriter(new FileWriter(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(Environment env){
		Map<Long,Content> contentMap = env.getContents();
		List<Content> contentList = new ArrayList<Content>(contentMap.values());
		try{
			PrintWriter writer = new PrintWriter("environment.txt", "UTF-8");
			writer.println(env.toString());
			for(Content c : contentList){
				writer.println("Content id:" + c.getId() + " Owner id:" + c.getOwnerId() + " Tags: " + c.getTags());
			}
			writer.println(env.printConfusion());
			writer.println(env.printTagTables());
			writer.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CSVWriter getCsvWriter() {
		return csvWriter;
	}

}
