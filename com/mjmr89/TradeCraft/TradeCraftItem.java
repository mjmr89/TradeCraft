package com.mjmr89.TradeCraft;

/**
 * 
 * @author ArmEagle
 * Store item type(ID) and optionally the data bit
 */
public class TradeCraftItem implements Comparable<TradeCraftItem> {
	public int id;
	public short data;
	
	TradeCraftItem(int id) {
		this(id, (short)0);
	}
	TradeCraftItem(int id, short data) {
		this.id = id;
		this.data = data;
	}
	
	public String toString() {
		return "TradeCraftItem("+ this.id +";"+ this.data +")";
	}
	
	public int compareTo(TradeCraftItem compare) {
		if ( this.id < compare.id ) {
			return -1;
		} else if ( this.id > compare.id ) {
			return 1;
		} else {
			if ( this.data < compare.data ) {
				return -1;
			} else if ( this.data > compare.data ) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}
