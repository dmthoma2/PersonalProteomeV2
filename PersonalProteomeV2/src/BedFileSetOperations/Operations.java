package BedFileSetOperations;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * 
 * Operations is a self contained driver that calculates various set properties.  Operations current supports Union and Intersection set operations.
 * 
 * 
 * @author David "Corvette" Thomas
 */
public class Operations {

	
	public static void main(String[] args){
		
		String input = "/Users/davidthomas/Peppy/ProteomeV2/BedFileSetOperations/BedFiles/Jainab/";
		
		
		Operations o = new Operations(input);
		
		o.findAndPrintUnion();
		
		o.findAndPrintIntersection();
		
		System.out.println("Done");
		
	}//main
	
	
	private ArrayList<BedFile> inputFiles;
	private File inputDir;
	
	/**
	 * @param directoryOfFiles  A directory containing bed files to upload.  These files cannot be in a sub directory and they must end in .bed.
	 */
	public Operations(String directoryOfFiles){
		inputDir = new File(directoryOfFiles);
		
		File folder = new File(directoryOfFiles);
		File [] files = folder.listFiles();
		
		inputFiles = new ArrayList<BedFile>(files.length);
		
		//Get every bedfile needed.
		for (int i = 0; i < files.length; i++) {
			if (files[i].isHidden()) continue;
			if (files[i].isDirectory()) continue;
			if (files[i].getName().endsWith(".bed")) {
				inputFiles.add(new BedFile(files[i]));
			}//if
		}//for
		
	}//Operations
	
	
	/**
	 *findAndPrintUnion finds the union for all of the input bed files, and prints this information out to the input directory in a file entitled "Union.txt"
	 */
	public void findAndPrintUnion(){
		//Get all of the lines and add them to a master list
		ArrayList<BedFileLine> allLines = new ArrayList<BedFileLine>();
		
		for(BedFile bf: inputFiles){
			allLines.addAll(bf);
		}//for
		
		//Go through and find any doubles.  Mark them for removal
		for(int i = 0; i < allLines.size(); i++){

			BedFileLine bflFirst = allLines.get(i);
			
			if(bflFirst.isMarkedForRemoval()){
				continue;
			}//if
			
			for(int j = i + 1; j < allLines.size(); j++){
				BedFileLine bflSecond = allLines.get(j);
				
				if(bflFirst.compareTo(bflSecond) == 0){
					bflSecond.markForRemoval();
					
					bflFirst.addParentFile(bflSecond.getParentFiles().get(0));
				}//if
				
			}//for
		
		}//for
		

		//Now all lines has been go through, and duplicates have been marked for removal, write them out to disk, indicating which files each line occurs in
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(inputDir.getAbsolutePath() + "/" + "Union.txt")));

			//Create the header
			String header = "Chromosome" + "\t" + "StartLocation" + "\t" + "StopLocation" + "\t";
			String[] chunks = allLines.get(0).toString().split("\\s");
			
			//Write out place holder values for the bed file, since this information can change depending on the bed file size/version
			for(int k = 3; k < chunks.length; k++){
				header += "BedValue" + "\t";
			}
			//Add the file names to the end of the list, so each bed line can clearly show its file of origin
			for(int i = 0; i < inputFiles.size(); i++){
				header += inputFiles.get(i).getFileName() + "\t";
			}
			
			//Calculate the size of the union
			int count = 0;
			for(BedFileLine bfl: allLines){
				if(bfl.isMarkedForRemoval()){
					continue;
				}//if
				count++;
			}//for
			out.write("//SizeOfUnion: " + count);
			out.newLine();
			
			
			//Write the header
			out.append(header);
			out.newLine();

			
			//Write out each bed line, ignoring those marked for removal since they are duplicates
			for(BedFileLine bfl: allLines){
				if(bfl.isMarkedForRemoval()){
					continue;
				}//if

				out.append(bfl.toString() + "\t" + getFileLocationString(bfl.getParentFiles()));
				out.newLine();
				
			}//for
		
			
			//Flush and close the writer
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//catch	
	}//findAndPrintUnion
	
	
	private String getFileLocationString(ArrayList<File> bedFiles){
		String out = "";
		boolean[] values = new boolean[inputFiles.size()];
		
		//Default this array to false
		for(int j = 0; j < values.length; j++){
			values[j] = false;
		}//for
		
		
		//Mark every file that this occurs in as true
		for(File f: bedFiles){
			int i = getIndexFromFileName(f);
			values[i] = true;
		}//for
		
		//Create the string based on the computed true/false values;
		for(int k = 0; k < values.length; k++){
			out += values[k];
			if(k != values.length -1){
				out += "\t";
			}//if
		}//for
		
		return out;
	}//getFileLocationString
	
	
	/**
	 * @param The file to look for in the input files array.
	 * @return Returns the index into the input files, based on the name of the file passed in
	 */
	private int getIndexFromFileName(File file){
		for(int i = 0; i < inputFiles.size(); i++){
			if(inputFiles.get(i).getFileName().equalsIgnoreCase(file.getName())){
				return i;
			}//if
		}//for
		return -1;
	}//getIndexFromFileName
	
	/**
	 * findAndPrintIntersection finds the intersection of all input bed files, and writes a file containing those bed file lines to the input directory entitled "Intersection.txt"
	 */
	public void findAndPrintIntersection(){
		
		//Get all of the bed file lines excluding the first one
		ArrayList<BedFile> allFilesExcludingFirst = new ArrayList<BedFile>();
		for(int i = 1; i < inputFiles.size(); i++){
			allFilesExcludingFirst.add(inputFiles.get(i));
		}//for
		
		//Only search through the first file, since every item in the intersection must be in this file
		ArrayList<BedFileLine> intersection = new ArrayList<BedFileLine>();
		for(BedFileLine bflFirst: inputFiles.get(0)){

			//Assume every line is in the intersection, and then compare it to every file to verify/disprove this
			boolean isIntersection = true;
			for(BedFile bf: allFilesExcludingFirst){
				//If the bflFirst line is not within this file, then set isIntersection == false;
				boolean contains = false;
				for(BedFileLine bflSecond: bf){
					if(bflSecond.compareTo(bflFirst) == 0){
						contains = true;
						break;
					}//if
				}//for
				
				//Mark isIntersection as false if the original bed line is missing from this folder
				if(contains == false){
					isIntersection = false;
				}
			}//for
			
			//If a line is found to be in the intersection, write it to disk
			if(isIntersection){
				intersection.add(bflFirst);
			}//if
		}//for
		
		
		//Now all lines has been go through, and duplicates have been marked for removal, write them out to disk, indicating which files each line occurs in
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(inputDir.getAbsolutePath() + "/" + "Intersection.txt")));

			if(intersection.size() == 0){
				out.write("No lines in common!");
				out.flush();
				out.close();
			}else{
				//Create the header
				String header = "Chromosome" + "\t" + "StartLocation" + "\t" + "StopLocation" + "\t";
				String[] chunks = intersection.get(0).toString().split("\\s");
				
				//Write out place holder values for the bed file, since this information can change depending on the bed file size/version
				for(int k = 3; k < chunks.length; k++){
					header += "BedValue";
					if(k != chunks.length - 1){
						header += "\t";
					}
				}
				
				out.write("//SizeOfIntersection: " + intersection.size());
				out.newLine();
				
				
				//Write the header
				out.append(header);
				out.newLine();
	
				
				//Write out each bed line, ignoring those marked for removal since they are duplicates
				for(BedFileLine bfl: intersection){
					out.append(bfl.toString());
					out.newLine();
				}//for
			
				
				//Flush and close the writer
				out.flush();
				out.close();
			}//else
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//catch
		
			
	}//findAndPrintIntersection
	
	
}//Operations
