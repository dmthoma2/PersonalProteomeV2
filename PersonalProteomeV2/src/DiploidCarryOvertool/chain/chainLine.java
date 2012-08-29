package DiploidCarryOvertool.chain;


/**
 * A single .chain format file line. Information can be found at UCSC: http://genome.ucsc.edu/goldenPath/help/chain.html
 * 
 * A dt of 0 and a dq of > 0 represents a insertion in the paternal sequence as compared to the reference.
 * A dt of > 0 and a dq of  0 represents a deletion in the paternal sequence as compared to the reference.
 * 
 * 
 * @author David "Corvette" Thomas
 *
 */
public class chainLine {

	
	
	int size;
	int dt;
	int dq;
	
	/**
	 * @param size
	 * @param dt
	 * @param dq
	 */
	public chainLine(int size, int dt, int dq) {
		super();
		this.size = size;
		this.dt = dt;
		this.dq = dq;
		
		if(dt != 0 && dq != 0){
//			U.p("Double Found");
//			U.p(this.toString());
		}
	}
	
	
	/**
	 * This constructor should be used as the last line in a chain file.  This line has only a single integer
	 * @param singleInt
	 */
	public chainLine(int singleInt){
		super();
		this.size = singleInt;
		dt = -1;
		dq = -1;
	}

	/**
	 * Returns a tab delimited string of this line as it would appear in a chain file. size dt dq
	 */
	public String toString(){
		
		if(dt == -1 && dq == -1){
			return size + "";
		}else{
			return size + "\t" + dt + "\t" + dq;
		}
	}
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return the dt (Distance skipped by the reference genome at this block)
	 */
	public int getDt() {
		return dt;
	}

	/**
	 * @return the dq (Distance skipped by the genome at this block)
	 */
	public int getDq() {
		return dq;
	}
	
	
	
	
}
