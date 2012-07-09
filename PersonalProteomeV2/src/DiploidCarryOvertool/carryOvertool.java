package DiploidCarryOvertool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


import DiploidCarryOvertool.chain.chainFile;
import DiploidCarryOvertool.chain.chainLine;
import Peppy.Sequence_DNA;
import PersonalProteome.U;


/**
 * carryOvertool takes in a genome and reference genome and uses a chain file to convert the coordinates of the genome to the reference genome.  It outputs the new
 * modified genome as well as a VCF file containing the changes.
 * 
 **** This is not a robust release ready software, but was designed as a in house tool to limited usage.  It is the first of several steps to create a HG19 based version
 *of a modified non-HG19 (eg. HG18) based genome.****
 * 
 * @author David "Corvette" Thomas
 *
 */
public class carryOvertool {

	
	
	
	
	
	//Store the genome and reference genome locations
	String genomeDir;
	String refGenomeDir;
	
	ArrayList<String> genomeFiles;
	ArrayList<String> refGenomeFiles;
	
	
	//Location of the chain file
	String chainLocation;
	
	//Store the file name ending for the genome files.
	String genomeFileSuffix;
	
	//Location of the directory to store output.
	String outputDir;
	File outputDirectory;
	
	//Storage variables
	chainFile cf;

	int globalMods = 0;

	StringBuffer bedOut;
	
	
	public carryOvertool(String genomeDir, String refGenomeDir, String chainFileLocation, String genomeFileSuffix, String outputDir){
		this.genomeDir = genomeDir;
		this.refGenomeDir = refGenomeDir;
		this.chainLocation = chainFileLocation;
		this.genomeFileSuffix = genomeFileSuffix;
		this.outputDir = outputDir;
		
		
		//Instantiate the genome/refGenome arraylist
		genomeFiles = new ArrayList<String>();
		refGenomeFiles = new ArrayList<String>();
		//Update the genomeFiles and the refGenomeFiles
		populateRefChrmArrayList(refGenomeDir);
		populateChrmArrayList(genomeDir, genomeFileSuffix);
		
		bedOut = new StringBuffer();
		
		outputDirectory = new File(outputDir + "/Modified" + genomeFileSuffix.substring(0, genomeFileSuffix.lastIndexOf('.')) + "/");
		
	}//Constructor
	
