import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FocusedCrawlingDFS {
	
	static String location = "linkfileTask2B.txt";
	static String htmlloc = "URLs/Task2B/";
	static String keyword = "solar";
	static ArrayList<String> visitedlist = new ArrayList<String>(); 
	static Stack<String> tovisit = new Stack<String>();
	
	static Document doc;
	static int index = 0;
	static int totalurlhit=0;
	
	// 363148
	
	public static void main(String[] args) throws IOException, InterruptedException{
		
		String seed ="https://en.wikipedia.org/wiki/Sustainable_energy";
		File linkfile = new File(location);
		FileWriter fw = new FileWriter(linkfile);
		fw.write("List of URL's saved as raw html \n");
		fw.close();
		System.out.println("crawling starts...");
		crawl(seed);
		System.out.println("back in main, program ends");
		System.out.println("TOTAL HITS: " + totalurlhit);
	}
	
	public static void crawl(String url) throws IOException, InterruptedException{
		String linkHref, linkabsHref, linkabs;
		boolean result, contFlag = true;
		int depth=1;
		int lastlevelcount=0;
		
		tovisit.push(url);
		
        while(!tovisit.isEmpty() && index<1000){
        	
            url = tovisit.pop();
            
            if(!visitedlist.contains(url)){
            	
            	while(contFlag){
    				try{
    					doc = Jsoup.connect(url).get();
    					contFlag = false;
    				}
    				catch(SocketTimeoutException soc){
    					TimeUnit.SECONDS.sleep(1);
    				}
    			}
            	//System.out.println("Popping from stack and saving raw html");
            	savehtml(url);
            	
            	if(depth==5 && lastlevelcount>0){
            		lastlevelcount--;
            		depth=4;
            		continue;
            	}
            	
            	//System.out.println("Adding child links to the stack");
    			Elements links = doc.select("a[href]");
    			File linkfile = new File(location);
    		
                // auxiliary stack to visit neighbors in the order they appear
                // in the adjacency list
                // alternatively: iterate through ArrayList in reverse order
                // but this is only to get the same output as the recursive dfs
                // otherwise, this would not be necessary
                Stack<String> auxStack = new Stack<String>();
                
                for(Element link : links ){
    				
    				totalurlhit++;
    				linkabsHref = link.attr("abs:href");
    				linkHref=link.attr("href");
    				result=linkabsHref.matches("https://en.wikipedia.org/wiki/.*");
    				
    				if(!visitedlist.contains(linkabsHref) && !tovisit.contains(linkabsHref)){
    					if(result){
    						if(!linkHref.contains(":")){
    							if(linkabsHref.contains("#"))
    								linkabsHref=linkabsHref.replaceAll("#.*", "");
    							linkabs=linkabsHref.replaceAll("\\s+","");
    							if(link.html().toLowerCase().contains(keyword)  ||
    								linkHref.toLowerCase().contains(keyword)){
    								
    							//	System.out.println("writing in linkfile");
    								auxStack.push(linkabs);
    						        if(depth==4){
    						        	lastlevelcount++;
    						        }  	
    							}
    						}
    					}
    				}
    			}
                
                depth++;
                //System.out.print("lastlevelcount:" + lastlevelcount);
                //System.out.println("Depth:" + depth);
                
                while(!auxStack.isEmpty()){
                    tovisit.push(auxStack.pop());
                }
            }
        }
        System.out.println(index);
        
	}
	
	public static void savehtml(String url) throws InterruptedException, IOException
	{ 
		String[] parts = url.split("https://en.wikipedia.org/wiki/", 2);
		//System.out.println(url);
		//System.out.println(parts[1] + "\n");
		String filename = parts[1];
		String pageloc= htmlloc + URLEncoder.encode(filename, "UTF-8") + ".html";
		
    	boolean flag=true;    
    	FileWriter fw;
		
    	while(flag){
    	    try{
    	    	
        	    doc = Jsoup.connect(url).get();
        	    
    	    	File pagefile = new File(pageloc);
    	    	fw = new FileWriter(pagefile,true);
    	    	fw.write(url + "\n");
    	    	fw.write(doc.toString());
    	    	fw.close();
    	    	visitedlist.add(url);
    	    	index++;

    	    	flag=false;
    	    	}catch(SocketTimeoutException soc){
    	    		System.out.println(soc.getMessage());
    	    		TimeUnit.SECONDS.sleep(1);
    	        }catch(IOException e){
    	    		e.printStackTrace();
    	    		flag=false;
    	    	}	 
    	}    	
    	//TimeUnit.SECONDS.sleep(1);
    	
		File linkfile = new File(location);
		fw = new FileWriter(linkfile,true);
		fw.write(url+"\n");
		fw.close();	
		
	}
}


