package searchEngine;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

public class Stopping {
	HashMap<String, LinkedList<DocumentEntry>> invertedIndex; // term and list of docID & tf
	HashMap<String, Integer> termDfMap; // term and df of that term
	LinkedHashMap<Integer, String> docIDNameMap; // docID and corresponding doc name
	HashMap<Integer, Integer> docIDLengthMap; // docID and length of the doc (total number of tokens)
	LinkedHashMap<Integer, String> queryMap = new LinkedHashMap<Integer, String>(); // query id and the query
	int totalNumOfDocs; // total number of documents in corpus
	private CorpusBuilding cb = new CorpusBuilding(); // use the removeCommonWords method to process query
	String indexDirectory = "index result/stopping/";
	
	public Stopping(LinkedHashMap<Integer, String> queryMap){
		Set<Map.Entry<Integer, String>> entrySet = queryMap.entrySet();
		for (Map.Entry<Integer, String> entry : entrySet){ // for each query
			int queryID = entry.getKey();
			String queryString = entry.getValue();
			String newQuery = this.cb.removeCommonWords(queryString);
			this.queryMap.put(queryID, newQuery);
			//System.out.println(queryID + "  " + queryString);
			//System.out.println(queryID + "  " + newQuery);
		}
		this.readObjs();
	}
	
	@SuppressWarnings("unchecked")
	private void readObjs(){
		try {
			ObjectInputStream input1 = new ObjectInputStream(new FileInputStream(this.indexDirectory + "invertedIndex.dat"));
			this.invertedIndex = (HashMap<String, LinkedList<DocumentEntry>>) input1.readObject();
			input1.close();
			
			ObjectInputStream input2 = new ObjectInputStream(new FileInputStream(this.indexDirectory + "termDf.dat"));
			this.termDfMap = (HashMap<String, Integer>) input2.readObject();
			input2.close();
			
			ObjectInputStream input3 = new ObjectInputStream(new FileInputStream(this.indexDirectory + "docIDName.dat"));
			this.docIDNameMap = (LinkedHashMap<Integer, String>) input3.readObject();
			input3.close();
			
			ObjectInputStream input4 = new ObjectInputStream(new FileInputStream(this.indexDirectory + "docIDLength.dat"));
			this.docIDLengthMap = (HashMap<Integer, Integer>) input4.readObject();
			input4.close();
			
			this.totalNumOfDocs = this.docIDNameMap.size();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
