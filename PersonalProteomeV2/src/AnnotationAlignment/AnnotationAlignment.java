package AnnotationAlignment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Callable;

import AnnotationAlignment.Subclasses.GTF_Line;
import AnnotationAlignment.Subclasses.Match;
import AnnotationAlignment.Subclasses.MatchCollection;
import AnnotationAlignment.Subclasses.AA_Line_Container;
import Peppy.Sequence_DNA;
import PersonalProteome.Definitions;
import PersonalProteome.U;
/**
 * **This class is currently designed to work with a single chromosomes annotation and a single DNA file, but will eventually be a class that is controlled by a manager object, and several of
 *   these will run simultaneously through a multi-threaded manager object***
 * 
 * AnnotationAlignment is a optimized DNA/Genome Annotation realignment tool.  It allows for a reference genome/DNA sequence and annotation to be taken in,
 * and then a have the annotation modified to fit a specified genome/DNA sequence.
 * 
 * ***Minimum memory usage is around ~4GB in its current implementation for the largest chromosome (CHRM1), but this could probably be reduced with some clever data structures manipulation and a smaller chunk size(< 10,000,000)***
 * 
 * 	Parameters:  safetyMargin  This is a value that determines how many additional characters to use when comparing strings.  This is a safeguard to protect against random
 *				  				occurrences of the same/similar sequences  The more characters used ensures higher accuracy but longer computation.  Stronger CPU's can handle larger values and maintain a reasonable runtime.
 * 							Recommended (5 - 20)
 * 
 *						  	Safety margin of 0, likely that this string will occur once in a random sequence of that length
 *							Safety margin of 1, likely that this string will occur once in a random sequence 4 times this length
 *							Safety margin of 5, likely that this string will occur once in a random sequence ~1,000 times this length
 *							Safety margin of 10, likely that this string will occur once in a random sequence ~1,000,000 times this length
 *							15:  ~1,000,000,000 (1 billion)
 *							20:  ~1,000,000,000,000 (1 trillion)
 *
 *
 *				range  This is a number of nucleotides away from a location to look for a match if the broad hash based search does not return any potential matches.  Larger is slower but more accurate 
 *						Recommended (500-25000)
 *				chunkSize  This is the number of bases to use at any one time when searching for matches.  This is chosen because this puts a upper limit on the distance 
 *						   away from an original location a peptide can occur, as well as save very significant time on searches(DAYS TO WEEKS faster).
 * 						Recommended (5,000,000(~3GB) - 15,000,000(~6GB)  More memory allows for for a larger window
 * 
 * Now able to be used in multi-threading.  Use the special constructor and the call method.
 * 
 * 
 * @author David "Corvette" Thomas
 *
 */
public class AnnotationAlignment implements Callable<ArrayList<AA_Line_Container>> {


	//Universal User Input
	private String inputAnnotation;
	private String outputDirectory;
	private boolean proteinOnly = true;
	
	//DNA Sequence User Input
	private String referenceDNAFile;
	private String DNAFile;
	
	//Runtime Parameters
	//Safety margin increases the size of the string past the size that there is statistically on average 1 of a given string in a sequence of DNA.
	final static int safetyMargin = 0;
	
	
	final static int range = 1;
	
	final int chunkSize = 15000000;
	
	//Date formating
	private SimpleDateFormat sdf = new SimpleDateFormat(Definitions.DATE_FORMAT);
	
	//Storage Variables
	//This Arraylist contains all of the information from the annotation.
	private ArrayList<AA_Line_Container> GTFlist = new ArrayList<AA_Line_Container>();
	private String annoHeader = "";
	
	//Final position represents the last place in the DNA to search for based on the determined string size.  This prevents index out of bounds errors.
	int finalPosition = 0;
	
