package AnnotationAlignment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import Peppy.Sequence_DNA;
import PersonalProteome.U;

/**
 * Fake DNA Creation creates a fake DNA sequence based on the size/modification parameters set by the user.  It allows for a Mutation rate to be set,'
 *  along with coordinate preservation and amplification.
 * 
 * @author David "Corvette" Thomas
 * 
 */

public class FakeDNACreation {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		U.p("Generating Fake DNA");
		
		args = new String[6];
		//Output Location
		args[0] = "/Users/davidthomas/Peppy/PeppyData/SimpleRandomDNA/";
		//Created DNA Size/Input DNA
		args[1] = "10000000";
//		args[1] = "/Users/davidthomas/Peppy/ProteomeV2/fakeDNA/input/chr1.fa";
//		args[1] = "/Users/davidthomas/Peppy/ProteomeV2/fakeDNA/input/chrM.fa";
//		args[1] = "/Users/davidthomas/Peppy/ProteomeV2/fakeDNA/input/chr21.fa";
		//Percent Mutate
		args[2] = "5";
		//Amplify
		args[3] = "true";
		//Preserve Coordinates
		args[4] = "true";
		//Amplification
		args[5] = "true";
	
		
		
		//Modify Existing DNA
//		U.p("Generating a DNA from" + args[1] + " with the mutation rate of " + args[2] + "% and the use of amplification is: " + args[3] + ".  The preservation of coordinates is: " + args[4]);
//		FakeDNACreation fdc = new FakeDNACreation(args[0], args[1], Double.valueOf(args[2]), args[3], args[4]);
		//Random DNA
		U.p("Generating a DNA sequence of length " + args[1] + " with the mutation rate of " + args[2] + "% and the use of amplification is: " + args[3] + ".  The preservation of coordinates is: " + args[4]);
		FakeDNACreation fdc = new FakeDNACreation(args[0], Integer.valueOf(args[1]), Double.valueOf(args[2]), args[3], args[4]);
		fdc.createOutput();
		
		U.p("Done");
		
