package PersonalProteome;



import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import Gene.CDS;
import Gene.Transcript;
import PersonalProteome.U;

public class TEST {

	
	public static void main(String[] args){
		

		
		try {
			Scanner s = new Scanner(new File("/Users/davidthomas/Desktop/Files used/Step3/HG19 based VCF Files/unMappedPaternal.txt"));
			
			StringBuffer sb = new StringBuffer();
			
			while(s.hasNextLine()){
				sb.append(s.nextLine());
			}
			
			int count = 0;
			for(int i = 0; i < sb.length(); i++){
				if(sb.charAt(i) == '#'){
					count++;
				}
			}
			
			U.p("Total number of # is " + count);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
//		U.p("WWWWW".indexOf("V"));
//		U.p("********* TESTING MESSED UP STOPS**************");
//		//Test for a normal transcript
//		Transcript t = new Transcript(0, "AAAATGAGATGATTTCGCTAA".trim(), 0 , 1337, 0, 20, true);
//		CDS c = new CDS(3, 8,  0);
//		t.addCDS(c);
//		U.p("Unit test starting");
//		//Verify stops and starts
//		if(!t.isAStop("TGA", Definitions.genomicStrandPOSITIVE)){
//			U.p("TGA WRONG");
//		}
//		if(!t.isAStop("TAG", Definitions.genomicStrandPOSITIVE)){
//			U.p("TAG WRONG");
//		}
//		if(!t.isAStop("TAA", Definitions.genomicStrandPOSITIVE)){
//			U.p("TAA WRONG");
//		}
//		
//		U.p("Stop should be found.");
//		t.createProtein();
//		U.p("New output should be: " + "MR");
//		U.p("                      " + t.getProtein());
//		
//		
//		//Test for a transcript with a start that is pushed back.
//		U.p("Creating a new protein");
//		Transcript t2 = new Transcript(0, "AAAATGAGATTTTTTCGCTAA", 0 , 1337, 0, 20, true);
//		CDS c2 = new CDS(3, 8, 0);
//		t2.addCDS(c2);
//		U.p("Stop should not be found 3 times");
//		t2.createProtein();
//		
//		U.p("New output should be: " + "MR+FFR");
//		U.p("                      " + t2.getProtein());
//		U.p("************* TESTING MESSED UP STARTS ******************");
//		//This one has a start pushed back 1 amino acid.  A & should be added to the start of its protein
//		Transcript t3 = new Transcript(0, "AAAAGAATGTGATTTCGCTAA", 0 , 1337, 0, 20, true);
//		CDS c3 = new CDS(3, 8, 0);
//		t3.addCDS(c3);
//		t3.createProtein();
//		U.p("Final protein should be: " + "&M");
//		U.p("                         " + t3.getProtein());
//		U.p("Finished");
//		
//		Transcript t4 = new Transcript(0, "AATCGCTTTTTACATAGAAAA", Definitions.genomicStrandNEGATIVE , 1337, 0, 20, true);
//		CDS c4 = new CDS(12, 17, 0);
//		t4.addCDS(c4);
//		t4.createProtein();
//		U.p("Final protein should be: " + "&M");
//		U.p("                         " + t4.getProtein());
//		U.p("Finished");
//		
//		Transcript t5 = new Transcript(0, "AATCGCTTTTTACCTAGAAAA", Definitions.genomicStrandNEGATIVE , 1337, 0, 20, true);
//		CDS c5 = new CDS(12, 17, 0);
//		t5.addCDS(c5);
//		t5.createProtein();
//		U.p("Final protein should be: " + "&&");
//		U.p("                         " + t5.getProtein());
//		U.p("Finished");
		
		
	}
	
}