	/**
	 * For use with a single DNA/Chromosome file.  Made for calling from a main method for use on a single file.
	 * 
	 * @param inputAnnotation
	 * @param outputDirectory
	 * @param referenceDNAFile
	 * @param DNAFile
	 * @param anything
	 */
	public AnnotationAlignment(String inputAnnotation, String outputDirectory, String referenceDNAFile, String DNAFile, boolean proteinOnly){
		this.inputAnnotation = inputAnnotation;
		this.outputDirectory = outputDirectory;
		this.referenceDNAFile = referenceDNAFile;
		this.DNAFile = DNAFile;
		this.proteinOnly = proteinOnly;
		
		
	}//AnnotationAlignment  DNA/Chromosome
	
	
	/**
	 * Constructor for Multithreading.  It only needs two specific DNA files to lookup and calculate needed lines
	 */
	public AnnotationAlignment(ArrayList<AA_Line_Container> GTFlist, String referenceDNAFile, String DNAFile){
		
		this.GTFlist = GTFlist;
		this.referenceDNAFile = referenceDNAFile;
		this.DNAFile = DNAFile;
		
	}//multithreading Alignment
	
	
	
	@Override
	/**
	 * Overides the call method for Callable.  This is meant as a mutlithreaded application 
	 */
	public ArrayList<AA_Line_Container> call() throws Exception {

//		uploadAnnotation(inputAnnotation, !proteinOnly);

		//Get sequence for each line Based on the size of the chromosome
		getSequenceForAnnoDNA();



		//Find that sequence in new genome
		findNewLocations();

		// TODO Auto-generated method stub
		return GTFlist;
	}//call
	
	/**
	 * Align DNA does everything required to align a single fasta file with a reference FASTA and annotation.
	 * This would be used if this class was desired to be used on its own.
	 */
	public void alignDNA(){
		//Upload annotation
		Driver.displayMem();
		U.p("Uploading Annotation");
		U.startStopwatch();
		uploadAnnotation(inputAnnotation, !proteinOnly);
		U.stopStopwatch();

		
		U.p("GTFList size " + GTFlist.size());
		
		Driver.displayMem();
		U.p("Get sequences for each location");
		U.startStopwatch();
		//Get sequence for each line Based on the size of the chromosome
		getSequenceForAnnoDNA();
		U.stopStopwatch();
		
		Driver.displayMem();
		U.p("Finding new genome locations");
		U.startStopwatch();
		//Find that sequence in new genome
		findNewLocations();
		U.stopStopwatch();

		
		U.p("Creating DNA output");
		U.startStopwatch();
		//Output the Updated Annotation
		createDNAOutput();
		U.stopStopwatch();
		
	}//alignDNA


