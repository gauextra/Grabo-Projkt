/**
 * 
 */

/**
 * @author kevad
 *
 */
public class ListItems {
	//"Apple", "Orange","Banana", "Watermelon", "Strawberry", "Grape", "Mango", "Kiwi", "Avacado"
	private Items[] obst = {new Items("Apple", 2.5), new Items("Obst", 2.0) }  ;
	private Items[] gemuese = {new Items("Gurke",2.1),new Items("Yams", 0.6)} ;
	private Items[] eis;
	private Items[] other;
	
	public Items[] getObstItems() {
		return obst;
	}
	public Items[] getGemueseItems() {
		return gemuese;
	}
	
	public Items[] getEisItems() {
		return eis;
	}
	
	public Items[] getOtherItems() {
		return other;
	}

	
	
}
