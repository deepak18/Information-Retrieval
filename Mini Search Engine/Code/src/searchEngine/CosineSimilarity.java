package searchEngine;
import java.util.*;

public class CosineSimilarity {

	private HashMap<String, LinkedList<DocumentEntry>> invertedIndex; // term and list of docID & tf
	private HashMap<String, Integer> termDfMap; // term and df of that term
	private LinkedHashMap<Integer, String> queryMap; // query id and the query
	private double totalNumOfDocs; // total number of documents in corpus
	int numDocsRetrieved = 100;
	String systemName = "Cosine_Similarity";
	
	public CosineSimilarity(HashMap<String, LinkedList<DocumentEntry>> invertedIndex, HashMap<String, Integer> termDfMap,
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
		HashMap<Integer, Double> accumulator = new HashMap<>(); // docID and score
    	HashMap<Integer, Double> docLength = new HashMap<>(); // document length, by adding square of each weighted tf in document
    	HashMap<Integer, Double> scoreMap = new HashMap<>(); // docID and score
    	
    	String[] tokens = queryString.split("\\s"); // global, warming, potential
    	ArrayList<String> terms = new ArrayList<>(); // list of query terms
    	for (String s : tokens){ // treat multiple occurrence as one
    		if (!terms.contains(s))
    			terms.add(s);
    	}
    	
    	ArrayList<Double> queryWeight = new ArrayList<>();
    	this.calculateQueryWeight(terms, queryWeight);
    	
    	for (int i = 0; i < terms.size(); i++){
    		if (!this.invertedIndex.containsKey(terms.get(i))) continue; // term not in corpus
    		LinkedList<DocumentEntry> invertedList = this.invertedIndex.get(terms.get(i));
    		for (DocumentEntry de : invertedList){
    			int docID = de.docid;
    			double tf = de.tf;
    			double tfWt = 1 + Math.log(tf); // weighted tf
    			double score = queryWeight.get(i) * tfWt;
    			if (accumulator.containsKey(docID)){
    				double newScore = score + accumulator.get(docID);
    				accumulator.put(docID, newScore);
    				double newLength = Math.pow(tfWt, 2) + docLength.get(docID);
    				docLength.put(docID, newLength);
    			}else {
    				accumulator.put(docID, score);
    				docLength.put(docID, Math.pow(tfWt, 2));
    			}
    		}
    	}
    	if (accumulator.isEmpty()) return new QueryResult(-1, null, null, null); // no result
    	
    	ArrayList<Integer> docList = new ArrayList<>(accumulator.size()); // list of docId
    	for (Map.Entry<Integer, Double> entry : accumulator.entrySet()){
    		int docID = entry.getKey();
    		double score = entry.getValue();
    		double length = Math.sqrt(docLength.get(docID)); 
    		// document length, can be used to normalize, used in this implementation
    		double nScore = score / length; // normalize score by dividing length of document
    		//double nScore = score; // don't normalize
    		docList.add(docID);
    		scoreMap.put(docID, nScore);
    	}
    	
    	Collections.sort(docList, new Comparator<Integer>(){ // compare on score
    		
			@Override
			public int compare(Integer o1, Integer o2) {
				return scoreMap.get(o2).compareTo(scoreMap.get(o1));
			}
    	});
    	
    	// keep 100 result, or less if not have 100 result
    	ArrayList<Integer> resultList = new ArrayList<Integer>();
    	for (int i = 0; i < Math.min(this.numDocsRetrieved, docList.size()); i++){
    		resultList.add(docList.get(i));
    	}
    	HashMap<Integer, Double> scoreMapTruncated = new HashMap<Integer, Double>(); // doc map to doc score
    	for (Integer docID : resultList){
    		scoreMapTruncated.put(docID, scoreMap.get(docID));
    	}
    	
    	QueryResult qr = new QueryResult(queryID, resultList, scoreMapTruncated, this.systemName);
		return qr;
	}
	
	private void calculateQueryWeight(ArrayList<String> terms, ArrayList<Double> queryWeight){
    	for (int i = 0; i < terms.size(); i++){
    		if (this.termDfMap.containsKey(terms.get(i))){
    			double df = this.termDfMap.get(terms.get(i));
        		double idf = Math.log(this.totalNumOfDocs / df);
        		queryWeight.add(idf); // 1 * idf
    		}else {
    			queryWeight.add(0.0); // the term doesn't appear in this corpus
    		}
    		
    	}
    	this.normalize(queryWeight);
    }
    
    /**
     * normalize the vector represented by the list
     * @param list
     */
    private void normalize(ArrayList<Double> list){
    	double squareSum = 0;
    	for (Double d : list){
    		squareSum += Math.pow(d, 2);
    	}
    	double squareRoot = Math.sqrt(squareSum);
    	for (int i = 0; i < list.size(); i++){
    		list.set(i, list.get(i) / squareRoot);
    	}
    }
    
}

