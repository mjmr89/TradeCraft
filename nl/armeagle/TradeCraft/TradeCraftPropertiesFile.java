package nl.armeagle.TradeCraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * This class, handles the actual configuration of the plugin. The TradeCraftConfiguration class
 * actually handles all the items that can be used by the shops in the game.
 */
public class TradeCraftPropertiesFile {
	private static final String fileName = TradeCraft.pluginName + ".properties";
    private static final String filePath = "plugins" + File.separator + TradeCraft.pluginName;
    public static final String defaultLanguage = "en";
    
    private TradeCraft plugin;
    private final YamlConfiguration properties;
    
    public TradeCraftPropertiesFile(TradeCraft plugin) {
    	this.plugin = plugin;
    	// make folder in the plugins dir if it doesn't exist yet
    	File path = new File(filePath);
    	if ( !path.exists() ) {
    		path.mkdirs();
    	}
    	path = null;
    	
    	// if file does not exist in this directory, copy it from the jar
    	File file = new File(filePath + File.separator + fileName); 
    	if ( !file.exists() ) {
    		this.plugin.log.info(filePath + File.separator + fileName +" does not exist, creating...");
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
    	
        properties = new YamlConfiguration();
        try {
        	properties.load(file);
        } catch (InvalidConfigurationException e) {
         	plugin.log(Level.SEVERE, "Failed to load file: %s", file.toURI());
        } catch (IOException e) {
         	plugin.log(Level.SEVERE, "Failed to read file: %s", file.toURI());
        }
    }
    
    protected void save() {
    	File file = new File(filePath + File.separator + fileName);
    	try {
    		properties.save(file);
    	} catch (IOException e) {
    		this.plugin.log(Level.SEVERE, "Error saving to file: %s", file.toURI());
    	}
    }
    
    public TradeCraftItem getCurrencyType(){
    	int id = properties.getInt("currency-id",266);
    	short data = (short)properties.getInt("currency-data",0);
    	return new TradeCraftItem(id, data); 
    }
    public void setCurrencyType(TradeCraftItem item) {
    	properties.set("currency-id", item.id);
    	properties.set("currency-data", item.data);
    	this.save();
    }
    public boolean getNormalStackSizeUsed(){
    	return properties.getBoolean("normal-stack-size", true);
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
    
    public String getLanguage() {
    	return properties.getString("language", TradeCraftPropertiesFile.defaultLanguage);
    }
    
    public boolean autoUpdateLanguageFiles() {
    	return properties.getBoolean("auto-update-language-files", true);
    }
    
    public boolean logShopUse() {
    	return properties.getBoolean("log-shop-use", false);
    }
    
    public boolean showShopLocation() {
    	return properties.getBoolean("show-shop-location", false);
    }
    
    public int getPlayerWorldShopLimit() {
        return properties.getInt("player-world-shop-limit", 5);
    }
    public int getPlayerTotalShopLimit() {
        return properties.getInt("player-total-shop-limit", 10);
    }
    
    public ChatColor getMessageTypeColor(TradeCraft.MessageTypes mtype) {
    	switch (mtype) {
    	case WITHDRAW:
    		return ChatColor.YELLOW;
    	case DEPOSIT:
    		return ChatColor.GRAY;
    	default:
    		return ChatColor.WHITE;
    	}
    }
}
