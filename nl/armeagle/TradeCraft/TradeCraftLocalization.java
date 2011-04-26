package nl.armeagle.TradeCraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

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
    	
    	// If file does not exist in this directory, copy it from the jar.
    	// Or if the file in the jar was updated and the setting allows for automatic updating, then do so.
    	String fileName = String.format(TradeCraftLocalization.filePreName, this.language);
    	
    	File file = new File(filePath + File.separator + fileName);
    	
    	if ( !file.exists()
    		 || this.plugin.properties.autoUpdateLanguageFiles()
    		 && TradeCraft.resourceLastModified("/"+ fileName) > file.lastModified() ) {
    		
    		InputStream input = this.getClass().getResourceAsStream("/" + fileName);
    		// If this file does not exist (not a default supported language), then revert back to the default language file.
    		// That file will exist in the jar for sure
			if ( input == null ) {
				this.plugin.log(Level.INFO, "%1$s was not found in %2$s.jar, using default language \"%3$s\" instead",
											fileName,
											TradeCraft.pluginName,
											TradeCraftPropertiesFile.defaultLanguage);
				this.language = TradeCraftPropertiesFile.defaultLanguage;
				fileName = String.format(TradeCraftLocalization.filePreName, this.language);
				file = new File(filePath + File.separator + fileName);
				// check whether this file already exists, or else copy it over
		    	if ( !file.exists() || this.plugin.properties.autoUpdateLanguageFiles()
		       		 && TradeCraft.resourceLastModified("/"+ fileName) > file.lastModified()) {
		    		input = this.getClass().getResourceAsStream("/" + fileName);
		    	}
			}
			// if input is not null, then we need to copy the given file from the jar
			if ( input != null ) {
				if ( !file.exists() ) {
					this.plugin.log(Level.INFO, "%1$s%2$s%3$s does not exist, creating...",
												filePath,
												File.separator,
												fileName);
				} else {
					this.plugin.log(Level.INFO, "%1$s has a new version, updating...",
							   				    fileName);
				}

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
