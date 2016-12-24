import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.Map.Entry;
import java.util.Scanner;

class DocumentEntry {
	public int docid;
	public int tf;

	public DocumentEntry(int docid, int tf) {
		this.docid = docid;
		this.tf = tf;
	}

	public DocumentEntry() {

	}
}

class TermEntry {
	public String term;
	public int tf;

	public TermEntry() {

	}

	public TermEntry(String term, int tf) {
		this.term = term;
		this.tf = tf;
	}
}

public class InvertedIndex extends ApplicationFrame {
	// invertedIndex(1-gram,(docid,tf))
	static LinkedHashMap<String, ArrayList<DocumentEntry>> unigramInvertedIndex = new LinkedHashMap<String, ArrayList<DocumentEntry>>();
	// invertedIndex(2-gram,(docid,tf))
	static LinkedHashMap<String, ArrayList<DocumentEntry>> bigramInvertedIndex = new LinkedHashMap<String, ArrayList<DocumentEntry>>();
	// invertedIndex(3-gram,(docid,tf))
	static LinkedHashMap<String, ArrayList<DocumentEntry>> trigramInvertedIndex = new LinkedHashMap<String, ArrayList<DocumentEntry>>();

	// docId(corpusfile,docid)
	static LinkedHashMap<String, Integer> docID = new LinkedHashMap<String, Integer>();
	// tokenCount(docid,#tokens)
	static LinkedHashMap<Integer, Integer> tokenCount = new LinkedHashMap<Integer, Integer>();

	static int noOfTokens = 0;

	// term-frequency for unigram
	static ArrayList<TermEntry> tfUnigram = new ArrayList<TermEntry>();

	// term-frequency for bigrams
	static ArrayList<TermEntry> tfBigram = new ArrayList<TermEntry>();

	// term-frequency for trigrams
	static ArrayList<TermEntry> tfTrigram = new ArrayList<TermEntry>();

	public InvertedIndex(String applicationTitle, String chartTitle, ArrayList<TermEntry> tft) {
		super(applicationTitle);
		JFreeChart lineChart = ChartFactory.createLineChart(chartTitle, "Rank", "Probability", createDataset(tft),
				PlotOrientation.VERTICAL, true, true, false);

		ChartPanel chartPanel = new ChartPanel(lineChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(560, 367));
		setContentPane(chartPanel);
	}

	private DefaultCategoryDataset createDataset(ArrayList<TermEntry> tft) {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		int i = 1;
		for (TermEntry te : tft) {
			dataset.addValue((float)te.tf/noOfTokens, "Frequency", Integer.toString(i));
			i++;
			if(i==25000)
				break;
		}

		return dataset;
	}

	public static void main(String[] args) throws IOException {
		int n;

		// maps each document in the corpus with the docID and saves the data
		// structure to disk
		mapDocID();

		Scanner in = new Scanner(System.in);

		do {
			System.out.println("What do you wanna do today? (1-4)");
			System.out.println("1. Create inverted indexes for n=1,2,3");
			System.out.println("2. Generate a term frequency table");
			System.out.println("3. Generate a document frequency table");
			System.out.println("4. Generate Stoplist");
			System.out.println("5. Get locations for already saved statistics");

			System.out.println("Enter a choice number: ");
			n = in.nextInt();

			// HashMap<Integer, Integer> value = new HashMap<Integer,Integer>();

			if (n == 1) {
				// creates the n-gram inverted indexes and writes to the file
				// (n=1,2,3)
				indexBuilder();
				// writes the token count of each document to the disk
				writeTokenCount();
			} else if (n == 2)
				// generate a term frequency table for all n-grams inverted
				// indexes
				generateTermFrequency();
			else if (n == 3)
				// generate a document frequency table for all n-grams inverted
				// indexes
				generateDocFrequency();
			else if (n == 4)
				// generate a stoplist
				generateStoplist();
			else if (n == 5)
				printLocations();
		} while (n < 5);

		in.close();

	}

