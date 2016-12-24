package evaluation;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import searchEngine.QueryResult;

class DocumentParameter{
	
	int docID;
	double precision;
	double recall;
	
	public DocumentParameter(int docID, double docPrecision, double docRecall) {
		this.docID = docID;
		this.precision = docPrecision;
		this.recall = docRecall;
	}
}

public class searchEngineEvaluation {

	private HashMap<Integer, HashSet<Integer>> relevanceJudgement; // relevance judgement structure
	private static ArrayList<QueryResult> resultList_TFIDF; // 64 query result for tf-idf
	private static ArrayList<QueryResult> resultList_CosineSimilarity; // 64 query result for Cosine Similarity
	private static ArrayList<QueryResult> resultList_Lucene; // 64 query result for Lucene
	private static ArrayList<QueryResult> resultList_BM25; // 64 query result for BM25
	private static ArrayList<QueryResult> resultList_expandingBM25; // 64 query result for BM25 with query expanding
	private static ArrayList<QueryResult> resultList_stoppingBM25; // 64 query result for BM25 with stopping
	private static ArrayList<QueryResult> resultList_stemmingBM25; // 64 query result for BM25 with stemming
	private static ArrayList<QueryResult> resultList_expandStopingBM25; // 64 query result for BM25 with query expanding and stopping
	
	// queryID with ranked docs and precision,recall
	private static HashMap<Integer, ArrayList<DocumentParameter>> modelEvaluation_TFIDF =  new HashMap<>(); 
	private static HashMap<Integer, ArrayList<DocumentParameter>> modelEvaluation_CosineSimilarity =  new HashMap<>(); 
	private static HashMap<Integer, ArrayList<DocumentParameter>> modelEvaluation_Lucene =  new HashMap<>(); 
	private static HashMap<Integer, ArrayList<DocumentParameter>> modelEvaluation_BM25 =  new HashMap<>(); 
	private static HashMap<Integer, ArrayList<DocumentParameter>> modelEvaluation_expandingBM25 =  new HashMap<>(); 
	private static HashMap<Integer, ArrayList<DocumentParameter>> modelEvaluation_stoppingBM25 =  new HashMap<>(); 
	private static HashMap<Integer, ArrayList<DocumentParameter>> modelEvaluation_stemmingBM25 =  new HashMap<>(); 
	private static HashMap<Integer, ArrayList<DocumentParameter>> modelEvaluation_expandStopingBM25 =  new HashMap<>(); 
	
	String relevanceInputFile= "resource/cacm_rel.txt";
	String baserunObjects = "retrieval result/";
	String resultDirectory = "evaluation result/";
	
