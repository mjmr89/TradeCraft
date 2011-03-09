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
				populate();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
        properties = new Configuration(f);
    }

    public void populate(){
    	properties.setProperty("infinite-shops-enabled",true);
    	
//    	properties.save();
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

    public String getGroupRequiredToCreateInfiniteShops() {
        return properties.getString("group-required-to-create-infinite-shops", "*");
    }

    public String getGroupRequiredToCreatePlayerOwnedShops() {
        return properties.getString("group-required-to-create-player-owned-shops", "*");
    }

    public String getGroupRequiredToCreateRepairShops() {
        return properties.getString("group-required-to-create-repair-shops", "*");
    }

    public String getGroupRequiredToBuyFromShops() {
        return properties.getString("group-required-to-buy-from-shops", "*");
    }

    public String getGroupRequiredToSellToShops() {
        return properties.getString("group-required-to-sell-to-shops", "*");
    }

    public String getGroupRequiredToUseRepairShops() {
        return properties.getString("group-required-to-use-repair-shops", "*");
    }

    public int getRepairCost() {
        return properties.getInt("repair-cost", 0);
    }

    public boolean getEnableDebugMessages() {
        return properties.getBoolean("enable-debug-messages", false);
    }
}