	private static void printLocations() {
		System.out.println("Relative locations of each already stored document: ");
		System.out.println("1. Saved Raw html : URLs/");
		System.out.println("2. Corpus : Corpus/");
		System.out.println("3. Unigram Inverted Indexes : InvertedIndexes/Unigram/unigramindexes.txt");
		System.out.println("4. Bigram Inverted Indexes : InvertedIndexes/Bigram/bigramindexes.txt");
		System.out.println("5. Trigram Inverted Indexes : InvertedIndexes/Trigram/trigramindexes.txt");
		System.out.println("7. Tokens count per Document: Storage/tokenCountPerDoc.txt");
		System.out.println("8. Term frequency for unigrams : Storage/Term Frequency(Task 3-1)/tfUnigram.txt");
		System.out.println("9. Term frequency for bigrams : Storage/Term Frequency(Task 3-1)/tfBigram.txt");
		System.out.println("10. Term frequency for trigrams : Storage/Term Frequency(Task 3-1)/tfTrigram.txt");
		System.out.println("11. Document frequency for unigrams : Storage/Doc Frequency(Task3-2)/dfUnigram.txt");
		System.out.println("12. Document frequency for bigrams : Storage/Doc Frequency(Task3-2)/dfBigram.txt");
		System.out.println("13. Document frequency for trigrams : Storage/Doc Frequency(Task3-2)/dfTrigram.txt");
		System.out.println("11. Stoplist : Storage/stoplist.txt");
	}

	public static void initializationFunction() {
		unigramInvertedIndex.clear();
		bigramInvertedIndex.clear();
		trigramInvertedIndex.clear();
	}

	private static void generateStoplist() throws IOException {
		String stoplist = "Computed/stoplist.txt";
		noOfTokens=0;
		File dir = new File(CorpusBuilding.CORPUS_DIRECTORY);
		File[] files = dir.listFiles();
		unigramInvertedIndex.clear();
		// unigram inverted indexing
		for (File corpusfile : files) {
			unigramIndexes(corpusfile);
		}

		System.out.println("Total no of unigram in the corpus: " + noOfTokens);

		float thresholdProbability = 0.001f;

		buildSortedtf(unigramInvertedIndex, tfUnigram);

		File linkfile = new File(stoplist);
		FileWriter fw = new FileWriter(linkfile);
		fw.close();

		fw = new FileWriter(linkfile, true);

		for (TermEntry te : tfUnigram) {
			if ((float) (te.tf) / noOfTokens > thresholdProbability)
				fw.write(te.term + "," + te.tf + "\n");
			else
				break;
		}
		fw.close();
		System.out.println("Stoplist has been written to disk");
	}

	private static void mapDocID() throws IOException {
		File dir = new File(CorpusBuilding.CORPUS_DIRECTORY);
		File[] files = dir.listFiles();
		int id = 1;
		String file;

		for (File corpusfile : files) {
			file = corpusfile.getName();
			file = file.replace(".txt", "");
			if (!docID.containsKey(file)) {
				docID.put(file, id);
				id++;
			}
		}

		File linkfile = new File("Computed/DocIDmapping.txt");
		FileWriter fw = new FileWriter(linkfile);
		fw.close();

		Set<Entry<String, Integer>> set = docID.entrySet();

		// Get an iterator
		Iterator<Entry<String, Integer>> itr = set.iterator();

		fw = new FileWriter(linkfile, true);
		// Writing data structure to the file
		while (itr.hasNext()) {
			Map.Entry<String, Integer> me = (Map.Entry<String, Integer>) itr.next();
			// write the docID and the corresponding # of Tokens
			fw.write(me.getKey() + "   " + me.getValue() + "\n");

		}
		fw.close();

	}

