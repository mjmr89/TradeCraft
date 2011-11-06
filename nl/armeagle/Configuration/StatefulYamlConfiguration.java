package nl.armeagle.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class StatefulYamlConfiguration extends YamlConfiguration {
	protected File file;
	
	public StatefulYamlConfiguration(File file) {
		this.file = file;
	}
	
	public void load() throws IOException {
		this.load(false);
	}
	public void load(boolean loadDefaults) throws IOException {
		YamlConfiguration baseConfig = YamlConfiguration.loadConfiguration(file);
		try {
			this.loadFromString(baseConfig.saveToString());
		} catch (InvalidConfigurationException e) {
			Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Invalid configuation");
		}
		
		if (loadDefaults) {
			InputStream defaultInput = this.getClass().getResourceAsStream("/" + file.getName());
			if ( null != defaultInput ) {
				YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultInput);
				this.setDefaults(defaultConfig);
				this.options().copyDefaults(true);
				this.save();
			}
		}
	}
	
	public void save() throws IOException {
		super.save(this.file);
	}
	
	public File getFile() {
		return this.file;
	}
}
