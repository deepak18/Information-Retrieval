package searchEngine;

public class DocumentEntry implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public int docid;
	public int tf;

	public DocumentEntry(int docid, int tf) {
		this.docid = docid;
		this.tf = tf;
	}
}
