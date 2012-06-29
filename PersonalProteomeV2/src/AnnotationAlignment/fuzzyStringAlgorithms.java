package AnnotationAlignment;

/**
 * Fuzzy String Algorithms is a class that holds various implementations of fuzzy string matching, and is used with Annotation Alignment
 *  to make a best guess about where a matching string belongs in a sequence.
 *  
 * @author David "Corvette" Thomas
 *
 */
public class fuzzyStringAlgorithms {
	
	
	/**
	 * Proprietary function that determines how variant the strings are.  Returns the total number of locations at which the strings differ.  It assumes a's length is <= b's length
	 * 
	 * Ex.  ABCD ABBD score = 1
	 * Ex.  ABCD ACBD score = 2
	 * @param a First string to compare
	 * @param b Second string to compare
	 * @param limit  This is the upper limit of differences this method will allow.  If the number of differences inbetween these two strings becomes greater then the limit, the method returns -1;
	 * @return Number of variants between strings if at or below the limit, -1 if it exceeds the limit.
	 */
	public static int variantLimit(String a, String b, int limit){
		//Initially there are no variants
		int variants = 0;
		
		//Iterate through the strings and compare characters at each location.
		for(int i = 0; i < a.length(); i++){
			if(a.charAt(i) != b.charAt(i)){
				variants++;
				if(variants > limit){
					return -1;
				}
			}
		}
		
		
		return variants;
	}
	
	/**
	 * Proprietary function that determines how variant the strings are.  Returns the total number of locations at which the strings differ.
	 * 
	 * Ex.  ABCD ABBD score = 1
	 * Ex.  ABCD ACBD score = 2
	 * @param a First string to compare
	 * @param b Second string to compare
	 * @param limit  This is the upper limit of differences this method will allow.  If the number of differences inbetween these two strings becomes greater then the limit, the method returns -1;
	 * @return Number of variants between strings if at or below the limit, -1 if it exceeds the limit.
	 */
	public static int variant(String a, String b){
		int variants = 0;
		
		for(int i = 0; i < a.length(); i++){
			if(a.charAt(i) != b.charAt(i)){
				variants++;
			}
		}
		
		
		return variants;
	}

	/**
	 * Code from: http://www.merriampark.com/ld.htm
	 * 
	 * Normal Implementation of Levenshtein Distance.
	 * 
	 * @param s
	 * @param t
	 * @return
	 */
	  public static int levenshteinDistance (String s, String t) {
	  int d[][]; // matrix
	  int n; // length of s
	  int m; // length of t
	  int i; // iterates through s
	  int j; // iterates through t
	  char s_i; // ith character of s
	  char t_j; // jth character of t
	  int cost; // cost

	    // Step 1

	    n = s.length ();
	    m = t.length ();
	    if (n == 0) {
	      return m;
	    }
	    if (m == 0) {
	      return n;
	    }
	    d = new int[n+1][m+1];

	    // Step 2

	    for (i = 0; i <= n; i++) {
	      d[i][0] = i;
	    }

	    for (j = 0; j <= m; j++) {
	      d[0][j] = j;
	    }

	    // Step 3

	    for (i = 1; i <= n; i++) {

	      s_i = s.charAt (i - 1);

	      // Step 4

	      for (j = 1; j <= m; j++) {

	        t_j = t.charAt (j - 1);

	        // Step 5

	        if (s_i == t_j) {
	          cost = 0;
	        }
	        else {
	          cost = 1;
	        }

	        // Step 6

	        d[i][j] = Minimum (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);

	      }

	    }

	    // Step 7

	    return d[n][m];

	  }

	/**
	 * Returns the minimum of 3 values
	 */
	private static int Minimum (int a, int b, int c) {
	int mi;
	
	  mi = a;
	  if (b < mi) {
	    mi = b;
	  }
	  if (c < mi) {
	    mi = c;
	  }
	  return mi;
	
	}

	
}
