package GencodeBPSequenceCreater;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import Peppy.Sequence_DNA;
import PersonalProteome.Annotation;
import PersonalProteome.Definitions;
import PersonalProteome.GENCODE_GTF_Line;
import PersonalProteome.U;

/**
 * 
 * GencodeBPSequence is a simple program that takes in a annotation and a genome and creates a fasta output file.  This output file conists of a series of header lines and a nucleotide sequence.
 * Each header is of the format:
 * 
 * >GeneName-TranscriptName|chr#|ProteinCoding: Y or N|Strand: + or -| Genetic Constructs (These can be UTR, EXON, or INTRON)
 * 
 * Some example lines:
 * >DDX11L1-DDX11L1-001|chr1|ProteinCoding: N|Strand: +|EXON1 141-188|INTRON1 189-309|EXON2 310-358|INTRON2 359-743|EXON3 744-828|INTRON3 829-1105|EXON4 1106-1183|INTRON4 1184-1351|EXON5 1352-1505|INTRON5 1506-1583|EXON6 1584-1801|
 * >FAM138A-FAM138A-001|chr1|ProteinCoding: Y|Strand: -|UTR1 0-344|EXON1 0-360|INTRON1 361-599|EXON2 600-804|INTRON2 805-906|EXON3 907-1527|UTR2 941-1527|
 * >OR4G11P-OR4G11P-001|chr1|ProteinCoding: N|Strand: +|EXON1 0-939|
 * >RNASEL-RNASEL-001|chr1|ProteinCoding: Y|Strand: -|UTR1 0-89|EXON1 0-89|UTR2 2286-2449|EXON2 2286-3929|INTRON1 3930-5089|EXON3 5090-5175|INTRON2 5176-6997|EXON4 6998-7203|INTRON3 7204-7898|EXON5 7899-8031|INTRON4 8032-12866|EXON6 12867-13000|INTRON5 13001-13677|EXON7 13678-15622|UTR3 13862-15622|
 *
 *Each set of header lines is followed by the DNA sequence for the preceding genes DNA.
 *
 *To save space, if several lines in a row come from the same gene, then those lines headers are stored back to back and only one sequence is printed.
 *
 *Ex.
 *>RNASEL-RNASEL-001|chr1|ProteinCoding: Y|Strand: -|UTR1 0-89|EXON1 0-89|UTR2 2286-2449|EXON2 2286-3929|INTRON1 3930-5089|EXON3 5090-5175|INTRON2 5176-6997|EXON4 6998-7203|INTRON3 7204-7898|EXON5 7899-8031|INTRON4 8032-12866|EXON6 12867-13000|INTRON5 13001-13677|EXON7 13678-15622|UTR3 13862-15622|
 *>RNASEL-RNASEL-002|chr1|ProteinCoding: Y|Strand: -|UTR1 0-89|EXON1 0-89|UTR2 2286-2449|EXON2 2286-3929|INTRON1 3930-5089|EXON3 5090-5175|INTRON2 5176-6997|EXON4 6998-7203|INTRON3 7204-7898|EXON5 7899-8031|INTRON4 8032-12866|EXON6 12867-13000|INTRON5 13001-13677|EXON7 13678-15622|UTR3 13862-15622|
 *Sequence
 *>OR4G11P-OR4G11P-001|chr1|ProteinCoding: N|Strand: +|EXON1 0-939|
 *Sequence
 *>FAM138A-FAM138A-001|chr1|ProteinCoding: Y|Strand: -|UTR1 0-344|EXON1 0-360|INTRON1 361-599|EXON2 600-804|INTRON2 805-906|EXON3 907-1527|UTR2 941-1527|
 *Sequence
 *
 *Usage:
 *
 *Gencode BP Sequence takes in three arguments in the following order:
 *
 *annotationFile - This is the path to a GENCODE gtf format annotation.  These can be had at: http://www.gencodegenes.org/
 *
 *genomeFile - This parameter is the directory where a series of fasta files named chr#.fa are located.  The reference human genome is  required for use with official GENCODE annotations
 *				and it can be found at: http://hgdownload.cse.ucsc.edu/downloads.html#human
 *
 *outputDirectory - This parameter simply states what folder the output file is to be placed in.  The output file will be named "SEQ_" + the original annotations name.  The file will be given
 *					the extension .fasta
 *
 *Requirements:
 *
 *GencodeBPSequence requires memory sufficient to hold the largest chromosome + the input annotations at any given time.  For humans with the complete gencode annotations around ~4GB is memory enough to ensure reasonable performance (More is better!).
 *Sets this with -Xmx4G as a parameter when launching this as a jar file.
 *
 * @author David "Corvette" Thomas June 2012
 *
 */
