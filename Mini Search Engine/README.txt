The easiest way to run this project is to use eclipse
Choose 'File', 'Import' and 'Existing Projects into Workspace'
And select the 'IR-Project' folder under 'Code' folder
Then run in the following sequence

searchEngine package
CorpusBuilding -> generate two folder 'corpus' and 'stopping corpus'
Indexer -> generate a folder 'index result' saving all index results
Retrieval -> generate two folder 'Lucene Index' saving lucene index
'retrieval result' saving 7 runs result plus the stemming.
Ignore *.dat files, they are used in evaluation

evaluation package
searchEngineEvaluation -> generate a folder 'evaluation result', saving all evaluation results

snippetGeneration package
SnippetGeneration -> provide interactive searching and print snippet for top 10 documents
the query terms in snippet content are highlighted with [].

If using command line, you need to use 'javac' to compile every file, and run them manually.
Remember to include Lucene libraries under the 'lib' folder to compile and run the Retrieval.java