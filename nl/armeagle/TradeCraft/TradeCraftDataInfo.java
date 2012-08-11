package nl.armeagle.TradeCraft;

class TradeCraftDataInfo {
    public String ownerName;
    public String worldName;
    public TradeCraftItem itemType;
    public int itemAmount;
    public int currencyAmount;
    
    public TradeCraftDataInfo() {
		this.ownerName = null;
		this.worldName = null;
		this.itemType = null;
		this.itemAmount = 0;
		this.currencyAmount = 0;
	}
}