	private static void generateDocFrequency() throws IOException {
		initializationFunction();

		String uniloc = "Computed/Doc Frequency(Task3-2)/dfUnigram.txt";
		String biloc = "Computed/Doc Frequency(Task3-2)/dfBigram.txt";
		String triloc = "Computed/Doc Frequency(Task3-2)/dfTrigram.txt";

		File dir = new File(CorpusBuilding.CORPUS_DIRECTORY);
		File[] files = dir.listFiles();

		// unigram inverted indexing
		for (File corpusfile : files) {
			unigramIndexes(corpusfile);
		}
		System.out.println("Sorting unigram Inverted Indexes ...");
		sortInvertedIndexes(unigramInvertedIndex);
		System.out.println("Writing doc-frequency table for unigram to the file ...");
		savedf(unigramInvertedIndex, uniloc);
		System.out.println("Table saved to disk.\n");
		unigramInvertedIndex.clear();

		files = dir.listFiles();
		// bigram inverted indexing
		for (File corpusfile : files) {
			bigramIndexes(corpusfile);
		}
		System.out.println("Sorting bigram Inverted Indexes ...");
		sortInvertedIndexes(bigramInvertedIndex);
		System.out.println("Writing doc-frequency table for bigram to the file ...");
		savedf(bigramInvertedIndex, biloc);
		System.out.println("Table saved to disk.\n");
		bigramInvertedIndex.clear();

		files = dir.listFiles();
		// trigram inverted indexing
		for (File corpusfile : files) {
			trigramIndexes(corpusfile);
		}
		System.out.println("Sorting trigram Inverted Indexes ...");
		sortInvertedIndexes(trigramInvertedIndex);
		System.out.println("Writing doc-frequency table for trigram to the file ...");
		savedf(trigramInvertedIndex, triloc);
		System.out.println("Table saved to disk.\n");
		trigramInvertedIndex.clear();

	}

	private static void savedf(LinkedHashMap<String, ArrayList<DocumentEntry>> invertedIndex, String location)
			throws IOException {
		File linkfile = new File(location);
		FileWriter fw = new FileWriter(linkfile);
		fw.close();
		// Get a set of the entries
		Set<Entry<String, ArrayList<DocumentEntry>>> set = invertedIndex.entrySet();

		// Get an iterator
		Iterator<Entry<String, ArrayList<DocumentEntry>>> itr = set.iterator();

		fw = new FileWriter(linkfile, true);
		// Write elements
		while (itr.hasNext()) {
			Map.Entry<String, ArrayList<DocumentEntry>> me = (Map.Entry<String, ArrayList<DocumentEntry>>) itr.next();

			ArrayList<DocumentEntry> post = new ArrayList<DocumentEntry>(me.getValue());
			for (DocumentEntry de : post) {
				fw.write("[" + me.getKey() + "," + de.docid + "," + de.tf + "]\n");
			}
		}
		fw.close();
	}

	private static void generateTermFrequency() throws IOException {

		String uniloc = "Computed/Term Frequency(Task 3-1)/tfUnigram.txt";
		String biloc = "Computed/Term Frequency(Task 3-1)/tfBigram.txt";
		String triloc = "Computed/Term Frequency(Task 3-1)/tfTrigram.txt";

		File dir = new File(CorpusBuilding.CORPUS_DIRECTORY);
		File[] files = dir.listFiles();

		System.out.println("Building unigram Inverted Indexes ...");
		// unigram inverted indexing
		for (File corpusfile : files) {
			unigramIndexes(corpusfile);
		}
		// sortInvertedIndexes(unigramInvertedIndex);
		buildSortedtf(unigramInvertedIndex, tfUnigram);		
		System.out.println("Writing term-frequency table for unigram to the file ...");
		savetf(tfUnigram, uniloc);
		System.out.println("Table saved to disk.\n");
		unigramInvertedIndex.clear();

		System.out.println("Building bigram Inverted Indexes ...");
		// bigram inverted indexing
		files = dir.listFiles();
		for (File corpusfile : files) {
			bigramIndexes(corpusfile);
		}
		// sortInvertedIndexes(bigramInvertedIndex);
		buildSortedtf(bigramInvertedIndex, tfBigram);
		System.out.println("Writing term-frequency table for bigram to the file ...");
		savetf(tfBigram, biloc);
		System.out.println("Table saved to disk.\n");
		bigramInvertedIndex.clear();

		System.out.println("Building trigram Inverted Indexes ...");
		// trigram inverted indexing
		files = dir.listFiles();
		for (File corpusfile : files) {
			trigramIndexes(corpusfile);
		}
		// sortInvertedIndexes(trigramInvertedIndex);
		buildSortedtf(trigramInvertedIndex, tfTrigram);
		
		System.out.println("Writing term-frequency table for trigram to the file ...");
		savetf(tfTrigram, triloc);
		System.out.println("Table saved to disk.\n");
		trigramInvertedIndex.clear();

/*
		InvertedIndex chart1 = new InvertedIndex("Rank Vs Probability", "Rank Vs Probability tfUnigram", tfUnigram);

		chart1.pack();
		RefineryUtilities.centerFrameOnScreen(chart1);
		chart1.setVisible(true);
*/
		InvertedIndex chart2 = new InvertedIndex("Rank Vs Probability", "Rank Vs Probability tfBigram", tfBigram);

		chart2.pack();
		RefineryUtilities.centerFrameOnScreen(chart2);
		chart2.setVisible(true);

		InvertedIndex chart3 = new InvertedIndex("Rank Vs Probability", "Rank Vs Probability tfTrigram", tfTrigram);

		chart3.pack();
		RefineryUtilities.centerFrameOnScreen(chart3);
		chart3.setVisible(true);
	}

