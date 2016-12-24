package snippetGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import evaluation.searchEngineEvaluation;
import searchEngine.BM25Model;
import searchEngine.CorpusBuilding;
import searchEngine.DocumentEntry;
import searchEngine.QueryResult;

class SentenceSpan{
	String[] words;
	double significanceFactor;
	int spanStart;
	int spanEnd;
	
	SentenceSpan(String[] words, double significanceFactor, int start, int end){
		this.words = words;
		this.significanceFactor = significanceFactor;
		this.spanStart = start;
		this.spanEnd = end;
	}
	
	public double getSignificanceFactor(){
		return this.significanceFactor;
	}
}

public class SnippetGeneration {

	private HashMap<String, LinkedList<DocumentEntry>> invertedIndex; // term and list of docID & tf
	private HashMap<String, Integer> termDfMap; // term and df of that term
	private LinkedHashMap<Integer, String> docIDNameMap; // docID and corresponding doc name
	private HashMap<Integer, Integer> docIDLengthMap; // docID and length of the doc (total number of tokens)
	private LinkedHashMap<Integer, String> queryMap = new LinkedHashMap<Integer, String>(); // query id and the query
	private int totalNumOfDocs; // total number of documents in corpus

	private CorpusBuilding cb = new CorpusBuilding(); // use the cleanText method to process query
	String htmlDirectory = "resource/cacm/";
	String indexDirectory = "index result/";
	String queryDirectory = "resource/cacm.query";
	String commonWordsDir = "resource/common_words";
	String resultDirectory = "retrieval result/";
	String searchModel = "TFIDF";
	String luceneIndexDirectory = "Lucene Index/";

 	private QueryResult queryResult_BM25; // 64 query result for BM25
	private int otherQueryID = 65;
	// word and its frequency in document
	private LinkedHashMap<String, Integer> wordFrequency = new LinkedHashMap<String, Integer>(); 
	ArrayList<SentenceSpan> sentencesData = new ArrayList<>(); // holds sentences and their significance factor, span start,end
	HashSet<String> commonWordsSet = new HashSet<String>();
	