	/**
	 * uploadAnnotation populates the GTFlist with AA_Line_Containers with line information from the annotation file.  The main information grabbed is the start/stop locations.
	 * @param annotationFile
	 * @param uploadAll
	 */
	private void uploadAnnotation(String annotationFile, boolean uploadAll){

		
		//GTFint List to work on
		//2588001 is the size of the complete v11 annotation  This is a small optimization, and should not be an issue of concern for future optimizations
		GTFlist = new ArrayList<AA_Line_Container>(2588001 * 2);
		//Represents a single line
		GTF_Line line;

		//Variables for the mandatory fields
		String begin = "";
		int startLocation = -1;
		int stopLocation = -1;
		String end = "";
		int id = 0;
		
		//Parse each line of the file and save it
		try{
			Scanner s = new Scanner(new File(annotationFile));
			String token;
			//Parse each Line, one at a time
			while(s.hasNextLine()){
				//Get the first token of the line
				token = s.next();
				
				//Move past commments
				if(token.startsWith("##")){
					annoHeader += token + s.nextLine() + "\n";
					continue;
				}
				
				//This represents the three columns before the start and stop location of this line
				begin += token + "\t" + s.next() + "\t" + s.next() + "\t";
				
				
				//Parse the start and stop locations out of the file
				startLocation = Integer.valueOf(s.next());		
				stopLocation = Integer.valueOf(s.next());
				
				
				//Store the rest of the line as is for printing it out later
				end = s.nextLine();
	
				
				line = new GTF_Line(id, begin, startLocation, stopLocation, end);

				//Determine if all lines are to be modified, or just lines used in protein creation are used(These are the only lines Personal Proteome uses).  
				if(uploadAll){
					
					GTFlist.add(new AA_Line_Container(line));
					id++;
					
				}else{
					
					/*Since we only care about encoding proteins, only add ones that are protein_coding*/
					/*Add the line object to the list of parsed lines*/
					if(end.contains("protein_coding") || end.contains("nonsense_mediated_decay")){
						/*Finished with variables, now create a GENCODE_GTF_Line object and insert it into the ArrayList*/
						GTFlist.add(new AA_Line_Container(line));
						id++;
						
					}//if
					
				}//else
				//reset all of the variables
				begin = "";
				startLocation = -1;
				stopLocation = -1;
				end = "";

			}//while
			
			GTFlist.trimToSize();
			
		}catch(FileNotFoundException e){
			U.p("Error populating GTF List: " + e);

		}//catch
		
	}//uploadAnnotation
	
	
	/**
	 * getSequenceForAnno populates the sequenceForGTF list with the appropriately sized sequence surrounding each GTF entry
	 */
	private void getSequenceForAnnoDNA(){
		Sequence_DNA seq = new Sequence_DNA(DNAFile);
		StringBuffer sequence = new StringBuffer();
		sequence.append(seq.getNucleotideSequences().get(0).getSequence());
		int stringSize = calculateStringSize(sequence.length());

		
		U.p("Safety Margin " + safetyMargin);
		U.p("String size " + stringSize);
		for(int i = 0; i < GTFlist.size(); i++){
			int start = GTFlist.get(i).getGtfLine().getStartLocation();
			int stop = GTFlist.get(i).getGtfLine().getStopLocation();
			int sizeMod = stringSize /2;

			//stringSize % 2 adds 1 to the length for all odd string sizes
			if(stop + (stringSize % 2) + sizeMod < sequence.length()){
				GTFlist.get(i).setRefStartStrand(sequence.substring(start - sizeMod, start +  (stringSize % 2) + sizeMod));
				GTFlist.get(i).setRefStopStrand(sequence.substring(stop - sizeMod, stop  + (stringSize % 2) +  sizeMod));
			}else{
				GTFlist.get(i).setRefStartStrand("2CLOSE2EDGE");//sequence.substring(start - sizeMod, start +  (stringSize % 2) + sizeMod));
				GTFlist.get(i).setRefStopStrand("2CLOSE2EDGE");//sequence.substring(stop - sizeMod, stop  + (stringSize % 2) +  sizeMod));
			}
		}//GTFlist

	
		//This just eliminates any extra empty objects in the GTFlist arraylist once all insertions are done.
		GTFlist.trimToSize();
	}//getSequenceForAnnoDNA
	

	
	/**
	 * This method determines what the positions in the genome are based on sequences from the reference genome.
	 * It completes each section based on a batch size that is predetermined on input.
	 */
	private void findNewLocations(){
		StringBuffer sequence = new StringBuffer();
		//Upload the mutatedDNA
		Sequence_DNA seq = new Sequence_DNA(referenceDNAFile);
		
		sequence.append(seq.getNucleotideSequences().get(0).getSequence().toUpperCase());
		int stringSize = calculateStringSize(sequence.length());
		
		HashMap<Integer, ArrayList<Integer>> answerKey = new HashMap<Integer, ArrayList<Integer>>();
		
		//final position represents the last position in the DNA to search for, since any position past this would cause a string out of bounds error.
		finalPosition = sequence.length() - stringSize;
		U.p("total sequenceLength: " + sequence.length());

		int currentStart = 0;
		int currentStop = 0;
		
		//Hash up the DNA
		HashMap<String, ArrayList<Integer>> locations = null;
		Driver.displayMem();
		
		//Search the new DNA to find 
		for(int i = 0; i < GTFlist.size(); i++){
			AA_Line_Container ut = GTFlist.get(i);
			
			//This code searches through a 
			if(ut.getGtfLine().getStopLocation() > currentStop - (int)(.1 * chunkSize)){
				
				//Ignore a rehash if it is at the end of the line
				if(currentStop != sequence.length()){
					//rehash and continue
					
					currentStart = ut.getGtfLine().getStartLocation() - (int)(.1 * chunkSize);
					currentStop = currentStart + chunkSize;
					
					if(currentStart < 0){
						currentStart = 0;
					}
					if(currentStop > sequence.length()){
						currentStop = sequence.length();
					}
					
					U.p("Total number of answers calculated by previous hash: " + answerKey.size());
					U.p("rehashing: " + currentStart + " " + currentStop);
					//Dump this object and let garbage collection claim it!  Is this necessary?  I don' think it is.  Confirm
					locations = null;
					//Clear the old hash
					locations = hashDNA(sequence, stringSize, currentStart, currentStop);
					
					
					//Clear the old answers
					answerKey = new HashMap<Integer, ArrayList<Integer>>();
					Driver.displayMem();
//					U.p("finished rehashing");
				}
				
			}//if
			
			
			MatchCollection mc = null;
			//Get the matches for a given start using HashMap 
			ArrayList<Integer> k = answerKey.get(ut.getGtfLine().getStartLocation());
			if(k == null){
				mc = getLocationsHash(sequence, ut.getRefStartStrand(), locations, ut.getGtfLine().getStartLocation());
				ArrayList<Integer> answer = new ArrayList<Integer>(5);
				int topScore = mc.getMatchesArray()[0].getScore();
				
				//Populate the locations list all of the top scoring lines
				for(int y = 0; y < mc.getMatchesArray().length; y++){
					if(mc.getMatchesArray()[y].getScore() == topScore){
						answer.add(mc.getMatchesArray()[y].getStart() + (stringSize)/2);
					}//if
					
				}//for

					//Store all of these in the answer key for use again, incase the same location occurs on a different line
					ut.setNewStarts(answer);
					answerKey.put(ut.getGtfLine().getStartLocation(), answer);

			}else{
				ut.setNewStarts(k);
			}//else

			//Get the matches for a given stop using HashMap
			k = answerKey.get(ut.getGtfLine().getStopLocation());
			if(k == null){
				mc = getLocationsHash(sequence, ut.getRefStopStrand(), locations, ut.getGtfLine().getStopLocation());
				ArrayList<Integer> answer = new ArrayList<Integer>(5);
				int topScore = mc.getMatchesArray()[0].getScore();
				
				//Populate the locations list all of the top scoring lines
				for(int y = 0; y < mc.getMatchesArray().length; y++){
					if(mc.getMatchesArray()[y].getScore() == topScore){
						answer.add(mc.getMatchesArray()[y].getStart() + (stringSize)/2);
					}//if
					
				}//for

					//Store all of these in the answer key for use again, incase the same location occurs on a different line
					ut.setNewStops(answer);
					answerKey.put(ut.getGtfLine().getStopLocation(), answer);
			}else{
				ut.setNewStops(k);
			}//else

		}//GTFList iterate for loop	
		
	}//findNew Locations
	
