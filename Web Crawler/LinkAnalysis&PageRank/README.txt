LinkAnalysis&PageRank is the project folder.

Uses jsoup library which has been included in the build path and is provided in the "lib" folder
No command line arguments to be passed.

In Eclipse, 
(right click on project) -> Build Path -> Configure Build Path and select the jsoup library from lib folder (add external jars) and the JavaSE-1.7

DirectedWebGraph.java is the Task 1  
PageRank.java is the Task 2

But,both classes uses functions among themselves.


"Task1", "Task2", "Task3" are the result folders.
wt2g_inlinks is the WT2g collection.

Task1:
	graphG1.txt is the graph G1
	sink_source_G1 contains sink and source of G1
	sink_source_G2 contains sink and source of G2

	
Task2:
	perplexityG1: perplexity of graph G1
	perplexityG2: perplexity of graph G2
	top50G1 : top 50 page rank score urls of G1
	top50G2 : top 50 page rank score urls of G2
	
Task3:
	top5inlinksG1 : top 5 links with max inlinks in G1
	top5inlinksG2 : top 5 links with max inlinks in G2
	
	