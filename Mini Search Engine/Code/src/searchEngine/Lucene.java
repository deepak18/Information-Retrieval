package searchEngine;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Lucene {
	private static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
	private static Analyzer sAnalyzer = new SimpleAnalyzer(Version.LUCENE_47);

	private IndexWriter writer; 	
	private ArrayList<File> queue = new ArrayList<File>();
	private LinkedHashMap<Integer, String> queryMap; // query id and the query
	String corpusDirectory = "corpus/";
	String systemName = "Lucene_Search";
	
	// constructor
	public Lucene(LinkedHashMap<Integer, String> queryMap, String luceneIndexDirectory) throws IOException {
		this.queryMap = queryMap;
		FSDirectory dir = FSDirectory.open(new File(luceneIndexDirectory));

		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47,sAnalyzer);
		config.setOpenMode(OpenMode.CREATE);

		writer = new IndexWriter(dir, config);
		
		// try to add file into the index
		try {
			this.indexFileOrDirectory(corpusDirectory);
		} catch (IOException e) {
			System.out.println("Error indexing " + corpusDirectory + " : "+ e.getMessage());
		}
		
		this.closeIndex();
	}

	public ArrayList<QueryResult> search(String luceneIndexDirectory) throws IOException{
		
		ArrayList<QueryResult> queryResultList = new ArrayList<QueryResult>(); // 64 query result
		Set<Map.Entry<Integer, String>> entrySet = this.queryMap.entrySet();
		for (Map.Entry<Integer, String> entry : entrySet){ // for each query
			int queryID = entry.getKey();
			String queryString = entry.getValue();
			QueryResult qr = this.searchForOneQuery(queryID, queryString, luceneIndexDirectory);
			queryResultList.add(qr);
		}
		return queryResultList;
	}
	private QueryResult searchForOneQuery(int queryID, String queryString, String luceneIndexDirectory) throws IOException{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(luceneIndexDirectory)));
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
		
		ArrayList<Integer> resultList = new ArrayList<Integer>();
		HashMap<Integer, Double> scoreMap = new HashMap<Integer, Double>(); // doc map to doc score
		
		try {
			Query q = new QueryParser(Version.LUCENE_47, "contents",sAnalyzer).parse(queryString);
		
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			// keep 100 result, or less if not have 100 result
			int docID;
			for (int i = 0; i < hits.length; ++i){
				docID = hits[i].doc;
				docID++;
				resultList.add(docID);
				scoreMap.put(docID, (double) hits[i].score);
				//System.out.println("Docid = " + hits[i].doc + " , score = " + hits[i].score);
			}
		} catch (ParseException e) {
			System.out.println("Error searching " + queryString + " : " );
			e.printStackTrace();
		}
		if (scoreMap.isEmpty()) return new QueryResult(-1, null, null, null); // no result
    	QueryResult qr = new QueryResult(queryID, resultList, scoreMap, this.systemName);
		return qr;
	}
	
	/**
	 * Indexes a file or directory
	 * 
	 * @param fileName
	 *            the name of a text file or a folder we wish to add to the
	 *            index
	 * @throws java.io.IOException
	 *             when exception
	 */
	public void indexFileOrDirectory(String fileName) throws IOException {
		// ===================================================
		// gets the list of files in a folder (if user has submitted
		// the name of a folder) or gets a single file name (is user
		// has submitted only the file name)
		// ===================================================
		addFiles(new File(fileName));

		int originalNumDocs = writer.numDocs();
		for (File f : queue) {
			FileReader fr = null;
			try {
				Document doc = new Document();

				// ===================================================
				// add contents of file
				// ===================================================
				fr = new FileReader(f);
				doc.add(new TextField("contents", fr));
				doc.add(new StringField("path", f.getPath(), Field.Store.YES));
				doc.add(new StringField("filename", f.getName(),
						Field.Store.YES));

				writer.addDocument(doc);
				//System.out.println("Added: " + f);
			} catch (Exception e) {
				System.out.println("Could not add: " + f);
			} finally {
				fr.close();
			}
		}

		int newNumDocs = writer.numDocs();
		System.out.println("");
		System.out.println("************************");
		System.out
		.println((newNumDocs - originalNumDocs) + " documents added.");
		System.out.println("************************");

		queue.clear();
	}
	
	private void addFiles(File file) {

		if (!file.exists()) {
			System.out.println(file + " does not exist.");
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				addFiles(f);
			}
		} else {
			String filename = file.getName().toLowerCase();
			// ===================================================
			// Only index text files
			// ===================================================
			if (filename.endsWith(".htm") || filename.endsWith(".html")
					|| filename.endsWith(".xml") || filename.endsWith(".txt")) {
				queue.add(file);
			} else {
				System.out.println("Skipped " + filename);
			}
		}
	}

	/**
	 * Close the index.
	 * 
	 * @throws java.io.IOException
	 *             when exception closing
	 */
	public void closeIndex() throws IOException {
		writer.close();
	}
	
}