		U.p("*****STILL NEED TO COMPLETE: Amplification for DNA*******");
	}
	
	//User Defined Parameters
	private String outputFolder = "";
	
	private int length = 1000;
	private double percentMutate = .1;
	private boolean amplify = false;
	private boolean perserveCoordinates = true;
	
	private String DNAInputFile = "";
	
	
	StringBuffer DNA = new StringBuffer();
	Random r = new Random();
	
	
	//Runtime Parameters
	private double ratioOfreplaceToInsertion = .75;
	
	
	/**
	 * Fake DNA from a reference input DNA
	 * @param outputFolder
	 * @param DNAInput
	 * @param percentMutate
	 * @param amplify
	 * @param perserveCoordinates
	 */
	public FakeDNACreation(String outputFolder, String DNAInput, double percentMutate, String amplify, String perserveCoordinates){
		this.outputFolder = outputFolder;
		this.DNAInputFile = DNAInput;
		this.percentMutate = percentMutate;
		this.amplify = Boolean.valueOf(amplify);
		this.perserveCoordinates = Boolean.valueOf(perserveCoordinates);
		uploadDNA();
		this.length = DNA.length();
		createOutput();
	}
	
	/**
	 * Fake DNA from scratch
	 * @param outputFolder
	 * @param length
	 * @param percentMutate
	 * @param amplify
	 * @param perserveCoordinates
	 */
	public FakeDNACreation(String outputFolder, int length, double percentMutate, String amplify, String perserveCoordinates){
		this.outputFolder = outputFolder;
		this.length = length;
		this.percentMutate = percentMutate;
		this.amplify = Boolean.valueOf(amplify);
		this.perserveCoordinates = Boolean.valueOf(perserveCoordinates);
		
		randomDNA();
		createOutput();
	}

	
	/**
	 * uploadDNA uploads DNA  from the DNA Input file, and stores it in a String Buffer.
	 */
	private void uploadDNA(){
		//Upload the DNA from the input File
		Sequence_DNA seq = new Sequence_DNA(new File(DNAInputFile));
		
		DNA.append(seq.getNucleotideSequences().get(0).getSequence());
		U.p("DNA Uploaded!");
	}//uploadDNA
	
	/**
	 * randomDNA creates a completely Random DNA sequence
	 */
	private void randomDNA(){
		/* Seed the random number generator with the current time*/
		r.setSeed(Calendar.getInstance().getTimeInMillis());
		
		/*Manually seed the random number generator*/
//		r.setSeed(1);
		
		
		//Create a random sequence of DNA nucleotides
		for(int i = 0; i < length; i++){
			DNA.append(this.getRandDNA(r));
		}
		
		//Ensure that each random DNA is in upper case for the reference sequence
		DNA = new StringBuffer(DNA.toString().toUpperCase());
	}//randomDNA
	
	
	/**
	 * createOutput mutates the reference DNA and creates output copies of both the reference DNA and the mutated DNA.
	 */
	public void createOutput(){
		
		
		/*Debug*/
//		U.p("Reference Strand: " + DNA.toString());

		StringBuffer mutatedDNA = new StringBuffer();

		//Create a mutable character array for very fast random insertions/deletions
		char[] tempArray = new char[DNA.length()];
		for(int i = 0; i < DNA.length(); i++){
			tempArray[i] = DNA.charAt(i);
		}
		
		/*Debug*/
		U.p("Mutated DNA is uplaoded, starting to mutate");
		U.p("Total number of mutations to complete " + (int)(length * (percentMutate/100)));
		
		
		//If not preserving coordinates
		if(!perserveCoordinates){
			
			//If choosing to amplify the data
			if(amplify){
				
				
				//Deletion
				
				//Grab a random section no larger then 10% of the genome
				
				//Then cut open the sequence at that section.
				
				// and insert a copy
				

				//if delete or amplify
				if( r.nextInt() % 2 == 0){

				}else{
					//Amplification
					
					//Same as delete but with a deletion instead of a copy

				}
				
				
				
			}//Amplify
		}//preserve Coordinates
		
		/*Mutate the DNA*/
		int mutationCount = (int)(tempArray.length * (percentMutate/100));
		for(int i = 0; i < mutationCount; i++){
			
			/*Debug*/
//			if(i % 10 == 0){
//				U.p(i);
//			}
			
			//Get a random location to modifiy
			int replaceLocation = r.nextInt(length);
			
			//If the random locaiton occurs outside of the range of the array, then generate a new random number
			while(replaceLocation + 1 > tempArray.length){
				replaceLocation = r.nextInt(length);
				U.p("Oops, guess out of range.  REDO!");
			}
			
			
			//If coordinates are not being preserved then insert/delete randomly.
			if(!perserveCoordinates){
				//Single Nucleotide Insertion and deletion
				//Roll the dice to to determine whether or not to insert or delete.
				int chance = r.nextInt(1000);
				
				//If by random chance this location exceeds the value for insertion/deletion then perform one of the two randomly
				if(chance > 1000 * ratioOfreplaceToInsertion){
					
					//Even chance values produce insertions
					if(chance % 2 == 0){
						//insert
						mutatedDNA.insert(replaceLocation, String.valueOf(getRandDNA(r)).toLowerCase());
						
					//Odd Chance Values produce deletions
					}else{
						//delete
						mutatedDNA.deleteCharAt(replaceLocation);
					}
					 
					//Continue since a mutation has been preformed 
					continue;
				}//Chance to insert/delete
				
			}//Determines if the original coordinates/length is to be preserved

			//If coordiante location is being preserved, then simply modify an existing position.
			tempArray[replaceLocation] = getRandDNA(r);
			
		}//Mutations Loop
		
		
		//Save the freshly modified DNA into the mutatedDNA string buffer
		mutatedDNA.append(new String(tempArray));
		
		
		
		/*Debug*/
//		U.p(DNA.toString());
//		U.p(mutatedDNA.toString());
		
		//Write the DNA to file
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFolder + "referenceStrand.fa"));
			
			out.write(">RandomDNA|Length: " + DNA.length() + "|" + "\n");
			int lineLength = 80;
			int linesOfProtein = DNA.length() / lineLength;
			
			for(int k = 0; k < linesOfProtein + 1; k++){
				//If it is the last line, just write all of them
				if(k == linesOfProtein){
						out.write(DNA.substring(k*lineLength));
						out.write("\n");
					
				}else{
				//Write 80 characters
				out.write(DNA.substring(k*lineLength, (k+1)*lineLength));
				out.write("\n");
				}
				
			}//for
			
			
			//Flush and close the bufferedWriter to guarantee the data gets written to disk.
			out.flush();
			out.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		
		
		//Write the mutated DNA to output
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFolder + "mutatantStrand.fa"));
			
			out.write(">mutantRandomDNA|Length: " + mutatedDNA.length() + "|" + "\n");
			int lineLength = 80;
			int linesOfProtein = mutatedDNA.length() / lineLength;
			
			for(int k = 0; k < linesOfProtein + 1; k++){
				//If it is the last line, just write all of them
				if(k == linesOfProtein){
						out.write(mutatedDNA.substring(k*lineLength));
						out.write("\n");
					
				}else{
				//Write 80 characters
				out.write(mutatedDNA.substring(k*lineLength, (k+1)*lineLength));
				out.write("\n");
				}
				
			}//for
			
			
			//Flush and close the bufferedWriter to guarantee the data gets written to disk.
			out.flush();
			out.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		
	}//createOutput
	
	/**
	 * Returns a random DNA character.
	 * @param r r is a random number generator that is already seeded when it is passed in.
	 */
	private char getRandDNA(Random r){
//		char[] possibleChoices = {'A', 'T', 'G', 'C'};
		char[] possibleChoices = {'a', 't', 'g', 'c'};
		
		return possibleChoices[r.nextInt(4)];
	}//getRandDNA
	
	
}//FakeDNACreation
