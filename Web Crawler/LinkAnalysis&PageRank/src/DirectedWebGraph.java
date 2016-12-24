import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Set;

public class DirectedWebGraph {

	static LinkedHashMap<String, LinkedHashSet<String>> inlinkgraphG1 = 
			new LinkedHashMap<String, LinkedHashSet<String>>();
	static LinkedHashMap<String, LinkedHashSet<String>> inlinkgraphG2 = 
			new LinkedHashMap<String, LinkedHashSet<String>>();
	static LinkedHashSet<String> inlinks= new LinkedHashSet<String>();
	static LinkedHashSet<String> empty = new LinkedHashSet<String>();
	static LinkedHashSet<String> linkhash = new LinkedHashSet<String>();
	static LinkedHashSet<String> mainlinkhash = new LinkedHashSet<String>();
	
	static String location = "Task1/graphG1.txt";
	static String attributeG1 = "Task1/sink_source_G1.txt";
	static String attributeG2 = "Task1/sink_source_G2.txt";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
	    // ------------------ GRAPH G1 --------------------------------//
		String seed ="https://en.wikipedia.org/wiki/Sustainable_energy";
		System.out.println("creating indexes");
	      // Put elements to the map
		makeKey(seed);
		
	    System.out.println("invert indexing of links");
	 // System.out.println(mainlinkhash);
		  // Invert Indexing
		invertIndex();
		//  writing the graph into text file
		writeToFile(inlinkgraphG1);
		
		PageRank.initializationFunction();
		// getting sinknodes
		PageRank.getOutLinkCount(inlinkgraphG1);
	    PageRank.getSinkNodes();
	    System.out.println("SinkNodes of G1: Total of " + PageRank.sinkNodes.size());
	    System.out.println("writing G1 sinknodes to file");
	    
	 // writing sink nodes to file "sink_source_G1.txt"
	    File linkfile = new File(attributeG1);
	    FileWriter fw = new FileWriter(linkfile);
	    fw.write("SinkNodes: \n");
		fw.close();
		
		writein(PageRank.sinkNodes, linkfile);
		
	    //getting sourcenodes
	    PageRank.getSourceNodes(inlinkgraphG1);
	    System.out.println("SourceNodes of G1 : Total of " + PageRank.sourceNodes.size());
	//  System.out.println(PageRank.sourceNodes);
	    
	    System.out.println("writing G1 sourcenodes to file");
	    // writing source nodes to file "sink_source_G1.txt"
	    fw = new FileWriter(linkfile,true);
	    fw.write("SourceNodes: \n");
		fw.close();
	    writein(PageRank.sourceNodes, linkfile);
	    
	    
	    // ------------------ GRAPH G2 -------------------------------//
		makeG2();
		
		PageRank.initializationFunction();
		// getting sinknodes
		PageRank.getOutLinkCount(inlinkgraphG2);
	    PageRank.getSinkNodes();
	    System.out.println("SinkNodes of G2: Total of " + PageRank.sinkNodes.size());
	   // System.out.println(PageRank.sinkNodes);
	    System.out.println("writing G2 sinknodes to file");
	    // writing sink nodes to file "sink_source_G2.txt"
	    linkfile = new File(attributeG2);
	    fw = new FileWriter(linkfile);
	    fw.write("SinkNodes: \n");
		fw.close();
	    
	    writein(PageRank.sinkNodes, linkfile);
	    
	    //getting sourcenodes
	    PageRank.getSourceNodes(inlinkgraphG2);
	    System.out.println("SourceNodes of G1 : Total of " + PageRank.sourceNodes.size());
	   // System.out.println(PageRank.sourceNodes);
	    
	    System.out.println("writing G2 sourcenodes to file");
	    // writing source nodes to file "sink_source_G2.txt"
	    fw = new FileWriter(linkfile,true);
	    fw.write("SourceNodes: \n");
		fw.close();
	    writein(PageRank.sourceNodes, linkfile);
	    

	//	System.out.println(inlinkgraphG2);
		System.out.println("Graph has been build");
	}
	
