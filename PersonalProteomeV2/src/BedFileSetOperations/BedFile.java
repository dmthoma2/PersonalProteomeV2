package BedFileSetOperations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import PersonalProteome.Definitions;


/**
 * BedFile is a abstract representation of a bed format file.  It is the child of an ArrayList parameterized with BedFileLine. 
 * The constructor takes in a file and automatically populates this list with BedFileLine objects.
 * 
 * @author David "Corvette" Thomas
 *
 */
public class BedFile extends ArrayList<BedFileLine> {
	
	//Serial Version ID, Not used for anything, but good to have around incase this class is ever updated
	private static final long serialVersionUID = 1L;
	
	private String fileName;
	private boolean inError = false;
	
	/**
	 * @param bedFile Bed File to upload and store meta-data/create list of BedFileLine objects from.
	 */
	public BedFile(File bedFile){
		this.fileName = bedFile.getName();
		
		//Upload the bed File
		try {
			BufferedReader br = new BufferedReader(new FileReader(bedFile));
			
			String line;
				int id = 0;
				while((line = br.readLine()) != null){
					String[] chunks = line.split("\\s");
					
					String token = chunks[0];
					int chromo = -1;
						if(token.equalsIgnoreCase("chrM")){
							chromo = Definitions.chromosomeM;
						}else if(token.equalsIgnoreCase("chrX")){
							chromo = Definitions.chromosomeX;
						}else if(token.equalsIgnoreCase("chrY")){
							chromo = Definitions.chromosomeY;
						}else{
							chromo = Integer.parseInt(token.substring(token.indexOf('r') + 1));	
						}
						

						int start = Integer.parseInt(chunks[1]);
						int stop = Integer.parseInt(chunks[2]);
						
						String restOfLine = "";
						for(int i = 3; i < chunks.length; i++){
							
							restOfLine += chunks[i];
							if(i != chunks.length - 1){
								restOfLine += "\t";
							}//if
						}//for
						
						this.add(new BedFileLine(id, chromo, start, stop, restOfLine, bedFile));

					id++;
				}//while
		
		} catch (FileNotFoundException e) {
			System.out.println("Unable to setup this bed file.  Error uploading file.");
			inError = true;
		} catch (IOException e) {	
			System.out.println("Unable to read this bed file.  Error uploading file.");
			inError = true;
		}

	}//bedFile

	public String getFileName(){
		return fileName;
	}
	
	public boolean wasUploadedSuccessfully(){
		return inError;
	}
}
