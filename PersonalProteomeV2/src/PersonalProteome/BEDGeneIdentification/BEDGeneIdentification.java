package PersonalProteome.BEDGeneIdentification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import PersonalProteome.Definitions;
import PersonalProteome.GENCODE_GTF_Line;
import PersonalProteome.U;
import PersonalProteome.BEDGeneIdentifier.SubClasses.BedFileLine;
import PersonalProteome.BEDGeneIdentifier.SubClasses.PeptideLocationLine;
/**
 * 
 * 
 * BEDGeneIdentification takes in a full bed file and outputs an appended bed file with additional information about the location of the DNA sequence represented by the bed file.  
 * Additional information includes: General Location (Exon, Intron, UTR, etc), a locations priority based on the number of transcripts it occurs in (Y if it is the best score, N otherwise.  See Score section for more information).
 * LocationSpecific information which details which exon/intron a line occurs in or whether transcript lines are 5PrimeUTR or 3PrimeUTR.  General information about gene/transcripts a line occurs in are included
 * 
 * This class outputs in the following format
 * ##chrom	chromStart	chromEnd	name	score	starnd	thickStart	thickEnd	itemRgb	blockCount	blockSizes	blockStarts	generalLocation	priority	locationSpecific	geneName	transcriptName	geneStatus	transcriptStatus";
	Example lines:
	chr1	34400	34550	TargetIntergenic	245916	-	229567780	229567828	0	2	12	0	INTERGENIC	Y	MIR1302-10-FAM138A	FAM138A	FAM138A-001	KNOWN	KNOWN
	chr1	860330	861300	TargetIntron	245916	+	229567780	229567828	0	1	48	0	TRANSCRIPT	N	5PrimeUTR	SAMD11	SAMD11-009	KNOWN	KNOWN
	chr1	879535	879600	Target3PrimeUTRINFirstTranscript	245916	+	229567780	229567828	0	1	48	0	EXON	N	EXON7	SAMD11	SAMD11-004	KNOWN	NOVEL
	chr1	879535	879600	Target3PrimeUTRINFirstTranscript	245916	+	229567780	229567828	0	1	48	0	TRANSCRIPT	N	3PrimeUTR	SAMD11	SAMD11-007	KNOWN	?
 * 
 * Output is sorted based on its genomic location.
 * 
 * 
 * Score:  If a line occurs in more the one trasncript, each line is scored.  The priority column contains a Y/N result that identifies the top scoring line.  There is only one Y(Top score) per group.
 * This score is to represent what is the most likely transcript this peptide occurred in.  Scored is based on the following values: EXON-3, INTRON-2, TRANSCRIPT-1, INtergenic-0.
 * The value of the start and stop location of a given line are added up to get the score.  The values are set to reward occurring in/around exons.
 * 
 * Example: 
 * 
 * StartLocation  StopLocation  Score
 * EXON				EXON		3+3 = 6
 * INTRON			EXON		3+2 = 5
 * INTRON			INTRON		2+2 = 4
 * INTERGENIC		TRANSCRIPT	0 + 1 = 1
 * 
 * 
 * ***Bed file must have atleast the first five columns to be used by this class***
 * 
 * @author David "Corvette" Thomas
 *
 */
public class BEDGeneIdentification{

	
	//Universal
	//This Arraylist contains all of the information from the annotation.
	private ArrayList<GENCODE_GTF_Line> GTFlist;


	
	//Location check
	private ArrayList<PeptideLocationLine> peptideLocationList  = new ArrayList<PeptideLocationLine>();
	private ArrayList<ArrayList<PeptideLocationLine>> peptideLocationListTopScores = new ArrayList<ArrayList<PeptideLocationLine>>();
	private HashMap<String, ArrayList<Integer>> genes = new HashMap<String, ArrayList<Integer>>();
	
	//Storage of Input
	private ArrayList<BedFileLine> BEDlist = new ArrayList<BedFileLine>();
	
	//File locations
	private String annotationFile;
	private String bedFile;
	private String outputDir;

	//Store the header information for the output
	String headerSix = "chrom	chromStart	chromEnd	name	score	strand	";
	String headerLast = "generalLocation	priority	locationSpecific	geneName	transcriptName	geneStatus	transcriptStatus";
	//Time/Formating
	private SimpleDateFormat sdf = new SimpleDateFormat(Definitions.DATE_FORMAT);
	private SimpleDateFormat outputDirectoryFormat = new SimpleDateFormat("Mdyyyykm");
	private Calendar cal;
	private String startTime = "";
	private String endTime = "";


	/**
	 * BEDGeneIdentification
	 * @param annotationFile Gencode gtf format annotation.
	 * @param bedFile  Bed file to upload.  Must be atleast a bed 5 file or larger.
	 * @param outputDir
	 */
	public BEDGeneIdentification(String annotationFile,String bedFile, String outputDir){
		//Set default values
		this.annotationFile = annotationFile;
		this.bedFile = bedFile;
		this.outputDir = outputDir;
		
	}//PeptideAnalysisTool

