package Gene;


/**
 * VariantPoint represents a single point in a proprietary output file for personal proteome.  This file
 * consists of five columns: Protein Name, Total number of variants in that protein, Perfecnt of protein that is variant,
 * Number of variants squared / length, and the number of nonsense acids in this protein.
 * 
 * 
 * 
 * ? nonsesne - a SNP that causes a non stop codon to turn into a stop codon thereby shortening the protein so it cannot perform its
 * intended function (eg. creating a nonsense protein).
 * 
 * 
 * @author David "Corvette" Thomas
 *
 */
public class VariantPoint implements Comparable<VariantPoint> {

	

	private String Name;
	private int count;
	private int total;
	private int nonsenseCount;
	
	
	/** 
	 * @param name Name of the protein this Variant Point represents.
	 * @param variantCount The number of variants this protein has from the reference protein. 
	 * @param length The length of this protein.
	 * @param nonsenseCount The number of nonsense codons formed in this protein because of SNPs.
	 */
	public VariantPoint(String name, int variantCount, int length, int nonsenseCount) {
		super();
		Name = name;
		this.count = variantCount;
		this.total = length;
		this.nonsenseCount = nonsenseCount;
	}//VariantPoint
	
	/**
	 * @return A string represnting this object.  It contains the 5 tab-delimited columns of this object.
	 */
	public String toString(){
		String tab = "\t";
		return Name + tab + count + tab + (getPercentVariant() * 100)  + "%" + tab + getVariantSquaredOverLength() + tab + nonsenseCount; 
	}//toString
	
	public int getNonsenseCount(){
		return nonsenseCount;
	}//getNonsenseCount
	
	public int getVariantCount(){
		return count;
	}//getVariantCount
	
	public double getPercentVariant(){
		return ((double) count) / ((double) total);
	}//getPercentVariant
	
	public double getVariantSquaredOverLength(){
		double out = 0;
		
		out = count * count;
		out = out/total;
		
		return out;
	}//getVariantSquaredOverLength


	@Override
	/**
	 * ---Larger values come before lower values.---
	 * 
	 * This overridden method compares variant points first on getVariantSquaredOverLength value, then name.
	 */
	public int compareTo(VariantPoint o) {
		if(this.getVariantSquaredOverLength() > o.getVariantSquaredOverLength()){
			return -1;
		}else if(this.getVariantSquaredOverLength() < o.getVariantSquaredOverLength()){
			return 1;
		}
		
		if(this.Name.compareTo(o.Name) < 0){
			return -1;
		}else if(this.Name.compareTo(o.Name) > 0){
			return 1;
		}
		return 0;
	}//compareTo
	
}//VariantPoint
