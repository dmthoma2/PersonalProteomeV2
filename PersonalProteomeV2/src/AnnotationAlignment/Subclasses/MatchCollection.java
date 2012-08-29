package AnnotationAlignment.Subclasses;
import java.util.Arrays;

/**
 * MatchCollection is a object that stores a sorted list of Match objects based on their score.  It can be fed Match Objects and will store those objects in a list based on their score,
 * automatically sorting them.  It has a minimum number of objects to store (3), and by default stores (2 * (string size - safety margin)) objects.
 * 
 * @author David "Corvette" Thomas
 *
 */
public class MatchCollection {

	//Constant for a minimum size of the array
	private final int minimumSize = 3;
	
	//Array Related variables
	int arraySize = minimumSize;
	Match matches[];
	
	//The worst score in the array. Used to determine which matches to add to the array during the feed method.
	int worstScore;
	
	/**
	 * Match Collection takes in a safetyMargin (The amount of extra bases in a sequence) and a stringSize (the size of tokens to compare) and initializes the the entire array to
	 * default values.
	 * @param safetyMargin
	 * @param stringSize
	 */
	public MatchCollection(int safetyMargin, int stringSize){
		
		//Calculate the size of the array
		if(stringSize - safetyMargin > minimumSize){
			arraySize = stringSize  - safetyMargin;
		}
		
		//Double the number of scores to keep for better accuracy
		arraySize *= 2;
		
		//Fill the matches array with matches that have scores that are worse then possible to get through comparison.
		//Integer.MAX_VALUE is the highest possible score (And worse) that is possible to get.
		matches = new Match[arraySize];
		for(int i = 0; i < arraySize; i++){
			matches[i] = new Match("", -1, -1, Integer.MAX_VALUE);
		}
		
		
		//All of the matches have the same score, so just grab any of thems score to set it as the worst.
		worstScore = matches[0].getScore();
	}//MatchCollection
	
	
	
	
	/**
	 * @return Returns the matches array of this matchCollection.  This list is sorted based on the score of the matches.  Lower score = higher rank.
	 */
	public Match[] getMatchesArray(){
		return matches;
	}
	
	
	
	/**
	 * Feed takes in matches and puts them in the current matches list based on their rank.  It keeps the list sorted in O(nlog(n)) time where n is the size of the list. 
	 * @param m
	 */
	public void feed(Match m){
		
		//If a match is better then the worst item on the list, add it to the list in that position, and sore the lsit
		if(m.getScore() < worstScore){
			matches[arraySize -1] = m;
			
			//Sort the new match list
			Arrays.sort(matches);
			
			//Reset the new worst score
			worstScore = matches[arraySize - 1].getScore();
			
		}//if statement
		
	}//feed
	
	
	/**
	 * Contains returns true if the current matches list contains a item with the same start location as Match m.  This method
	 * can be used to ensure duplicate locations are not added to the matches array through the feed method.
	 * @param m
	 * @return
	 */
	public boolean contains(Match m){

		//Iterate through the matches array
		for(int i = 0; i < matches.length; i++){
			
			//If a match has the same location as one in the current array, mark it as true
			if(matches[i].getStart() == m.getStart()){
				return true;
			}//if
			
		}//for
			
		
		//Return the result of the comparisons.
		return false;
	}//contains
	
	
}//MatchCollection