	/**
	 * checkPeptideLocation uses the input files to create a list of transcripts/exons and peptides.  It then labels each peptide based on its location in the annotation.
	 * Finally checkPeptideLocation creates a set of output files in the specified output directory.  This method should only be called after its appropriate constructor.
	 */
	public void checkPeptideLocation(){
		cal = Calendar.getInstance();
		startTime = sdf.format(cal.getTime());
		U.p("Starting up: " + startTime);
		
		//Populate peptides list
		U.p("Uploading bedfile and storing it.");
		U.startStopwatch();
		uploadBedFile();
		U.stopStopwatch();
		
		U.p("Bed list size " + BEDlist.size());
		
		//Populate GTFlist
		U.p("Uploading annotation.");
		U.startStopwatch();
		populateGTFList(true);
		U.stopStopwatch();
		
		
		U.p("Total number of protein coding Genes/Exons int annotation: " + GTFlist.size());
		U.p("Total number of peptides to check location on is: " + peptideLocationList.size());
		
		//Populate peptides list
		U.p("Determining the location of each peptide.");
		U.startStopwatch();
		labelPeptidesBasedOnLocation();
		U.stopStopwatch();
		
		
		
		//Create Location output
		U.p("Creating output based on location");
		U.startStopwatch();
		//Use this matched list to get the needed information for this output
		createLocationOutput();
		U.stopStopwatch();
		
		
		//Get the ending time
		cal = Calendar.getInstance();
		endTime = sdf.format(cal.getTime());
		U.p("Finsihing up: " + endTime);
	}//checkPeptideLocation
	

