package DiploidCarryOvertool.chain;

import java.util.ArrayList;

/**
 * Chain file contains a list of headers and a each header contains a list of lines associated with that header from the chain file.
 * Learn more about chain files at UCSC: http://genome.ucsc.edu/goldenPath/help/chain.html
 * Example format:
 * chain 247006070 chr1 247249719 + 0 247249719 chr1_paternal 247023597 + 0 247023597 1
 * 768165 0 2
 * 12394 0 2
 * 62 2 0
 * 6616 4 0
 * 2923
 * 
 * chain 242736403 chr2 242951149 + 0 242951149 chr2_paternal 242755937 + 0 242755937 2
 * 31254 0 1
 * 11277 0 1
 * 76793 0 3
 * 17138
 * 
 * 
 * @author David "Corvette" Thomas
 *
 */
public class chainFile {

	ArrayList<ArrayList<chainLine>> lines;
	ArrayList<String> header;
	
	
	/**
	 * Creates a new empty chain File object.  Add headers and lines useing the respective functions.
	 */
	public chainFile(){
		//initialize variables
		this.header = new ArrayList<String>();
		lines = new ArrayList<ArrayList<chainLine>>();
		
	}
	
	
	/**
	 * Returns a string that is identical to the original file format for this chainFile.
	 * @return Returns a string representation of this file.
	 */
	public String toString(){
		StringBuffer output = new StringBuffer();
		
		//Iterate through the headers
		for(int i = 0; i < header.size(); i++){
			output.append(header.get(i) + "\n");
			
			//Append each headers associated lines
			for(int j = 0; j < lines.get(i).size(); j++){
				output.append(lines.get(i).get(j) + "\n");
			}//j
			
			//Separate each section with a new line
			output.append("\n");
		}//i
		
		
		return output.toString();
	}//toString
	
	/**
	 * Add a header line for a specific index.  The index used here should be the same one used for hte add line.
	 * @param index Place in the header array to place this header.  
	 * @param headerInfo  The string to insert into this header position.
	 */
	public void addHeader(int index, String headerInfo){
		//Increase the list until there are enough sections for the index
		while(index > header.size()){
			header.add("");
		}
		
		header.add(headerInfo);
	
		
	}//addHeader
	
	/**
	 * Add line inserts a given line into the appropriate bucket.
	 * 
	 * @param line chainLine object to add to this chainFile.  Index specifies which ArrayList of chain lines to add this to.
	 * 		  If the list of sections is not long enough, it will be increased in size until that section is added.
	 */
	public void addLine(int index, chainLine line){
		//Increase the list until there are enough sections for the index
		while(index + 1 > lines.size()){
			lines.add(new ArrayList<chainLine>());
		}
		
		//Add the line to that index
		lines.get(index).add(line);
	}//addLine
	
	/**
	 * Get lines returns an arraylist of arraylist of chain lines, each representing the lines following the corresponding header.
	 * @return return the entire collection of arraylist of lines
	 */
	public ArrayList<ArrayList<chainLine>> getLines(){
		return lines;
	}
	
	/**
	 * getLinesSection returns a ArrayList of chainLine objects at a specific index.
	 * @return return a single ArrayList of lines from a specific section of a chain file
	 */
	public ArrayList<chainLine> getLinesSection(int section){
		return lines.get(section);
	}
	
	/**
	 * Returns a string representing a specified header line in this chainfile.
	 * @return A specific header line of this file
	 */
	public String getHeader(int index){
		return header.get(index);
	}
	/**
	 * Returns a list of all of the header lines for this file.
	 * @return The header line of this file
	 */
	public ArrayList<String> getHeader(){
		return header;
	}
	
	
}
