package PersonalProteome.BEDGeneIdentification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import PersonalProteome.U;


/**
 * Properties is a class that uploads and stores data about runtime parameters for BedGeneIdentification.  It loads its properties dynamically from a File using
 * the loadProperties method.  The entire class contains static methods and values and should not be instantiated.
 * @author David "Corvette" Thomas, modeled after code from Brian Risk
 *
 */
public class BGIProperties {

	
	//File/Directory Locations
	public static String annotationFile;
	public static String outputDir;
	public static String peptideListFile;
	
	
	//Null private constructor so this class can't be instantiated
	private BGIProperties(){
	}

	/**
	 * loadProperties Loads in the properties from the properties file and stores them in variables in the AnalysisProperties class.
	 * @param fileName the name of our properties file
	 */
	public static void loadProperties(File propertiesFile) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(propertiesFile));
			String line = br.readLine();
			while (line != null) {
				setPropertyFromString(line);
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			U.p("Could not find the properties file: " + propertiesFile.getName());
			U.p("Using default properties...");
		} catch (IOException e) {
			U.p("Could not read the properties file: " + propertiesFile.getName());
			e.printStackTrace();
		}
		
	}//loadProperties
	
	/**
	 * Each from the properties file is brought in and put in its variable.
	 * @param line A line from the properties file.
	 */
	private static void setPropertyFromString(String line) {
		line = line.trim();
		
		/* ignore blank lines */
		if (line.equals("")) return;
		
		/* ignore comments */
		if (line.startsWith("//")) return;
		if (line.startsWith("#")) return;
		
		/* ignore lines that do not have a space in them */
		if (line.indexOf(" ") == -1) return;
		
		/* getting the property name and the propert value */
		String propertyName = line.substring(0, line.indexOf(" "));
		String propertyValue = line.substring(line.indexOf(" ") + 1, line.length());
		
		//Files
		if (propertyName.equals("annotationFile")) 
			annotationFile = propertyValue.trim();
		if (propertyName.equals("outputDir")) 
			outputDir = propertyValue.trim();
		if(propertyName.equals("bedFile"))
			peptideListFile = propertyValue.trim();

	}//if
	
}//BGIProperties