	/**
	 * uploadBedFile allows uploads and stores lines for each bed file.  The input file must have at least the first 5 bed columns.
	 */
	private void uploadBedFile(){
		File peptideFile = new File(bedFile);
		//
		int id = 0;
		int chromosomeName = -1;
		int startLocation = -1;
		int stopLocation = -1;
		String sequence = "";
		String score = "";
		int strand = -1;
		String color = "";
		String blockCount = "";
		String blockSize = "";
		String blockStart = "";
		String restOfLine = "";
		
		//Parse each line of the file and save it
		try{
			Scanner s = new Scanner(peptideFile);
			String token;
			
			//Parse each Line, one at a time
			while(s.hasNextLine()){
				
				token = s.nextLine();
				String[] chunks = token.split("\\s");
				if(chunks.length < 6){
					break;
				}
				
				
				 token = chunks[0];
				 /*Plug values from the fields array into variables*/
				if(token.equalsIgnoreCase("chrM")){
					chromosomeName = Definitions.chromosomeM;
				}else if(token.equalsIgnoreCase("chrX")){
					chromosomeName = Definitions.chromosomeX;
				}else if(token.equalsIgnoreCase("chrY")){
					chromosomeName = Definitions.chromosomeY;
				}else{
					chromosomeName = Integer.parseInt(token.substring(token.indexOf('r') + 1));
					
				}
					
				
				token = chunks[1];
				//Get the start location
				startLocation = Integer.parseInt(token);
				
				token = chunks[2];
				//Get the stop location
				stopLocation = Integer.parseInt(token);
				 
				//Get the amino acid sequence from the file
				token = chunks[3];
				sequence = token;
				
				//Get the first score from the file
				token = chunks[4];
				score = token;
				
				
				//Determine genomic strand
				token = chunks[5];
				if(token.equalsIgnoreCase("+")){
					strand = Definitions.genomicStrandPOSITIVE;
				}else {
					strand = Definitions.genomicStrandNEGATIVE;
				}
				
				if(chunks.length >= 7){
					
				//There are no columns for the second start and stop location, so just add them into the color object
				color =  chunks[6] + "\t";
				}
				
				if(chunks.length >= 8){
				color += chunks[7] + "\t";
				}
				
				if(chunks.length >= 9){
				token = chunks[8];
				color += token;
				}
				
				if(chunks.length >= 10){
				token = chunks[9];
				blockCount = token;
				}
				
				if(chunks.length >=11){
				token = chunks[10];
				blockSize = token;
				}
				
				if(chunks.length >= 12){
				token = chunks[11];
				blockStart = token;
				}
				
				if(chunks.length >= 13){
					for(int k = 12; k < chunks.length; k++){
						restOfLine += chunks[k];
						if(k != chunks.length - 1){
							restOfLine += "\t";
						}
					}//for
				}//if
				
				//Add the newly read line into the peptideLocationList
				BEDlist.add(new BedFileLine(id ,chromosomeName, startLocation, stopLocation, sequence, score, strand, color, blockCount, blockSize, blockStart, restOfLine));
				String lastBits = color + " " + blockCount + " " + blockSize + " " + blockStart + " " + restOfLine;
				peptideLocationList.add(new PeptideLocationLine(chromosomeName, startLocation, stopLocation, sequence, score, strand, lastBits));
				
				id++;
				 //Reset all variables
				chromosomeName = -1;
				startLocation = -1;
				stopLocation = -1;
				sequence = "";
				score = "";
				strand = -1;
				color = "";
				blockCount = "";
				blockSize = "";
				blockStart = "";
				restOfLine = "";

			}//while
			
			
			//
			Collections.sort(BEDlist);
			Collections.sort(peptideLocationList);

		//Let the user know if the bed file is not found	
		}catch(FileNotFoundException e){
			U.p("Error populating Bed File List: " + e);

		}//catch
	}//upLoadBedFile
	
	
	/**
	 * labelPeptidesBasedOnLocation iterates through the PeptideLocationList and determines which unit of the annotation that each peptideLOcaitonLine belongs to. Each line remembers 
	 * where it occurs as it is manipulated directly.  It sorts and scores these lines.
	 */
	private void labelPeptidesBasedOnLocation(){
		
		//Sort the peptide list.  Uses a merge sort and sorts in mlog(m) time.
		Collections.sort(peptideLocationList);
		
		//Iterate through the peptides, moving the through the annotation as each peptide is checked.
		int annIndex = 0;
		for(int i = 0; i < 25; i++){
			U.p("Working on Chrm: " + (i + 1));

			
			//Iterate through the peptide list
			for(PeptideLocationLine p: peptideLocationList){
				//Ignore peptides that are not on the current chromosome
				if(p.getChromosomeName() != (i+1)){
					continue;
				}

				
				while(p.getChromosomeName() > GTFlist.get(annIndex).getChromosomeName()){
					annIndex++;
				}

				//Loop through the annotation until a unit is found that the peptide is in or beyond.
				for(int whatEver = 0; p.getChromosomeName() == GTFlist.get(annIndex).getChromosomeName() && p.getStartLocation() > genes.get(GTFlist.get(annIndex).getGeneID()).get(1); annIndex++){
					whatEver++;
				}

				
				String geneID = GTFlist.get(annIndex).getGeneID();
				
				ArrayList<Integer> transList = new ArrayList<Integer>();
				transList.add(annIndex);
				//Add the additional transcirpts to the transList
				int temp = 1;
				//Add all of the other transcripts within a gene
				while(geneID.equals(GTFlist.get(annIndex+temp).getGeneID())){
					if(GTFlist.get(annIndex+temp).getFeatureType() == Definitions.featureTypeTRANSCRIPT){
							transList.add(annIndex+temp);
					}//Outer If
					
					temp++;
				}//While Loop

				ArrayList<PeptideLocationLine> pepPossibleClassifications = new ArrayList<PeptideLocationLine>();

				//Iterate through each possible transcript this peptide could occur in, and classify it.
				for(int k = 0; k < transList.size(); k++){
					int transID = transList.get(k);
					int startID = transID;
					int stopID = transID;
					

					
					//Save this peptides transcript ID
					p.setTranscriptID(transID);
					
					
					//A gene is always identified
					//Potentially creep forward to determine if a Exon is more accurate then a transcript
					int creepIndex = transID + 1;
					//Check to determine 
					while(GTFlist.get(creepIndex).getFeatureType() != Definitions.featureTypeTRANSCRIPT){
						
						//If a peptide has its start within any exon
						if(p.getStartLocation() >= GTFlist.get(creepIndex).getStartLocation() && p.getStartLocation() <= GTFlist.get(creepIndex).getStopLocation()){
							startID = creepIndex;
						}
						//If a peptide has its stop within any exon
						if(p.getStopLocation() >= GTFlist.get(creepIndex).getStartLocation() && p.getStopLocation() <= GTFlist.get(creepIndex).getStopLocation()){
							stopID = creepIndex;
						}
						
						
						creepIndex++;
					}

					
					p.setStartID(startID);
					p.setStopID(stopID);
					
					//Determine how many exons this transcript contains
					int testIndex = transID + 1;
					int exonsCount = 0;
					while(GTFlist.get(testIndex).getFeatureType() != Definitions.featureTypeTRANSCRIPT){
							exonsCount++;
							testIndex++;
					}//transcript while loop
					
					//Store the locaitons of all exons within this transcript
					ArrayList<Integer> exons = new ArrayList<Integer>();
					
					
					//List of of the exons
					for(int j = transID + 1; GTFlist.get(j).getFeatureType() != Definitions.featureTypeTRANSCRIPT; j++){
						if(GTFlist.get(j).getFeatureType() == Definitions.featureTypeEXON){
							exons.add(j);
						}
						
					}//for
					
					
					GENCODE_GTF_Line transcriptUnit = GTFlist.get(transID);
					GENCODE_GTF_Line startUnit = GTFlist.get(startID);
					GENCODE_GTF_Line stopUnit = GTFlist.get(stopID);
					

					
					//Check situations where the start and stop occur inside or before a transcript
					if(transID == startID && transID == stopID){
						//Start and stop occur before the transcript
						if(p.getStartLocation() < transcriptUnit.getStartLocation() && p.getStopLocation() < transcriptUnit.getStartLocation()){
							p.setStartLocType(Definitions.LOCATION_TYPE_INTERGENIC);
							p.setStopLocType(Definitions.LOCATION_TYPE_INTERGENIC);
//							p.setSameSubUnit(true);
						}
						//Start occurs before transcript, stop occurs inside of transcript
						if(p.getStartLocation() < transcriptUnit.getStartLocation() && p.getStopLocation() >= transcriptUnit.getStartLocation()){
							p.setStartLocType(Definitions.LOCATION_TYPE_INTERGENIC);
							p.setStopLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
//							p.setSameSubUnit(false);
							
							//Double check to determine if stop locaiton is a INTRON vs a generic transcript
							//*** Verify
							//Write a check to determine which intron this falls in
							
							for(int s = 1; s < exons.size(); s++){
								if(p.getStopLocation() >= GTFlist.get(exons.get(s -1)).getStopLocation() && p.getStopLocation() <= GTFlist.get(exons.get(s)).getStartLocation()){
									p.setStopLocType(Definitions.LOCATION_TYPE_INTRON);
										
								}//if
								
							}//for
							
							
						}
						//Start occurs in transcript, stop occurs after transcript
						if(p.getStartLocation() >= transcriptUnit.getStartLocation() && p.getStopLocation() > transcriptUnit.getStopLocation()){
							//Double check start location to ensure that the start is not in a intron
							//**** VERIFY THIS
							for(int s = 1; s < exons.size(); s++){
								if(p.getStartLocation() >= GTFlist.get(exons.get(s -1)).getStopLocation() && p.getStartLocation() <= GTFlist.get(exons.get(s)).getStartLocation()){
									p.setStartLocType(Definitions.LOCATION_TYPE_INTRON);
										
								}//if
								
							}//for
							
							p.setStartLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
							p.setStopLocType(Definitions.LOCATION_TYPE_INTERGENIC);
//							p.setSameSubUnit(false);
						}
						//Start and stop occur inside of transcript
						if(p.getStartLocation() >= transcriptUnit.getStartLocation() && p.getStopLocation() <= transcriptUnit.getStopLocation()){
							//Since both occur in the transcript, then the peptide must be in the transcript or a intron, possibly spanning the two.
							
							//Default value for the transcript
							p.setStartLocType(Definitions.LOCATION_TYPE_INTRON);
							p.setStopLocType(Definitions.LOCATION_TYPE_INTRON);
//							p.setSameSubUnit(true);
							

			
							//Determine whether either the start or stop occur in the transcript.
							if(p.getStrand() == Definitions.genomicStrandPOSITIVE){
								//if peptide occurs before first EXON
								int first = GTFlist.get(transID + 1).getStartLocation();
								if(p.getStartLocation() < first ){
									p.setStartLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
									
								}
								if(p.getStopLocation() < first){
									p.setStopLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
								}
								int last = GTFlist.get(transID + exonsCount).getStopLocation();
								if(p.getStartLocation() > last){
									p.setStartLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
									
								}
								if(p.getStopLocation() > last){
									p.setStopLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
								}
								//or after last exon, it is transcriptic
							//Negative
							}else{
								int first = GTFlist.get(transID + 1).getStopLocation();
								if(p.getStartLocation() > first && p.getStopLocation() > first){
									p.setStartLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
									p.setStopLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
								}
								int last = GTFlist.get(transID + exonsCount).getStartLocation();
								if(p.getStartLocation() < last && p.getStopLocation() < last){
									p.setStartLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
									p.setStopLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
								}
							}//Negative
							
							
						}//Start/Stop occur inside of transcript
						
						
					}//tran tran
					
					//Check the situation where the start and stop both occur in an exon
					if(startUnit.getFeatureType() == Definitions.featureTypeEXON && stopUnit.getFeatureType() == Definitions.featureTypeEXON){
						p.setStartLocType(Definitions.LOCATION_TYPE_EXON);
						p.setStopLocType(Definitions.LOCATION_TYPE_EXON);
						
//						if(startID == stopID){
//							p.setSameSubUnit(true);
//						}else{
//							p.setSameSubUnit(false);
//						}
						
					}//exon exon
					//Consider the situation when a start is in a transcript, and the stop is in an exon.
					if(startID == transID && stopUnit.getFeatureType() == Definitions.featureTypeEXON){
						
						//Start occurs before the transcript, end occurs in an exon
						if(p.getStartLocation() < transcriptUnit.getStartLocation()){
							p.setStartLocType(Definitions.LOCATION_TYPE_INTERGENIC);
	
						}else if((transID + 1) == stopID){
							p.setStartLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
	
						}else{
						
						//At this point, the stop must occur in a exon that is not the first one.  Since the start is in the transcript, it must occur in an intron
						p.setStartLocType(Definitions.LOCATION_TYPE_INTRON);
					
						}
						p.setStopLocType(Definitions.LOCATION_TYPE_EXON);
//						p.setSameSubUnit(false);
						
						
					}//tran exon

					
					//Consider the situation when the start is in an exon, and the stop is in a transcript
					if(startUnit.getFeatureType() == Definitions.featureTypeEXON && stopID == transID){
						
						//Stop occurs after the transcript ends
						if(p.getStopLocation() > transcriptUnit.getStopLocation()){
							p.setStopLocType(Definitions.LOCATION_TYPE_INTERGENIC);
							
							//Stop occurs after the exons
						}else if(p.getStopLocation() > GTFlist.get(transID + exonsCount).getStopLocation()){
								p.setStopLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
						}else{
								p.setStopLocType(Definitions.LOCATION_TYPE_INTRON);
						}
						
						
						
						//Otherwise stop occurs within an intron
						p.setStartLocType(Definitions.LOCATION_TYPE_EXON);
//						p.setSameSubUnit(false);
					}//exon tran
					

					//Determine if a intergenic location is really inside of a gene but outside of a transcript
					//Get this genes start location
					int startLocation = genes.get(GTFlist.get(transID).getGeneID()).get(0);
	
					//Get this genes stop location
					int stopLocation = genes.get(GTFlist.get(transID).getGeneID()).get(1);
					
					if(p.getStartLocType() == Definitions.LOCATION_TYPE_INTERGENIC){
						if(p.getStartLocation() > startLocation){
							p.setStartLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
						}
						//if start is in previous gene
						if(p.getStartLocation() < stopLocation && p.getStartLocation() > GTFlist.get(exons.get(exons.size() - 1)).getStopLocation()){
							p.setStartLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
						}
					}//if
					
					if(p.getStopLocType() == Definitions.LOCATION_TYPE_INTERGENIC){
						//if stop is in current gene
						if(p.getStopLocation() > startLocation){
							p.setStopLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
						}
						
						//if stop is in previous gene
						if(p.getStopLocation() < stopLocation  && p.getStopLocation() > GTFlist.get(exons.get(exons.size() - 1)).getStopLocation()){
							p.setStopLocType(Definitions.LOCATION_TYPE_TRANSCRIPT);
						}
						
						
					}//if
					
	
					//Wrong Strand check
					if(GTFlist.get(transID).getGenomicStrand() != p.getStrand()){
						p.setStartLocType(Definitions.LOCATION_TYPE_INTERGENIC);
						p.setStopLocType(Definitions.LOCATION_TYPE_INTERGENIC);
//						p.setSameSubUnit(true);
					}
					
					
					//Create a copy of p to store in the possibilites list
					PeptideLocationLine placeHolder = new PeptideLocationLine(p.getChromosomeName(), p.getStartLocation(), p.getStopLocation(), p.getSequence(), p.getScore(), p.getStrand(), p.getRestOfLine());
					placeHolder.setStartLocType(p.getStartLocType());
					placeHolder.setStopLocType(p.getStopLocType());
					placeHolder.setSameSubUnit(p.isSameSubUnit());
					placeHolder.setTranscriptID(p.getTranscriptID());
					placeHolder.setStartID(p.getStartID());
					placeHolder.setStopID(p.getStopID());
					
					//Add a peptide to the possibilities list.
					pepPossibleClassifications.add(placeHolder);
					//Return this peptide to its default values
					p.setSameSubUnit(true);
					p.setStartLocType(Definitions.LOCATION_TYPE_UNKNOWN);
					p.setStopLocType(Definitions.LOCATION_TYPE_UNKNOWN);
					p.setTranscriptID(-1);
					p.setStartID(-1);
					p.setStopID(-1);
				}//trans loop

				
				//Line the peptides up by top score
				Collections.sort(pepPossibleClassifications);
			
				//Trim away any extra empty PeptideLocationLines
				pepPossibleClassifications.trimToSize();
				//Store the sorted list into the TopScores list
				peptideLocationListTopScores.add(pepPossibleClassifications);

			}//for iterator through peptide list
		}//for loop chromosome files
		
		
		
		//Checking for unidentified peptides
		U.p("Error printing.  Any peptide with a unidentified start or stop location type will be printed here.");
		for(ArrayList<PeptideLocationLine> possiblePositions: peptideLocationListTopScores){
			for(PeptideLocationLine p: possiblePositions){
				if(p.getStartLocType() == Definitions.LOCATION_TYPE_UNKNOWN || p.getStopLocType() == Definitions.LOCATION_TYPE_UNKNOWN){
					U.p("Peptide: " +   PeptideLocationLine.convertLocTypeToString(p.getStartLocType()) + " " + PeptideLocationLine.convertLocTypeToString(p.getStopLocType()) + " sameSub " + p.isSameSubUnit());
					U.p(p.toString());
				}//if
			}//inner for

		}//outer for
		U.p("Total number of peptides identified: " + peptideLocationListTopScores.size());
	}//labelPeptidesBasedOnLocation
	