	public static void main(String[] args) throws IOException {
		
		searchEngineEvaluation se = new searchEngineEvaluation();
		File folder = new File(se.resultDirectory);
		if (!folder.exists()) folder.mkdirs();
		// load relevance judgement input file
		se.loadRelevanceJudgement();
		se.readObjs();
		
		File mapFile  = new File("evaluation result/MAP.csv");
		FileWriter output = new FileWriter(mapFile);
		mapFile  = new File("evaluation result/MRR.csv");
		output = new FileWriter(mapFile);
		output.close();  
	/*	output.write("System_Name" + "," + "MAP" );
		output.write("\n");*/
		
		
		// calculating Precision and Recall for each query and at each intermediate ranking of docs
		se.calculateModelParameters(resultList_TFIDF, modelEvaluation_TFIDF);
		se.calculateModelParameters(resultList_CosineSimilarity, modelEvaluation_CosineSimilarity);
		se.calculateModelParameters(resultList_Lucene, modelEvaluation_Lucene);
		se.calculateModelParameters(resultList_BM25, modelEvaluation_BM25);
		se.calculateModelParameters(resultList_expandingBM25, modelEvaluation_expandingBM25);
		se.calculateModelParameters(resultList_stoppingBM25, modelEvaluation_stoppingBM25);
		se.calculateModelParameters(resultList_stemmingBM25, modelEvaluation_stemmingBM25);
		se.calculateModelParameters(resultList_expandStopingBM25, modelEvaluation_expandStopingBM25);
		
		
		String[] systemNames = {"TFIDF" , "CosineSimilarity", "Lucene", "BM25", 
								"Expanding_BM25", "Stopping_BM25", "Stemming_BM25", "ExpandStopping_BM25"};
		// saving P@K
		int[] K = {5, 20};
		
		String dir = "evaluation result/P@K/";
		File folder2 = new File(dir);
		if (!folder2.exists()) folder2.mkdirs();
		File resultFile = new File(dir + systemNames[0] +".csv");
		output = new FileWriter(resultFile);
		output.write("QueryID" + "," + "DocID" + "," + "Document Rank" + "," + "Precision" + "," + "System_Name");
		output.write("\n");
		output.close();
		se.savePatK(modelEvaluation_TFIDF, systemNames[0], K);
		
		resultFile = new File(dir + systemNames[1] +".csv");
		output = new FileWriter(resultFile);
		output.write("QueryID" + "," + "DocID" + "," + "Document Rank" + "," + "Precision" + "," + "System_Name");
		output.write("\n");
		output.close();
		se.savePatK(modelEvaluation_CosineSimilarity, systemNames[1], K);
		
		resultFile = new File(dir + systemNames[2] +".csv");
		output = new FileWriter(resultFile);
		output.write("QueryID" + "," + "DocID" + "," + "Document Rank" + "," + "Precision" + "," + "System_Name");
		output.write("\n");
		output.close();
		se.savePatK(modelEvaluation_Lucene, systemNames[2], K);
		
		resultFile = new File(dir + systemNames[3] +".csv");
		output = new FileWriter(resultFile);
		output.write("QueryID" + "," + "DocID" + "," + "Document Rank" + "," + "Precision" + "," + "System_Name");
		output.write("\n");
		output.close();
		se.savePatK(modelEvaluation_BM25, systemNames[3], K);
		
		resultFile = new File(dir + systemNames[4] +".csv");
		output = new FileWriter(resultFile);
		output.write("QueryID" + "," + "DocID" + "," + "Document Rank" + "," + "Precision" + "," + "System_Name");
		output.write("\n");
		output.close();
		se.savePatK(modelEvaluation_expandingBM25, systemNames[4], K);
		
		resultFile = new File(dir + systemNames[5] +".csv");
		output = new FileWriter(resultFile);
		output.write("QueryID" + "," + "DocID" + "," + "Document Rank" + "," + "Precision" + "," + "System_Name");
		output.write("\n");
		output.close();
		se.savePatK(modelEvaluation_stoppingBM25, systemNames[5], K);
		
		resultFile = new File(dir + systemNames[6] +".csv");
		output = new FileWriter(resultFile);
		output.write("QueryID" + "," + "DocID" + "," + "Document Rank" + "," + "Precision" + "," + "System_Name");
		output.write("\n");
		output.close();
		se.savePatK(modelEvaluation_stemmingBM25, systemNames[6], K);
		
		resultFile = new File(dir + systemNames[7] +".csv");
		output = new FileWriter(resultFile);
		output.write("QueryID" + "," + "DocID" + "," + "Document Rank" + "," + "Precision" + "," + "System_Name");
		output.write("\n");
		output.close();
		se.savePatK(modelEvaluation_expandStopingBM25, systemNames[7], K);
		System.out.println("Evaluation ends. Files saved at specified location.");
	}

	private void savePatK(HashMap<Integer, ArrayList<DocumentParameter>> baseModel, String sysName, int[] K) throws IOException {
		
		String systemName = sysName; 
		String dir = this.resultDirectory + "P@K/";
		File folder = new File(dir);
		if (!folder.exists()) folder.mkdirs();

		File resultFile = new File(dir + systemName +".csv");
		FileWriter output = new FileWriter(resultFile, true);
		
		for (Map.Entry<Integer, ArrayList<DocumentParameter>> entry : baseModel.entrySet()){
			int queryID = entry.getKey();
			ArrayList<DocumentParameter> dpList = entry.getValue();
			for(int index : K){
				DocumentParameter docEntry = dpList.get(index-1);
				int docID = docEntry.docID;
				double precision = docEntry.precision;
			//	System.out.println(queryID + "," + docID + "," + index + "," + precision + "," + systemName);
				output.write(queryID + "," + docID + "," + index + "," + precision + "," + systemName);
				output.write("\n");
			}

		}
		output.close();
	}

