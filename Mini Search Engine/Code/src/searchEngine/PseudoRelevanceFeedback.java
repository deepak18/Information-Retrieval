package searchEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class PseudoRelevanceFeedback {
	BM25Model bm25;
	HashSet<String> commonWordsSet = new HashSet<String>();
	String commonWordsDir = "resource/common_words";
	String corpusDirectory = "corpus/";
	
	int numOfTop = 5;
	int minAppearance = 6;
	int numDocsRetrieved;
	
	public PseudoRelevanceFeedback(BM25Model bm25){
		this.bm25 = bm25;
		this.numDocsRetrieved = bm25.numDocsRetrieved;
		this.loadCommonWords();
	}
	
	public ArrayList<QueryResult> search(){
		LinkedHashMap<Integer, String> queryMap = bm25.queryMap;
		LinkedHashMap<Integer, String> newQueryMap = new LinkedHashMap<Integer, String>();
		bm25.numDocsRetrieved = this.numOfTop; // top 5 documents
		ArrayList<QueryResult> firstRoundResult = this.bm25.search();
		for (QueryResult qr : firstRoundResult){ // 64 queries
			int queryID = qr.getQueryID();
			ArrayList<Integer> docList = qr.getDocList();
			ArrayList<String> chosenTerms = this.processDocs(docList);
			if (chosenTerms.size() == 0) continue;
			String oldQueryString = queryMap.get(queryID);
			String newQueryString = oldQueryString;
			for (String term : chosenTerms){
				if (!oldQueryString.contains(term)) newQueryString += " " + term;
			}
			newQueryMap.put(queryID, newQueryString);
			//System.out.println(queryID + " old: " + oldQueryString);
			//System.out.println(queryID + " new: " + newQueryString);
		}
		bm25.queryMap = newQueryMap;
		bm25.numDocsRetrieved = this.numDocsRetrieved;
		bm25.systemName = bm25.systemName + "_Expanding";
		return bm25.search();
	}
	
	private ArrayList<String> processDocs(ArrayList<Integer> docList){
		HashMap<String, Integer> tfMap = new HashMap<String, Integer>();
		ArrayList<String> chosenTerms = new ArrayList<String>();
		for (Integer docID : docList){ // for top 5 docs, count term and tf
			String fileName = this.calculateDocName(docID);
			File file = new File(fileName);
			try {
				Scanner input = new Scanner(file);
				while (input.hasNextLine()){
					String line = input.nextLine();
					if (line.matches("ca\\d+.*")) break;
					String[] tokens = line.trim().split("\\s+");
					if (tokens[0].equals("")) continue;
					for (String s : tokens){
						if (tfMap.containsKey(s)){
							tfMap.put(s, tfMap.get(s)+1);
						}else{
							tfMap.put(s, 1);
						}
					}
				}
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		Set<Map.Entry<String, Integer>> entrySet = tfMap.entrySet();
		for (Map.Entry<String, Integer> entry : entrySet){ // store the result of one file to this.invertedIndex
			String term = entry.getKey(); // term
			Integer tf = entry.getValue(); // tf
			if (!this.commonWordsSet.contains(term) && tf >= this.minAppearance && !chosenTerms.contains(term)){
				chosenTerms.add(term);
				//System.out.println(term + "  " + tf);
			}
		}
		return chosenTerms;
	}
	
	private String calculateDocName(int docID){
	/*	String fileName = "";
    	if (docID < 10){
    		fileName = "000" + String.valueOf(docID);
    	}else if (docID < 100){
    		fileName = "00" + String.valueOf(docID);
    	}else if (docID < 1000){
    		fileName = "0" + String.valueOf(docID);
    	}else {
    		fileName = String.valueOf(docID);
    	}  */
    	String fileName = String.format("%04d",docID);
		
    	fileName = this.corpusDirectory + "CACM-" + fileName + ".txt";
    	//System.out.println(fileName);
    	return fileName;
	}
	
	private void loadCommonWords(){
		File cwFile = new File(this.commonWordsDir);
		try {
			Scanner s = new Scanner(cwFile);
			while (s.hasNextLine()){
				this.commonWordsSet.add(s.nextLine());
			}
			s.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
