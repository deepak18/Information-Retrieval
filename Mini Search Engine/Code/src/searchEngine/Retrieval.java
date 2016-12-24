package searchEngine;
import java.io.*;
import java.util.*;

public class Retrieval {
	private HashMap<String, LinkedList<DocumentEntry>> invertedIndex; // term and list of docID & tf
	private HashMap<String, Integer> termDfMap; // term and df of that term
	private LinkedHashMap<Integer, String> docIDNameMap; // docID and corresponding doc name
	private HashMap<Integer, Integer> docIDLengthMap; // docID and length of the doc (total number of tokens)
	private LinkedHashMap<Integer, String> queryMap = new LinkedHashMap<Integer, String>(); // query id and the query
	private int totalNumOfDocs; // total number of documents in corpus
	
	private CorpusBuilding cb = new CorpusBuilding(); // use the cleanText method to process query
	String indexDirectory = "index result/";
	String queryDirectory = "resource/cacm.query";
	String resultDirectory = "retrieval result/";
	String searchModel = "TFIDF";
	String luceneIndexDirectory = "Lucene Index/";
	
	private ArrayList<QueryResult> resultList_TFIDF; // 64 query result for tf-idf
	private ArrayList<QueryResult> resultList_CosineSimilarity; // 64 query result for Cosine Similarity
	private ArrayList<QueryResult> resultList_Lucene; // 64 query result for Lucene
	private ArrayList<QueryResult> resultList_BM25; // 64 query result for BM25
	private ArrayList<QueryResult> resultList_expandingBM25; // 64 query result for BM25 with query expanding
	private ArrayList<QueryResult> resultList_stoppingBM25; // 64 query result for BM25 with stopping
	private ArrayList<QueryResult> resultList_stemmingBM25; // 7 query result for BM25 with stemming
	private ArrayList<QueryResult> resultList_expandStopingBM25; // 64 query result for BM25 with query expanding and stopping
	
	private void performSearch(){
		
		// tf-idf model
		TFIDFModel tfidf = new TFIDFModel(this.invertedIndex, this.termDfMap, this.queryMap, this.totalNumOfDocs);
		resultList_TFIDF = tfidf.search();
		this.saveResult(resultList_TFIDF);
	
		// Cosine-Similarity model
		CosineSimilarity cs = new CosineSimilarity(this.invertedIndex, this.termDfMap, this.queryMap, this.totalNumOfDocs);
		resultList_CosineSimilarity = cs.search();
		this.saveResult(resultList_CosineSimilarity);
		
		// BM25
		BM25Model bm = new BM25Model(this.invertedIndex, this.queryMap, this.docIDLengthMap, this.totalNumOfDocs);
		this.resultList_BM25 = bm.search();
		this.saveResult(resultList_BM25); 
		
		// Lucene model
		try {
			Lucene indexer = new Lucene(this.queryMap, luceneIndexDirectory);
			resultList_Lucene = indexer.search(luceneIndexDirectory);
			this.saveResult(resultList_Lucene);
		} catch (IOException e) {
			System.out.println("Cannot create index..." + e.getMessage());
			System.exit(-1);
		}
		
		// BM25 with Pseudo Relevance Feedback
		BM25Model expandingBM = new BM25Model(this.invertedIndex, this.queryMap, this.docIDLengthMap, this.totalNumOfDocs);
		PseudoRelevanceFeedback prf = new PseudoRelevanceFeedback(expandingBM);
		this.resultList_expandingBM25 = prf.search();
		this.saveResult(resultList_expandingBM25);
		
		// BM25 with Stopping
		Stopping stopping = new Stopping(this.queryMap);
		BM25Model stoppingBM = new BM25Model(stopping.invertedIndex, stopping.queryMap, stopping.docIDLengthMap, stopping.totalNumOfDocs);
		stoppingBM.systemName += "_Stopping";
		this.resultList_stoppingBM25 = stoppingBM.search();
		this.saveResult(resultList_stoppingBM25);
		
		// BM25 with Stemming
		Stemming stemming = new Stemming();
		BM25Model stemmingBM = new BM25Model(stemming.invertedIndex, stemming.queryMap, stemming.docIDLengthMap, stemming.totalNumOfDocs);
		stemmingBM.systemName += "_Stemming";
		this.resultList_stemmingBM25 = stemmingBM.search();
		this.saveResult(resultList_stemmingBM25);
		
		// BM25 with Pseudo Relevance Feedback and Stopping
		Stopping stopping2 = new Stopping(this.queryMap);
		BM25Model stoppingBM2 = new BM25Model(stopping.invertedIndex, stopping.queryMap, stopping.docIDLengthMap, stopping.totalNumOfDocs);
		stoppingBM2.systemName += "_Stopping";
		PseudoRelevanceFeedback prf2 = new PseudoRelevanceFeedback(stoppingBM2);
		this.resultList_expandStopingBM25 = prf2.search();
		this.saveResult(resultList_expandStopingBM25);
		
	}
	
