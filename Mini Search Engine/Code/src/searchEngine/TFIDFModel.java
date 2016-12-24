package searchEngine;
import java.util.*;

public class TFIDFModel {
	private HashMap<String, LinkedList<DocumentEntry>> invertedIndex; // term and list of docID & tf
	private HashMap<String, Integer> termDfMap; // term and df of that term
	private LinkedHashMap<Integer, String> queryMap; // query id and the query
	private double totalNumOfDocs; // total number of documents in corpus
	String systemName = "TFIDF";
	int numDocsRetrieved = 100;
	
	public TFIDFModel(HashMap<String, LinkedList<DocumentEntry>> invertedIndex, HashMap<String, Integer> termDfMap,
			LinkedHashMap<Integer, String> queryMap, int totalNumOfDocs){
		this.invertedIndex = invertedIndex;
		this.termDfMap = termDfMap;
		this.queryMap = queryMap;
		this.totalNumOfDocs = totalNumOfDocs;
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
	
	private QueryResult searchForOneQuery(int queryID, String queryString){
		HashMap<Integer, Double> scoreAccumulator = new HashMap<Integer, Double>();
		String[] queryTokens = queryString.split("\\s+");
		//System.out.println(queryString);
		for (int i = 0; i < queryTokens.length; i++){ // for each term
			String term = queryTokens[i];
			if (!this.invertedIndex.containsKey(term)) continue;
			LinkedList<DocumentEntry> invertedList = this.invertedIndex.get(queryTokens[i]);
			double termWeight = Math.log(this.totalNumOfDocs / this.termDfMap.get(term)); // logarithmically scaled idf
			for (DocumentEntry de : invertedList){
				int docID = de.docid;
				int tf = de.tf;
				double docWeight = 1.0 + Math.log(tf); // logarithmically scaled tf
				double score = termWeight * docWeight;
				if (scoreAccumulator.containsKey(docID)){
					scoreAccumulator.put(docID, scoreAccumulator.get(docID) + score);
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
    	//Collections.reverse(docList);
    	
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

}
