package searchEngine;

import java.util.*;
import java.io.*;

public class Stemming {
	HashMap<String, LinkedList<DocumentEntry>> invertedIndex = new HashMap<String, LinkedList<DocumentEntry>>();  // term and list of docID & tf
	HashMap<String, Integer> termDfMap = new HashMap<>(); // term and df of that term
	HashMap<Integer, Integer> docIDLengthMap = new HashMap<Integer, Integer>(); // docID and length of the doc (total number of tokens)
	LinkedHashMap<Integer, String> queryMap = new LinkedHashMap<Integer, String>(); // query id and the query
	int totalNumOfDocs; // total number of documents in corpus
	String stemCorpusDir = "resource/cacm_stem.txt";
	String stemQueryDir = "resource/cacm_stem.query.txt";
	
	String resultDirectory = "index result/stemming/";
	HashMap<Integer, Integer> queryIDMap = new HashMap<Integer, Integer>();
	
	public Stemming(){
		File folder = new File(this.resultDirectory);
		if (!folder.exists()) folder.mkdirs();
		File f = new File(this.resultDirectory + "invertedIndex.dat");
		this.initQueryIDMap();
		if (f.exists()){
			this.readObjs();
		} else {
			this.loadCorpus();
			this.calculateDfMap();
			this.saveIndexResult();
			this.saveDocIDandNumTokens();
			this.saveObjs();
		}
		this.loadQueries();
	}
	
	private void initQueryIDMap(){
		this.queryIDMap.put(1, 12); // portabl oper system 
		this.queryIDMap.put(2, 13); // code optim for space effici
		this.queryIDMap.put(3, 19); // parallel algorithm 
		this.queryIDMap.put(4, 23); // distribut comput structur and algorithm 
		this.queryIDMap.put(5, 24); // appli stochast process
		this.queryIDMap.put(6, 25); // perform evalu and model of comput system
		this.queryIDMap.put(7, 50); // parallel processor in inform retriev 
	}
	