	private static void buildSortedtf(LinkedHashMap<String, ArrayList<DocumentEntry>> invertedIndex,
			ArrayList<TermEntry> tftable) {
		int termfreq = 0;

		tftable.clear();
		Set<Entry<String, ArrayList<DocumentEntry>>> set = invertedIndex.entrySet();
		// Get an iterator
		Iterator<Entry<String, ArrayList<DocumentEntry>>> itr = set.iterator();

		while (itr.hasNext()) {
			termfreq = 0;
			Map.Entry<String, ArrayList<DocumentEntry>> me = (Map.Entry<String, ArrayList<DocumentEntry>>) itr.next();

			ArrayList<DocumentEntry> post = me.getValue();

			for (DocumentEntry de : post) {
				termfreq += de.tf;
			}

			TermEntry entry = new TermEntry(me.getKey(), termfreq);
			tftable.add(entry);

		}
		Collections.sort(tftable, new Comparator<TermEntry>() {
			@Override
			public int compare(TermEntry te1, TermEntry te2) {
				return te2.tf - te1.tf;
			}
		});

		/*
		 * ArrayList<TermEntry> entries = new ArrayList<Map.Entry<String,
		 * Integer>>(unsortedMap.entrySet());
		 * 
		 * // sorting array in decreasing order of term-frequency
		 * Collections.sort(entries, new Comparator<Map.Entry<String,
		 * Integer>>() { public int compare(Map.Entry<String, Integer> a,
		 * Map.Entry<String, Integer> b){ return
		 * b.getValue().compareTo(a.getValue()); } });
		 */

		/*
		 * tftable.clear();
		 * 
		 * // putting sorted values in sortedMap for (TermEntry te :
		 * unsortedMap) { tftable.add(te); }
		 */
	}

	private static void savetf(ArrayList<TermEntry> tftable, String location) throws IOException {
		File linkfile = new File(location);
		FileWriter fw = new FileWriter(linkfile);
		fw.close();

		fw = new FileWriter(linkfile, true);
		for (TermEntry te : tftable) {
			fw.write(te.term + "," + te.tf + "\n");
			// fw.write((float)te.tf/noOfTokens +"\n");
		}
		fw.close();

	}

	public static void indexBuilder() throws IOException {
		initializationFunction();
		String uniloc = "InvertedIndexes/Unigram/unigramindexes.txt";
		String biloc = "InvertedIndexes/Bigram/bigramindexes.txt";
		String triloc = "InvertedIndexes/Trigram/trigramindexes.txt";

		File dir = new File(CorpusBuilding.CORPUS_DIRECTORY);
		File[] files = dir.listFiles();
		// unigram inverted indexing
		for (File corpusfile : files) {
			unigramIndexes(corpusfile);
		}
		System.out.println("Writing unigram Inverted Indexes into file");
		saveInvertedIndexes(unigramInvertedIndex, uniloc);
		System.out.println("Unigram Inverted Indexes saved to disk.\n");
		unigramInvertedIndex.clear();

		// bigrams inverted indexing
		files = dir.listFiles();
		for (File corpusfile : files) {
			bigramIndexes(corpusfile);
		}
		System.out.println("\nWriting bigram Inverted Indexes into file");
		saveInvertedIndexes(bigramInvertedIndex, biloc);
		System.out.println("Bigram Inverted Indexes saved to disk.\n");
		bigramInvertedIndex.clear();

		// trigrams inverted indexing
		files = dir.listFiles();
		for (File corpusfile : files) {
			trigramIndexes(corpusfile);
		}
		System.out.println("\nWriting trigram Inverted Indexes into file");
		saveInvertedIndexes(trigramInvertedIndex, triloc);
		System.out.println("Trigram Inverted Indexes saved to disk.\n");
		trigramInvertedIndex.clear();
	}

