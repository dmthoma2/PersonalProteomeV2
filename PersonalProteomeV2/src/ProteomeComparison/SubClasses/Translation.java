package ProteomeComparison.SubClasses;

import PersonalProteome.Definitions;

/**
 * Translation stores a single protein with it associated ID.
 * @author David "Corvette" Thomas
 *
 */
public class Translation {

	private String geneID;
	private String transcriptID;
	private String sequence;
	private int matchType = Definitions.MATCH_TYPE_UNMATCHED;
	private int variants = 0;

	/**
	 * @param geneID The gene ID of this translation.
	 * @param transcriptID The transcript ID of this translation.  
	 * @param sequence  The sequence of this translation
	 */
	public Translation(String geneID, String transcriptID, String sequence) {
		super();
		this.geneID = geneID;
		this.transcriptID = transcriptID;
		this.sequence = sequence;
	}//constructor
	
	/**
	 * @return a string representation of this object.  TranscirptID|GeneID|Sequence
	 */
	public String toString(){
		return transcriptID + "|" + geneID + "|" + sequence;
	}//toString
	/**
	 * @return the geneID
	 */
	public String getGeneID() {
		return geneID;
	}//getGeneID
	/**
	 * @return the transcriptID
	 */
	public String getTranscriptID() {
		return transcriptID;
	}//getTranscriptID
	/**
	 * @return the sequence
	 */
	public String getSequence() {
		return sequence;
	}//getSequence
	/**
	 * 
	 */
	public void setSequence(String newSeq){
		this.sequence = newSeq;
	}//setSequence
	/**
	 * Based on matched type in definitions.
	 * @return the matchType
	 */
	public int getMatchType() {
		return matchType;
	}//getMatchType
	/**
	 * Set based on matched type in definitions.
	 * @param matchType the matchType to set
	 */
	public void setMatchType(int matchType) {
		this.matchType = matchType;
	}//setMatchType
	
	/**
	 * @return
	 */
	public int getVariants(){
		return variants;
	}//getVariatns
	/**
	 * Increments the variant count by 1.
	 */
	public void incrementVariants(){
		variants++;
	}//incrementVariants
	
	
}//Translation
