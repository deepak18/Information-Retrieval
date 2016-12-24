import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

class DocumentEntry implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public int docid;
	public int tf;

	public DocumentEntry(int docid, int tf) {
		this.docid = docid;
		this.tf = tf;
	}

	public DocumentEntry() {

	}
}

public class CosineSimilarity {

	// documentScore(docid, score)
	static LinkedHashMap<Integer, Double> documentScore = new LinkedHashMap<Integer, Double>();
	
	// queryVector(queryTerm, queryWeight)
	static LinkedHashMap<String,Double> queryVector = new LinkedHashMap<String,Double>();
	
	// invertedIndex(1-gram,(docid,tf))
	static LinkedHashMap<String, ArrayList<DocumentEntry>> unigramInvertedIndex = new LinkedHashMap<String, ArrayList<DocumentEntry>>();
	
	// docId(corpusfile,docid)
	static LinkedHashMap<Integer, String> docID = new LinkedHashMap<Integer, String>();
	
	// tokenCount(docid,#tokens)
	static LinkedHashMap<Integer, Integer> tokenCountPerDoc = new LinkedHashMap<Integer, Integer>();
	
	static String CORPUS_DIRECTORY = "Corpus/";
	
	private static String task2Tables = "Task2/";
	
	@SuppressWarnings({ "resource", "unchecked" })
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		// generating the DocId and Document_Title mapping
		mapDocID();
		
		// Read the unigram inverted indexes from "file.out" into unigramInvertedIndex
		System.out.println("Reading Hashmap of Inverted Indexes");
		FileInputStream fin = new FileInputStream("file.out");
		ObjectInputStream ois = new ObjectInputStream(fin);
		unigramInvertedIndex = (LinkedHashMap<String, ArrayList<DocumentEntry>>) ois.readObject();
		ois.close();
		
		// Read the token count of each document in tokenCountPerDoc
		System.out.println("Reading data structure of token count per document\n");
		fin = new FileInputStream("tokenPerDoc.out");
		ois = new ObjectInputStream(fin);
		tokenCountPerDoc = (LinkedHashMap<Integer, Integer>) ois.readObject();
		ois.close();
		
		String s =" ";
		String[] queryTerms;  // to hold individual query terms
		int queryID = 0;
		
		File inputFile  = new File("queries.txt");
		