	private void calculateModelParameters(ArrayList<QueryResult> baseModel, HashMap<Integer, ArrayList<DocumentParameter>> evaluationModel) throws IOException {
		double sumAvgPrecision = 0;
		String systemName = "";
		double sumReciprocalRank = 0;
		
		for(int i=0; i< baseModel.size(); i++){
			double avgPrecisionForQuery = 0;
			QueryResult q = baseModel.get(i);
			int queryID = q.getQueryID();
			ArrayList<Integer> docList= q.getDocList();
			systemName = q.getSystemName();
			
			avgPrecisionForQuery = avgPrecisionForSingleQuery(queryID, docList, systemName, evaluationModel);
			//System.out.println("Avg precision for QueryID: " + queryID + " is : "+ avgPrecisionForQuery);
			sumAvgPrecision += avgPrecisionForQuery;
			
			double reciprocalRank = getReciprocalRank(queryID, docList);
			//System.out.println("QueryID: " + queryID + ", RR : " + reciprocalRank);
			sumReciprocalRank += reciprocalRank;
		}
		
		double meanAvgPrecision = sumAvgPrecision / baseModel.size();
		double meanReciprocalRank = sumReciprocalRank/baseModel.size();
		
		saveResult(evaluationModel, systemName);
		
		File mapFile  = new File("evaluation result/MAP.csv");
		FileWriter output = new FileWriter(mapFile, true);
		output.write(systemName + "," + meanAvgPrecision);
		output.write("\n");
		output.close();
		
		mapFile  = new File("evaluation result/MRR.csv");
		output = new FileWriter(mapFile, true);
		output.write(systemName + "," + meanReciprocalRank);
		output.write("\n");
		output.close();
		
	}
	
	private double getReciprocalRank(int queryID, ArrayList<Integer> docList) {
		if(!relevanceJudgement.containsKey(queryID)) return 0;
		HashSet<Integer> relDocList = this.relevanceJudgement.get(queryID);
		
		for(int i=0; i< docList.size() ; i++){
			int docID =docList.get(i);
			
			if(relDocList.contains(docID)){ 
				double reciprocalRank = 1/(double)(i+1);
				return reciprocalRank;
			}
		}
		return 0;
	}

	private double avgPrecisionForSingleQuery(int queryID, ArrayList<Integer> docList, String sysName, HashMap<Integer, ArrayList<DocumentParameter>> evaluationModel){
		double docPrecision = 0, docRecall = 0;
		double avgPrecisionAtRelDocs = 0;
		double sumPrecisionAtRelDoc = 0;
		
		if(!relevanceJudgement.containsKey(queryID)) return 0;
		
		HashSet<Integer> relDocList = this.relevanceJudgement.get(queryID); // relevant doc list for a particular query
		int totalRelDocs = relDocList.size();  // no of relevant docs
		int noOfRelDocsRet = 0;
		
		ArrayList<DocumentParameter>queryEvaluation = new ArrayList<DocumentParameter>();
		
		for(int i=0; i< docList.size() ; i++){
			int docID =docList.get(i);
			
			if(relDocList.contains(docID)){ 
				noOfRelDocsRet++;
				sumPrecisionAtRelDoc += noOfRelDocsRet/(double)(i+1) ;
			}
			
			docPrecision = noOfRelDocsRet/(double)(i+1);
			docRecall = noOfRelDocsRet/(double)totalRelDocs;
			//System.out.println("docID that matched for " + queryID + " is : " +docID + " " + docPrecision + "  " + docRecall);
			
			DocumentParameter dp = new DocumentParameter(docID, docPrecision, docRecall);
			queryEvaluation.add(dp);	
		}
		evaluationModel.put(queryID, queryEvaluation);
		if(sumPrecisionAtRelDoc == 0) return 0;
		avgPrecisionAtRelDocs = sumPrecisionAtRelDoc/noOfRelDocsRet ;
		return avgPrecisionAtRelDocs;
	}