	private static void writeTokenCount() throws IOException {
		File linkfile = new File("Computed/tokenCountPerDoc.txt");
		FileWriter fw = new FileWriter(linkfile);
		fw.close();

		Set<Entry<Integer, Integer>> set = tokenCount.entrySet();

		// Get an iterator
		Iterator<Entry<Integer, Integer>> itr = set.iterator();

		fw = new FileWriter(linkfile, true);
		// Writing data structure to the file
		while (itr.hasNext()) {
			Map.Entry<Integer, Integer> me = (Map.Entry<Integer, Integer>) itr.next();
			// write the docID and the corresponding # of Tokens
			fw.write("[" + me.getKey() + "," + me.getValue() + "]" + "\n");

		}
		fw.close();
	}

	private static void saveInvertedIndexes(LinkedHashMap<String, ArrayList<DocumentEntry>> invertedIndex,
			String location) throws IOException {
		// Sorting the inverted indexes
		sortInvertedIndexes(invertedIndex);

		// Preparing to write in a file
		File linkfile = new File(location);
		FileWriter fw = new FileWriter(linkfile);
		fw.close();
		// Get a set of the entries
		Set<Entry<String, ArrayList<DocumentEntry>>> set = invertedIndex.entrySet();

		// Get an iterator
		Iterator<Entry<String, ArrayList<DocumentEntry>>> itr = set.iterator();

		fw = new FileWriter(linkfile, true);
		// Write elements
		while (itr.hasNext()) {
			Map.Entry<String, ArrayList<DocumentEntry>> me = (Map.Entry<String, ArrayList<DocumentEntry>>) itr.next();

			fw.write(me.getKey() + " -> ");

			ArrayList<DocumentEntry> post = new ArrayList<DocumentEntry>(me.getValue());

			for (DocumentEntry de : post) {
				fw.write("[" + de.docid + "," + de.tf + "] ");
			}
			fw.write("\n");

		}
		fw.close();
		System.out.println("Done writing the inverted indexes.");

	}

