import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CorpusBuilding {

	public static final String FILES_TO_INDEX_DIRECTORY = "URLs";
	public static final String CORPUS_DIRECTORY = "Corpus/";
	/*
	public static String extractText(Reader reader) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    BufferedReader br = new BufferedReader(reader);
	    String line;
	    while ( (line=br.readLine()) != null) {
	      sb.append(line);
	    }
	    String textOnly = Jsoup.parse(sb.toString()).text();
	    return textOnly;
	  }*/
	
	public static void main(String[] args) throws IOException {
		
		File dir = new File(FILES_TO_INDEX_DIRECTORY);
		File[] files = dir.listFiles();
		
		for (File infile : files) {
			//System.out.println(infile);
			Document doc = Jsoup.parse(infile, "UTF-8");
			
			Elements t = doc.getElementsByTag("title");
			String title =t.html();
			//System.out.println(title);
			
			Element e = doc.getElementById("bodyContent");
			Elements elements =e.getAllElements();
			boolean flag=false;
			
			for (Element element:elements) {
				if(flag==true){
					element.remove();
				}
				else if(element.className().equalsIgnoreCase("toc")){
			    	 
			    	element.remove();
			    }
				else if(element.tagName().equalsIgnoreCase("TABLE")){
					element.text("");
					//System.out.println(element.children());
				}
				else if(element.className().equalsIgnoreCase("reference")){
					element.remove();
				}
			    else if(element.id().equalsIgnoreCase("mw-navigation")){
			    	element.remove();
			    }
			    
			    else if(element.id().equalsIgnoreCase("jump-to-nav")){
			    	element.remove();
			    }
			    else if(element.html().matches("http.*")){
			    	element.text("");
			    }
			    else if(element.id().equalsIgnoreCase("references")){
			    	element.remove();
			    	flag=true;
			    }
			}
			
			String text = title + " ";
			text+=e.text();
		
			String rawtext = text.replaceAll("[^A-Za-z0-9\\s-.,]"," ");
			boolean success =true;
			
			while(success){
				String pattern = "(?<r1>[^0-9])(?<alp>[.,])(?<r2>[^0-9])";
				Pattern regex = Pattern.compile(pattern);
				Matcher matcher = regex.matcher(rawtext);
				success = matcher.find();
				if(success)
					rawtext=matcher.replaceFirst(matcher.group("r1")+matcher.group("r2"));
				else
					break;
			}
			
			rawtext=rawtext.toLowerCase();
			rawtext=rawtext.replaceAll("\\s+", " ");
			
			String filename=infile.getName();
			filename = filename.replaceAll("[_/%]", "");
			String name = filename.replace(".html",".txt");
			String path= CORPUS_DIRECTORY + name;
			
			
			 try{
	             // create new file
	            
	             File file = new File(path);

	                FileWriter fw = new FileWriter(file);
	                BufferedWriter bw = new BufferedWriter(fw);
	                // write in file
	                bw.write(rawtext);
	                // close connection
	                bw.close();
	          }catch(Exception ex){
	              System.out.println(ex);
	          }
		}
	}

}