		try{
			
			Scanner inputQuery = new Scanner(inputFile);
			while(inputQuery.hasNextLine()){
				s = inputQuery.nextLine();
				s = s.toLowerCase();  // converts query to lower case same as when we done while indexing
				queryTerms = s.split("\\s+");   // getting query terms from whole query
				queryID++;
				
				// makes a query vector based on the weigths : here based on frequency
				makeQueryVector(queryTerms);
				
				// calculate document score for each document
				calculateDocumentScore(queryTerms);
				
				// sort the documentScore data structure based on score
				sortDocumentByScore();
				
				// writing top results to the file
				System.out.println("Writing results of query: " + s +"\n");
				getTopDocuments(s,queryID);
			}
		}catch(Exception e){
			System.out.println("The input file \"queries.txt\" was not found");
			e.printStackTrace();
		}
	}
	

	private static void makeQueryVector(String[] queryTerms) {
		// clearing queryVector
		queryVector.clear();
		String index;
		double newWeight;

		// initializing the queryVector with 0
		for(Map.Entry<String, ArrayList<DocumentEntry>> invertedindex : unigramInvertedIndex.entrySet()){
			index = invertedindex.getKey();  // each unigram index
			newWeight = 0.0;
			queryVector.put(index, newWeight);
		}
		
		// adding tf of query that are in queryTerms to queryVector
		for(String query: queryTerms){
			newWeight = 1.0;
			if(queryVector.containsKey(query)){
				newWeight+=queryVector.get(query); // added one to the tf of that query term
				queryVector.put(query, newWeight);
			}
		}
	}
	
	private static void calculateDocumentScore(String[] queryTerms) {
		int docId;
		double numerator = 0, denominator = 0;
		double cosineScore = 0;
		
		documentScore.clear();
		
		// Calculating score for each document
		for(int i=0; i< docID.size();i++){
			docId = i;
			// get the numerator value to calculate document score
			numerator = getNumerator(docId);
			
			// get the denominator value to calculate document score
			denominator = getDenominator(docId);
			
			cosineScore = numerator / denominator;
			// writing the docId and cosine_score in the hashmap
			documentScore.put(docId, cosineScore);
		}
	//	System.out.println(documentScore);
	}

	private static double getNumerator(int docId) {
		double numeratorScore = 0;
		double normalizedTf = 0;
		double Idf = 0;
		double normalizedTfIdf=0;
		
		// taking each query entry from queryVector
		for(Map.Entry<String, Double> query : queryVector.entrySet()){
			// getting the posting of particular query term from inverted indexes
			ArrayList<DocumentEntry> posting = new ArrayList<DocumentEntry>();
			posting = unigramInvertedIndex.get(query.getKey());
			
			// if no posting of the term, returns 0
		//	if(posting.isEmpty()) continue;
			
			// finding the doc with docId in the posting and retrieving the tf of query in it
			for(DocumentEntry docEntry : posting){
				if(query.getValue() != 0 && docEntry.docid == docId){
					// normalizing tf by dividing by document length
					normalizedTf = docEntry.tf / (double) tokenCountPerDoc.get(docId);
					// calculates idf for the query in the corpus
					Idf = 1 + Math.log(docID.size() / posting.size());
					normalizedTfIdf = normalizedTf * Idf;
					// score is normalized for document and depends on tf for query term
					numeratorScore += (normalizedTfIdf * query.getValue());
					
					break;
				}
			}
		}
		
		return numeratorScore;
	}
	
	private static double getDenominator(int docId) {
		double denominatorScore = 1.0;
		double sumSquareDoc = 0;
		double sumSquareQuery = 0;
		double normalizedTf = 0;
		double Idf = 0;
		double normalizedTfIdf = 0;
		
		// taking each query entry from queryVector
		for(Map.Entry<String, Double> query : queryVector.entrySet()){
			
			// getting the posting of particular query term from inverted indexes
			ArrayList<DocumentEntry> posting = new ArrayList<DocumentEntry>();
			posting = unigramInvertedIndex.get(query.getKey());

			// if no posting of the term, checks for next query term
		//	if(posting.isEmpty()) continue;
			
			// finding the doc with docId in the posting and retrieving the tf of query in it
			for(DocumentEntry docEntry : posting){
				if(docEntry.docid == docId){
					// normalizing tf by dividing by document length
					normalizedTf = docEntry.tf /  (double) tokenCountPerDoc.get(docId);
					// calculates idf for the query in the corpus
					Idf = 1 + Math.log(docID.size() / posting.size());
					normalizedTfIdf = normalizedTf * Idf;
					sumSquareDoc += (normalizedTfIdf * normalizedTfIdf );
					break;
				}
			}
			
			sumSquareQuery += (query.getValue() * query.getValue());
		}
		
		denominatorScore = Math.sqrt(sumSquareDoc * sumSquareQuery);
		
		// in case no query term is in our index
		if(denominatorScore == 0)
			return 1.0;
		else
			return denominatorScore;
	}
	
	private static void sortDocumentByScore() {
		
		//LinkedHashMap<Integer,Double> documentScore : <docid,score>
		ArrayList<Map.Entry<Integer,Double>> entries =
				new ArrayList<Map.Entry<Integer,Double>>(documentScore.entrySet());
		// sorting array in decreasing order of PageRank
		Collections.sort(entries, new Comparator<Map.Entry<Integer,Double>>() {
			public int compare(Map.Entry<Integer,Double> a, Map.Entry<Integer,Double> b){
				return b.getValue().compareTo(a.getValue());

			}
		});

		documentScore.clear();
		
		// putting back the sorted values
		for (Map.Entry<Integer,Double> entry : entries) {
			documentScore.put(entry.getKey(), entry.getValue());
		}
	}

	private static void getTopDocuments(String s, int queryID) throws IOException {
		String location = task2Tables + s + ".csv";
		File output = new File(location);
		FileWriter out = new FileWriter(output);
		out.close();
		
		int topCount = docID.size();  // Count of top documents needed
		
		Set<Entry<Integer, Double>> set = documentScore.entrySet();
	      
		// Get an iterator
	    Iterator<Entry<Integer, Double>> itr = set.iterator();
	      
	    // Display elements
	   for(int i=1 ; i<=topCount;i++){
		   out = new FileWriter(output,true);
		   Map.Entry<Integer, Double> document = (Map.Entry<Integer, Double>)itr.next();
		   // System.out.println(i + ".  " + docID.get(document.getKey()) + "  " + document.getKey() + "  " + document.getValue());
		   out.write(queryID + ",Q0," + docID.get(document.getKey()) + ","  + i  + "," + document.getValue() + ",Cosine_Relevance," + "\n");
		   out.close();
	   }
	}
	
	private static void mapDocID() throws IOException {
		File dir = new File(CORPUS_DIRECTORY);
		File[] files = dir.listFiles();
		int id = 0;
		String file;

		// for each file in the Corpus put docid,document_title pair in data structure
		for (File corpusfile : files) {
			file = corpusfile.getName();
			file = file.replace(".txt", "");
			if (!docID.containsValue(file)) {
				docID.put(id, file);
				id++;
			}
		}

		// writes to the file
		File linkfile = new File("DocIDmapping.csv");
		FileWriter fw = new FileWriter(linkfile);
		fw.close();

		Set<Entry<Integer, String>> set = docID.entrySet();

		// Get an iterator
		Iterator<Entry<Integer, String>> itr = set.iterator();

		fw = new FileWriter(linkfile, true);
		// Writing data structure to the file
		while (itr.hasNext()) {
			Map.Entry<Integer, String> me = (Map.Entry<Integer, String>) itr.next();
			// write the docID and the corresponding # of Tokens
			fw.write(me.getKey() + "," + me.getValue() + "\n");

		}
		fw.close();
	}
}