	/**
	 * This is the generic hands off GO method for this class.  After instantiating his object this method should be called and wait for its completion.
	 */
	public void createGenomeAndVCF(){
		
		//uploadChain file
		uploadChainFile(this.chainLocation);
		

		//Create a VCF File header here
		
		//Work each file from the harddisk one at a time
		for(int i = 0; i < genomeFiles.size(); i++){
			U.p("Working on Chromosome " + (i + 1));
			U.startStopwatch();
			matchFileVCFCompile( refGenomeFiles.get(i), genomeFiles.get(i), i);
			U.stopStopwatch();
		}
		
		
		//Output the bed file
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputDirectory.getAbsoluteFile() + "/" + "VCF" + genomeFileSuffix.substring(0, genomeFileSuffix.lastIndexOf('.')) + ".txt")));
			
			out.write("#Number of lines: " + globalMods + "\n");
			out.write(bedOut.toString());
			
			out.flush();
			out.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
		U.p("Modifications of entire genomes " + globalMods);
		
	}//Create a genome first
	
	
	
	/**
	 * uploadChainFile uploads a chain file and stores in in the chainFile class as a abstract representation of the file.
	 * @param fileLoc  A string representing the file location of this chain file.
	 */
	private void uploadChainFile(String fileLoc){
		
		//Header and line variables
		String header;
		String line;
		int runningTotal = 0;
		//Upload the chain file
		try {
			//Scanner to parse the file
			Scanner s = new Scanner(new File(fileLoc));

			//Instantiate the global cf variable
			cf = new chainFile();
			
			//Loop through the chain file extracting each section and storing it appropriately in the cf object
			int index = 0;
			while(s.hasNextLine()){
				
				line = s.nextLine();
				header = line;
				
				//End of the file reached
				if(header.length() == 0){
					break;
				}
				
				//Insert the header line of this chain file section into storage
				cf.addHeader(index, header);
				
				//Repeat this loop until a line without 3 chunks(The last line) is found
				while(true){
					//Iterate through the lines
					line = s.nextLine();
					//Parse the line along spaces
					String [] chunks = line.split(" ");
					
				
					//Normal lines are stored int he chainLine and inserted into the chain file
					if(chunks.length == 3){
						chainLine temp = new chainLine(Integer.valueOf(chunks[0].trim()), Integer.valueOf(chunks[1].trim()), Integer.valueOf(chunks[2].trim()));
						runningTotal += Integer.valueOf(chunks[0].trim());
						cf.addLine(index, temp);
					}else{
						
						//The last line signals to break from this loop
						chainLine temp = new chainLine(Integer.valueOf(chunks[0].trim()));
						cf.addLine(index, temp);
						break;
					}

				}//true loop
				
				
				s.nextLine();
				index++;
				
			}//hasNextLine
		
			
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}//uploadChainFile
	
	
	private void matchFileVCFCompile(String refDNA, String DNA, int chainFileIndex){
		//Get the reference DNA into the refBuffer
		Sequence_DNA seqDNA = new Sequence_DNA(new File(refDNA));
		StringBuffer refBuffer = new StringBuffer();
		refBuffer.append(seqDNA.getNucleotideSequences().get(0).getSequence());
		
		//Get the DNA into the dnaBuffer
		seqDNA = new Sequence_DNA(new File(DNA));
		StringBuffer dnaBuffer = new StringBuffer();
		dnaBuffer.append(seqDNA.getNucleotideSequences().get(0).getSequence());
		
		//Save memory
		seqDNA = null;
		
		//DNA to store the modifications in
		StringBuffer modifiedDNA = new StringBuffer();
		
		//Work the chain file to create the modified DNA.
		ArrayList<chainLine> lines = cf.getLinesSection(chainFileIndex);
		
		int currentIndex = 0;
		
		
		int count = 0;
//		int nextModDt = 0;
		int modDqTotal = 0;
		for(int i = 0; i < lines.size(); i++){
			chainLine currentLine = lines.get(i);
			//Normal Line
			if(currentLine.getDq() != -1 && currentLine.getDt() != -1){
				//Debug
				count += currentLine.getDt();
				count -= currentLine.getDq();
				
			
				//Step 1, add GM data from the aligned block to the modified DNA.
				modifiedDNA.append(dnaBuffer.substring(currentIndex , currentIndex + currentLine.getSize()));
			
				
				//Step 2,  Add dna to GM12878 from HG18 if there is a positive dt value.
				if(currentLine.getDt() > 0){
					modifiedDNA.append(refBuffer.substring(currentIndex + currentLine.getSize() - modDqTotal  , currentIndex + currentLine.getSize() + currentLine.getDt() - modDqTotal ));
				}
				
				//Step 3,  Skip the index forward to move over base pairs in GM12878 if dq value is positive
				if(currentLine.getDq() > 0){
					currentIndex += currentLine.getDq();
					modDqTotal += currentLine.getDq();
				}

				
				//Move the index through the chromosome
				currentIndex += currentLine.getSize();

			//Last line wihtout any mods
			}else{
				modifiedDNA.append(dnaBuffer.substring(currentIndex));
			}
			
			
		}
		
		
		//Write VCF Lines here
		
//		U.p("refDNA length " + refBuffer.length());
//		U.p("DNA    length " + dnaBuffer.length());
//		U.p("modDNA length " + modifiedDNA.length());
//		U.p("Count " + count);
		//Write that new chromosome to disk'
		
		String chrmName = "chr" + (chainFileIndex + 1);
		if(chainFileIndex == 22){
			chrmName = "chrX";
		}
		


		//Create the directory to store the genome in
		
		outputDirectory.mkdir();
		
		//Create a writer to write out the genomic data to disk
		String outputFile = outputDirectory.getAbsolutePath() + "/" + chrmName + genomeFileSuffix;
		
		try{
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputFile)));
		
		//Write the header to the file
		out.write(">" + chrmName + "\n");
		
		
		//Write the rest of the data to disk based on a specified line length
		int lineLength = 40;
		int numLines = modifiedDNA.length() / lineLength;
		
		//Write the lines of bases to the disk
		for(int i = 0; i <= numLines; i++){
			if(i + 1 > numLines){
				out.write(modifiedDNA.substring(i * lineLength) + "\n");
				break;
			}
			
			out.write(modifiedDNA.substring(i * lineLength, (i + 1) * lineLength) + "\n");
			
		}

		out.flush();
		out.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	int modCount = 0;
	//Convert the reference DNA to upper case for comparison purposes
	refBuffer = new StringBuffer(refBuffer.toString().toUpperCase());
	
	//Sequence the modified DNA for comparison purposes
	seqDNA = new Sequence_DNA(new File(outputFile));
	modifiedDNA = new StringBuffer(seqDNA.getNucleotideSequences().get(0).getSequence());
	
	//Compare the modified and reference DNA for differences
	for(int k = 0; k < refBuffer.length(); k++){
		if(refBuffer.charAt(k) != modifiedDNA.charAt(k)){

//			bedOut.append(chrmName + "\t" + k + "\t" + "." + "\t" + refBuffer.charAt(k) + "\t" + modifiedDNA.charAt(k));
			bedOut.append(chrmName + "\t" + k + "\t" + (k + 1) + "\t" + refBuffer.charAt(k) + "/" + modifiedDNA.charAt(k));
			bedOut.append("\n");
			modCount++;
		}//if
	
	}//refBuffer for loop

		U.p("Number of Modifications " + modCount);
		globalMods += modCount;
		

	}//matchFileVCFCompile

	
	
	
	/**
	 * Populates the ref chromosome array with the file locations of each chromosome.
	 * @param chrmDir The directory containing the chromosome files.
	 */
	private void populateRefChrmArrayList(String chrmDir){
		for(int i = 0; i < 22; i++){
			refGenomeFiles.add(chrmDir + "chr" + (i + 1) + ".fa");
		}
//		refGenomeFiles.add(chrmDir + "chrM.fa");
		refGenomeFiles.add(chrmDir + "chrX.fa");
//		refGenomeFiles.add(chrmDir + "chrY.fa");
	}
	/**
	 * Populates the chromosome array with the file locations of each chromosome.'
	 * @param chrmDir The directory containing the chromosome files.
	 */
	private void populateChrmArrayList(String chrmDir, String genomeFileSuffix){
		for(int i = 0; i < 22; i++){
			genomeFiles.add(chrmDir + "chr" + (i + 1) + genomeFileSuffix);
		}
//		refGenomeFiles.add(chrmDir + "chrM.fa");
		genomeFiles.add(chrmDir + "chrX" + genomeFileSuffix);
//		refGenomeFiles.add(chrmDir + "chrY.fa");
	}
}