	private static void unigramIndexes(File corpusfile) {
		String docname = corpusfile.getName();
		// System.out.println("docname:" + docname);
		docname = docname.replace(".txt", "");
		Integer docid = docID.get(docname);
		// System.out.println("docid:" + docid);
		String line;
		int i;
		boolean flag;

		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(corpusfile);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				// System.out.println(line);
				String[] tokens = line.split(" ");
				// Saving the number of tokens in the doc (corpusfile) in data
				// structure tokenCount
				tokenCount.put(docid, tokens.length);
				noOfTokens += tokens.length;
				for (i = 0; i < tokens.length; i++) {
					ArrayList<DocumentEntry> post;
					flag = false;
					if (unigramInvertedIndex.containsKey(tokens[i])) {
						post = unigramInvertedIndex.get(tokens[i]);
						for (DocumentEntry p : post) {
							if (p.docid == docid) {
								p.tf++;
								flag = true;
								break;
							}
						}
						if (!flag) {
							post.add(new DocumentEntry(docid, 1));
							unigramInvertedIndex.put(tokens[i], post);
						}
					} else {
						post = new ArrayList<DocumentEntry>();
						post.add(new DocumentEntry(docid, 1));
						// post.put(docid,termFrequency);
						unigramInvertedIndex.put(tokens[i], post);
					}
				}
			}
			// Always close files.
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + corpusfile + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + corpusfile + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}

	}

	private static void bigramIndexes(File corpusfile) {
		String docname = corpusfile.getName();
		// System.out.println("docname:" + docname);
		docname = docname.replace(".txt", "");
		Integer docid = docID.get(docname); // getting the docID of this
											// document in corpus
		// System.out.println("docid:" + docid);

		String bigram;
		String line;
		int i;
		boolean flag;

		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(corpusfile);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				// System.out.println(line);
				String[] tokens = line.split(" ");

				for (i = 0; i < tokens.length - 1; i++) {
					bigram = tokens[i] + " " + tokens[i + 1];
					// System.out.println("tokens: " + bigram);
					ArrayList<DocumentEntry> post;
					flag = false;
					if (bigramInvertedIndex.containsKey(bigram)) {
						post = bigramInvertedIndex.get(bigram);
						for (DocumentEntry p : post) {
							if (p.docid == docid) {
								p.tf++;
								flag = true;
								break;
							}
						}
						if (!flag) {
							post.add(new DocumentEntry(docid, 1));
							bigramInvertedIndex.put(bigram, post);
						}
					} else {
						post = new ArrayList<DocumentEntry>();
						post.add(new DocumentEntry(docid, 1));
						// post.put(docid,termFrequency);
						bigramInvertedIndex.put(bigram, post);
					}
				}
			}
			// Always close files.
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + corpusfile + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + corpusfile + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
	}

	private static void trigramIndexes(File corpusfile) {
		String docname = corpusfile.getName();
		// System.out.println("docname:" + docname);
		docname = docname.replace(".txt", "");
		Integer docid = docID.get(docname); // getting the docID of this
											// document in corpus
		// System.out.println("docid:" + docid);
		String trigram;
		boolean flag;

		String line;
		int i;

		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(corpusfile);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				// System.out.println(line);
				String[] tokens = line.split(" ");

				for (i = 0; i < tokens.length - 2; i++) {
					trigram = tokens[i] + " " + tokens[i + 1] + " " + tokens[i + 2];
					// System.out.println("tokens: " + trigram);
					ArrayList<DocumentEntry> post;
					flag = false;
					if (trigramInvertedIndex.containsKey(trigram)) {
						post = trigramInvertedIndex.get(trigram);
						for (DocumentEntry p : post) {
							if (p.docid == docid) {
								p.tf++;
								flag = true;
								break;
							}
						}
						if (!flag) {
							post.add(new DocumentEntry(docid, 1));
							trigramInvertedIndex.put(trigram, post);
						}
					} else {
						post = new ArrayList<DocumentEntry>();
						post.add(new DocumentEntry(docid, 1));
						// post.put(docid,termFrequency);
						trigramInvertedIndex.put(trigram, post);
					}
				}
			}
			// Always close files.
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + corpusfile + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + corpusfile + "'");
			// Or we could just do this:
			// ex.printStackTrace();
		}
		//
	}

	/*
	 * private static void sortInvertedIndexes(LinkedHashMap<String,
	 * LinkedHashMap<Integer, Integer>> invertedIndex) {
	 * ArrayList<Map.Entry<String, LinkedHashMap<Integer, Integer>>> entries =
	 * new ArrayList<Map.Entry<String,LinkedHashMap<Integer,
	 * Integer>>>(invertedIndex.entrySet());
	 * 
	 * // sorting array in decreasing order of term-frequency
	 * Collections.sort(entries, new
	 * Comparator<Map.Entry<String,LinkedHashMap<Integer, Integer>>>() { public
	 * int compare(Map.Entry<String, LinkedHashMap<Integer, Integer>> a,
	 * Map.Entry<String, LinkedHashMap<Integer, Integer>> b){ return
	 * a.getKey().compareTo(b.getKey()); } });
	 * 
	 * invertedIndex.clear(); // putting sorted values in sortedMap for
	 * (Map.Entry<String,LinkedHashMap<Integer, Integer>> entry : entries) {
	 * invertedIndex.put(entry.getKey(), entry.getValue()); } }
	 */

	private static void sortInvertedIndexes(LinkedHashMap<String, ArrayList<DocumentEntry>> invertedIndex) {
		Map<String, ArrayList<DocumentEntry>> sortedMap = new TreeMap<String, ArrayList<DocumentEntry>>(invertedIndex);
		// System.out.println(sortedMap);

		invertedIndex.clear();
		// invertedIndex = new LinkedHashMap<String,
		// ArrayList<DocumentEntry>>(sortedMap);
		// putting sorted values in sortedMap
		for (Map.Entry<String, ArrayList<DocumentEntry>> entry : sortedMap.entrySet()) {
			invertedIndex.put(entry.getKey(), entry.getValue());
		}
		System.out.println("Sorted now.");
	}
}
