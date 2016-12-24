package searchEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class BM25Model {
	private HashMap<String, LinkedList<DocumentEntry>> invertedIndex; // term and list of docID & tf
	LinkedHashMap<Integer, String> queryMap; // query id and the query
	private HashMap<Integer, Integer> docIDLengthMap; // docID and length of the doc (total number of tokens)
	private int totalNumOfDocs; // total number of documents in corpus
	int numDocsRetrieved = 100;
	String systemName = "BM25";
	String relevanceInputFile= "resource/cacm_rel.txt";
	
	double k1 = 1.2, k2 = 100, b = 0.75;
	double avdl; // average doc length
	HashMap<Integer, HashSet<Integer>> relevanceJudgement = new HashMap<Integer, HashSet<Integer>>();
	
	public BM25Model(HashMap<String, LinkedList<DocumentEntry>> invertedIndex, LinkedHashMap<Integer, String> queryMap, 
			HashMap<Integer, Integer> docIDLengthMap, int totalNumOfDocs){
		this.invertedIndex = invertedIndex;
		this.docIDLengthMap = docIDLengthMap;
		this.queryMap = queryMap;
		this.totalNumOfDocs = totalNumOfDocs;
		this.loadRelevanceJudgement();
		this.avdl = this.calculateAvdl();
	}
	
	
	public ArrayList<QueryResult> search(){
		ArrayList<QueryResult> queryResultList = new ArrayList<QueryResult>(); // 64 query result
		Set<Map.Entry<Integer, String>> entrySet = this.queryMap.entrySet();
		for (Map.Entry<Integer, String> entry : entrySet){ // for each query
			int queryID = entry.getKey();
			String queryString = entry.getValue();
			QueryResult qr = this.searchForOneQuery(queryID, queryString);
			queryResultList.add(qr);
		}
		return queryResultList;
	}
	
	public QueryResult searchForOneQuery(int queryID, String queryString){
		HashMap<Integer, Double> scoreAccumulator = new HashMap<>(); // docID and score
		
		String[] tokens = queryString.split("\\s"); // global, warming, potential
    	ArrayList<String> termList = new ArrayList<>(); // list of query terms
    	HashMap<String, Integer> termFrequency = new HashMap<>(); // query term frequency
    	for (String s : tokens){ // treat multiple occurrence as one
    		if (termList.contains(s)){
    			termFrequency.put(s, termFrequency.get(s) + 1);
    		}else {
    			termList.add(s);
    			termFrequency.put(s, 1);
    		}
    	}
    	
    	for (int i = 0; i < termList.size(); i++){ // term at a time
    		String term = termList.get(i);
    		if (!this.invertedIndex.containsKey(term)) continue; // term not in corpus
    		
    		double qfWeight = this.calculateQrWeight(termFrequency.get(term)); // third part
    		LinkedList<DocumentEntry> invertedList = this.invertedIndex.get(term);
    		double relWeight = this.calculateRelevanceWeight(queryID, invertedList); // first part
    		
    		for (DocumentEntry de : invertedList){
    			int docID = de.docid;
    			int tf = de.tf;
    			double tfWeight = this.calculateTfWeight(docID, tf); // second part
    			double score = relWeight * tfWeight * qfWeight;
    			
    			if (scoreAccumulator.containsKey(docID)){
    				double newScore = score + scoreAccumulator.get(docID);
    				scoreAccumulator.put(docID, newScore);
    			}else {
    				scoreAccumulator.put(docID, score);
    			}
    		}
    	}
    	if (scoreAccumulator.isEmpty()) return new QueryResult(-1, null, null, null); // no result
    	ArrayList<Integer> docList = new ArrayList<>(scoreAccumulator.size()); // list of docId
    	for (Map.Entry<Integer, Double> entry : scoreAccumulator.entrySet()){
    		docList.add(entry.getKey());
    	}

    	Collections.sort(docList, new Comparator<Integer>(){ // compare on score

    		@Override
    		public int compare(Integer o1, Integer o2) {
    			return scoreAccumulator.get(o2).compareTo(scoreAccumulator.get(o1));
    		}
    	});
    	
    	// keep 100 result, or less if not have 100 result
    	ArrayList<Integer> resultList = new ArrayList<Integer>();
    	for (int i = 0; i < Math.min(this.numDocsRetrieved, docList.size()); i++){
    		resultList.add(docList.get(i));
    	}
    	HashMap<Integer, Double> scoreMap = new HashMap<Integer, Double>(); // doc map to doc score
    	for (Integer docID : resultList){
    		scoreMap.put(docID, scoreAccumulator.get(docID));
    	}
    	
    	QueryResult qr = new QueryResult(queryID, resultList, scoreMap, this.systemName);
		return qr;
		
	}
	
	private double calculateRelevanceWeight(int queryID, LinkedList<DocumentEntry> invertedList){ // for term in query
		double N = this.totalNumOfDocs;
		double ni = invertedList.size(); // total number of appearance 
		double R;
		double ri;
		if (this.relevanceJudgement.containsKey(queryID)){
			HashSet<Integer> docSet = this.relevanceJudgement.get(queryID);
			R = docSet.size(); // total number of relevant files
			double accumulator = 0;
			for (DocumentEntry de : invertedList){
				if (docSet.contains(de.docid)) accumulator++;
			}
			ri = accumulator;
		} else {
			R = 0;
			ri = 0;
		}
		
		double up = (ri + 0.5)/(R - ri + 0.5);
		double down = (ni - ri + 0.5)/(N - ni - R + ri + 0.5);
		return Math.log(up/down);
	}
	
	private double calculateAvdl(){
		double sum = 0.0;
		Set<Map.Entry<Integer, Integer>> entrySet = this.docIDLengthMap.entrySet();
		for (Map.Entry<Integer, Integer> entry : entrySet){ // for each doc
			int length = entry.getValue();
			sum += length;
		}
		return sum / this.totalNumOfDocs;
	}
	
	private double calculateTfWeight(int docID, int tf){
		int dl = this.docIDLengthMap.get(docID);
		double K = this.k1 * ((1 - this.b) + (this.b * (dl / this.avdl)));
		return ((this.k1 + 1) * tf) / (K + tf);
	}
	
	private double calculateQrWeight(int queryTf){
		return ((this.k2 + 1) * queryTf) / (this.k2 + queryTf); // e.g. queryTf = 2, 202/102
	}
	
	private void loadRelevanceJudgement() {
		File file = new File(this.relevanceInputFile);
		try {
			Scanner input = new Scanner(file);
			while (input.hasNext()){
				String nextLine = input.nextLine();
				String parts[] = nextLine.split(" ");
				String docName = parts[2];
				int queryID = Integer.valueOf(parts[0]);
				int docID = Integer.valueOf(docName.substring(5));  // getting docID
				//System.out.println(queryID + "  " + docID);
				if(this.relevanceJudgement.containsKey(queryID)){
					this.relevanceJudgement.get(queryID).add(docID);
				}
				else{
					HashSet<Integer> docSet = new HashSet<Integer>();
					docSet.add(docID);
					this.relevanceJudgement.put(queryID, docSet);
				}
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