	/**
	 * createLocationOuput creates a set of files containing the various classifications of peptides, as well as a file containing information about what files were used and statistics of the run.
	 * 
	 * The output follows the format established in this classes description
	 */
	private void createLocationOutput() {
		
		//Writing to HDD related variables
		BufferedWriter out;
		File outFile;
		

		try{
		//Create a time based unqiuely named output folder this this runs output
		File tempFile = new File(outputDir);
		tempFile.mkdir();
		outFile = new File( outputDir + "/" + outputDirectoryFormat.format(cal.getTime()) +  "/");
		outFile.mkdir();
		String outputDir = outFile.getAbsolutePath() + "/";
		//Create the new folders output
		out = new BufferedWriter(new FileWriter(outputDir + bedFile.substring(bedFile.lastIndexOf('/') + 1, bedFile.lastIndexOf('.')) + ".txt"));
			
		
		//Determine how many space filling column headers to include, because it is unknown how large of a bed file will be input
		String midParts = "";
		
		String example = BEDlist.get(0).toString();
		String[] chunks = example.split("\\s");
		
		//6 = # required lines in a bed file
		//7 = The number of new columns added
		int requiredColumns = (6);
		
		for(int i = 0; i < chunks.length - requiredColumns; i++){
			midParts +=  "Column" + (6 + (i + 1)) + "\t";
		}//for
		
		//Write the header out to the file
		out.write(headerSix + midParts + headerLast + "\n");
		
		//iterate through the bed file list and output the results
		for(int i = 0; i < BEDlist.size(); i++){
			//We now have an array List of all of the possible genetic locaitons for a given peptide
			ArrayList<PeptideLocationLine> locations = peptideLocationListTopScores.get(i);

			//Iterate through all of the possible locaitons for a given peptide
			for(int j = 0; j < locations.size(); j++){
				out.write(BEDlist.get(i).toString());

				//Establish General Location
				String generalLocation = "";
				generalLocation = getGeneralLocation(locations.get(j));
				
				//Priority
				String priority = "N";
				if(j == 0){
					priority = "Y";
				}
				//Location specific
				String locationSpecific = "";
				locationSpecific = getLocationSpecificInformation(locations.get(j));
				//geneName
				String geneName = "";
				geneName = GTFlist.get(locations.get(j).getTranscriptID()).getGene_Name();
				//transcriptName
				String transcriptName = "";
				transcriptName = GTFlist.get(locations.get(j).getTranscriptID()).getTranscript_Name();
				//geneStatus
				String geneStatus = "";
				geneStatus = Definitions.convertStatusToString(GTFlist.get(locations.get(j).getTranscriptID()).getGene_Status());
				//transcriptStatus
				String transcriptStatus = "";
				transcriptStatus = Definitions.convertStatusToString(GTFlist.get(locations.get(j).getTranscriptID()).getTranscirpt_Status());			
				
				
				out.write(generalLocation + "\t" + priority + "\t" + locationSpecific + "\t" + geneName + "\t" + transcriptName + "\t" + geneStatus + "\t" + transcriptStatus);
				out.write("\n");
			
			}//for
			
		}//for
		
		
		
		//Ensure that the file is closed and written to disk.
		out.flush();
		out.close();
	
		
		}catch (IOException e){
			e.printStackTrace();
		}
		
	}//createLocationOutput
	
	
	/**
	 * getGeneralLocation takes in a PeptideLocationLine and returns the general location type.  If the start and stop occur in and Exon, then it will return "EXON", etc.
	 * 
	 * COMPLETE LIST OF POSSIBLE RETURN VALUES:
	 * EXON
	 * INTRON
	 * TRANSCRIPT(This represents both 5Prime and 3PrimeUTR)
	 * INTERGENIC
	 *  Any combination of two of the previous values in the form of "VALUE-VALUE" 
	 *
	 * @param p
	 * @return
	 */
	private String getGeneralLocation(PeptideLocationLine p){
		//Return a default value to let the user no nothing was able to be decided about this peptide
		String out = "?";

		//determine output if the two locations are equal
		if(p.getStartLocType() == p.getStopLocType()){
			//Return the human readable form of the locaiton type
			out = Definitions.getLocType(p.getStartLocType());
		}else{
			//Return the human readable form of the locaiton types
			out = Definitions.getLocType(p.getStartLocType()) + "-" + Definitions.getLocType(p.getStopLocType());
		}

		return out;
	}//getGeneralLocation
	

