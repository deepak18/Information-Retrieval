import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawler1 {
	static String seed ="https://en.wikipedia.org/wiki/Sustainable_energy";
	static String location = "linkfileTask1.txt";
	static String htmlloc = "URLs/Task1/";
	
	static LinkedHashSet<String> linkhash = new LinkedHashSet<String>();
	static LinkedHashSet<String> mainlinkhash = new LinkedHashSet<String>();
	static LinkedHashSet<String> removed = new LinkedHashSet<String>();
	
	static Document doc;
	static int depth=1;
	static int index = 0;
	static int linkcount = 1;
	static int prelinkcount=1;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		boolean contFlag = true;
		while(contFlag){
		try{
			doc = Jsoup.connect(seed).get();
			contFlag = false;
		}
		catch(SocketTimeoutException sock){
			System.out.println(sock.getMessage());
			TimeUnit.SECONDS.sleep(5);
		}}
		
	
		Elements links = doc.select("a[href]");
		
		//Adding Seed to the text file of links
		
		File linkfile = new File(location);
		FileWriter fw = new FileWriter(linkfile);
		fw.write("List of URLs crawled: \n");
		
		fw.close();
		System.out.println("Crawling the seed page..\n");
		crawllink(linkfile, links);
		
		linkhash = new LinkedHashSet<String>();
		if(linkhash.isEmpty()){
			for(String s: mainlinkhash){
				linkhash.add(s);
			}
		}
		
		prelinkcount=linkcount;
	   
		//Going through each element in the hashset
		eachlink();
		System.out.println("back in main, program ends");
	}
	
	private static void crawllink(File linkfile, Elements links){
		String linkHref;
		String linkabsHref, linkabs;
		boolean result;
		
		// Adding each single list to text file of links
		for(Element link : links ){
			if(linkcount<=1000){
				
				// checking for the regex and administrative, section links
				
				linkabsHref = link.attr("abs:href");
				linkHref=link.attr("href");
				result=linkabsHref.matches("https://en.wikipedia.org/wiki.*");
				if(result){
					if(!linkHref.contains(":")){
						if(linkabsHref.contains("#"))
							linkabsHref=linkabsHref.replaceAll("#.*", "");
						linkabs=linkabsHref.replaceAll("\\s+","");
						try {
							if(!mainlinkhash.contains(linkabs) && !removed.contains(linkabs)){
								  FileWriter fw = new FileWriter(linkfile,true);
								  
						          fw.write(linkabs+"\n");
						          fw.close();
						          mainlinkhash.add(linkabs);
						          linkcount++;
							}  
					      }catch (IOException e) {
					    	  e.printStackTrace();
						  }
					}
				}
			}
			else{
				System.out.println("links crawled count has reached 1000...\n");
				break;
			}
		}
		
	}
	
	private static void eachlink() throws IOException, InterruptedException{
		boolean full=false;
		String stringlink;
		
		System.out.println("saving raw htmls...\n");
		do{
			for(String s : linkhash){
				//Saving each html page with its name
				stringlink = s;	
				
				savehtml(stringlink);
				TimeUnit.SECONDS.sleep(1);
				
				Elements links = doc.select("a[href]");
    	    	File linkfile = new File(location);
    	    	
    	    	// keeping track of depth
    	    	
				if(index == prelinkcount){
					//FileWriter fw = new FileWriter(linkfile,true);
    	    		depth++;
    	    		prelinkcount=linkcount;
    	    		//fw.write("Depth: " + depth + "\n");
    	    		//fw.close();
    	    	}
				
	    	    if(!full){	
	    	    	crawllink(linkfile, links);
	    	    	if(linkcount>1000)
	    	    		full=true;
	    	    }
	    	    break;
			}
		
			if(index>1000)
			break;
			//System.out.println(linkcount);
			
			linkhash = new LinkedHashSet<String>();
			if(linkhash.isEmpty()){
				//System.out.println(mainlinkhash.size());
				for(String s: mainlinkhash){
					linkhash.add(s);
				}
			}
				
		}while(!linkhash.isEmpty());	
	}
	
	private static void savehtml(String stringlink) throws UnsupportedEncodingException{
		
		String[] parts = stringlink.split("https://en.wikipedia.org/wiki/", 2);
		String filename = parts[1];
		String pageloc= htmlloc + URLEncoder.encode(filename, "UTF-8") + ".html";
    	
    	boolean flag=true;    
    	FileWriter fw;
		
    	while(flag){
    	    try{
    	    	//saving page as raw html
        	    doc = Jsoup.connect(stringlink).get();
        	    
    	    	File pagefile = new File(pageloc);
    	    	fw = new FileWriter(pagefile,true);
    	    	fw.write(stringlink + "\n");
    	    	fw.write(doc.toString());
    	    	fw.close();
    	    	mainlinkhash.remove(stringlink);
    	    	removed.add(stringlink);
    	    	index++;

    	    	flag=false;
    	    	}catch(SocketTimeoutException sock){
    	    		System.out.println(sock.getMessage());
    	    		try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    	        }catch(IOException e){
    	    		e.printStackTrace();
    	    		flag=false;
    	    	}	 
    	}
	}
}