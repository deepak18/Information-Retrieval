import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Combined {
	 static String location = "linkfileTask3.txt";;
	 static String htmlloc = "URLs/Task3/";
	 static int totallinkcount=0;
	 static ArrayList<String> enarray = new ArrayList<String>();
	 static ArrayList<String> mainarray = new ArrayList<String>();
	 static ArrayList<String> pwarray = new ArrayList<String>();
	 
		
	public static void main(String[] args) throws IOException, InterruptedException {
		String energyseed="https://en.wikipedia.org/wiki/Sustainable_energy";
		String powerseed="https://en.wikipedia.org/wiki/Solar_power";
		
		Document energydoc = null, powerdoc = null;
		boolean contFlag = true;
		// connecting to links
		while(contFlag){
		try{
			energydoc = Jsoup.connect(energyseed).get();
			powerdoc = Jsoup.connect(powerseed).get();
			contFlag = false;
		}
		catch(SocketTimeoutException sock){
			System.out.println(sock.getMessage());
			TimeUnit.SECONDS.sleep(5);
		}}
		
		Elements energylinks = energydoc.select("a[href]");
		Elements powerlinks = powerdoc.select("a[href]");
		
		//Adding Seed to the text file of links
		
		File linkfile = new File(location);
		FileWriter fw = new FileWriter(linkfile);
		fw.write("List of URL's saved as raw html :\n");
		
		fw.close();
		System.out.println("Crawling with energy seed");
		crawldepth1(linkfile,energylinks);
		System.out.println("Crawling with power seed");
		crawldepth2(linkfile,powerlinks);
		
		enarray.add(0, energyseed);
		pwarray.add(0, powerseed);
		
		System.out.println("Merging links of both seed");
		merge();
		
		System.out.println("Saving links of both seed");
		saveall();
		
		System.out.println("back in main, program ends");
		
	}


	public static void saveall() throws InterruptedException, IOException {
		for(int i=0; i<mainarray.size();i++)
		{
			String url=mainarray.get(i);
			String[] parts = url.split("https://en.wikipedia.org/wiki/", 2);
			//System.out.println(url);
			//System.out.println(parts[1] + "\n");
			String filename = parts[1];
			String pageloc= htmlloc + URLEncoder.encode(filename, "UTF-8") + ".html";
			
	    	boolean flag=true;    
	    	FileWriter fw;
			
	    	while(flag){
	    	    try{
	    	    	// saving fle as raw html
	        	    Document doc = Jsoup.connect(url).get();
	        	    
	    	    	File pagefile = new File(pageloc);
	    	    	fw = new FileWriter(pagefile,true);
	    	    	fw.write(url + "\n");
	    	    	fw.write(doc.toString());
	    	    	fw.close();
	    	    	
	    	    	flag=false;
	    	    	}catch(SocketTimeoutException soc){
	    	    		System.out.println(soc.getMessage());
	    	    		TimeUnit.SECONDS.sleep(1);
	    	        }catch(IOException e){
	    	    		e.printStackTrace();
	    	    		flag=false;
	    	    	}	 
	    	}
			
			File linkfile = new File(location);
			fw = new FileWriter(linkfile,true);
			fw.write(url+"\n");
			fw.close();	
		}
		
	}


	private static void merge() {
		String x;
		// merging two arraylists for total of 1000
		// adding one from each till one gets empty
		// one gets empty, adding rest of links from another
		
		for(;totallinkcount<1000;){
			if(enarray.size()>0){
				x=enarray.get(0);
				if(!mainarray.contains(x)){
					mainarray.add(x);
					enarray.removeAll(mainarray);
					totallinkcount++;
				}
			}
			else{
				for(;totallinkcount<1000;)
				{
					x=pwarray.get(0);
					if(!mainarray.contains(x)){
						mainarray.add(x);
						pwarray.removeAll(mainarray);
						totallinkcount++;
					}
				}
				break;
			}
				
			if(pwarray.size()>0){
				x=pwarray.get(0);
				if(!mainarray.contains(x)){
					mainarray.add(x);
					pwarray.removeAll(mainarray);
					totallinkcount++;
				}
			}
			else{
				for(;totallinkcount<1000;)
				{
					x=enarray.get(0);
					if(!mainarray.contains(x)){
						mainarray.add(x);
						enarray.removeAll(mainarray);
						totallinkcount++;
					}
				}
				break;
			}
		}
	}

	// crawling for powerseed
	public static void crawldepth2(File linkfile, Elements links){
		String linkHref;
		String linkabsHref, linkabs;
		boolean result;
		int linkcount=0;
		
		// Adding each single list to text file of links
		for(Element link : links ){
			if(linkcount<=1000){
				// checking for needed regex 
				// handling ":" and "#"
				linkabsHref = link.attr("abs:href");
				linkHref=link.attr("href");
				result=linkabsHref.matches("https://en.wikipedia.org/wiki.*");
				if(result){
					if(!linkHref.contains(":")){
						if(linkabsHref.contains("#"))
							linkabsHref=linkabsHref.replaceAll("#.*", "");
						linkabs=linkabsHref.replaceAll("\\s+","");
						if(!pwarray.contains(linkabsHref)){
							  pwarray.add(linkabs);
						      linkcount++;
						}
					}
				}
			}
			else
				break;
		}
	}
	// crawling for energyseed
		public static void crawldepth1(File linkfile, Elements links){
			String linkHref;
			String linkabsHref, linkabs;
			boolean result;
			int linkcount=0;
			
			// Adding each single list to text file of links
			for(Element link : links ){
				if(linkcount<=1000){
					// checking for needed regex 
					// handling ":" and "#"
					linkabsHref = link.attr("abs:href");
					linkHref=link.attr("href");
					result=linkabsHref.matches("https://en.wikipedia.org/wiki.*");
					if(result){
						if(!linkHref.contains(":")){
							if(linkabsHref.contains("#"))
								linkabsHref=linkabsHref.replaceAll("#.*", "");
							linkabs=linkabsHref.replaceAll("\\s+","");
							if(!enarray.contains(linkabsHref)){
								  enarray.add(linkabs);
							      linkcount++;
							}
						}
					}
				}
				else
					break;
			}
		}
	

}