	/**
	 * 
	 * This is the most basic scoring algorithm for a match, and it uses a simple edit distance for all possible strings in a window around the original location.
	 * It is fast/accurate but requires for a close approximation of the location of a sequence to be known otherwise it will suffer drastically in accuracy/speed.  
	 * 
	 * This approximation of location is based on two input variables: Original Location and range.  Original location is a best guess at the location of this sequence
	 * and range is a number of nucleotides to search in either direction for a given sequence.
	 *
	 * @param StringSize
	 * @return
	 */

	private MatchCollection getLocationsWindowMethod(StringBuffer DNAsequence, String tokenToMatch,int originalLocation, int range){
		MatchCollection out = new MatchCollection(safetyMargin,tokenToMatch.length());

		//Define the window to search in
		int begin = originalLocation - range;;
		int end = originalLocation + range;
		
		
		//Clamp the beginning and end to ensure that strings past the end of the chromosome are not searched
		if(begin < 0){
			begin = 0;
			
		}

		if(end > finalPosition){
			end = finalPosition;
		}
		
		//Iterate through the DNA, and feed each string to the MatchCollection
		for(int i = begin; i < end ; i++){
			
			int start = i;
			int stop = i + tokenToMatch.length();
			String temp = DNAsequence.substring(start, stop);
			int score = fuzzyStringAlgorithms.levenshteinDistance(tokenToMatch, temp);
			
			//Alternative options to use instead of edit distance. Variant is faster then levenshtein distance but is much less accurate if indices are not preserved between the two genomes.
			// eg. levenshtein distance is slower but far better in the real world situations.
//			int score = fuzzyStringAlgorithms.variant(tokenToMatch, temp);
			
			//Feed matches into the Match collections.  if they are 
			Match m = new Match(temp, start,stop, score);
			if(!out.contains(m)){
				out.feed(m);
			}	
		}
		
		return out;
		
	}//getLocationsWindowMethod
	