	private void performSearch(){
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("---------------------------- Mini Search Engine ----------------------------");	
		int choice = 1;
		while(choice != 3){
			System.out.println("1. Pre-selected Queries (four queires total)");
			System.out.println("2. Search your own Query");
			System.out.println("3. Exit");
			BM25Model bm = new BM25Model(this.invertedIndex, this.queryMap, this.docIDLengthMap, this.totalNumOfDocs);
			
			try {
				choice = Integer.parseInt(br.readLine());
				System.out.println(choice);
				if(choice == 1){
					
					String[] queryOptions =
						{"code optimization for space efficiency", 
						"portable operating systems", 
						"Distributed computing structures and algorithms",
						"Parallel processors in information retrieval"};
					
					String s = queryOptions[(int) (Math.random() * 3)];
					System.out.println("Query : "+ s);
					String queryString = s.trim().toLowerCase();	
					int queryID = this.getQueryID(queryString);
					
					// result for a single query from BM25 model
					this.queryResult_BM25 = bm.searchForOneQuery(queryID, queryString);
					this.displayResults(queryString);
					this.wordFrequency.clear();
					
				}
				else if(choice == 2){
					searchYourQuery();
				} else if (choice == 3){
					System.exit(0);
				}
				else {
					System.out.println("Ooops ! Try Agian.");
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
		
	private void searchYourQuery(){
		// BM25
		BM25Model bm = new BM25Model(this.invertedIndex, this.queryMap, this.docIDLengthMap, this.totalNumOfDocs);
		
		String s = "";

		while (!s.equalsIgnoreCase("q")) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("Ready for search, please enter the query. (q=quit): ");
				s = br.readLine();
				if (s.equalsIgnoreCase("q")) {
					break;
				}
				String queryString = s.trim().toLowerCase();	
				int queryID = this.getQueryID(queryString);
				
				// result for a single query from BM25 model
				this.queryResult_BM25 = bm.searchForOneQuery(queryID, queryString);
				this.displayResults(queryString);
				this.wordFrequency.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void displayResults(String queryString) {
		int displayCount = 10;
		int queryID = this.queryResult_BM25.getQueryID();
		ArrayList<Integer> docList = this.queryResult_BM25.getDocList();
		if (docList == null){
			System.out.println("No results found, try some other queires");
			return;
		}
		System.out.print("\n");
		for(int i=0; i<displayCount; i++){
			int docID = docList.get(i);
			this.wordFrequency.clear();
			System.out.println("Doc = " + docID);
			
			String[] docText = parseHtmlFile(docID); // title and the body text
			String title = docText[0];
		//	System.out.println(docText[1] + "\n");
			String[] sentences = docText[1].split("\\.");
			// trimming the white spaces in beginning of each sentence
			for(int s=0; s<sentences.length;s++){ sentences[s]= sentences[s].trim();}
			
			this.generateWordFrequencies(title, sentences);
			
			System.out.println("Doc Title: " + title);
			this.getSignificantSentences(sentences, queryString);
				
			System.out.print("------------------------------------------------------------------------------------\n");
				
		}
		
	}

	private void getSignificantSentences(String[] sentences, String queryString) {
		int noOfSentences = sentences.length;
		this.sentencesData.clear();
		for(int i=0; i<noOfSentences; i++){
			sentences[i] = sentences[i].replaceAll("\\s+", " ");
			
			sentences[i] = sentences[i].trim();
	//		System.out.println("Sentence: " + sentences[i]+ "deepak");
			if(sentences[i] != null && !sentences[i].isEmpty()){
				this.sentencesData.add(getSignificantFactor(sentences[i], noOfSentences));
			}
		}
		
		Collections.sort(this.sentencesData, new Comparator<SentenceSpan>() {
		    @Override
		    public int compare(SentenceSpan s1, SentenceSpan s2) {
		        return Double.compare(s2.getSignificanceFactor(), s1.getSignificanceFactor());
		    }
		});

		String[] queryTerms = queryString.split("\\s+");
		//-------------------------------------------------------------------------------------//
		// Displaying Snippet
		//	System.out.println("Snippet");
		int sentencesToDisplay = Math.min(2, this.sentencesData.size());
		boolean match = false;
		
		for(int i=0; i< sentencesToDisplay; i++){
			SentenceSpan s = this.sentencesData.get(i);
			// Maximum 15 character long line of snippet
			for(int j=s.spanStart ; j<= Math.min(s.spanEnd, s.spanStart+12); j++){
				match = false;
				for(int q=0; q<queryTerms.length; q++){
					if(s.words[j].equalsIgnoreCase(queryTerms[q])){
						System.out.print("["+s.words[j]+"] ");
						match = true;
						break;
					}
				}
				if(! match)
					System.out.print(s.words[j] + " ");
			}
			System.out.print("..\n");
		}
	}

	private SentenceSpan getSignificantFactor(String sentence, int noOfSentences) { 
		
		String regexString = "[.,]+(?!\\d)|<\\/*\\w+>|[^A-Za-z0-9\\s-]";
		Pattern pattern = Pattern.compile(regexString);
		String formattedSentence = pattern.matcher(sentence).replaceAll(" ").trim();
		
		String[] words = formattedSentence.split("[^A-Za-z0-9-]+");
		int noOfWords = words.length;
		boolean[] significantWord = new boolean[noOfWords];
	//	System.out.println("no of words in sentence : " + noOfWords);
		for(int i=0; i<noOfWords; i++){
			String word = words[i].trim().toLowerCase();
	//		System.out.println("Word = " + word);
			
			if(this.commonWordsSet.contains(word)){
				significantWord[i] = false;
			}
			else if(noOfSentences < 25){
				significantWord[i] = (this.wordFrequency.get(word) >= (3 - 0.1 * (25 - noOfSentences)));
			}
			else if(noOfSentences >= 25 && noOfSentences < 40){
				significantWord[i] = (this.wordFrequency.get(word) >= 7) ;
			}
			else{
				significantWord[i] = (this.wordFrequency.get(word) >= (3 + 0.1 * (noOfSentences - 40)));
			}
		}
		
	/*	for(int i=0;i<noOfWords;i++)
			System.out.println(words[i] + " " + significantWord[i]);  */
		
		int noOfSignificants = 0;
		int noOfNonSignificants = 0;
		int spanLength = 0;
		boolean span = false;
		int spanStart = 0, spanEnd = noOfWords-1;
		int allowedNonSignificantWords = 4;
		
		for(int i=0; i<noOfWords;i++){
			if(significantWord[i] == false && span == false) continue;
			else if (significantWord[i] == true ){
				if(span == false){spanStart = i;}
				span = true;
				noOfSignificants++;
				spanLength++;
			}
			else if(significantWord[i] == false && span == true){
				noOfNonSignificants++;
				if(noOfNonSignificants > allowedNonSignificantWords){
					
					spanEnd = i-1;
					break;
				}
				spanLength++;
			}
		}
	//	System.out.println("Sentence: " + sentence);
	//	System.out.println("Start : " + spanStart + ". End: " +spanEnd + "  Spanlength : " +spanLength);
		double significanceFactor = 0;
		
		if(spanLength != 0){
			significanceFactor = (noOfSignificants * noOfSignificants)/(double)spanLength;
		}
	//	System.out.println("Significance factor: " + significanceFactor);
		SentenceSpan spanData = new SentenceSpan(words, significanceFactor, spanStart, spanEnd);
		return spanData;
	}

	
	private void generateWordFrequencies(String title, String[] sentences) {
		String regexString = "[.,]+(?!\\d)|<\\/*\\w+>|[^A-Za-z0-9\\s-]";
		Pattern pattern = Pattern.compile(regexString);
	//	System.out.println("----------------------------------");
		String formattedTitle = pattern.matcher(title).replaceAll(" ").toLowerCase();
	//	System.out.println(formattedTitle);
		
		String[] formattedSentences = new String[sentences.length];
		
		for(int i=0; i<sentences.length; i++){
			
			formattedSentences[i] = pattern.matcher(sentences[i]).replaceAll(" ").toLowerCase();
			formattedSentences[i] = formattedSentences[i].trim();
		//	System.out.println(formattedSentences[i]);
		}
	//	System.out.println("----------------------------------");
		this.wordFrequency.clear();
		
		String[] wordsInTitle = formattedTitle.split("\\s+");
		if (!wordsInTitle.equals("")){
			for(int i = 0; i<wordsInTitle.length; i++){
				if(this.wordFrequency.containsKey(wordsInTitle[i]))
					this.wordFrequency.put(wordsInTitle[i], this.wordFrequency.get(wordsInTitle[i])+1);
				else
					this.wordFrequency.put(wordsInTitle[i], 1);
			}
		}

		for(int i=0; i<formattedSentences.length; i++){
			formattedSentences[i] = formattedSentences[i].replaceAll("\\s+", " ");
			String[] wordsInSentence = formattedSentences[i].split("\\s+");
			if (!wordsInSentence.equals("")){
				for(int s=0; s<wordsInSentence.length; s++){
					if(this.wordFrequency.containsKey(wordsInSentence[s]))
						this.wordFrequency.put(wordsInSentence[s], this.wordFrequency.get(wordsInSentence[s])+1);
					else
						this.wordFrequency.put(wordsInSentence[s], 1);
				}
			}
		}
	/*	System.out.println("Word and frequencies");
		for(Map.Entry<String, Integer> entry : this.wordFrequency.entrySet()){
			System.out.println(entry.getKey() + " " + entry.getValue());
		} */
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
	
	private String[] parseHtmlFile(int docID){
		String formattedDocID = String.format("%04d",docID);
		String fileName = this.htmlDirectory + "CACM-"+formattedDocID + ".html";
		String docText[] = {"",""};
		//System.out.println(fileName);
		File htmlFile = new File(fileName);
		try {
			Scanner input = new Scanner(htmlFile);
			while(input.hasNext()){
				String nextLine = input.nextLine();
				String title = this.cleanText(nextLine);
				if (title.equals("")) { continue;}
				title = title.replaceAll("\\s+", " ");
				docText[0] = title;  // storing title
		//		System.out.println(docText[0]);
				break;
			}
			
			String c;
			do{
				String currentLine = input.nextLine();
				c = this.cleanText(currentLine);
			}while(c == "");
			
			while (input.hasNext()){
				
				String nextLine = input.nextLine();
				String s = this.cleanText(nextLine);
				if (!s.equals("")) {
					if(s.matches("CA\\d{6}.*")){
						break;
					}
					c = c.replaceAll("\\s+", " ");
					docText[1]+= c.trim() +" ";	
					c = s;
				}
				
			}
			input.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		
		return docText;
	}
	

	public String cleanText(String s){
		String regexString = "<\\/*\\w+>";
		Pattern pattern = Pattern.compile(regexString);
		return pattern.matcher(s).replaceAll("");
	}

	private int getQueryID(String queryString) {
		for(Map.Entry<Integer, String> entry : this.queryMap.entrySet()){
			if(queryString.equals(entry.getValue())){
				return entry.getKey();
			}
		}
		return (otherQueryID++);
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

	private void start(){
		File folder = new File(this.resultDirectory);
		if (!folder.exists()) folder.mkdirs();
		this.loadCommonWords();
		this.readObjs();
		this.readQueries();
		this.performSearch();
	//	this.saveObjs();
		System.out.println("Retrieval Done!");
	}

	public static void main(String[] args) {
		SnippetGeneration sg = new SnippetGeneration();
		sg.start();

	}

}
