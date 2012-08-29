package PersonalProteome;


import java.awt.Color;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * A basic utility class.  This class is contains all static methods and should not be instantiated.
 * Saves a lot of time not writing out "System.out.println".
 * Javadoc comments written by David "Corvette" Thomas".
 * 
 * @author Brian Risk
 *
 */
public class U {
	public static final long SECOND = 1000;
	public static final long MINUTE = SECOND * 60;
	public static final long HOUR = MINUTE * 60;
	public static final long DAY = HOUR * 24;
	public static final long YEAR = (long) (DAY * 365.25);
	
	private static long startTimeMilliseconds;
	private static long stopTimeMilliseconds;
	
	
	/**
	 * This class should not and can not be instantiated.  It should be used for static methods only.
	 */
	private U(){
		//Private blank constructor keeps this class from being instantiated
	}//U
	
	/**
	 * startStopWatch estabished the current time in this static object.  It uses this time for each stopStopwatch method call moving
	 * forward until startStopwatch is called again.
	 */
	public static void startStopwatch() {
		startTimeMilliseconds = System.currentTimeMillis();
	}//startStopWatch
	
	/**
	 * stopStopwatch outputs to System.out a string representing in humanreadable format the amount of time that has passed since the most recent
	 * startStopwatch call.
	 * @return A long value of how many milliseconds that have elapsed between this call and the most recent startStopwatch call.
	 */
	public static long stopStopwatch() {
		stopTimeMilliseconds = System.currentTimeMillis();
		long timeElapsed = stopTimeMilliseconds - startTimeMilliseconds;
		U.p("Time Elapsed: " + U.millisecondsToString(timeElapsed));
		return timeElapsed;
	}//stopStopwatch
	
	
	/**
	 * printTimeReamining prints out an approximation of time remaining based on a percentage of work completed based on the amountComplete variable.
	 *  This method assumes the start point is the most recent startStopwatch call.
	 *  
	 *  For example:  If 2 minutes have passed and printTimeRemaining is passed a value of .66, it would print that there is 1 minute remaining.
	 * @param amountComplete A percentage of time remaining.
	 */
	public static void printTimeRemaining(double amountComplete) {
		double elapsed = System.currentTimeMillis() - startTimeMilliseconds;
		long timeRemaining = (long) ((elapsed / amountComplete) - elapsed);
		U.p("Time Remaining: " + U.millisecondsToString(timeRemaining));
	}//printTimeRemaining
	
	
	/**
	 * Save is a utility method that writes the output string to a file specified by fileName.
	 * @param fileName  The file to write the output string to.
	 * @param output The string to be saved to disk.
	 */
	public static void save(String fileName, String output) {
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			pw.print(output);
			pw.flush();
			pw.close();
		}catch (IOException ioe) {
			p("could not save file: " + fileName);
			ioe.printStackTrace();
		}
	}//save
	
	
	/**
	 * readFileToString simply reads the contents of a file from the disk, and returns a string containing the text within a file.
	 * @param fileName A string with the path to the file to read from.
	 * @return A string containing the contents of the file specified by fileName.
	 */
	public static String readFileToString(String fileName) {
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}//readFileToString
	
	
	/**
	 * printUserDirectory prints the user directory to the standard output.
	 */
	public static void printUserDirectory() {
		String tmp=System.getProperty("user.dir");
	    U.p(tmp);
	}//priintUserDirectory
	
	
	/**
	 * millisecondsToString takes in a long value representing milliseconds of time, and it converts them into human readable values (Minutes, Seconds, Hours, Days, Years, etc).
	 * This method is used by the stopStopwatch method for creating human readable format.
	 * @param timeElapsed 
	 * @return
	 */
	public static String millisecondsToString(long timeElapsed) {
		String response = "";
		long amount;
		if (timeElapsed >= YEAR) {
			amount = timeElapsed / YEAR;
			response += amount + " year";
			if (amount != 1) response += "s";
			response += ", ";
			timeElapsed -= YEAR * amount;
		}
		if (timeElapsed >= DAY) {
			amount = timeElapsed / DAY;
			response += amount + " day";
			if (amount != 1) response += "s";
			response += ", ";
			timeElapsed -= DAY * amount;
		}
		if (timeElapsed >= HOUR) {
			amount = timeElapsed / HOUR;
			response += amount + " hour";
			if (amount != 1) response += "s";
			response += ", ";
			timeElapsed -= HOUR * amount;
		}
		if (timeElapsed >= MINUTE) {
			amount = timeElapsed / MINUTE;
			response += amount + " minute";
			if (amount != 1) response += "s";
			response += ", ";
			timeElapsed -= MINUTE * amount;
		}
		if (timeElapsed >= SECOND) {
			amount = timeElapsed / SECOND;
			response += amount + " second";
			if (amount != 1) response += "s";
			timeElapsed -= SECOND * amount;
		}
		return response;
	}//millisecondsToString
	
	/*Print line methods*/
	public static void p(Object o) {System.out.println(o);}
	public static void p(double o) {System.out.println(o);}
	public static void p(int o) {System.out.println(o);}
	public static void p(char o) {System.out.println(o);}
	public static void p() {System.out.println();}
	/*End print line methods*/
	
	
	/**
	 * in is a method that reads input from the System.in and returns what text it reads as a String
	 */
	public static String in() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String out = "";
		try {
			out = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}//int
	
	
	/**
	 * beep sounds the systems system alert sound, or a hardware beep from the motherboard if no soundcard is attached.
	 */
	public static void beep() {Toolkit.getDefaultToolkit().beep();}
	
	
	/**
	 * copyFile uses an InputStream and an OutputStream to transfer the information from a source file to a destination file.  It transfers the file in 1 kilobyte buffers.
	 * @param sourceFile  The source file to get information from.
	 * @param destinationFile  The location to transfer information to.
	 */
	public static void copyfile(File sourceFile, File destinationFile){
	    try{
	      InputStream in = new FileInputStream(sourceFile);
	      OutputStream out = new FileOutputStream(destinationFile);

	      byte[] buffer = new byte[1024];
	      int len;
	      while ((len = in.read(buffer)) > 0){
	        out.write(buffer, 0, len);
	      }
	      in.close();
	      out.close();
	    }
	    catch(FileNotFoundException ex){
	      System.out.println(ex.getMessage() + " in the specified directory.");
	      System.exit(0);
	    }
	    catch(IOException e){
	      System.out.println(e.getMessage());      
	    }
	 }//copyFile
	
	
	/**
	 * A simple automation of a the mathematical log function.
	 * @param base  The base of this logarithmic function.
	 * @param of The value of logarithmic function.
	 * @return The calculated result of the log function specified by base and of.
	 */
	public static double log(double base, double of) {
		return Math.log(of) / Math.log(base);
	}//log
	
	
	/**
	 * getFileNameWithoutSuffix simply returns a file without its extension.
	 * @param file The File to get its name from.
	 * @return The file without the name suffix
	 */
	public static String getFileNameWithoutSuffix(File file) {
        return file.getName().substring(0, file.getName().lastIndexOf('.'));
	}//getFileNameWIthoutSuffix
	
	/**
	 * reverseString uses a fast method to reverse the contents of a input string.
	 * @param in String to reverse.
	 * @return A string containing the reversed contents to the input string.
	 */
	public static String reverseString(String in) {
		return new StringBuffer(in).reverse().toString();
	}//reverseString
	
	
	/**
	 * getRGBStringFromPercent converts a percentage to a hex based RGB string
	 * @param A percent representing which color to return.
	 * @return A string containing hexadecimal color.
	 */
	public static String getRGBStringFromPercent(double percent) {
		Color hsb = Color.getHSBColor((float) percent, 1.0f, 1.0f);
		String rgb = Integer.toHexString(hsb.getRGB());
		rgb = rgb.substring(2, rgb.length());
		return rgb;
	}//getRGBStringFromPercent

}//U
