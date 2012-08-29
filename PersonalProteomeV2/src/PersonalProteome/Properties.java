package PersonalProteome;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import PersonalProteome.U;


/**
 * Properties is a class that uploads and stores data about runtime parameters for personal proteome.  It loads its properties dynamically from a File using
 * the loadProperties method.  The entire class contains static methods and values and should not be instantiated.
 * @author David "Corvette" Thomas, modeled after code from Brian Risk
 *
 */
public class Properties {

	
	//Null private constructor so this class can't be instantiated
	private Properties(){
	}
	
	//File/Directory Locations
	public static String annotationFile = "No annotation file set!";
	public static String refGenomeDir = "No reference gonome directory set!";
	public static String genomeDir = "No gonome directory set!";
	public static String outputDir = "output/";
	public static String regionsOfInterestFile = "noregions";
	public static String outputFileName = "Genome.txt";
	public static String statsOutputFile = "Statistics.txt";
	public static String dataPointsFileName = "DataPoints.txt";
	public static String variatnDataFileName = "VariantData.txt";
	
	//Runtime parameters
	//These values are all set to default to false
	public static boolean debug = false;
	public static boolean useModifiedStopsAndStarts = false;
	public static boolean preFilterOutTranWithoutStartCodon = false;
	public static boolean proteomeLite = false;
	
	/**
	 * loadProperties parses a input file and uploads all data it can from the file.
	 * @param fileName the name of our properties file
	 */
	public static void loadProperties(File propertiesFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(propertiesFile));
			String line = br.readLine();
			
			while (line != null) {
				setPropertyFromString(line);
				line = br.readLine();
			}//while
			
			br.close();
		} catch (FileNotFoundException e) {
			U.p("Could not find the properties file: " + propertiesFile.getName());
			U.p("Using default properties...");
		} catch (IOException e) {
			U.p("Could not read the properties file: " + propertiesFile.getName());
			e.printStackTrace();
		}//catch
		
	}//loadProperties
	
	
	/**
	 * setPropertiyFromString is a helper method that populates the various properties in this file based on lines read
	 * from the input file.  It is called repeatedly from the loadProperties method.
	 */
	private static void setPropertyFromString(String line) {
		line = line.trim();
		
		/* ignore blank lines */
		if (line.equals("")) return;
		
		/* ignore comments */
		if (line.startsWith("//")) return;
		if (line.startsWith("##")) return;
		
		/* ignore lines that do not have a space in them */
		if (line.indexOf(" ") == -1) return;
		
		/* getting the property name and the property value */
		String propertyName = line.substring(0, line.indexOf(" "));
		String propertyValue = line.substring(line.indexOf(" ") + 1, line.length());
		
		//Files
		if (propertyName.equals("annotationFile")) 
			annotationFile = propertyValue.trim();
		if (propertyName.equals("refGenomeDir")) 
			refGenomeDir = propertyValue.trim();
		if (propertyName.equals("genomeDir")) 
			genomeDir = propertyValue.trim();
		if (propertyName.equals("outputDir")) 
			outputDir = propertyValue.trim();
		if (propertyName.equals("regionsOfInterestFile"))
			regionsOfInterestFile = propertyValue.trim();
		if (propertyName.equals("outputFileName")) 
			outputFileName = propertyValue.trim();
		if (propertyName.equals("statsOutputFile")) 
			statsOutputFile = propertyValue.trim();
		if (propertyName.equals("dataPointsFileName"))
			dataPointsFileName = propertyValue.trim();
		if (propertyName.equals("variatnDataFileName"))
			variatnDataFileName = propertyValue.trim();
		
		//runtime parameters
		if (propertyName.equals("detailedRunInfo"))
			debug = Boolean.valueOf(propertyValue.trim());
		if (propertyName.equals("useModifiedStopsAndStarts"))
			useModifiedStopsAndStarts = Boolean.valueOf(propertyValue.trim());
		if (propertyName.equals("preFilterOutTranWithoutStartCodon"))
			preFilterOutTranWithoutStartCodon = Boolean.valueOf(propertyValue.trim());
		if (propertyName.equals("proteomeLite"))
			proteomeLite = Boolean.valueOf(propertyValue.trim());
		
	}//setPropertyFromString
}//Properties
