package nl.armeagle.TradeCraft;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("TradeCraftConfiguration")
class TradeCraftConfigurationInfo implements ConfigurationSerializable {
	public String name;
    public TradeCraftItem type;
    public int buyAmount;
    public int buyValue;
    public int sellAmount;
    public int sellValue;
    
    TradeCraftConfigurationInfo() {
    }

    TradeCraftConfigurationInfo(Map<String, Object> map, String name) {
    	this.name = name;
    	this.type = new TradeCraftItem((Integer) map.get("itemTypeId"), (Integer) map.get("itemTypeData"));
    	this.buyAmount = (Integer) map.get("buyAmount");
    	this.buyValue = (Integer) map.get("buyValue");
    	this.sellAmount = (Integer) map.get("sellAmount");
    	this.sellValue = (Integer) map.get("sellValue");
    }
    TradeCraftConfigurationInfo(Map<String, Object> map) {
    	this.name = (String) map.get("name");
    	this.type = new TradeCraftItem((Integer) map.get("itemTypeId"), (Integer) map.get("itemTypeData"));
    	this.buyAmount = (Integer) map.get("buyAmount");
    	this.buyValue = (Integer) map.get("buyValue");
    	this.sellAmount = (Integer) map.get("sellAmount");
    	this.sellValue = (Integer) map.get("sellValue");
    }

    public Map<String, Object> toMap() {
    	LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
    	map.put("name", this.name);
    	map.put("itemTypeId", this.type.id);
    	map.put("itemTypeData", this.type.data);
    	map.put("buyAmount", this.buyAmount);
    	map.put("buyValue", this.buyValue);
    	map.put("sellAmount", this.sellAmount);
    	map.put("sellValue", this.sellValue);
		return map;
    }
    @Override
	public Map<String, Object> serialize() {
    	return this.toMap();
	}
}