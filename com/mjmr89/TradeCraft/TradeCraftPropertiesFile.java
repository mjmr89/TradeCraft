package com.mjmr89.TradeCraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.util.config.Configuration;

public class TradeCraftPropertiesFile {
	private static final String fileName = TradeCraft.pluginName + ".properties";
    private static final String filePath = "plugins" + File.separator + TradeCraft.pluginName;

    private final Configuration properties;

    public TradeCraftPropertiesFile() {
    	// make folder in the plugins dir if it doesn't exist yet
    	File path = new File(filePath);
    	if ( !path.exists() ) {
    		path.mkdirs();
    	}
    	path = null;
    	
    	// if file does not exist in this directory, copy it from the jar
    	File file = new File(filePath + File.separator + fileName); 
    	if ( !file.exists() ) {
    		InputStream input = this.getClass().getResourceAsStream("/" + fileName);
			if ( input != null ) {
				FileOutputStream output = null;

	            try {
	                output = new FileOutputStream(file);
	                byte[] buf = new byte[8192];
	                int length = 0;
	                while ((length = input.read(buf)) > 0) {
	                    output.write(buf, 0, length);
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            } finally {
	                try {
	                    if (input != null) {
	                        input.close();
	                    }
	                } catch (IOException e) {}

	                try {
	                    if (output != null) {
	                        output.close();
	                    }
	                } catch (IOException e) {}
	            }
			}
    	}
    	
        properties = new Configuration(file);
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
