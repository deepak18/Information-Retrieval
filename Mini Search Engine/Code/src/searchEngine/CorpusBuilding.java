package searchEngine;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class CorpusBuilding {
	String regexString = "[,.](?!\\d)|<\\/*\\w+>|[^A-Za-z0-9\\s-.,]";
	Pattern pattern = Pattern.compile(regexString);
	String folderName = "resource/cacm";
	String commonWordsDir = "resource/common_words";
	String corpusDirectory = "corpus/";
	String sCorpusDirectory = "stopping corpus/";
	HashSet<String> commonWordsSet = new HashSet<String>();
	
	public CorpusBuilding(){
		this.loadCommonWords();
	}
	
	private void loadHtmlFiles (){
		File folder = new File(this.folderName);
		File[] files = folder.listFiles();
		//System.out.println(files[0].getName());
		for (int i = 0; i < files.length; i++){
			this.processHtmlFile(files[i]);
		}
	}
	
	private void processHtmlFile(File file){
		String txtFileName = file.getName().replace(".html", ".txt");
		try {
			Scanner input = new Scanner(file);
			PrintWriter output1 = new PrintWriter(this.corpusDirectory + txtFileName);
			PrintWriter output2 = new PrintWriter(this.sCorpusDirectory + txtFileName);
			while (input.hasNext()){
				String nextLine = input.nextLine();
				String s1 = this.cleanText(nextLine);
				String s2 = this.removeCommonWords(s1);
				if (!s1.equals("")) output1.println(s1);
				if (!s2.equals("")) output2.println(s2);
			}
			input.close();
			output1.close();
			output2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String cleanText(String s){
		return this.pattern.matcher(s).replaceAll("").toLowerCase();
	}
	
	public String removeCommonWords(String s){
		if (s.equals("")) return "";
		String[] tokens = s.split("\\s+");
		String result = "";
		for(int i = 0; i < tokens.length; i++){
			String token = tokens[i];
			if (!this.commonWordsSet.contains(token))
				result += token + " ";
		}
		return result.trim();
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

	public static void main(String[] args) {
		CorpusBuilding cb = new CorpusBuilding();
		File folder1 = new File(cb.corpusDirectory);
		if (!folder1.exists()) folder1.mkdirs();
		File folder2 = new File(cb.sCorpusDirectory);
		if (!folder2.exists()) folder2.mkdirs();
		cb.loadHtmlFiles();

	}

}