	/**
	 * getLocationSpecificInformation takes in a PeptideLocationLine and returns more specific information about that said peptide.  
	 * Example outputs:
	 * EXON1
	 * INTRON2
	 * 5PrimeUTR-EXON1
	 * INTRON6-3PrimeUTR
	 * 
	 * 
	 * The output takes the form of StartLocation-StopLocaiton.  There is no hypenation if the start and stop occur in the same subunit.
	 * 
	 * 
	 * @param p  The peptideLocaitonLine to get more sepcific information
	 * @return A string representing this peptides location in a human readable form.
	 */
	private String getLocationSpecificInformation(PeptideLocationLine p){
		//Default value to output if no information is determined
		String out = "?";
		
		//To determine a location of a peptide, go through its transcipt ID and check to determine which exon/intron it appears in.
		String startSpec = getSpecificLocation(p.getTranscriptID(),p.getStartID(), p.getStartLocType(), p.getStartLocation(), p.getStrand());
		String stopSpec = getSpecificLocation(p.getTranscriptID(),p.getStopID(), p.getStopLocType(), p.getStopLocation(), p.getStrand());
		
		//Combine the start and stop locations if they are the same, otherwise hyphenate them as output
		if(startSpec.equals(stopSpec)){
			out = startSpec;
		}else{
			out = startSpec + "-" + stopSpec;
		}//else
		
		
		return out;
	}//getLocationSpecificInformation
	
