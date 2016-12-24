package searchEngine;
import java.io.*;
import java.util.*;

public class Indexer {
	private HashMap<String, LinkedList<DocumentEntry>> invertedIndex = new HashMap<String, LinkedList<DocumentEntry>>();  // term and list of docID & tf
	private HashMap<String, Integer> termDfMap = new HashMap<>(); // term and df of that term
	private LinkedHashMap<Integer, String> docIDNameMap = new LinkedHashMap<Integer, String>(); // docID and corresponding doc name
	private HashMap<Integer, Integer> docIDLengthMap = new HashMap<Integer, Integer>(); // docID and length of the doc (total number of tokens)
	
	String corpusDirectory = "corpus/";
	String resultDirectory = "index result/";
	
	private void loadCorpus(){
		File folder = new File(this.corpusDirectory);
		File[] files = folder.listFiles();
		for (int i = 0; i < files.length; i++) { // for each file
			String fileName = files[i].getName(); // CACM-0001.txt
			String docName = fileName.replace(".txt", "");
			int docID = Integer.valueOf(docName.substring(5));
			this.docIDNameMap.put(docID, docName);
			//System.out.println(docName + "  " + docID);
			
			try {
				HashMap<String, Integer> terms = new HashMap<String, Integer>(); // for all terms and tf in one file
				Scanner input = new Scanner(files[i]);
				while (input.hasNext()){
					String nextLine = input.nextLine();
					String[] tokens = nextLine.trim().split("\\s+");
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
				input.close();
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
				//System.out.println(totalTokens);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
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
		File docIDFile = new File(this.resultDirectory + "docIDNameLength.txt");
		try {
			PrintWriter output = new PrintWriter(docIDFile);
			output.println("Document ID | " + "Document Name | " + "Number of tokens");
			Set<Map.Entry<Integer, String>> entrySet = this.docIDNameMap.entrySet();
			for (Map.Entry<Integer, String> entry : entrySet){
				int docID = entry.getKey();
				String name = entry.getValue();
				int length = this.docIDLengthMap.get(docID);
				output.printf("%-15s %-20s %s", docID, name, length);
				output.println();
			}
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// save the four map 
	private void saveObjs(){
		try {
			ObjectOutputStream output1 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "invertedIndex.dat"));
			output1.writeObject(this.invertedIndex);
			output1.close();
			
			ObjectOutputStream output2 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "termDf.dat"));
			output2.writeObject(this.termDfMap);
			output2.close();
			
			ObjectOutputStream output3 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "docIDName.dat"));
			output3.writeObject(this.docIDNameMap);
			output3.close();
			
			ObjectOutputStream output4 = new ObjectOutputStream(new FileOutputStream(this.resultDirectory + "docIDLength.dat"));
			output4.writeObject(this.docIDLengthMap);
			output4.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start(){
		File folder = new File(this.resultDirectory);
		if (!folder.exists()) folder.mkdirs();
		this.loadCorpus();
		this.calculateDfMap();
		this.saveIndexResult();
		this.saveDocIDandNumTokens();
		this.saveObjs();
		System.out.println("Index Done!");
	}

	public static void main(String[] args) {
		Indexer i1 = new Indexer();
		i1.start();
		// index the stopping corpus
		Indexer i2 = new Indexer();
		i2.corpusDirectory = "stopping corpus/";
		i2.resultDirectory = "index result/stopping/";
		i2.start();
	}
}
