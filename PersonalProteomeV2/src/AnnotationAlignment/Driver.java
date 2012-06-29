package AnnotationAlignment;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.NumberFormat;
import java.util.ArrayList;

import AnnotationAlignment.Subclasses.AA_Line_Container;
import PersonalProteome.U;
/**
 * Driver for Annotation Alignment
 * @author David "Corvette" Thomas
 *
 *
 */
public class Driver {
	
	//Look at maximum memory usage 
	static MemoryUsage memoryUsage;
	static long maxMemoryUsed = 0;
	
		public static void main(String[] args){
			/* track memory */
			MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
			memoryUsage = mbean.getHeapMemoryUsage();

			/*  hello! */
			printGreeting();
			
			args = new String[5];
//			args[0] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/annotation/DNATest.gtf";
//			args[0] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/annotation/gencode.v11.chrm1.gtf";
//			args[0] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/annotation/gencode.v11.chrm21.gtf";
//			args[0] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/annotation/gencode.v11.chrmM.gtf";
			
//			args[1] = "/Users/davidthomas/Peppy/ProteomeV2/AA/output";
//			args[2] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/DNA/mutatantStrand.fa";
//			args[2] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/DNA/10Percentchr1.fa";
//			args[2] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/DNA/5Percentchr21.fa";
//			args[2] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/DNA/10PercentchrM.fa";
			
			
//			args[3] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/refDNA/referenceStrand.fa";
//			args[3] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/refDNA/referencechr1.fa";
//			args[3] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/refDNA/referencechr21.fa";
			
//			args[3] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/refDNA/referencechrM.fa";
//			args[4] = "false";
			
			/*Work time!*/
//			AnnotationAlignment aa = new AnnotationAlignment(args[0], args[1], args[2], args[3], Boolean.valueOf(args[4]), "Corvette");
			
//			aa.alignDNA();
//			(String inputAnnotation, String outputDirectory, String referenceDNAGenome, String DNAGenome, boolean proteinOnly){
			
			args[0] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/annotation/gencode.v11.annotation.gtf";
//			args[0] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/annotation/gencode.v11.chrm15+21.gtf";
//			args[0] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/annotation/gencode.v11.chrm21.gtf";
			args[1] = "/Users/davidthomas/Peppy/ProteomeV2/AA/output/";
			args[2] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/referenceGenome/HG19/";
			args[3] = "/Users/davidthomas/Peppy/ProteomeV2/AA/input/genome/WHIM2_xeno_and_germline/";
			args[4] = "false";
			AAManager aam = new AAManager(args[0], args[1], args[2], args[3], Boolean.valueOf(args[4]));
			
			ArrayList<AA_Line_Container> out = aam.getModifiedAnnotation();
			aam.createOutput(out);
			
			/* i'm finished! */
			printFarewell();
		}//Main
		
		private static double maxMem = 0;
		/**
		 * @return Returns the maximum used memory value in gigabytes of any displayMem calls up this the time of this methods invocation.
		 */
		public static double getMaxMem(){
			return maxMem / (1024 * 1024 * 1024);
		}
		/**
		 * returns the maximum memory used so far
		 * from http://stackoverflow.com/questions/74674/how-to-do-i-check-cpu-and-memory-usage-in-java
		 */
		public static void displayMem(){
			
			
			Runtime runtime = Runtime.getRuntime();

		    NumberFormat format = NumberFormat.getInstance();

//		    StringBuilder sb = new StringBuilder();
		    long maxMemory = runtime.maxMemory();
		    long allocatedMemory = runtime.totalMemory();
		    long freeMemory = runtime.freeMemory();

//		    U.p("free memory: " + format.format(freeMemory / (1024 * 1024 * 1024)) );
//		    U.p("allocated memory: " + format.format(allocatedMemory/ (1024 * 1024 * 1024)));
//		    U.p("max memory: " + format.format(maxMemory/ (1024 * 1024 * 1024)));
		    U.p("Total memory usage is: " + format.format(((double)(allocatedMemory - freeMemory))/ (1024 * 1024 * 1024)) + " GB" + " out of " + format.format(((double)maxMemory)/ (1024 * 1024 * 1024)) + " GB");
		    //Total free memroyformat.format((freeMemory + (maxMemory - allocatedMemory))/ (1024 * 1024 * 1024))
		    
		    //Set the max memory
		    if((allocatedMemory - freeMemory) > maxMem){
		    	maxMem = allocatedMemory - freeMemory;
		    }
		    
	
		}
		
		
		public static void printGreeting() {		
			U.p("Annotation Alignment at the speed of a turbo-Vette! Built with Personal Proteome v2.0");
			U.p("max available memory: " + (double) memoryUsage.getMax() / (1024 * 1024 * 1024) + " gigabytes");
		}
		
		public static void printFarewell() {
			U.p("Until next time, signing off...");
		}
}
