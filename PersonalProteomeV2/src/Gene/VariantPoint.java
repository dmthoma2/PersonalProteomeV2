package Gene;

public class VariantPoint implements Comparable<VariantPoint> {

	

	private String Name;
	private int count;
	private int total;
	private int nonsenseCount;
	
	
	/**
	 * @param name
	 * @param count
	 * @param total
	 */
	public VariantPoint(String name, int variantCount, int length, int nonsenseCount) {
		super();
		Name = name;
		this.count = variantCount;
		this.total = length;
		this.nonsenseCount = nonsenseCount;
	}
	
	
	public String toString(){
		String tab = "\t";
//		 DecimalFormat df = new DecimalFormat("#.##");
		return Name + tab + count + tab + (getPercentVariant() * 100)  + "%" + tab + getVariantSquaredOverLength() + tab + nonsenseCount; 
	}
	
	public int getNonsenseCount(){
		return nonsenseCount;
	}
	
	public int getVariantCount(){
		return count;
	}
	
	public double getPercentVariant(){
		return ((double) count) / ((double) total);
	}
	
	public double getVariantSquaredOverLength(){
		double out = 0;
		
		out = count * count;
		out = out/total;
		
		return out;
	}


	@Override
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