	/**
	 * getLocationsHash uses a hash to store possible locations where this peptide may occur.  
	 * If no suitable potential locations are found, then it just guesses based on a window of nucleotides defined by range.
	 * 
	 * This method uses a large "pre-filter" in the form of a hash table containing locations of various sequences within a large window of the genome.  This window size is decided based on 
	 * accuracy/memory concerns.  Default is generally at least 2.5 million nucleotides in either direction, with up to 5 million nucleotides in either direction depending where a sequence falls 
	 * within a hash window.  For more information look up the hashDNA method.
	 * 
	 * 
	 * This is the code to optimize.  It does > 99% of the work.
	 * @return A MatchCollection of the top matches for this DNA sequence and tokenToMatch
	 * 
	 */
	private MatchCollection getLocationsHash(StringBuffer DNAsequence, String tokenToMatch, HashMap<String, ArrayList<Integer>> DNAhash, int originalLocation){
		MatchCollection out = new MatchCollection(safetyMargin,tokenToMatch.length());
		ArrayList<Integer> possibleLocations = new ArrayList<Integer>(); //= DNAhash.get(tokenToMatch.substring(0, hashSize));
		
		//Limit is the number of SNPS to allow for in a given sequence
	    int limit = 2;
		//Precompute all of the mutations and search for only those.
		ArrayList<String> mutations = getMutations(tokenToMatch, limit);
		
		//Search for any DNA that matches this sequence or a mutation of this sequence
		for(String s: mutations){
			ArrayList<Integer> temp = DNAhash.get(s);
			
			//If a match is found, store that information in possible locaitons
			if(temp != null){
				possibleLocations.addAll(temp);
			}
			
		}
		
		//peakedInterestSize is the value that denotes range around a predicted location
		//It is used to assign a better score to sequences that occur very close to predicted sequences locaiton
		int peakedInterestSize = (int)(DNAsequence.length() * .001);
		//Guarantee that the size is at least 1
		if (peakedInterestSize < 1){
			peakedInterestSize = 1;
		}
		
		if(possibleLocations.size() > 0){
			//Iterate through the DNA, and feed each string to the MatchCollection
			for(int i = 0; i < possibleLocations.size(); i++){
				
				int start = possibleLocations.get(i);
				int stop = start + tokenToMatch.length();
				String temp = DNAsequence.substring(start, stop);
				int score = fuzzyStringAlgorithms.levenshteinDistance(tokenToMatch, temp);
				//Alternative options to use instead of edit distance. Variant is faster then levenshtein distance but is much less accurate if indices are not preserved between the two genomes.
				// eg. levenshtein distance is slower but far better in the real world situations.
//				int score = fuzzyStringAlgorithms.variant(tokenToMatch, temp);
				
				//Give a boost to peptides that occur very close to where the orignal genomes location was
				if(Math.abs(originalLocation - start) < peakedInterestSize){
					//How to boost the score?  Cut in half?  This can be any number of score scaling
					score *= .33;
					
				}//if
				
				//Add matches to the match container, and let it sort out which ones are best
				Match m = new Match(temp, start,stop, score);
				if(!out.contains(m)){
					out.feed(m);
				}//if
			}//for
			
		//If there are no possible choices for comparison in the hash, use the window method to guess
		}else{
			return getLocationsWindowMethod(DNAsequence,tokenToMatch, originalLocation, range);
		}//else
		
		
		return out;
		
	}//getLocationsHash
	