	/**
	 * Get Specific location takes in a series of inputs about a PeptideLocaitonLine object, and returns the Specific location of this line.  Examples return values
	 * 
	 * EXON1
	 * INTRON2 
	 * 3PrimeUTR
	 * 5PrimeUTR
	 *  
	 * @param transID  The index into the GTFlist of this transcript.
	 * @param unitID  THe index into the specific unit(Exon or transcript) of this peptideLocationLine.
	 * @param locType  The defintions file defined location type of this exon (INTRON, EXON, TRANSCRIPT, etc).
	 * @param location  The numerical locaiton of this PeptideLocationLine in the chromosome file
	 * @param p  The peptideLocaitonLine object 
	 * @returnm  A string telling a specific locaiton in a gene
	 */
	private String getSpecificLocation(int transID,int unitID, int locType, int location, int strand){
		//Return a default value to let the user no nothing was able to be decided about this peptide
		String out = "?";

		/*Prep section*/
		//Name of the gene that occursa prior to the gene of the current peptide
		String previousGeneName = null;
		String currentGeneName = GTFlist.get(transID).getGene_Name();
		
		//Calculate the name of the previous GENE
		for(int i = transID - 1; i >= 0; i--){
			if(!GTFlist.get(i).getGene_Name().equals(currentGeneName)){
				previousGeneName = GTFlist.get(i).getGene_Name();
				break;
			}//if

		}//for loop
		
		//Store the locaitons of all exons within this transcript
		ArrayList<Integer> exons = new ArrayList<Integer>();
		
		
		//List of  the exons within this genes transcirpt
		for(int j = transID + 1; GTFlist.get(j).getFeatureType() != Definitions.featureTypeTRANSCRIPT; j++){
			if(GTFlist.get(j).getFeatureType() == Definitions.featureTypeEXON){
				exons.add(j);
			}
			
		}//for
		/*End Prep Section*/
		
		/*Work section*/
		//exon
		//if it is an exon, just count up and return the exon.
		if(locType == Definitions.LOCATION_TYPE_EXON){
			
			//Go through the exons and determine which exon this locaiton occurs in
			for(int k = 0; k < exons.size(); k++){
				if(unitID == exons.get(k)){
					//Determine the output for based on strand
					if(strand == Definitions.genomicStrandPOSITIVE){
						out = "EXON" + (k+1);
					}else{
						out = "EXON" + (exons.size() - k);
					}
					
				}//if
			}//for
		
			
			//****Insverse for negative strands
			
			
		}//exon
		
		//intron
		//if it is an intron, just count up the intron and return intron
		if(locType == Definitions.LOCATION_TYPE_INTRON){

			//Write a check to determine which intron this peptide falls in
			
			
			//Positive
			if(strand == Definitions.genomicStrandPOSITIVE){
				for(int k = 1; k < exons.size(); k++){
					if(location >= GTFlist.get(exons.get(k -1)).getStopLocation() && location <= GTFlist.get(exons.get(k)).getStartLocation()){
						
						out = "INTRON" + k;
							
					}//if
					
				}//for
			//NEGATIVE
			}else{
				for(int k = 1; k < exons.size(); k++){
					if(location <= GTFlist.get(exons.get(k -1)).getStartLocation() && location >= GTFlist.get(exons.get(k)).getStopLocation()){
						//The order of exons is reversed for negatives, so inverse the number
						out = "INTRON" + (exons.size() - k);
							
					}//if
					
				}//for
			}//else
		}//intron
			
		//intergenic
		//if it is intergenic, just get the previous gene and the current gene  Intergenic occurs on or before a gene
		if(locType == Definitions.LOCATION_TYPE_INTERGENIC){

			//If this is not the first gene then output normally with both genes
			if(previousGeneName != null){
				out = previousGeneName + "--" + currentGeneName;
			}else{
				//Since this is the first gene just 
				out = "before-" + currentGeneName;
			}//else

		}//intergenic
		
		//UTR
		//if it is not either of the previous then check which UTR to return.  If it occurs before the first exon return 5Prime, otherwise return 3Prime
		if(locType == Definitions.LOCATION_TYPE_TRANSCRIPT){
			
			if(location < GTFlist.get(exons.get(0)).getStartLocation() ){
				out = "5PrimeUTR";
			}else{
				out = "3PrimeUTR";
			}
	
		}//transcript
		/*End work section*/
	
		return out;
	}//getSpecific string
	

