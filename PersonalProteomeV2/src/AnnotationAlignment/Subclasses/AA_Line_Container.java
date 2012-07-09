package AnnotationAlignment.Subclasses;

import java.util.ArrayList;
import java.util.Scanner;


/**
 * AA_Line_Contaier stores a annotation line and in accompanying reference strands and updated locations.  This is simply a container for all information about uploading a line and converting it.
 * It also handles toString for a line.
 * 
 * @author David "Corvette" Thomas
 *
 */
public class AA_Line_Container implements Comparable<AA_Line_Container>{

	//The GTFLine to store inside of this AA
	private GTF_Line gtfLine;
	
	//The reference string for this containers stop and start locations
	private String refStartStrand;
	private String refStopStrand;
	
	//The array of start/stop locations for this object.  These arrays store the locations of the top matches for this object.
	private ArrayList<Integer> newStarts = new ArrayList<Integer>();
	private ArrayList<Integer> newStops = new ArrayList<Integer>();
	
	//Chromosome this occurs in
	private int chromosome;
	
	
	/**
	 * The gtfLine is the minimum amount of information to create a AA_Line_Container.  It needs a refStartStrand/refStopStrand and a newStarts/newStops arrays also.
	 * @param gtfLine
	 * @param chromosome
	 */
	public AA_Line_Container(GTF_Line gtfLine, int chromosome) {
		this.gtfLine = gtfLine;
		this.chromosome = chromosome;
	}
	
	/**
	 * toString has the ability to but the code is commented out: Returns a series of annotation lines with the new start/stop locations added.  For every possible start/stop location in the 
	 * newStarts/newStops arrays, a GTF line is returned.  The best guess is returned normally, and all secondary guesses are returned as commented out lines.
	 * 
	 * 
	 * @return A updated version of the original GTF line.  It is updated with the new predicted locations.  
	 */
	public String toString(){
		String line = gtfLine.toString();
		StringBuffer output = new StringBuffer();
		
		//This code outputs every guess, versus just the top guess.  It comments out every guess but the top guess with ##
//		//Loop through the starts
//		for(int i = 0; i < newStarts.size(); i++){
//			//Loops thorugh the stops
//			for(int j = 0; j < newStops.size(); j++){
//				if(i != 0 && j != 0){
//					output.append("##");
//				}
//				
//				
//				Scanner s = new Scanner(line);
//
//				
//				//Output the first three columns
//				for(int k = 0; k < 3; k++){
//					output.append(s.next() + "\t");
//				}
//				
//				//Skip the start and stop locations
//				s.next();
//				s.next();
//				//Output the new start/stop locations
//				output.append(newStarts.get(i) + "\t" + newStops.get(j));
//				
//				//Then output the next line and a new line character if needed for formating.
//				if(i == newStarts.size() - 1 && j == newStops.size() - 1){
//					output.append(s.nextLine());
//				}else{
//					
//				}//if
//				
//			}//inner loop
//			
//		}//outer loop
		
		
			Scanner s = new Scanner(line);
	
			
			//Output the first three columns
			for(int k = 0; k < 3; k++){
				output.append(s.next() + "\t");
			}
			
			//Skip the start and stop locations
			s.next();
			s.next();
			//Output the new start/stop locations
			output.append(newStarts.get(0) + "\t" + newStops.get(0));
			output.append(s.nextLine());
			
		//Return the output
		return output.toString();
	}//toString
	
	/**
	 * @return the gtfLine
	 */
	public GTF_Line getGtfLine() {
		return gtfLine;
	}
	/**
	 * @return the refStartStrand
	 */
	public String getRefStartStrand() {
		return refStartStrand;
	}
	/**
	 * @return the refStopStrand
	 */
	public String getRefStopStrand() {
		return refStopStrand;
	}
	/**
	 * @param gtfLine the gtfLine to set
	 */
	public void setGtfLine(GTF_Line gtfLine) {
		this.gtfLine = gtfLine;
	}
	/**
	 * @param refStartStrand the refStartStrand to set
	 */
	public void setRefStartStrand(String refStartStrand) {
		this.refStartStrand = refStartStrand;
	}
	/**
	 * @param refStopStrand the refStopStrand to set
	 */
	public void setRefStopStrand(String refStopStrand) {
		this.refStopStrand = refStopStrand;
	}

	/**
	 * @return the newStarts
	 */
	public ArrayList<Integer> getNewStarts() {
		return newStarts;
	}

	/**
	 * @return the newStops
	 */
	public ArrayList<Integer> getNewStops() {
		return newStops;
	}

	/**
	 * @param newStarts the newStarts to set
	 */
	public void setNewStarts(ArrayList<Integer> newStarts) {
		this.newStarts = newStarts;
	}

	/**
	 * @param newStops the newStops to set
	 */
	public void setNewStops(ArrayList<Integer> newStops) {
		this.newStops = newStops;

	}
	@Override
	/**
	 * Sort simply on chromosome.  This will ensure that the annotation will be grouped based on chromosome in the correct order from Collections.sort.
	 * 
	 * This method is for sorting these objects after a multithreaded task messing up a collection of these objects.
	 */
	public int compareTo(AA_Line_Container arg0) {
		
		if(this.chromosome < arg0.chromosome){
			return -1;
		}
		
		if(this.chromosome > arg0.chromosome){
			return 1;
		}
		// TODO Auto-generated method stub
		return 0;
	}

	
}//AA__Line_Container