public class GencodeBPSequence {

	
	
	/*
	 * Main simply launches GencodeBPSequence.
	 * 
	 * Arguments are annotationFile, genome directory, and output directory.
	 */
	public static void main(String[] args){
		//Check to enusre that there are exactly three parameters
		if(args.length != 3){
			U.p("There must be three arguments: annotationFile   genomeDirectory     outputDirectory");
			return;
		}
		
		//Create a format to let the user know the start time of this program
		SimpleDateFormat sdf = new SimpleDateFormat(Definitions.DATE_FORMAT);
		U.p("Starting up on: " + sdf.format(Calendar.getInstance().getTime()));
		U.p("Creating a .fasta file with genes and genetic sequences.");
		
//		args = new String[3];
		
		/*Various sample inputs used during debugging*/
		//annotation
//		args[0] = "/Users/davidthomas/Peppy/ProteomeV2/GencodeBPSequence/annotation/gencode.v11.chrm1.gtf";
//		args[0] = "/Users/davidthomas/Peppy/ProteomeV2/GencodeBPSequence/annotation/gencode.v11.annotation.gtf";
//		args[0] = "/Users/davidthomas/Peppy/ProteomeV2/GencodeBPSequence/annotation/gencode.v7.annotation.gtf";
		//genome
//		args[1] = "/Users/davidthomas/Peppy/ProteomeV2/GencodeBPSequence/genome/hg19/";
//		output
//		args[2] = "/Users/davidthomas/Peppy/ProteomeV2/GencodeBPSequence/output/";
		
		U.p("Creating a sequence file for: " + args[0]);
		
		GencodeBPSequence gbps = new GencodeBPSequence(args[0], args[1], args[2]);
		
		gbps.go();
		
		U.p("A job well done...");
	}//main
	
	//Genome Directory
	private String genomeDirectory;
	ArrayList<String> genomeFiles;
	//Annotation File
	private String annotationFile;
	//output directory
	private String outputDirectory;

	
	//ArrayList of lines from the GTF File
	//This Arraylist contains all of the information from the annotation.
	private ArrayList<GENCODE_GTF_Line> GTFlist;
	
	//
	private ArrayList<ArrayList<Integer>> functGroups;