	/**
	 * parseGTFFile takes a GTF format annotation file and populates an ArrayList, (GTFlist), of GENCODE_GTF_Line objects representing each line of the file.
	 * @param onlyTranscriptsExons is a boolean variable that only allows for transcripts and exons of protein encoding genes to be stored.
	 */
	private void populateGTFList(boolean onlyTranscriptExons){
		File GTFFile = new File(annotationFile);
		//GTFint List to work on
		GTFlist = new ArrayList<GENCODE_GTF_Line>();
		//Represents a single line
		GENCODE_GTF_Line line;
		//Working string
		String temp = null;
		//Fields of the line
		String[] mandField;
		String[] optionalField;
		//Variables for the mandatory fields
		int id = 0;
		int chromosomeName;
		int annotationSource = -1;
		int featureType = -1;
		int startLocation = -1;
		int stopLocation = -1;
		int score = -1;
		int genomicStrand = -1;
		int genomicPhase = -1;
		String geneID;
		String transcriptID;
		String gene_Type;
		int gene_Status = -1;
		String gene_Name;
		String transcript_Type;
		int transcript_Status = -1;
		String transcript_Name;
		int level = -1;
		boolean skip = false;
		boolean finished = false;
		int count = 1;
		
		//Parse each line of the file and save it
		try{
			Scanner s = new Scanner(GTFFile);

			//Parse each Line, one at a time
			while(s.hasNextLine()){
				
				if(!skip){
				temp = s.next();
				}

				//Move past commments
				if(temp.startsWith("##")){
					s.nextLine();
					
					continue;
				}
				
				/*Get Mandatory fields*/
				//Initialize variables
				mandField = new String[17];
				//Parse out the information from the line and store it in the mandFields array
				for(int i = 0; i < 8; i++){
					mandField[i] = temp;
					if(s.hasNext()){
					temp = s.next();
					}else{
						finished = true;
						if(finished){
							break;
						}
					}
				}
				if(finished){
					break;
				}
				
				/*Populate 8-16 with values*/
				for(int i = 8; i < 16; i++){
					temp = s.next();
					mandField[i] = temp.substring(temp.indexOf('"') +1 , temp.lastIndexOf('"'));
					s.next();
				}
				
				//Current token is level after the prvious loop, so add level in and start checking for optional parameters
				temp = s.next();
				mandField[16] = temp.substring(0, temp.length() - 1);
				
				
				/*Search for optional fields*/
				/*0 = tag, 1 = ccdsid, 2 = havana_gene, 3 = havana_transcript, 4 = ont*/
				optionalField = new String[5];
				
				
				//Iterate through and handle optional fields
				while(s.hasNext()){
					temp = s.next();

					if(temp.equals("tag")){
						temp = s.next();
						optionalField[0] = temp.substring(temp.indexOf('"') + 1, temp.lastIndexOf('"'));
					}else if(temp.equals("ccdsid")){
						temp = s.next();
						optionalField[1] = temp.substring(temp.indexOf('"') + 1, temp.lastIndexOf('"'));
					}else if(temp.equals("havana_gene")){
						temp = s.next();
						optionalField[2] = temp.substring(temp.indexOf('"') + 1, temp.lastIndexOf('"'));
					}else if(temp.equals("havana_transcript")){
						temp = s.next();
						optionalField[3] = temp.substring(temp.indexOf('"') + 1, temp.lastIndexOf('"'));
					}else if(temp.equals("ont")){
						temp = s.next();
						optionalField[4] = temp.substring(temp.indexOf('"') + 1, temp.lastIndexOf('"'));
					}else{
						break;
					}
					
				}//while
				skip = true;
				
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
				
				

				if(mandField[1].equalsIgnoreCase("ENSEMBL")){
					annotationSource = Definitions.ENSEMBL;
				}else{
					annotationSource = Definitions.HAVANA;
				}
				
				if(mandField[2].equalsIgnoreCase("gene")){
					featureType = Definitions.featureTypeGENE;
				}else if(mandField[2].equalsIgnoreCase("transcript")){
					featureType = Definitions.featureTypeTRANSCRIPT;
				}else if(mandField[2].equalsIgnoreCase("CDS")){
					featureType = Definitions.featureTypeCDS;
				}else if(mandField[2].equalsIgnoreCase("start_codon")){
					featureType = Definitions.featureTypeSTART_CODON;
				}else if(mandField[2].equalsIgnoreCase("stop_codon")){
					featureType = Definitions.featureTypeSTOP_CODON;
				}else if(mandField[2].equalsIgnoreCase("exon")){
					featureType = Definitions.featureTypeEXON;
				}else if(mandField[2].equalsIgnoreCase("UTR")){
					featureType = Definitions.featureTypeUTR;
				}else if(mandField[2].equalsIgnoreCase("Selenocysteine")){
					featureType = Definitions.featureTypeSELENOCYSTEINE;
				}
				

				
				startLocation = Integer.parseInt(mandField[3]);
				stopLocation = Integer.parseInt(mandField[4]);

				/*Score is not used, so a place holder of -1 is put in*/
				score = -1;
				
				if(mandField[6].equalsIgnoreCase("+")){
					genomicStrand = Definitions.genomicStrandPOSITIVE;
				}else {
					genomicStrand = Definitions.genomicStrandNEGATIVE;
				}
				
				if(mandField[7].equalsIgnoreCase("0")){
					genomicPhase = Definitions.genomicPhaseZERO;
				}else if(mandField[7].equalsIgnoreCase("1")){
					genomicPhase = Definitions.genomicPhaseONE;
				}else if(mandField[7].equalsIgnoreCase("2")){
					genomicPhase = Definitions.genomicPhaseTWO;
				}else if(mandField[7].equalsIgnoreCase(".")){
					genomicPhase = Definitions.genomicPhasePERIOD;
					
				}

				geneID = mandField[8];
				transcriptID = mandField[9];
				gene_Type = mandField[10];
				
				if(mandField[11].equalsIgnoreCase("KNOWN")){
					gene_Status = Definitions.geneStatusKNOWN;
				}else if(mandField[11].equalsIgnoreCase("NULL")){
					gene_Status = Definitions.geneStatusNULL;
				}else if(mandField[11].equalsIgnoreCase("NOVEL")){
					gene_Status = Definitions.geneStatusNOVEL;
				}else if(mandField[11].equalsIgnoreCase("UNKNOWN")){
					gene_Status = Definitions.geneStatusUNKNOWN;
				}
				
				gene_Name = mandField[12].trim();
				transcript_Type = mandField[13].trim();
				
				if(mandField[14].trim().equalsIgnoreCase("KNOWN")){
					transcript_Status = Definitions.transcriptStatusKNOWN;
				}else if(mandField[14].equalsIgnoreCase("NULL")){
					transcript_Status = Definitions.transcriptStatusNULL;
				}else if(mandField[14].equalsIgnoreCase("NOVEL")){
					transcript_Status = Definitions.transcriptStatusNOVEL;
				}else if(mandField[14].equalsIgnoreCase("UNKNOWN")){
					transcript_Status = Definitions.transcriptStatusUNKNOWN;
				}

				transcript_Name = mandField[15];
				if(mandField[16].equalsIgnoreCase("1")){
					level = Definitions.GENCODELevelONE;
				}else if(mandField[16].equalsIgnoreCase("2")){
					level = Definitions.GENCODELevelTWO;
				}else if(mandField[16].equalsIgnoreCase("3")){
					level = Definitions.GENCODELevelTHREE;
				}
						

				line = new GENCODE_GTF_Line(id, chromosomeName,  annotationSource,  featureType,  startLocation,  stopLocation,  score,  genomicStrand,  genomicPhase,
						 geneID,  transcriptID,  gene_Type,  gene_Status,  gene_Name,  transcript_Type,  transcript_Status,
						 transcript_Name,  level,  optionalField[0],  optionalField[1],  optionalField[2],  optionalField[3],  optionalField[4]);
				
				
				if(line.getFeatureType() == Definitions.featureTypeGENE){
					ArrayList<Integer> startNStop = new ArrayList<Integer>();
					startNStop.add(line.getStartLocation());
					startNStop.add(line.getStopLocation());
					
					genes.put(line.getGeneID(), startNStop);
				}
				
				
				/*Since we only care about encoding proteins, only add ones that are protein_coding*/
				/*Add the line object to the list of parsed lines*/
//				if(transcript_Type.equalsIgnoreCase("protein_coding") || transcript_Type.equalsIgnoreCase("nonsense_mediated_decay")){
					/*Finished with variables, now create a GENCODE_GTF_Line object and insert it into the ArrayList*/
					if(!onlyTranscriptExons){
						GTFlist.add(line);
						id++;
					}else{
						//Only genes are exons are important for location information
						if(line.getFeatureType() == Definitions.featureTypeTRANSCRIPT || line.getFeatureType() == Definitions.featureTypeEXON){
							GTFlist.add(line);
							id++;
						}
						
					}
//				}
				//reset all of the variables
				 
				 chromosomeName = -1;
				 annotationSource = -1;
				 featureType = -1;
				 startLocation = -1;
				 stopLocation = -1;
				 score = -1;
				 genomicStrand = -1;
				 genomicPhase = -1;
				 geneID = "";
				 transcriptID = "";
				 gene_Type = "";
				 gene_Status = -1;
				 gene_Name = "";
				 transcript_Type = "";
				 transcript_Status = -1;
				 transcript_Name = "";
				 level = -1;
				 count++;
			}//while

			
		}catch(FileNotFoundException e){
			U.p("Error populating GTF List: " + e);

		}//catch

	}//populateGTFList
}//Peptide Analysis tool
