package searchEngine;
import java.util.*;

public class QueryResult implements java.io.Serializable{
	private static final long serialVersionUID = 2L;
	private int queryID;
	private ArrayList<Integer> docList = new ArrayList<Integer>(); // 100 ranked docs or less
	HashMap<Integer, Double> scoreMap = new HashMap<Integer, Double>(); // doc map to doc score
	private String systemName;
	// query_id	Q0 doc_id rank score system_name
	
	public QueryResult(int queryID, ArrayList<Integer> docList, HashMap<Integer, Double> scoreMap, String systemName){
		this.setQueryID(queryID);
		this.setDocList(docList);
		this.scoreMap = scoreMap;
		this.setSystemName(systemName);
	}

	public int getQueryID() {
		return queryID;
	}

	public void setQueryID(int queryID) {
		this.queryID = queryID;
	}

	public ArrayList<Integer> getDocList() {
		return docList;
	}

	public void setDocList(ArrayList<Integer> docList) {
		this.docList = docList;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}

}