	private void loadQueries(){
		File queryFile = new File(this.stemQueryDir);
		try {
			Scanner input = new Scanner(queryFile);
			int i = 1;
			while (input.hasNextLine()){
				String nextLine = input.nextLine();
				this.queryMap.put(this.queryIDMap.get(i), nextLine);
				//System.out.println(i + "  " + nextLine);
				i++;
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void loadCorpus(){
		File corpusFile = new File(this.stemCorpusDir);
		try {
			Scanner input = new Scanner(corpusFile);
			String firstLine = input.nextLine();
			int docID = Integer.valueOf(firstLine.substring(2));
			HashMap<String, Integer> terms = new HashMap<String, Integer>(); // for all terms and tf in one file
			while (input.hasNextLine()){
				String line = input.nextLine();
				if (line.startsWith("#")){
					Set<Map.Entry<String, Integer>> entrySet = terms.entrySet();
					int totalTokens = 0; // total number of tokens in one file (doc length)
					for (Map.Entry<String, Integer> entry : entrySet){ // store the result of one file to this.invertedIndex
						String key = entry.getKey(); // term
						Integer value = entry.getValue(); // tf
						totalTokens += value;
						DocumentEntry de = new DocumentEntry(docID, value);
						//System.out.println(key + "  " + value);
						if (this.invertedIndex.containsKey(key)){
							this.invertedIndex.get(key).add(de);
						}else {
							LinkedList<DocumentEntry> list = new LinkedList<DocumentEntry>();
							list.add(de);
							this.invertedIndex.put(key, list);
						}
					}
					this.docIDLengthMap.put(docID, totalTokens);
					docID = Integer.valueOf(line.substring(2));
					terms = new HashMap<String, Integer>(); // new hash map for new file
				}else {
					String[] tokens = line.trim().split("\\s+");
					if (tokens[0].equals("")) continue;
					//if (tokens.length == 0) continue;
					for (String s : tokens){
						if (terms.containsKey(s)){
							terms.put(s, terms.get(s)+1);
						}else{
							terms.put(s, 1);
						}
					}
				}
			}
			Set<Map.Entry<String, Integer>> entrySet = terms.entrySet();
			int totalTokens = 0; // total number of tokens in one file (doc length)
			for (Map.Entry<String, Integer> entry : entrySet){ // store the result of one file to this.invertedIndex
				String key = entry.getKey(); // term
				Integer value = entry.getValue(); // tf
				totalTokens += value;
				DocumentEntry de = new DocumentEntry(docID, value);
				//System.out.println(key + "  " + value);
				if (this.invertedIndex.containsKey(key)){
					this.invertedIndex.get(key).add(de);
				}else {
					LinkedList<DocumentEntry> list = new LinkedList<DocumentEntry>();
					list.add(de);
					this.invertedIndex.put(key, list);
				}
			}
			this.docIDLengthMap.put(docID, totalTokens);
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.totalNumOfDocs = this.docIDLengthMap.size();
		//System.out.println(this.totalNumOfDocs);
	}
	
	// fill this.termDfMap
	private void calculateDfMap(){
		Set<Map.Entry<String, LinkedList<DocumentEntry>>> entrySet = this.invertedIndex.entrySet(); // term and list
		for (Map.Entry<String, LinkedList<DocumentEntry>> entry : entrySet){
			String term = entry.getKey();
			LinkedList<DocumentEntry> list = entry.getValue();
			this.termDfMap.put(term, list.size());
		}
	}
	
	// save inverted index to txt file
	private void saveIndexResult(){
		File indexFile = new File(this.resultDirectory + "invertedIndex.txt");
		Set<Map.Entry<String, LinkedList<DocumentEntry>>> entrySet = this.invertedIndex.entrySet();
		ArrayList<String> termList = new ArrayList<String>();
		for (Map.Entry<String, LinkedList<DocumentEntry>> entry : entrySet){
			termList.add(entry.getKey());
		}
		Collections.sort(termList);
		try {
			PrintWriter output = new PrintWriter(indexFile);
			//Set<Map.Entry<String, LinkedList<DocumentEntry>>> entrySet = this.invertedIndex.entrySet();
			//for (Map.Entry<String, LinkedList<DocumentEntry>> entry : entrySet){
			for (String term : termList){
				String key = term;
				output.print(key + " ");
				LinkedList<DocumentEntry> value = this.invertedIndex.get(term);
				ListIterator<DocumentEntry> iterator = value.listIterator();
				while (iterator.hasNext()){
					DocumentEntry de = iterator.next();
					output.print(de.docid + "," + de.tf + " ");
				}
				output.println();
			}
			output.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// save docID, corresponding fileName and total number of tokens
	private void saveDocIDandNumTokens(){
		File docIDFile = new File(this.resultDirectory + "docIDLength.txt");
		try {
			PrintWriter output = new PrintWriter(docIDFile);
			output.println("Document ID | " + "Number of tokens");
			Set<Map.Entry<Integer, Integer>> entrySet = this.docIDLengthMap.entrySet();
			for (Map.Entry<Integer, Integer> entry : entrySet){
				int docID = entry.getKey();
				int length = entry.getValue();
				output.printf("%-15s %s", docID, length);
				output.println();
			}
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// save the three map 
	private void saveObjs(){
		try {
			ObjectOutputStream output1 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "invertedIndex.dat"));
			output1.writeObject(this.invertedIndex);
			output1.close();

			ObjectOutputStream output2 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "termDf.dat"));
			output2.writeObject(this.termDfMap);
			output2.close();

			ObjectOutputStream output4 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "docIDLength.dat"));
			output4.writeObject(this.docIDLengthMap);
			output4.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void readObjs(){
		try {
			ObjectInputStream input1 = new ObjectInputStream(new FileInputStream(this.resultDirectory + "invertedIndex.dat"));
			this.invertedIndex = (HashMap<String, LinkedList<DocumentEntry>>) input1.readObject();
			input1.close();
			
			ObjectInputStream input2 = new ObjectInputStream(new FileInputStream(this.resultDirectory + "termDf.dat"));
			this.termDfMap = (HashMap<String, Integer>) input2.readObject();
			input2.close();
			
			ObjectInputStream input4 = new ObjectInputStream(new FileInputStream(this.resultDirectory + "docIDLength.dat"));
			this.docIDLengthMap = (HashMap<Integer, Integer>) input4.readObject();
			input4.close();
			
			this.totalNumOfDocs = this.docIDLengthMap.size();
			//System.out.println(this.totalNumOfDocs);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
//	public static void main(String[] args){
//		Stemming s = new Stemming();
//	}

}