// index creation	
 public static void makeKey(String url) throws IOException, InterruptedException{
		String linkHref, linkabsHref;
		String docid;
		String[] parts;
		Document doc = null;
		Elements links;
		int linkcount=1, i=0;
		boolean result;
		
		
		//inlinkgraph.put("Sustainable_energy",null);
		
		while(linkcount<=1000){
			boolean contFlag = true;
			while(contFlag){
				try{
					doc = Jsoup.connect(url).get();
					contFlag = false;
				}
				catch(SocketTimeoutException sock){
					System.out.println(sock.getMessage());
					TimeUnit.SECONDS.sleep(1);
				}}
				
				links = doc.select("a[href]");
				
				for(Element link : links ){
					if(linkcount<=1000){
						// checking for the regex and administrative, section links
						
						linkabsHref = link.attr("abs:href");
						linkHref=link.attr("href");
						result=linkabsHref.matches("https://en.wikipedia.org/wiki.*") &&
								!linkabsHref.matches("https://en.wikipedia.org/wiki/Main_Page");
						if(result){
							if(!linkHref.contains(":")){
								if(linkabsHref.contains("#"))
									linkabsHref=linkabsHref.replaceAll("#.*", "");
								linkabsHref=linkabsHref.replaceAll("\\s+","");
								parts =linkabsHref.split("https://en.wikipedia.org/wiki/", 2);
								docid = parts[1];
								
								// add new-indexes only
								if(!inlinkgraphG1.containsKey(docid)){
									  empty=new LinkedHashSet<String>();
									  inlinkgraphG1.put(docid, empty);
									  linkcount++;
									  mainlinkhash.add(linkabsHref);
								}
							}
						}
					}
				}
				//System.out.println(linkcount);
				url=mainlinkhash.toArray()[i].toString();
				i++;
		}
			
	}
	// adding pointers to index
	public static void invertIndex() throws IOException, InterruptedException{
		Document doc = null;
		Elements links;
		String[] partsindex,partsinlink;
		String docidindex,docidinlink;
		String linkabsHref;
		
		for(String s: mainlinkhash){
			boolean contFlag = true;
			while(contFlag){
				try{
					doc = Jsoup.connect(s).get();
					contFlag = false;
				}
				catch(SocketTimeoutException sock){
					System.out.println(sock.getMessage());
					TimeUnit.SECONDS.sleep(1);
				}}
				
				links = doc.select("a[href]");
				
				for(Element link : links){
					linkabsHref = link.attr("abs:href");
					if(mainlinkhash.contains(linkabsHref)){
						partsindex =linkabsHref.split("https://en.wikipedia.org/wiki/", 2);
						docidindex = partsindex[1];
						
						partsinlink =s.split("https://en.wikipedia.org/wiki/", 2);
						docidinlink = partsinlink[1];
						inlinks= new LinkedHashSet<String>();
						//if(docidindex != docidinlink){
						
						// includes self-links too
							inlinks=inlinkgraphG1.get(docidindex);
							
							//System.out.println("Inlinks before: " + inlinks);
							inlinks.add(docidinlink);
							
							//System.out.println("Inlinks after: " + inlinks);
							inlinkgraphG1.put(docidindex, inlinks);
							//System.out.println(inlinkgraphG1.get(docidindex));
						//}
						
					}
					
				}
		}
	}
	// make Graph G2
	public static void makeG2(){
		String fileName = "wt2g_inlinks.txt";
		//String fileName = "test.txt";
        // This will reference one line at a time
        String line = null;
        int i=0;
        
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =  new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
               // System.out.println(line);
                String[] docids = line.split(" ");
                inlinks= new LinkedHashSet<String>();
                for(i=1;i<docids.length;i++)
                {
                	inlinks.add(docids[i]);
                }
                inlinkgraphG2.put(docids[0], inlinks);	
            }   

            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
	}
	
	public static void writeToFile(LinkedHashMap<String, LinkedHashSet<String>> graph) throws IOException{
		System.out.println("Writing into file");
		  // Preparing to write in a file
		File linkfile = new File(location);
		FileWriter fw = new FileWriter(linkfile);
		fw.close();
	      // Get a set of the entries
		Set<Entry<String, LinkedHashSet<String>>> set = graph.entrySet();
	      
	      // Get an iterator
	    Iterator<Entry<String, LinkedHashSet<String>>> itr = set.iterator();
	      
	      // Display elements
	    while(itr.hasNext()) {
	        Map.Entry<String, LinkedHashSet<String>> me = (Map.Entry<String, LinkedHashSet<String>>)itr.next();
	        fw = new FileWriter(linkfile,true);
	        fw.write(me.getKey() + " ");
	        HashSet<String> hs = new HashSet<String>();
	        hs=me.getValue();
	        for(String s:hs){
	        	fw.write(s+" ");
	        }
	        fw.write("\n");
	        fw.close();
	    } 
	    System.out.println("Done writing.");
	}
	
	public static void writein(ArrayList<String> list, File linkfile) throws IOException{
		FileWriter fw = new FileWriter(linkfile,true);
		for(String s: list){
			fw.write(s);
			fw.write("\n");
		}
		fw.write("\n");
		fw.close();
	}
}