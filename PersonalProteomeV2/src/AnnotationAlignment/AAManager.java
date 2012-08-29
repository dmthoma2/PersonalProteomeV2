package AnnotationAlignment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import AnnotationAlignment.Subclasses.AA_Line_Container;
import AnnotationAlignment.Subclasses.GTF_Line;
import PersonalProteome.Definitions;
import PersonalProteome.U;


/**
 * AAManager takes in parameter information and configures/launches multiple threads to complete a genome wide annotation matching with the Annotation Alignment class.
 * 
 * 
 * **Monday July 9th - Currently is experiencing ridiculous memory requirments.  Should come up with a system to limit memory and/or number of threads.
 * The code funamentally works but needs to be fine tuned for real world usage.**x
 * 
 * @author David "Corvette" Thomas
 *
 */
public class AAManager {

	private int NTHREDS;
	
	//InputVariables
	private String annotationFile;
	private String outputDirectory;
	private String referenceDNAGenome;
	private String DNAGenome;
	private boolean proteinOnly;
	
	//StorageVariables
	private ArrayList<String> genomeFiles;
	private ArrayList<String> refGenomeFiles;

	private boolean previouslyRun = false;
	
	//Output variables
	private String annoHeader = "";
	private ArrayList<AA_Line_Container> GTFlist = new ArrayList<AA_Line_Container>();
	
	
	
	
	/**
	 * AAManager handles the alignment of an annotation to a novel genome from a reference genome.
	 * @param inputAnnotation  The annotation to be aligned.
	 * @param outputDirectory The directory to store output.
	 * @param referenceDNAGenome Reference genome to get reference DNA strands from.
	 * @param DNAGenome Genome to align the Annotation to.
	 * @param proteinOnly  special boolean tag that determines to selectively filter only genes/transcripts that are involved in protein coding.  If true
	 * this value only selects those protein genes for alignment, otherwise it aligns all lines of the annotation.
	 */
	public AAManager(String inputAnnotation, String outputDirectory, String referenceDNAGenome, String DNAGenome, boolean proteinOnly){
		this.annotationFile = inputAnnotation;
		this.outputDirectory = outputDirectory;
		this.referenceDNAGenome = referenceDNAGenome;
		this.DNAGenome = DNAGenome;
		this.proteinOnly = proteinOnly;
		
		//Populate chromsome ArrayList
		refGenomeFiles = populateChrmArrayList(this.referenceDNAGenome);
	    genomeFiles = populateChrmArrayList(this.DNAGenome);
		
		//Create enough threads to have one for each chromosome
	    if(Runtime.getRuntime().availableProcessors() < genomeFiles.size()){
	    	NTHREDS = Runtime.getRuntime().availableProcessors();
	    }else{
	    	NTHREDS = genomeFiles.size();
	    }
	    
	    /*TO prevent memory shortage issues just run a single thread*/
	    NTHREDS = 1;
	    
	    U.p("Total number of threads being utilized is: " + NTHREDS );
	   
	}//Constructor
	
	
	/**
	 * This method uploads an annotation and splits it into chunks for use in multi-threading.  Once this is complete it returns an ArrayList<AA_Line_Container>
	 * of lines from the Annotation.
	 * 
	 * @return Collections of AA lines 
	 */
	public ArrayList<AA_Line_Container> getModifiedAnnotation(){
		
		if(previouslyRun){
			return null;
		}
		
		ArrayList<Future<ArrayList<AA_Line_Container>>> outThreads = new ArrayList<Future<ArrayList<AA_Line_Container>>>();
		//Multithreading variabls
		ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
		
//		Object[] outputArray = new Object[genomeFiles.size()];
		ArrayList<AA_Line_Container> outputArrayNew = new ArrayList<AA_Line_Container>();
		List<AA_Line_Container> outputList = Collections.synchronizedList(outputArrayNew);
		//Storage variable for output
		ArrayList<AA_Line_Container> out = new ArrayList<AA_Line_Container>();
	
		
		/*Upload the annotation*/
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
		boolean uploadAll = !proteinOnly;
		int previousChrm = -1;
		
		
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
				
				//Get the chromosomeName
				String [] mandField = begin.split("\t");
				
				int chromosomeName;
				/*Plug values from the fields array into variables*/
				if(mandField[0].equalsIgnoreCase("chrM")){
					chromosomeName = Definitions.chromosomeM;
				}else if(mandField[0].equalsIgnoreCase("chrX")){
					chromosomeName = Definitions.chromosomeX;
				}else if(mandField[0].equalsIgnoreCase("chrY")){
					chromosomeName = Definitions.chromosomeY;
				}else{
					chromosomeName = Integer.parseInt(mandField[0].substring(mandField[0].indexOf('r') + 1));
					
				}
				if(previousChrm == -1){
					previousChrm = chromosomeName;
				}
				
				//Parse the start and stop locations out of the file
				startLocation = Integer.valueOf(s.next());		
				stopLocation = Integer.valueOf(s.next());
				
				
				//Store the rest of the line as is for printing it out later
				end = s.nextLine();
	
				
				line = new GTF_Line(id, begin, startLocation, stopLocation, end);

				

				
				/*Launch threads in this section once a chromosome has been completed*/
				if(chromosomeName != previousChrm || !s.hasNextLine()){
					if(chromosomeName != previousChrm){
						U.p("Chrom Different "  + previousChrm + "/" + chromosomeName);
					}
					if(!s.hasNextLine()){
						U.p("No Next Line " + previousChrm + "/" + chromosomeName);
					}
					U.p("Starting up to use chr" + + previousChrm + "/" + chromosomeName);
					
					U.p("Total size of GTFList is: " + GTFlist.size());
					ArrayList<AA_Line_Container> temp = new ArrayList<AA_Line_Container>();
					
					for(int i = 0; i < GTFlist.size(); i++){
						temp.add(GTFlist.get(i));
					}
					Callable<ArrayList<AA_Line_Container>> worker = new AnnotationAlignment(temp, refGenomeFiles.get(previousChrm - 1), genomeFiles.get(previousChrm - 1));
					
					Future<ArrayList<AA_Line_Container>> tempOut = executor.submit(worker);
					
					
					outThreads.add(tempOut);

					
					GTFlist = new ArrayList<AA_Line_Container>();
					
				
					
					
					
					

				}//if A chromosome is done
				
				//Determine if all lines are to be modified, or just lines used in protein creation are used(These are the only lines Personal Proteome uses).  
				if(uploadAll){
					
					GTFlist.add(new AA_Line_Container(line, chromosomeName));
					id++;
					
				}else{
					
					/*Since we only care about encoding proteins, only add ones that are protein_coding*/
					/*Add the line object to the list of parsed lines*/
					if(end.contains("protein_coding") || end.contains("nonsense_mediated_decay")){
						/*Finished with variables, now create a GENCODE_GTF_Line object and insert it into the ArrayList*/
						GTFlist.add(new AA_Line_Container(line, chromosomeName));
						id++;
						
					}//if
					
				}//else
				//Launch a new thread with a given GTFlist by creating a copy and sending it to the AA
				//Also launch a thread if the end of the file has been reached
				
				
				
				//clear The GTFlist and start uploading a new one
				
				/*End launching threads*/
				
				//reset all of the variables
				begin = "";
				startLocation = -1;
				stopLocation = -1;
				end = "";
				//Keep track of each Chromsome
				previousChrm = chromosomeName;
				
			}//while
			
