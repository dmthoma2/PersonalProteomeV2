package AnnotationAlignment.Subclasses;

/**
 * Match represents the results of a single sequence/sequence comparison.  It keeps a sequence, genetic locations and a score.  It is comparable with other match objects based on its score.
 * 
 * @author David "Corvette" Thomas
 *
 */
public class Match implements Comparable<Match>{

	String sequence;
	int start;
	int stop;
	int score;
	/**
	 * @param sequence
	 * @param start
	 * @param stop
	 * @param score
	 */
	public Match(String sequence, int start, int stop, int score) {
		super();
		this.sequence = sequence;
		this.start = start;
		this.stop = stop;
		this.score = score;
	}
	/**
	 * @return the sequence
	 */
	public String getSequence() {
		return sequence;
	}
	/**
	 * @return the start
	 */
	public int getStart() {
		return start;
	}
	/**
	 * @return the stop
	 */
	public int getStop() {
		return stop;
	}
	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}

	/**
	 * Sorts based on score
	 */
	public int compareTo(Match m) {
		if(this.getScore() < m.getScore()){
			return -1;
		}
		if(this.getScore() > m.getScore()){
			return 1;
		}
		return 0;
	}//compareTo

	
	

}//Match
