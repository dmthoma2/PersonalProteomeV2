package DiploidCarryOvertool;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import PersonalProteome.U;

public class driver {

	
	//Look at maximum memory usage 
	static MemoryUsage memoryUsage;
	static long maxMemoryUsed = 0;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/* track memory */
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		memoryUsage = mbean.getHeapMemoryUsage();
		
		/*Say hello*/
		printGreeting();
		
		args = new String[5];
		
		//Paternal genome dir
		args[0] = "/Users/davidthomas/Peppy/ProteomeV2/carryOverTool/input/pat_genome/";
//		args[0] = "/Users/davidthomas/Peppy/ProteomeV2/carryOverTool/input/mat_genome/";
		//Reference genome dir
		args[1] = "/Users/davidthomas/Peppy/ProteomeV2/carryOverTool/input/ref_genome/hg18/";
		//Paternal chain
		args[2] = "/Users/davidthomas/Peppy/ProteomeV2/carryOverTool/input/pat_chain/paternal.chain";
//		args[2] = "/Users/davidthomas/Peppy/ProteomeV2/carryOverTool/input/mat_chain/maternal.chain";
		//Paternal genome file name ending  chr# + file name ending
		args[3] = "_NA12878_paternal.fa";
//		args[3] = "_NA12878_maternal.fa";
		//Location of output file
		args[4] = "/Users/davidthomas/Peppy/ProteomeV2/carryOverTool/output/";
		
		carryOvertool cot = new carryOvertool(args[0], args[1], args[2], args[3], args[4]);
		
		cot.createGenomeAndVCF();
		
		/*Goodbye*/
		printFarewell();

	}
	
	
	
	public static void printGreeting() {		
		U.p("Chain file lift over at the speed of a turbo-Vette! Built with Personal Proteome v2.0");
		U.p("max available memory: " + (double) memoryUsage.getMax() / (1024 * 1024 * 1024) + " gigabytes");
	}
	
	public static void printFarewell() {
		U.p("Until next time, signing off...");
	}

}
