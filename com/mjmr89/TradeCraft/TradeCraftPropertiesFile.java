package com.mjmr89.TradeCraft;

import java.io.File;
import java.io.IOException;

import org.bukkit.util.config.Configuration;

public class TradeCraftPropertiesFile {

	private File f = new File(TradeCraft.pluginName + ".properties");
    private final Configuration properties;

    public TradeCraftPropertiesFile() {
    	
    	if(!f.exists()){
			try {
				f.createNewFile();
//				populate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
        properties = new Configuration(f);
    }

    public void populate(){
    	properties.setProperty("infinite-shops-enabled",true);
    	
    	properties.save();
    }
    
    public int getCurrencyTypeId(){
    	return properties.getInt("currency-id",266);
    }
    
    public boolean getInfiniteShopsEnabled() {
        return properties.getBoolean("infinite-shops-enabled", true);
    }

    public boolean getPlayerOwnedShopsEnabled() {
        return properties.getBoolean("player-owned-shops-enabled", true);
    }

    public boolean getRepairShopsEnabled() {
        return properties.getBoolean("repair-shops-enabled", false);
    }

    public int getRepairCost() {
        return properties.getInt("repair-cost", 0);
    }

    public boolean getEnableDebugMessages() {
        return properties.getBoolean("enable-debug-messages", false);
    }
}