	public GencodeBPSequence(String annotationFile, String genomeDirectory, String outputDirectory){
		this.genomeDirectory = genomeDirectory;
		this.annotationFile = annotationFile;
		this.outputDirectory = outputDirectory;

		//Create a list of all of the file locations within the genome directory based the given directory
		genomeFiles = new ArrayList<String>();
		populateChrmArrayList(this.genomeDirectory);
		
		//Instantiate the functGroups variable
		functGroups = new ArrayList<ArrayList<Integer>>();
	}//GencodeBPSequence
	
	
	/*
	 * Go uploads all the genome and annotation, seperates the genes and calculates indices, and then creates output.
	 * Go does this through a series of appropriate method calls and does not do any calculation itself.
	 */
	public void go(){
		
		//upload annotation
		U.p("Uploading annotation.");
		U.startStopwatch();
		uploadAnnotation();
		U.stopStopwatch();

		//Break down each gene into a functional group to use with a method that pulls sequences
		U.p("Breaking genes into functional groups.");
		U.startStopwatch();
		seperateGenes();
		U.stopStopwatch();
		
		
		//Pull those sequences and create output in the same method
		U.p("Generating output.");
		U.startStopwatch();
		try {
			grabSequenceAndOutput();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		U.stopStopwatch();
	}//go
	
	/**
	 * uploadAnnotation uses PersonalProteome V2 to upload the annotation and stores it in GTFlist.
	 * 
	 */
	private void uploadAnnotation(){
		//Grab the mitocondrian DNA and don't select for only protein coding genes
		boolean hasMito = true;
		boolean proteinOnly = false;
		
		Annotation a = new Annotation(hasMito, proteinOnly);
		

		//Upload the complete annotation file
		a.populateGTFList(new File(annotationFile));
		
		//Save the annotation
		GTFlist = a.getAnnotaitonLines();
		
	}//uploadAnnotation
	
	/**
	 * This method populates the functGroups variable, for use in the grabSequenceAndOuptut method.
	 * SeperateGenes creates a series of "buckets" representing each gene.  A series of indices into the GTFlist are added to the functional groups list.  
	 * The first position of this is the gene, and the second position is the transcript (0 and 1 respectively).  Each following index is an exon within that transcirpt.
	 * Each entry contains a gene, but not all entries have a transcript.  If a gene has a transcript it may or may not have any exons.  A gene must have a transcript to have
	 * and exons present.
	 */
	private void seperateGenes(){
	
		//iterate through the GTF list until a gene is found, then add a new section to the functional gene groups
		for(int i = 0; i < GTFlist.size(); i++){
			GENCODE_GTF_Line currentGene = GTFlist.get(i);
			ArrayList<Integer> workingGroup = new ArrayList<Integer>();
			int beginSize = functGroups.size();
			//Check for genes
			if(currentGene.getFeatureType() == Definitions.featureTypeGENE){
				workingGroup.add(i);
				
				//When a gene is found, determine what members are within that gene, and once another gene is found stop and return to the current index
				for(int j = i + 1; j < GTFlist.size() && GTFlist.get(j).getFeatureType() != Definitions.featureTypeGENE; j++){
					GENCODE_GTF_Line currentTranscript = GTFlist.get(j);
					
					
					//Check for transcripts
					if(currentTranscript.getFeatureType() == Definitions.featureTypeTRANSCRIPT){
						//Add the gene to this functional group
						workingGroup = new ArrayList<Integer>();
						//Add the gene and transcript to this functional group
						workingGroup.add(i);
						workingGroup.add(j);
						

						
						for(int k = j + 1; k < GTFlist.size() && GTFlist.get(k).getFeatureType() != Definitions.featureTypeTRANSCRIPT; k++){
							GENCODE_GTF_Line currentExon = GTFlist.get(k);
							
							//Add every exon to the working group
							if(currentExon.getFeatureType() == Definitions.featureTypeEXON || currentExon.getFeatureType() == Definitions.featureTypeUTR){
								
								//Add exons and UTR regions to the working group
								workingGroup.add(k);
								
								
								
							}//if Exon
							
							
							
						}//for exon
						functGroups.add(workingGroup);
					}//If transcript
					
				}//For gene for loop
				if(functGroups.size() == beginSize){
					functGroups.add(workingGroup);
				}
			}//Gene found if
			
			
		}//for GTFlist
		
		
	}//seperateGenes
	
	/**
	 * grabSequenceAndOuput iterates through the functional groups variable and creates and writes to disk the appropriate headers and sequences.  It uses the getHeader method to calculate header information
	 * for each functional group, then grabs the appropriate nucleotide sequence from the DNA file for the header.  It uploads each chromosome from disk only when it needs to, so sorting the 
	 * Annotation based on chromosome significantly improves performance(This is done already if using the GENCODE annotation).  
	 * 
	 * @throws IOException To make code easier to read, this method throws this exception instead of catching it to make a complex sub routine have less code in it.
	 * 
	 */
	private void grabSequenceAndOutput() throws IOException{
		
			BufferedWriter out = new BufferedWriter(new FileWriter(outputDirectory + "/" + "SEQ_"  + annotationFile.substring(annotationFile.lastIndexOf('/') + 1, annotationFile.lastIndexOf('.')) + ".fasta"));
	

			Sequence_DNA seq = null;
			int currentSequence = 0;
			
	
			//Loop through the functGroups
			for(int j = 0; j < functGroups.size(); j++){
				ArrayList<Integer> currentGroup = functGroups.get(j);
				
				//Load up the appropriate chromosome to get DNA from
				if(currentSequence != GTFlist.get(currentGroup.get(0)).getChromosomeName()){
					currentSequence = GTFlist.get(currentGroup.get(0)).getChromosomeName();
					U.p("Sequencing chromsome file: " + genomeFiles.get(currentSequence - 1).substring(genomeFiles.get(currentSequence -1).lastIndexOf('/') + 1, genomeFiles.get(currentSequence -1 ).lastIndexOf('.')));
					seq = new Sequence_DNA(genomeFiles.get(currentSequence - 1));
				}
				
				//Calculate the header information for the current group
				String header = getHeader(currentGroup);
				
				
				
				//Write out each lines header to the results file
				out.write(header + "\n");
				
				
				//Grab this genes sequence
				StringBuffer sequence = new StringBuffer();
				if(GTFlist.get(currentGroup.get(0)).getGenomicStrand() == Definitions.genomicStrandPOSITIVE){
					sequence.append(seq.getNucleotideSequences().get(0).getSequence().substring(GTFlist.get(currentGroup.get(0)).getStartLocation() - 1, GTFlist.get(currentGroup.get(0)).getStopLocation() - 1));
				}

				
				//If the strand is negative
				if(GTFlist.get(currentGroup.get(0)).getGenomicStrand() == Definitions.genomicStrandNEGATIVE){
					sequence = new StringBuffer();
					//The reverse strand requires a index off by one
					sequence.append(seq.getNucleotideSequences().get(0).getSequence().substring(GTFlist.get(currentGroup.get(0)).getStartLocation() , GTFlist.get(currentGroup.get(0)).getStopLocation()));
					
					
					//Reverse and compliment the negative strand so it will be accurate
					sequence.reverse();
					
					
					//Compliment the sequence
					StringBuffer temp = new StringBuffer();
					for(int k = 0; k < sequence.length(); k++){
						temp.append(DNACompliment(sequence.charAt(k)));
					}
					
					sequence = temp;
		
				}//Negative
			
				//Only write the sequence if this is the last occurrence of this gene
				if(j + 1 < functGroups.size()){
					if(GTFlist.get(functGroups.get(j+1).get(0)).getGene_Name().equals(GTFlist.get(currentGroup.get(0)).getGene_Name())){
						if(GTFlist.get(functGroups.get(j+1).get(0)).getGene_Type().equals(GTFlist.get(currentGroup.get(0)).getGene_Type())){
							continue;
						}	
					}
				}

				
				//Write hte sequence out
				out.write(sequence.toString() + "\n");
				
			}//for


			//Ensure that the BufferedWriter flushes the pipeline and writes the data to disk.
			out.flush();
			out.close();

	}//grabSequenceAndOutput
	
	
	/**
	 * getHeader calculates the header in the output fasta for a given functional group.  More information about the format of this header can be found in the section describing this class.
	 * Reverse strand genes are stored in the opposite order in the GENCODE gtf, so this method returns indices based on the end of a reversed gene instead of the beginning.  The indices
	 * for reverse genes is correct if the matching sequence is reversed and complimented.  This creates a more uniform and understandable output file.
	 * 
	 * @param group  A function group consisting of the indices of a gene, one of its transcripts, and all exons and UTR regions within that transcript.
	 * @return A string representing the header for the functional group passed in.
	 */
	private String getHeader(ArrayList<Integer> group){
		String header = "";
		
		int size = group.size();
		
		String proteinCoding = "ProteinCoding: ";
		String strand = "?";
		//Test for just a gene
		if(size >= 1){
			header = ">" + GTFlist.get(group.get(0)).getGene_Name();
			strand = "+";
			if(GTFlist.get(group.get(0)).getGenomicStrand() == Definitions.genomicStrandNEGATIVE){
				strand = "-";
			}
		}//if size >= 1
		
		
		//If there are no transcripts (Only a gene entry), then there 
		if(size == 1){
			if(GTFlist.get(group.get(0)).getTranscript_Type().equals("protein_coding") || GTFlist.get(group.get(0)).getTranscript_Type().equals("nonsense_mediated_decay")){
				proteinCoding += "Y";
				
			}else{
				proteinCoding += "N";
			}
			

			header  += "-" + "NoTranscripts" + "|" + Definitions.convertChrmNumToString(GTFlist.get(group.get(0)).getChromosomeName()) + "|" + proteinCoding + "|" +"Strand: " + strand + "|" ;
		}
		
		//Test for a gene and a transcript
		if(size >= 2){
			
			if(GTFlist.get(group.get(0)).getTranscript_Type().equals("protein_coding") || GTFlist.get(group.get(0)).getTranscript_Type().equals("nonsense_mediated_decay")){
				proteinCoding += "Y";
				
			}else{
				proteinCoding += "N";
			}
			header += "-" + GTFlist.get(group.get(1)).getTranscript_Name() + "|" + Definitions.convertChrmNumToString(GTFlist.get(group.get(1)).getChromosomeName()) + "|" + proteinCoding + "|" +"Strand: " + strand + "|";
		}//if size >= 2
		
		//Test for a gene/transcript/exons
		if(size  >= 3){
			//Positive
			if(GTFlist.get(group.get(0)).getGenomicStrand() == Definitions.genomicStrandPOSITIVE){
				int geneStart = GTFlist.get(group.get(0)).getStartLocation();
				
				//Create an Array list of all of the UTR and exons, then sort them based on their location
				ArrayList<GENCODE_GTF_Line> utrAndExon = new ArrayList<GENCODE_GTF_Line>();
				for(int j = 2; j < group.size(); j++){
					utrAndExon.add(GTFlist.get(group.get(j)));
				}
				
				//Sort utrAndExon  Ensure that the UTR and exons are in the correct order
				Collections.sort(utrAndExon);
				

				int UTRCount = 0;
				int EXONCount = 0;
				int INTRONCount = 0;
				//UTR-# #-#   EXON-# #-#  INTRON-# #-#
				for(int k = 0; k < utrAndExon.size(); k++){
					GENCODE_GTF_Line currentLine = utrAndExon.get(k);
					
					//Write the appropriate header section for a positive UTR
					if(currentLine.getFeatureType() == Definitions.featureTypeUTR){
						UTRCount++;
						header += "UTR" + UTRCount + " " + (currentLine.getStartLocation() - geneStart) + "-" + (currentLine.getStopLocation() - geneStart) + "|";
					}//if a UTR
					
					//Write the appropriate header section for a positive EXON
					if(currentLine.getFeatureType() == Definitions.featureTypeEXON){
						EXONCount++;
						header += "EXON"  + EXONCount + " " + (currentLine.getStartLocation() - geneStart) + "-" + (currentLine.getStopLocation() - geneStart) + "|";
						
						//IF this is not the last line in the utrAndExon, see if there is a exon following it(This creates an intron)
						if(k + 1 != utrAndExon.size()){
							//There is an intron located between these two exons
							if(utrAndExon.get(k+1).getFeatureType() == Definitions.featureTypeEXON){
								INTRONCount++;
								header += "INTRON" + INTRONCount + " " + (currentLine.getStopLocation() + 1 - geneStart) + "-" + (utrAndExon.get(k+1).getStartLocation() - 1 - geneStart) + "|";
							}
						}//if
						
					}//if an exon
					
				}//utrAndExon for loop
				
			}//if positive
			
			//Negative
			if(GTFlist.get(group.get(0)).getGenomicStrand() == Definitions.genomicStrandNEGATIVE){
				int geneStop = GTFlist.get(group.get(0)).getStopLocation();
				
				//Create an Array list of all of the UTR and exons, then sort them based on their location
				ArrayList<GENCODE_GTF_Line> utrAndExon = new ArrayList<GENCODE_GTF_Line>();
				for(int j = 2; j < group.size(); j++){
					utrAndExon.add(GTFlist.get(group.get(j)));
				}
				
				//Sort utrAndExon and reverse it.  The Exons are in backwards order in the GENCODE annotaitons for - strand.  
				Collections.sort(utrAndExon);
				Collections.reverse(utrAndExon);

				
				
				int UTRCount = 0;
				int EXONCount = 0;
				int INTRONCount = 0;
				//UTR-# #-#   EXON-# #-#  INTRON-# #-#
				for(int k = 0; k < utrAndExon.size(); k++){
					GENCODE_GTF_Line currentLine = utrAndExon.get(k);
					//Write the appropriate header section for a negative UTR
					if(currentLine.getFeatureType() == Definitions.featureTypeUTR){
						UTRCount++;
						header += "UTR" + UTRCount + " " + -1*(currentLine.getStopLocation() - geneStop) + "-" + -1*(currentLine.getStartLocation() - geneStop) + "|";
					}//if a UTR
					
					//Write the appropriate header section for a negative EXON
					if(currentLine.getFeatureType() == Definitions.featureTypeEXON){
						EXONCount++;
						header += "EXON"  + EXONCount + " " + -1*(currentLine.getStopLocation() - geneStop) + "-" + -1*(currentLine.getStartLocation() - geneStop) + "|";
						//IF this is not the last line in the utrAndExon, see if there is a exon following it(This creates an intron
						if(k + 1 != utrAndExon.size()){
							//IF this is not the last line in the utrAndExon, see if there is a exon following it(This creates an intron)
							if(utrAndExon.get(k+1).getFeatureType() == Definitions.featureTypeEXON){
								INTRONCount++;
								header += "INTRON" + INTRONCount + " " + -1*(currentLine.getStartLocation() - 1 - geneStop) + "-" + -1*(utrAndExon.get(k+1).getStopLocation() + 1 - geneStop) + "|";
							}
						}//if
						
					}//if an exon
					
				}//utrAndExon for loop
			}//if negative
			
		}//if size >= 3
		
		
		return header;
	}//getHeader
	
	/**
	 * 
	 * DNACompliment takes in a DNA character(agtc or AGTC) and returns its upper case compliment (A<->T and G<->C)
	 * Source: http://answers.yahoo.com/question/index?qid=20090217022055AARubGS
	 * @param inputBase
	 * @return
	 */
	private char DNACompliment(char inputBase){
		char matchingBase = 'X';
		switch(inputBase)
		
		
		{
		case 'A':
		matchingBase = 'T';
		break;
		case 'a':
		matchingBase = 'T';
		break;
		case 'G':
		matchingBase = 'C';
		break;
		case 'g':
		matchingBase = 'C';
		break;
		case 'T':
		matchingBase = 'A';
		break;
		case 't':
		matchingBase = 'A';
		break;
		case 'C':
		matchingBase = 'G';
		break;
		case 'c':
		matchingBase = 'G';
		break;
		}
		
		return matchingBase;
	}//DNA Compliment
	
	
	
	/**
	 * Populates the ref chromosome array with the file locations of each chromosome.
	 * @param chrmDir The directory containing the chromosome files.
	 */
	private void populateChrmArrayList(String chrmDir){
		for(int i = 0; i < 22; i++){
			genomeFiles.add(chrmDir + "chr" + (i + 1) + ".fa");
		}
		genomeFiles.add(chrmDir + "chrM.fa");
		genomeFiles.add(chrmDir + "chrX.fa");
		genomeFiles.add(chrmDir + "chrY.fa");
	}//populateChrmArrayList
	
}//class