	/**
	 * tokenToMatch is a string to mutate with a number of SNP's defined by numMutations.  
	 * @param tokenToMatch
	 * @param numMutations
	 * @return returns an ArrayList of Strings that contains every possible combinations of SNP's.
	 */
	private ArrayList<String> getMutations(String tokenToMatch, int numMutations){
		
		//combos represents all of the possible combinations of strings to attach 
		ArrayList<String> combos = new ArrayList<String>();

		combos.add(tokenToMatch);
		
		for(int i = 0; i < numMutations; i++){
			combos = replaceEveryLetter(combos);
		}//numMutations
		
		combos.add(tokenToMatch);
		
		return combos;
		
	}//getMutations
	
	
	/**
	 * This method takes in a ArrayList<String> that are to be manipulated by every SNP at every possible location.  It returns a list that is 4*Size of Input.
	 * 
	 * Ez. Input
	 * INPUT GT
	 * 
	 * returns:
	 * GC
	 * GT
	 * GG
	 * GA
	 * AT
	 * TT
	 * GT
	 * CT
	 * 
	 * @param toReplaceList
	 * @return an ArrayList of strings that contains all the possible mutations
	 */
	private ArrayList<String> replaceEveryLetter(ArrayList<String> toReplaceList){
		ArrayList<String> combos = new ArrayList<String>();
		
		char[] possibleChoices = {'A', 'T', 'G', 'C'};	
		StringBuffer temp = new StringBuffer();
		
		for(String toReplace: toReplaceList){
			for(int i = 0; i < toReplace.length(); i++){
				for(int j = 0; j < 4; j++){
					temp = new StringBuffer();
					temp.append(toReplace);
					String start = temp.substring(0, i);
					String end = temp.substring(i + 1);
					temp = new StringBuffer();
					temp.append(start);
					temp.append(possibleChoices[j]);
					temp.append(end);
					
					combos.add(temp.toString());
				}//Inner for
			}//Iterate each character
		}//Iterate entire lists
		
		
		return combos;
		
	}//replaceEveryLetter
	

