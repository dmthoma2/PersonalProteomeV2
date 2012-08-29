package ProteomeComparison.SubClasses;


/**
 * Translation Match stores two translation objects, and is a simple abstraction of a pair of translations with the same ID that come from different sources.
 * @author David "Corvette" Thomas
 *
 */
public class TranslationMatch {

	
	private Translation gencodeTrans = null;
	private Translation ppTrans = null;
	/**
	 * @return the gencodeTrans
	 */
	public Translation getGencodeTrans() {
		return gencodeTrans;
	}//getGencodeTrans
	/**
	 * @return the ppTrans
	 */
	public Translation getPpTrans() {
		return ppTrans;
	}//getPpTrans
	/**
	 * @param gencodeTrans the gencodeTrans to set
	 */
	public void setGencodeTrans(Translation gencodeTrans) {
		this.gencodeTrans = gencodeTrans;
	}//setGencodeTrans
	/**
	 * @param ppTrans the ppTrans to set
	 */
	public void setPpTrans(Translation ppTrans) {
		this.ppTrans = ppTrans;
	}//setPpTrans
	
	
}//TranslationMatch
