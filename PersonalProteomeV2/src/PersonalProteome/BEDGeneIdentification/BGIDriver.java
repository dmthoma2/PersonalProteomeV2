package PersonalProteome.BEDGeneIdentification;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;



import PersonalProteome.U;

/**
 * Runs the Peptide Analysis Tool.  The only argument is a Properties file
 * @author David "Corvette" Thomas
 */
public class BGIDriver {

	
	//Look at maximum memory usage 
	static MemoryUsage memoryUsage;
	static long maxMemoryUsed = 0;
	/**
	 * 
	 * @author David "Corvette" Thomas
	 * @param args The first argument should be the file location of the properties file.
	 */
	public static void main(String[] args) {
		/* track memory */
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		memoryUsage = mbean.getHeapMemoryUsage();

		/*  hello! */
		printGreeting();
		
		if(args.length < 1){
			U.p("The properties file location must be the first parameter.");
			return;
		}
		/*Load properties*/

		BGIProperties.loadProperties(new File(args[0]));
		
		/*setup and do some work!*/

		U.p("Checking for peptide locations.");
		BEDGeneIdentification pepResults = new BEDGeneIdentification(BGIProperties.annotationFile, BGIProperties.peptideListFile, BGIProperties.outputDir);
		pepResults.checkPeptideLocation();
		
		
		
		/* i'm finished! */
		printFarewell();
	}//main
		
	public static void printGreeting() {		
		U.p("Peptide Analysis at the speed of a turbo-Vette!");
		U.p("max available memory: " + (double) memoryUsage.getMax() / (1024 * 1024 * 1024) + " gigabytes");
	}//printGreeting
	
	public static void printFarewell() {
		U.p("Until next time, signing off...");
	}//pritnFarewell
}//BGIDriver



