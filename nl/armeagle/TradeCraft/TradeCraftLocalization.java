package nl.armeagle.TradeCraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.util.config.Configuration;

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
    private static final String filePath = "plugins" + File.separator + TradeCraft.pluginName;

    private TradeCraft plugin;
    private String language;
    private static Configuration localization;

	public TradeCraftLocalization(TradeCraft plugin) {
    	this.plugin = plugin;
		this.language = plugin.properties.getLanguage();
		
		// make folder in the plugins dir if it doesn't exist yet
    	File path = new File(filePath);
    	if ( !path.exists() ) {
    		path.mkdirs();
    	}
    	path = null;
    	
    	// if file does not exist in this directory, copy it from the jar
    	String fileName = String.format(TradeCraftLocalization.filePreName, this.language);
    	File file = new File(filePath + File.separator + fileName);
    	if ( !file.exists() ) {
    		InputStream input = this.getClass().getResourceAsStream("/" + fileName);
    		
    		// If this file does not exist (not a default supported language), then revert back to the default language file.
    		// That file will exist in the jar for sure
			if ( input == null ) {
				this.plugin.log.info(fileName +" was not found in "+ TradeCraft.pluginName +".jar, using default language \""+ TradeCraftPropertiesFile.defaultLanguage +"\" instead");
				this.language = TradeCraftPropertiesFile.defaultLanguage;
				fileName = String.format(TradeCraftLocalization.filePreName, this.language);
				file = new File(filePath + File.separator + fileName);
		    	if ( !file.exists() ) {
		    		input = this.getClass().getResourceAsStream("/" + fileName);
		    	}
			}
			// if input is not null, then we need to copy the given file from the jar
			if ( input != null ) {
	    		this.plugin.log.info(filePath + File.separator + fileName +" does not exist, creating...");

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
    	
        TradeCraftLocalization.localization = new Configuration(file);
        TradeCraftLocalization.localization.load();
	}

	public static String get(String key) {
		return TradeCraftLocalization.localization.getString(key, "key error \""+ key +"\"");
	}
}
