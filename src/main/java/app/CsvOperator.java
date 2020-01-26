package app;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVReader;

public class CsvOperator {
	
	private CSVReader reader;
	private BufferedWriter writer;
	
	public CsvOperator(){}
	
	public CsvOperator(String inputFile){
		setReader(inputFile);
	}
	
	public CsvOperator(String inputFile, String outputFile){
		setReader(inputFile);
		setWriter(outputFile);
	}
	
	public void skipLines(int startPosition) {
		try {
			while(startPosition > 0){
				reader.readNext();
				startPosition--;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public String[] readLine() {
		String[] line = null;
		try {
			line = reader.readNext();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return line;
	}

	public void writeFile(String entries) {
		try {
			writer.write(entries + System.lineSeparator());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void flushData(){
		try {
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void closeAll(){
		closeWriter();
		closeReader();
	}

	public CSVReader getReader() {
		return reader;
	}
	
	public void setReader(String inputFile){
		try {
			reader = new CSVReader(new FileReader(inputFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void closeReader() {
		try {
			this.reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BufferedWriter getWriter() {
		return writer;
	}
	
	public void setWriter(String outputFile){
		try {
			this.writer = new BufferedWriter(new FileWriter(outputFile, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void closeWriter() {
		try {
			this.writer.flush();
			this.writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
}