	private void saveResult(HashMap<Integer, ArrayList<DocumentParameter>> modelEvaluation, String sysname){	
		String systemName = sysname; 
		String dir = this.resultDirectory + systemName + " Result/";
		File folder = new File(dir);
		if (!folder.exists()) folder.mkdirs();

		for (Map.Entry<Integer, ArrayList<DocumentParameter>> entry : modelEvaluation.entrySet()){
		    int queryID = entry.getKey();
			ArrayList<DocumentParameter> dpList = entry.getValue();
			
			File resultFile = new File(dir + "Query" + queryID + ".csv");
			try {
				PrintWriter output = new PrintWriter(resultFile);
				output.println("QueryID" + "," + "DocID" + "," + "Precision" + "," + "Recall" + "," + "System_Name");
				for (int i = 0; i < dpList.size(); i++){	
					DocumentParameter dp = dpList.get(i);
					int docID = dp.docID;
					double precision = dp.precision;
					double recall = dp.recall;		
					
					output.println(queryID + "," + docID + "," + precision + "," + recall + "," + systemName);
				}
				output.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void loadRelevanceJudgement(){
		relevanceJudgement = new HashMap<Integer, HashSet<Integer>>();
		File relevanceFile = new File(this.relevanceInputFile);
		Scanner input;
		try {
			input = new Scanner(relevanceFile);
			
			while (input.hasNext()){
				String nextLine = input.nextLine();
				String parts[] = nextLine.split(" ");
				String docName = parts[2];
				int queryID = Integer.valueOf(parts[0]);
				int docID = Integer.valueOf(docName.substring(5));  // getting docID
				if(relevanceJudgement.containsKey(queryID)){
					HashSet<Integer> docsList = relevanceJudgement.get(queryID);
					if(!docsList.contains(docID)){ 
						docsList.add(docID);
						relevanceJudgement.put(queryID, docsList);	
					}
				}
				else{
					HashSet<Integer> docSet = new HashSet<Integer>();
					docSet.add(docID);
					relevanceJudgement.put(queryID, docSet);
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
			ObjectInputStream input1 = new ObjectInputStream(new FileInputStream(this.baserunObjects + "TfIdf.dat"));
			resultList_TFIDF = (ArrayList<QueryResult>) input1.readObject();
			input1.close();
			
			ObjectInputStream input2 = new ObjectInputStream(new FileInputStream(this.baserunObjects + "CosineSimilarity.dat"));
			resultList_CosineSimilarity = (ArrayList<QueryResult>) input2.readObject();
			input2.close();
			
			ObjectInputStream input3 = new ObjectInputStream(new FileInputStream(this.baserunObjects + "LuceneAnalyzer.dat"));
			resultList_Lucene = (ArrayList<QueryResult>) input3.readObject();
			input3.close();
			
			ObjectInputStream input4 = new ObjectInputStream(new FileInputStream(this.baserunObjects + "BM25.dat"));
			resultList_BM25 = (ArrayList<QueryResult>) input4.readObject();
			input4.close();
			
			ObjectInputStream input5 = new ObjectInputStream(new FileInputStream(this.baserunObjects + "BM25_Stopping.dat"));
			resultList_stoppingBM25 = (ArrayList<QueryResult>) input5.readObject();
			input5.close();
			
			ObjectInputStream input6 = new ObjectInputStream(new FileInputStream(this.baserunObjects + "BM25_Expanding.dat"));
			resultList_expandingBM25 = (ArrayList<QueryResult>) input6.readObject();
			input6.close();
		
			ObjectInputStream input7 = new ObjectInputStream(new FileInputStream(this.baserunObjects + "BM25_ExpandStoping.dat"));
			resultList_expandStopingBM25 = (ArrayList<QueryResult>) input7.readObject();
			input7.close();
			
			ObjectInputStream input8 = new ObjectInputStream(new FileInputStream(this.baserunObjects + "BM25_Stemming.dat"));
			resultList_stemmingBM25 = (ArrayList<QueryResult>) input8.readObject();
			input8.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
