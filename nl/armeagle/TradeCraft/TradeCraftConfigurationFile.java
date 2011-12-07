package nl.armeagle.TradeCraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.MemorySection;

import nl.armeagle.Configuration.StatefulYamlConfiguration;

/**
 * The name of this class is a bit misleading. This class stores all the items and their default
 * trade rates that can be used in the game. The actual configuration of the plugin itself
 * is handled by the TradeCraftProperties class.
 */
class TradeCraftConfigurationFile {
    private static final String fileName = TradeCraft.pluginName + ".txt";
    private static final String configName = "items";

    private static final Pattern commentPattern = Pattern.compile("^\\s*#.*$");
    private static final Pattern infoPattern = Pattern.compile(
            "^\\s*([^,]+)\\s*," + // name
            "\\s*(\\d+(?:;\\d+)?)\\s*" + // id[;data] (optional ;data)
            "(?:,\\s*(\\d+)\\s*:\\s*(\\d+))?\\s*" + // buyAmount:buyValue
            "(?:,\\s*(\\d+)\\s*:\\s*(\\d+))?\\s*$"); // sellAmount:sellValue
    
    private HashMap<String, String> mapItemNames = new HashMap<String, String>();

    private final TradeCraft plugin;
    // create an index from an TradeCraftItem (id;data) to the TradeCraftConfigurationInfo entry. This for configured name lookup based on id;data  
    private final Map<TradeCraftItem, String> TCitemInfoIndex = new HashMap<TradeCraftItem, String>();
    
    TradeCraftConfigurationFile(TradeCraft plugin) {
        this.plugin = plugin;
    }
    
    public StatefulYamlConfiguration getConfig() {
    	return this.plugin.getConfig(TradeCraftConfigurationFile.configName);
    }

    void load() {
    	StatefulYamlConfiguration config = (StatefulYamlConfiguration) this.getConfig();
    	
    	// if file exists, load the config to it once and then rename the old config
    	File file = new File(plugin.getDataFolder().getAbsolutePath(), fileName);
    	if ( file.exists() ) {
	        try {
	        	FileReader reader = new FileReader(file);
	            BufferedReader configurationFile = new BufferedReader(reader);
	
	            int lineNumber = 0;
	            String line;
	
	            while ((line = configurationFile.readLine()) != null) {
	                lineNumber += 1;
	
	                if (line.trim().equals("")) {
	                    continue;
	                }
	
	                Matcher commentMatcher = commentPattern.matcher(line);
	
	                if (commentMatcher.matches()) {
	                    continue;
	                }
	
	                Matcher infoMatcher = infoPattern.matcher(line);
	
	                if (!infoMatcher.matches()) {
	                    plugin.log.warning(
	                            "Failed to parse line number " + lineNumber +
	                            " in " + file.getAbsolutePath() +
	                            ": " + line);
	                    continue;
	                }
	
	                TradeCraftConfigurationInfo info = new TradeCraftConfigurationInfo();
	                info.name = infoMatcher.group(1);
	
	                // try to split ID and Data, separated by a semicolon mark
	                Matcher IdSplitData = TradeCraft.itemPatternIdSplitData.matcher(infoMatcher.group(2));
	                
	                if (!IdSplitData.matches()) {
	                    plugin.log.info(
	                            "Failed to parse line number " + lineNumber +
	                            " in " + file.getAbsolutePath() +
	                            ": " + line);
	                    continue;
	                }
	                
	                int id = Integer.parseInt(IdSplitData.group(1));
	                if ( IdSplitData.group(2) != null ) {
	                	short data = Short.parseShort(IdSplitData.group(2));
	                	info.type = new TradeCraftItem(id, data);
	                } else {
	                	info.type = new TradeCraftItem(id);
	                }
	
	                if (infoMatcher.group(3) != null) {
	                    info.sellAmount = info.buyAmount = Integer.parseInt(infoMatcher.group(3));
	                    info.sellValue = info.buyValue = Integer.parseInt(infoMatcher.group(4));
	                }
	
	                if (infoMatcher.group(5) != null) {
	                    info.sellAmount = Integer.parseInt(infoMatcher.group(5));
	                    info.sellValue = Integer.parseInt(infoMatcher.group(6));
	                }

//	                config.set(info.name.toUpperCase(), info.toMemoryConfiguration());
	            	Iterator<Entry<String, Object>> iter = info.toMap().entrySet().iterator();
	            	while (iter.hasNext()) {
	            		Map.Entry<String, Object> pairs = (Map.Entry<String,Object>)iter.next();
	            		if (!pairs.getKey().equals("name")) {
	            			config.set(info.name +"."+ pairs.getKey(), pairs.getValue());
	            		}
	            	}
	            	
//	                TCitemInfoIndex.put(info.type, info.name);
	            }
	            configurationFile.close();
	            reader.close();
	            config.save();
	            if (file.renameTo(new File(file.getAbsolutePath() + ".converted.to."+ TradeCraftConfigurationFile.configName +".yml"))) {
	            	plugin.log.info("Converted old config to new style and renamed the old config file");
	            } else {
	            	plugin.log.severe("FAILED to convert old config to new style");
	            }
	
	        } catch (IOException e) {
	            plugin.log.severe("Error reading " + file.getAbsolutePath());
	        }
    	} else {
    		try {
				config.load(true);
			} catch (IOException e) {
				plugin.log.severe("Error loading plugin config file");
			}
    	}

    	Iterator<Entry<String, Object>> iter = config.getValues(false).entrySet().iterator();
		while (iter.hasNext()) {
	    	// store map of lowercase item names to key names in the configuration
    		Map.Entry<String, Object> entry = (Map.Entry<String,Object>)iter.next();
			this.mapItemNames.put(entry.getKey().toLowerCase(), entry.getKey());
			// store map of item types to key names
			MemorySection section = (MemorySection) entry.getValue();
    		TradeCraftItem tcItem = new TradeCraftItem(section.getInt("itemTypeId", 266), section.getInt("itemTypeData", 0));
    		TCitemInfoIndex.put(tcItem, entry.getKey());
		}
    }

    public String[] getNames() {
        String[] names = this.getConfig().getKeys(false).toArray(new String[0]);
        Arrays.sort(names);
        return names;
    }

    public boolean isConfigured(String name) {
    	return this.mapItemNames.containsKey(name.toLowerCase());
    }

    public TradeCraftConfigurationInfo get(String name) {
    	// @todo, config code doesn't seem to like serialized objects, cannot seem to find the TradeCraftConfigurationInfo class
    	// though, the otherwise resulting ==: classpath lines aren't very user friendly anyway.
//    	return (TradeCraftConfigurationInfo) plugin.getConfig().get(name.toUpperCase());
    	String itemName = this.mapItemNames.get(name.toLowerCase());
    	if (null == itemName) {
    		return null;
    	} else {
    		return new TradeCraftConfigurationInfo(((MemorySection)this.getConfig().get(itemName)).getValues(false), name);
    	}
    }
    public TradeCraftConfigurationInfo get(int id) {
    	return this.get(new TradeCraftItem(id));
    }
    public TradeCraftConfigurationInfo get(int id, short data) {
    	return this.get(new TradeCraftItem(id, data));
    }
    public TradeCraftConfigurationInfo get(TradeCraftItem item) {
        return this.get(TCitemInfoIndex.get(item));
    }
}