package AnnotationAlignment.Subclasses;

/**
 * GTF_Line represents a single line in a GENCODE formatted annotation file.  It stores the start and stop location as intergers, and everything else as a large string.
 * @author davidthomas
 *
 */
public class GTF_Line {

	
	String beginning;
	int start;
	int stop;
	String end;
	int id;
	
	
	/**
	 * @param beginning
	 * @param start
	 * @param stop
	 * @param end
	 */
	public GTF_Line(int id, String beginning, int start, int stop, String end) {
		super();
		this.beginning = beginning;
		this.start = start;
		this.stop = stop;
		this.end = end;
	}
	
	
	public String toString(){
		return beginning + start + "\t" + stop + end;
	}
	
	public int getid(){
		return id;
	}
	/**
	 * @return the beginning
	 */
	public String getBeginning() {
		return beginning;
	}
	/**
	 * @return the start
	 */
	public int getStartLocation() {
		return start;
	}
	/**
	 * @return the stop
	 */
	public int getStopLocation() {
		return stop;
	}
	/**
	 * @return the end
	 */
	public String getEnd() {
		return end;
	}
}