	// query_id	Q0 doc_id rank score system_name
	private void saveResult(ArrayList<QueryResult> resultList){
		QueryResult qr1 = resultList.get(0);
		String systemName = qr1.getSystemName();
		String dir = this.resultDirectory + systemName + " Result/";
		File folder = new File(dir);
		if (!folder.exists()) folder.mkdirs();
		
		for (QueryResult qr : resultList){
			int queryID = qr.getQueryID();
			ArrayList<Integer> docList = qr.getDocList();
			File resultFile = new File(dir + "Query" + queryID + ".txt");
			try {
				PrintWriter output = new PrintWriter(resultFile);
				for (int i = 0; i < docList.size(); i++){
					int docID = docList.get(i);
					int rank = i + 1;
					double score = qr.scoreMap.get(docID);
					output.println(queryID + " Q0 " + docID + " " + rank + " " + score + " " + systemName);
				}
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void readQueries(){
		File queryFile = new File(this.queryDirectory);
		try {
			Scanner input = new Scanner(queryFile);
			while (input.hasNextLine()){
				String line = input.nextLine();
				if (line.startsWith("<DOCNO>")){
					int queryID = Integer.valueOf(line.replaceAll("\\D", ""));
					String queryString = "";
					while (input.hasNextLine()){
						String s = input.nextLine();
						if (s.startsWith("</DOC>")) break;
						queryString += " " + s;
					}
					queryString = this.cb.cleanText(queryString.trim());
					queryString = queryString.replaceAll("\\s+", " ");
					this.queryMap.put(queryID, queryString);
					//System.out.println(queryID + "  " + queryString);
				}
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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
			//System.out.println(this.totalNumOfDocs);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	private void saveObjs(){
		try {
			ObjectOutputStream output1 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "TfIdf.dat"));
			output1.writeObject(this.resultList_TFIDF);
			output1.close();
			
			ObjectOutputStream output2 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "CosineSimilarity.dat"));
			output2.writeObject(this.resultList_CosineSimilarity);
			output2.close();
			
			ObjectOutputStream output3 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "LuceneAnalyzer.dat"));
			output3.writeObject(this.resultList_Lucene);
			output3.close();
			
			ObjectOutputStream output4 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "BM25.dat"));
			output4.writeObject(this.resultList_BM25);
			output4.close();
			
			ObjectOutputStream output5 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "BM25_Stopping.dat"));
			output5.writeObject(this.resultList_stoppingBM25);
			output5.close();
			
			ObjectOutputStream output6 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "BM25_Expanding.dat"));
			output6.writeObject(this.resultList_expandingBM25);
			output6.close();
			
			ObjectOutputStream output7 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "BM25_ExpandStoping.dat"));
			output7.writeObject(this.resultList_expandStopingBM25);
			output7.close();
			
			ObjectOutputStream output8 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "BM25_Stemming.dat"));
			output8.writeObject(this.resultList_stemmingBM25);
			output8.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void start(){
		File folder = new File(this.resultDirectory);
		if (!folder.exists()) folder.mkdirs();
		this.readObjs();
		this.readQueries();
		this.performSearch();
		this.saveObjs();
		System.out.println("Retrieval Done!");
	}

	public static void main(String[] args) {
		Retrieval r = new Retrieval();
		r.start();

	}

}
