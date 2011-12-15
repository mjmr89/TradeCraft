package nl.armeagle.TradeCraft;

import java.io.IOException;
import java.util.logging.Level;
import nl.armeagle.Configuration.StatefulYamlConfiguration;

/**
 * 
 * @author ArmEagle
 * Localization support.
 *  
 * You can put different language files in the plugins/TradeCraft/ folder, similar to the default TradeCraft.en.lang.
 * There is a "language" option in the TradeCraft.properties file (default: "en") with which you can then select
 * such other language file to be used.
 */
public class TradeCraftLocalization {
	private static final String filePreName = TradeCraft.pluginName + ".%1$s.lang";
    private static StatefulYamlConfiguration localization;

	public TradeCraftLocalization(TradeCraft plugin) {
    	String filename = String.format(TradeCraftLocalization.filePreName, TradeCraft.properties.getLanguage());
        TradeCraftLocalization.localization = plugin.getConfig(filename);

        try {
        	TradeCraftLocalization.localization.load(true);
        } catch (IOException e) {
        	plugin.log(Level.SEVERE, "Failed to read file: %s", filename);
        }

        String defaultFilename = String.format(TradeCraftLocalization.filePreName, "en");
        if (TradeCraftLocalization.localization.getKeys(false).isEmpty()) {
        	plugin.log(Level.INFO, "Language file %s does not exist or is empty, defaulting to %s", filename, defaultFilename);
        } else {
        	return;
        }
        
        TradeCraftLocalization.localization = plugin.getConfig(defaultFilename);
        try {
        	TradeCraftLocalization.localization.load(true);
        } catch (IOException e) {
        	plugin.log(Level.SEVERE, "Failed to read file: %s", defaultFilename);
        }

        if (TradeCraftLocalization.localization.getKeys(false).isEmpty()) {
        	plugin.log(Level.SEVERE, "Default language file %s is also empty", defaultFilename);
        }
	}

	public static String get(String key) {
		return TradeCraftLocalization.localization.getString(key, "key error \""+ key +"\"");
	}
}
