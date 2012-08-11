package nl.armeagle.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.error.YAMLException;

public class StatefulYamlConfiguration extends YamlConfiguration {
	protected File file;
	
	public StatefulYamlConfiguration(File file) {
		this.file = file;
	}
	
	public void load() throws IOException {
		this.load(false);
	}
	public void load(boolean forceDefaults) throws IOException {
		boolean notLoaded = false;
		if (this.file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        YamlConfiguration baseConfig = new YamlConfiguration();

        try {
            baseConfig.load(this.file);
        } catch (FileNotFoundException fnf) {
			notLoaded = true;
		} catch (IOException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + this.file, ex);
        } catch (InvalidConfigurationException ex) {
            if (ex.getCause() instanceof YAMLException) {
                Bukkit.getLogger().severe("Config file " + this.file + " isn't valid! " + ex.getCause());
            } else if ((ex.getCause() == null) || (ex.getCause() instanceof ClassCastException)) {
                Bukkit.getLogger().severe("Config file " + this.file + " isn't valid!");
            } else {
                Bukkit.getLogger().log(Level.SEVERE, "Cannot load " + this.file + ": " + ex.getCause().getClass(), ex);
            }
        }

        if (notLoaded || forceDefaults) {
			InputStream defaultInput = this.getClass().getResourceAsStream("/" + file.getName());
			if ( null != defaultInput ) {
				YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(defaultInput);

				try {
					this.loadFromString(defaultConfig.saveToString());
				} catch (InvalidConfigurationException e) {
					Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Invalid default configuation");
				}
				this.save();
			}
		} else if (! notLoaded) {
	        try {
				this.loadFromString(baseConfig.saveToString());
			} catch (InvalidConfigurationException e) {
				Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Invalid configuation");
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