	/**
	 * Hashes the DNA into a buckets based on sequence, and each bucket is filled with locations of that sequence.  
	 * This is then used as a super fast lookup of locations for possible matches in the getLocationsHash method.
	 * 
	 * It works by creating a n-length(Computed at runtime by AA) sequence of DNA starting at every location within a pre determined range(This range is based on memory concerns MORE MEMORY = MORE DNA STORED AT ANY GIVEN TIME).
	 * It remembers the location of each sequence in a hash, and if a sequence is found twice, it just adds a second location to the hash value of the first time that sequence was found.
	 * This allows for quick storage/retrieval of locations of sequences.
	 * 
	 * @param DNAsequence
	 * @return  Returns a HashMap of all of the various substrings of DNA found in the larger DNAsequence.
	 */
	private HashMap<String, ArrayList<Integer>> hashDNA(StringBuffer DNAsequence, int size, int start, int stop){
		HashMap<String, ArrayList<Integer>> out = new HashMap<String, ArrayList<Integer>>();
		
		//finalPos represents the last location that a sequence can occur before it runs over end of this sub-sequence
		int finalPos = stop - size;
		ArrayList<Integer> temp;
		
		//Iterate through the defined sequence range and hash each seqeunce found
		for(int i = start; i < finalPos; i++){
			//Let the user know every million dna sequences that is hashed.
//			if(i % 1000000 == 0){
//				U.p(i);
//			}
			//Create a copy of the targeted sequence
			temp = out.get(DNAsequence.substring(i, i + size));
			
			//If this sequence does not exist, then create a new bucket for it
			if(temp == null){
				temp = new ArrayList<Integer>();
				temp.add(i);
				out.put(DNAsequence.substring(i, i + size), temp);
				
			//Since this sequence already has a bucket, just throw this location in with the rest of them
			}else{
				temp.add(i);
			}//else
		}//for
		
		return out;
		
	}//hashDNA
	
	
	/**
	 * Create DNA output creates a new annotation file with updated locations, and a file showing all locations that do not match the original annotation. 
	 */
	private void createDNAOutput(){

		U.p("Max memory used during this run was: " + Driver.getMaxMem() + " GB");
	
		//Create a header for the output file
		annoHeader = "##Modified by annotationAlignmenttool on: " + sdf.format(Calendar.getInstance().getTime()) + "\n" + "##Modified from original to fit: " + DNAFile.substring(DNAFile.lastIndexOf('/') + 1) + "\n";
		
		
		//Write the new predicted annotation to output
		try {
			U.p("Name of output file is: " + DNAFile.substring(DNAFile.lastIndexOf('/') + 1, DNAFile.lastIndexOf('.')));
			BufferedWriter out = new BufferedWriter(new FileWriter(outputDirectory +"/" + DNAFile.substring(DNAFile.lastIndexOf('/') + 1, DNAFile.lastIndexOf('.')) + ".gtf" ));
			
			//Write header information into the file
			out.write(annoHeader);
			
			//Output the new modified lines
			for(int i = 0; i < GTFlist.size();i++){
				out.write(GTFlist.get(i).toString() + "\n");

			}
			
			//Flush and close the writer to guarantee that the file was written to disk
			out.flush();
			out.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Write the mismatched lines to output
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outputDirectory +"/" + "MismatchedLines.txt" ));
			StringBuffer sb = new StringBuffer();
			
			//Count keeps track of how many false matches occur, and it is used during the calculation of the statistics for the mismatched output file.
			int count = 0;
			for(AA_Line_Container aalc: GTFlist){
				
				//Assume all predicted locations are false, until they can be verified
				boolean match = false;

				//Confirm that the original line matches up to the predicted locations
				if(aalc.getGtfLine().getStartLocation() == aalc.getNewStarts().get(0) || aalc.getGtfLine().getStopLocation() == aalc.getNewStops().get(0)){
					match = true;
				}
				
				//Output if 
				if(match == false){
					sb.append("UpdatedLine: " + aalc.toString() + "\n");
					sb.append("OriginalLine: " + aalc.getGtfLine().toString() + "\n");
					count++;
				}

			}//Go through entire GTFlist
			
			
			//Write out the output and close the file writer
			out.write("##MismatchedLines/Total " + count + "/" + GTFlist.size() +  "\n");
			out.write("##PercentCorrect " + (1 - ((double)count) / GTFlist.size())*100 + "%" + "\n"); 
			out.write(sb.toString());
			
			
			//Flush and close the writer to guarantee that the file was written to disk
			out.flush();
			out.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//catch

	}//createDNAOutput


	/**
	 * calculateStringSize calculates the size needed for a string so that it is statistically going to occur just once in a given length of DNA.
	 * It then adds the user specified safety margin and returns that value. 
	 */
	private int calculateStringSize(int lengthOfDNA){
		int out = 0;
		
		//Determine the size of DNA that is likely to occur only one time 
		while(Math.pow(4, out) < lengthOfDNA){
			out++;
		}
		
		//Add the safety margin to further reduce the chances of more then one occurrence of a sequence in the DNA.  This a trade off on accuracy vs computational speed.  Larger values are more accurate but slower.
		out += safetyMargin;
		
		//Modify String Size
		out *= 1;
		

		return out;
	}//calculateStringSize


	
}//AnnotationAlignment
