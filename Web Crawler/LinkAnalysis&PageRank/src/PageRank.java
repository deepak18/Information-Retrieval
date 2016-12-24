import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class PageRank {

	static LinkedHashMap<String, Integer> numberOfOutLinks = new LinkedHashMap<String, Integer>();
	static LinkedHashMap<String, Integer> numberOfInLinks = new LinkedHashMap<String, Integer>();
	static LinkedHashMap<String, Double> pR = new LinkedHashMap<String,Double>();
	static LinkedHashMap<String, Double> newpR = new LinkedHashMap<String,Double>();
	static ArrayList<String> sinkNodes = new ArrayList<String>();
	static ArrayList<String> sourceNodes = new ArrayList<String>();
	
	
	static String perplexityG1 = "Task2/perplexityG1.txt";
	static String perplexityG2 = "Task2/perplexityG2.txt";
	static String top50G1 = "Task2/top50G1.txt";
	static String top50G2 = "Task2/top50G2.txt";
	
	static String top5inlinksG1 ="Task3/top5inlinksG1";
	static String top5inlinksG2 ="Task3/top5inlinksG2";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		// ---------------------- GRAPH G1 --------------------------------//
		String seed ="https://en.wikipedia.org/wiki/Sustainable_energy";
		System.out.println("creating indexes");
	      // Put elements to the map
		DirectedWebGraph.makeKey(seed);
		
	    System.out.println("invert indexing of links");
		  // Invert Indexing
		DirectedWebGraph.invertIndex();
		// Initialize all data-structures
		initializationFunction();
		
		getOutLinkCount(DirectedWebGraph.inlinkgraphG1);
		getSinkNodes();
		getSourceNodes(DirectedWebGraph.inlinkgraphG1);
		
		
		// writing perplexity to file of G1
		File linkfile = new File(perplexityG1);
	    FileWriter fw = new FileWriter(linkfile);
		fw.close();
		System.out.println("Calculating Page Rank of Graph G1... ");
		calculatePageRank(DirectedWebGraph.inlinkgraphG1, linkfile);
		
		// writing Top 50 Ranked pages of G2
		linkfile = new File(top50G1);
		fw = new FileWriter(linkfile);
		fw.write("PageRank Score             Document ID \n");
		fw.close();
		getTopLinks(50, pR, linkfile);
		
		getInLinkCount(DirectedWebGraph.inlinkgraphG1);
		// TASK 3: Top 5 in-links
		linkfile = new File(top5inlinksG1);
		fw = new FileWriter(linkfile);
		fw.write("In-links     Document ID \n");
		fw.close();
		getMostInLinks(5, numberOfInLinks, linkfile);
		
		
	   // ------------------------- GRAPH G2 -------------------------------//
		// initialize all data structures
		initializationFunction();
		
		DirectedWebGraph.makeG2();
		
		getOutLinkCount(DirectedWebGraph.inlinkgraphG2);
		getSinkNodes();
		
		// writing perplexity to file of G2
		linkfile = new File(perplexityG2);
	    fw = new FileWriter(linkfile);
		fw.close();
		System.out.println("Calculating Page Rank of Graph G2... ");
		calculatePageRank(DirectedWebGraph.inlinkgraphG2, linkfile);
		
		
		// writing Top 50 Ranked pages of G2
		linkfile = new File(top50G2);
		fw = new FileWriter(linkfile);
		fw.write("PageRank Score           Document ID \n");
		fw.close();
		getTopLinks(50, pR, linkfile);
		
		
		getInLinkCount(DirectedWebGraph.inlinkgraphG2);
		// TASK 3: Top 5 in-links
		linkfile = new File(top5inlinksG2);
		fw = new FileWriter(linkfile);
		fw.write("In-links      Document ID \n");
		fw.close();
		getMostInLinks(5, numberOfInLinks, linkfile);
				
		System.out.println("Writing to file ends. End of Program");
		
	}
	
	

	public static void initializationFunction(){
		numberOfOutLinks = new LinkedHashMap<String, Integer>();
		numberOfInLinks = new LinkedHashMap<String, Integer>();
		sinkNodes = new ArrayList<String>();
		sourceNodes = new ArrayList<String>();
		pR = new LinkedHashMap<String,Double>();
		newpR = new LinkedHashMap<String,Double>();
	}
	
	public static void getOutLinkCount(LinkedHashMap<String, LinkedHashSet<String>> graph){
		int count=0;
		// initializing numberofOutLinks of each index with 0 
		for(String s: graph.keySet()){
			numberOfOutLinks.put(s, 0);
		}
		
		// incrementing the out-link count of each pointer of inverted list
		for(String index:graph.keySet()){
			for(String inLink : graph.get(index)){
				count=numberOfOutLinks.get(inLink);
				count++;
				//numberOfOutLinks.put(inLink, count);
				numberOfOutLinks.replace(inLink, count);
			}
		}
	//	System.out.println(numberOfOutLinks);
	}
	
	public static void getSinkNodes(){
		
		for(String index:numberOfOutLinks.keySet()){
			if(numberOfOutLinks.get(index)==0){
				// collecting all sink-nodes
				sinkNodes.add(index);
			}
		}
	}
	
	public static void getInLinkCount(LinkedHashMap<String, LinkedHashSet<String>> graph){
		for(String index: graph.keySet()){
			numberOfInLinks.put(index, graph.get(index).size());
			
		}
	}
	
	public static void getSourceNodes(LinkedHashMap<String, LinkedHashSet<String>> graph){
		
		for(String index: graph.keySet()){
			if(graph.get(index).size() == 0){
				sourceNodes.add(index);
			}
		}
	}
	
	static double lastperplexity = 0; //used to store lastperplexity
	static int consecutiveIterations=0; //used to count number of consecutive iterations of page rank with
    							 //perplexity difference of less than 1
	
	public static void calculatePageRank(LinkedHashMap<String, LinkedHashSet<String>> graph, File linkfile) throws IOException{
		double d= 0.85; 			//Teleportation factor
	    double N=graph.size();      //denotes size of pages 
	    double sinkPR=0;        	// Page Rank of Sink nodes
	    double perplexity=0;          //stores perplexity of current iteration
	    
	    double value=1/N;
	    double teleportation = (1-d)/N;
	    FileWriter fw;
	    System.out.println("Writing Perplexity Values to the file");
	    
		for(String index:graph.keySet()){
			pR.put(index, value);
			newpR.put(index, value);
		}
		
		
		int flag=0;  
		while(true){
			flag++;
			sinkPR=0;
				//for each page	p in S	
			for(String sinkIndex:sinkNodes){
				sinkPR+=pR.get(sinkIndex);
			}
			
			double spread = d*sinkPR/N;
				//for each page p in P
			for(String index:graph.keySet()){
				value=0;
				//newpR.replace(index, teleportation);
				value=teleportation+spread;
				newpR.replace(index,value);
				
				value=0;
				for(String inLink : graph.get(index)){
					value=newpR.get(index);
					value+=d*pR.get(inLink)/(double)numberOfOutLinks.get(inLink);
					newpR.replace(index, value);
				}
			}
			pR.putAll(newpR);
			
			perplexity=calculatePerplexity();
			
			// writing perplexity to file
			fw = new FileWriter(linkfile,true);
			fw.write("Perplexity:- "+ perplexity + "; Iteration:- "+ flag +"\n");
			fw.close();
			
			 if(notenoughchange (perplexity))  // convergence condition
				{   System.out.println("Convergence Limit Reached!!!");
					break;                 // breaks the loop and returns the pR for the current iteration
				}
			}
	}
	
	public static double calculatePerplexity(){
		double entropy = 0;
		for(String pageRank:pR.keySet()){
			double pr=pR.get(pageRank);
			entropy-=pr*((double)Math.log10(pr)/(double)Math.log10(2));
		}
		return Math.pow(2, entropy);
		
	}

	private static boolean notenoughchange(double perplexity) { 
		   // if difference of perplexities is less than 1 for 4 consecutive  iterations - isLimitReached 
		  //returns true
				if(lastperplexity==0 || Math.abs(perplexity-lastperplexity)<1)
				{
					lastperplexity=perplexity;  
					consecutiveIterations++;
					if(consecutiveIterations==5) 
						return true;
					else
						return false;
				}
				lastperplexity=perplexity; // storing the value to compare it with next iteration
				consecutiveIterations=0;
				return false;
	}

	private static void getTopLinks(int count, LinkedHashMap<String,Double> map, File linkfile) throws IOException{
		//LinkedHashMap<String, Double> map = newpR;
		ArrayList<Map.Entry<String, Double>> entries =
				  new ArrayList<Map.Entry<String, Double>>(map.entrySet());
		// sorting array in decreasing order of PageRank
				Collections.sort(entries, new Comparator<Map.Entry<String, Double>>() {
				  public int compare(Map.Entry<String, Double> a, Map.Entry<String, Double> b){
				    return b.getValue().compareTo(a.getValue());
				    
				  }
				});
				
				LinkedHashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
				// putting sorted values in sortedMap
				for (Map.Entry<String, Double> entry : entries) {
				  sortedMap.put(entry.getKey(), entry.getValue());
				}
				
				FileWriter fw = new FileWriter(linkfile,true);
				
				Set<Entry<String, Double>> set = sortedMap.entrySet();
			      
			      // Get an iterator
			    Iterator<Entry<String, Double>> itr = set.iterator();
			      
			      // Display elements
			   for(int i=0;i<count;i++){
					Map.Entry<String, Double> page = (Map.Entry<String, Double>)itr.next();
					fw.write(page.getValue() + "    " + page.getKey()+ "\n");
					//System.out.println(page.getKey() + "  " + page.getValue());
				}
			   fw.close();
	}
	
	private static void getMostInLinks(int count, LinkedHashMap<String, Integer> numberOfInLinks, File linkfile) throws IOException {
		ArrayList<Map.Entry<String, Integer>> entries =
				  new ArrayList<Map.Entry<String, Integer>>(numberOfInLinks.entrySet());
		// sorting array in decreasing order of PageRank
				Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
				  public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b){
				    return b.getValue().compareTo(a.getValue());
				    
				  }
				});
				
				LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
				// putting sorted values in sortedMap
				for (Map.Entry<String, Integer> entry : entries) {
				  sortedMap.put(entry.getKey(), entry.getValue());
				}
				
				FileWriter fw = new FileWriter(linkfile,true);
				
				Set<Entry<String, Integer>> set = sortedMap.entrySet();
			      
			      // Get an iterator
			    Iterator<Entry<String, Integer>> itr = set.iterator();
			      
			      // Display elements
			   for(int i=0;i<count;i++){
					Map.Entry<String, Integer> indexentry = (Map.Entry<String, Integer>)itr.next();
					fw.write(indexentry.getValue() + "    " + indexentry.getKey()+ "\n");
					//System.out.println(page.getKey() + "  " + page.getValue());
				}
			   fw.close();
		
	}
}
