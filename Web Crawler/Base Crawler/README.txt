Base Crawler is the project folder.

Uses jsoup library which has been included in the build path and is provided in the "lib" folder
No command line arguments to be passed.

" I have used the Eclipse IDE "
adding 2 screenshots regarding configurations: eclipsex64_config1 & eclipsex64_config2
1) (right click on project) -> Build Path -> Configure Build Path and select the jsoup library and the JavaSE-1.7

Crawler1.java is the Task 1  (linkfileTask1)
FocusedCrawler.java is the Task 2-A (linkfileTask2A)
FocusedCrawlingDFS.java is the Task 2-B (linkfileTask2B)
Combined.java is the Task 3 (linkfileTask3)

"Link Files" folder contains all the text files with urls crawled listed for each case.

For each .java,

"location" is the directory location to save the text file with URLs crawled. : by default the same folder 

"htmlloc" is the directory location to save all the raw html files. All html pages will be saved in the respective below folders:
by default: 
URLs/Task1 for Task1
URLs/Task2A for Task2A
URLs/Task2B for Task2B
URLs/Task3 for Task3
