package model.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.network.Agent;
import model.network.Environment;
import model.network.Relation;
import model.network.RelationType;
import utils.Utils;

public class SnapDataLoader {
	
	List<String> featureNames;
	
	public SnapDataLoader(){
		featureNames = new ArrayList<String>();
	}
	
	public void loadData(Environment env, String featFile, String edgeFile){
		loadNodes(new File(featFile), env);
		loadEdges(new File(edgeFile), env);
	}
	/**
	 * Loads edges from file
	 * 
	 * @param file edge file
	 * @param env environment
	 */
	private void loadEdges(File file, Environment env){
		if(file != null){
			try(BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
				String line = br.readLine();
				int source = 0;
				int dest = 0;
				RelationType[] rTypes = RelationType.values();
				while (line != null) {
					String[] splitted = line.split(" ");
					source = Integer.parseInt(splitted[0]);
					dest = Integer.parseInt(splitted[1]);					
					Relation rel = new Relation(Utils.getRelationId(), rTypes[0], source, dest);
					//if relation type is given in the file
					if(splitted.length == 3){
						int rTypeNo = Integer.parseInt(splitted[2]);
						if(rTypeNo <= rTypes.length){
							rel.setRelType(rTypes[rTypeNo]);
						}
						else{
							System.err.println("Given relation type is not defined in the system");
						}
					}
					env.addRelation(source, dest, rel);
					Agent a = env.getAgent(source);
					a.addRelation(rel);
					/*
					 * if relations in the environment are bidirectional,
					 * create the both directions.
					 */
					line = br.readLine();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println("Edges are loaded");
	}
	/**
	 * Loads nodes from file
	 * 
	 * @param file node file
	 * @param env environment
	 */
	private void loadNodes(File file, Environment env){
		if(file != null){
			try(BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
				String line = br.readLine();
				int id = 0;
				while (line != null) {
					String[] splitted = line.split(" ");
					id = Integer.parseInt(splitted[0]);
					Agent a = new Agent(id, "Agent " + id);
					env.addAgent(a);
					/* Convert all features from str to int here
					for(int i = 0; i < splitted.length; i++) {
			    		effects[i] = Integer.parseInt(splitted[i]);
			    	}
			    	*/
					line = br.readLine();
//					System.out.print(id + " ");
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//System.out.println("Nodes are loaded.");
	}
	
}
