package BedFileSetOperations;

import java.io.File;
import java.util.ArrayList;

import PersonalProteome.Definitions;


/**
 * BedFile Line is a class that stores information on a single bed file line.
 * @author David "Corvette" Thomas
 *
 */
public class BedFileLine implements Comparable<BedFileLine>{

	
	private int id = 0;
	private int chromosomeName = -1;
	private int startLocation = -1;
	private int stopLocation = -1;
	private String restOfLine = "";
	
	private boolean markedForRemoval = false;
	
	private ArrayList<File> parentFile;
	/**
	 * @param id
	 * @param chromosomeName
	 * @param startLocation
	 * @param stopLocation
	 * @param sequence
	 * @param score
	 * @param strand
	 * @param blockCount
	 * @param blockSize
	 * @param blockStart
	 */
	public BedFileLine(int id, int chromosomeName, int startLocation,
			int stopLocation, String restOfLine, File file) {
		super();
		this.id = id;
		this.chromosomeName = chromosomeName;
		this.startLocation = startLocation;
		this.stopLocation = stopLocation;

		this.restOfLine = restOfLine;
		
		parentFile = new ArrayList<File>();
		parentFile.add(file);
	}
	
	
	/** 
	 * Returns a valid bed file line representing this object.
	 */
	public String toString() {
		
		
		return  Definitions.convertChrmNumToString(chromosomeName) + "\t"
				+ startLocation + "\t"
				+ stopLocation + "\t"
				+ restOfLine;
	}//toString


	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @return the chromosomeName
	 */
	public int getChromosomeName() {
		return chromosomeName;
	}
	/**
	 * @return the startLocation
	 */
	public int getStartLocation() {
		return startLocation;
	}
	/**
	 * @return the stopLocation
	 */
	public int getStopLocation() {
		return stopLocation;
	}

	public void markForRemoval(){
		markedForRemoval = true;
	}
	
	public boolean isMarkedForRemoval(){
		return markedForRemoval;
	}
	
	
	public ArrayList<File> getParentFiles(){
		return parentFile;
	}
	public void addParentFile(File f){
		parentFile.add(f);
	}

	@Override
	public int compareTo(BedFileLine bfl) {
		// TODO Auto-generated method stub
		
		if(this.chromosomeName < bfl.getChromosomeName()){
			return -1;
		}
		if(this.chromosomeName > bfl.getChromosomeName()){
			return 1;
		}
		
		
		if(this.startLocation < bfl.getStartLocation()){
			return -1;
		}
		if(this.startLocation > bfl.getStartLocation()){
			return 1;
		}
		
		
		if(this.stopLocation < bfl.getStopLocation()){
			return -1;
		}
		if(this.stopLocation > bfl.getStopLocation()){
			return 1;
		}
		return 0;
	}//compareTo


	
}//BedFileLine