			GTFlist.trimToSize();
			
		}catch(FileNotFoundException e){
			U.p("Error populating GTF List: " + e);

		}//catch
		
		

		
		
		
		//Close the executor
		executor.shutdown();
	
		
		//Wait until all threads are finished
		while (!executor.isTerminated()) {

		}
		
		//SAVE ALL OF THE OUTPUT
		for(int j = 0; j < outThreads.size(); j++){
			try{
				
				
				outputList.addAll(outThreads.get(j).get());
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}//for
		
//		for(int k = 0; k < outputList.size(); k++){

		out.addAll(outputList);
		
//		}//for
		
		//After all threads are done sort the output
		Collections.sort(out);
		//Remember if this method has been run before
		previouslyRun = true;
		GTFlist = out;
		return out;
	}//getMOdifiedAnnotaiton
	
	
	/**
	 * createOutput takes in the results from the annotation alignment and simply outputs them to the output directory, with the file name of the 
	 * results file taken from the name of the input genome.
	 * 
	 */
	public void createOutput(ArrayList<AA_Line_Container> GTFlist){
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(outputDirectory +"/" + DNAGenome.substring(DNAGenome.substring(0, DNAGenome.length() - 1).lastIndexOf('/') + 1, DNAGenome.lastIndexOf('/')) + "_PREDICTED_ALIGNED_ANNOTATION" + ".gtf" ));
			
			for(int i = 0; i < GTFlist.size(); i++){
				out.write(GTFlist.get(i).toString() + "\n");
			}//for
			
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//createOutput
	
	/**
	 * Get the header information from the most recent annotation uploaded by this class.
	 * 
	 * @return The header information from the most recent annotation uploaded by this class.
	 */
	public String getAnnoHeader(){
		return annoHeader;
	}//getAnnoHeader
	
	/**
	 * Returns the GTFlist object of this method.  Use this to get the completed GTFlist after this object has been ran.
	 * @return
	 */
	public ArrayList<AA_Line_Container> getGTFlist(){
		return GTFlist;
	}//getGTFlist
	
	/**
	 * Populates the ref chromosome array with the file locations of each chromosome.
	 * @param chrmDir The directory containing the chromosome files.
	 */
	private ArrayList<String> populateChrmArrayList(String chrmDir){
		ArrayList<String> out = new ArrayList<String>();
		for(int i = 0; i < 22; i++){
			out.add(chrmDir + "chr" + (i + 1) + ".fa");
		}
		out.add(chrmDir + "chrM.fa");
		out.add(chrmDir + "chrX.fa");
		out.add(chrmDir + "chrY.fa");
		
		return out;
	}//ChrmArrayList
	
}//AAManager
