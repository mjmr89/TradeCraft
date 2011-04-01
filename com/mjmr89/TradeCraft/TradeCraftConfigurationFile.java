package com.mjmr89.TradeCraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class TradeCraftConfigurationFile {
    private static final String fileName = TradeCraft.pluginName + ".txt";
    private static final String filePath = "plugins" + File.separator + TradeCraft.pluginName;

    private static final Pattern commentPattern = Pattern.compile("^\\s*#.*$");
    private static final Pattern infoPattern = Pattern.compile(
            "^\\s*([^,]+)\\s*," + // name
            "\\s*(\\d+)\\s*" + // id
            "(?:,\\s*(\\d+)\\s*:\\s*(\\d+))?\\s*" + // buyAmount:buyValue
            "(?:,\\s*(\\d+)\\s*:\\s*(\\d+))?\\s*$"); // sellAmount:sellValue

    private final TradeCraft plugin;
    private final Map<String, TradeCraftConfigurationInfo> infos = new HashMap<String, TradeCraftConfigurationInfo>();

    TradeCraftConfigurationFile(TradeCraft plugin) {
        this.plugin = plugin;
    }

    void load() {
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
        try {
            infos.clear();

            BufferedReader configurationFile = new BufferedReader(new FileReader(filePath + File.separator + fileName));

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
                            " in " + filePath + File.separator + fileName +
                            ": " + line);
                    continue;
                }

                TradeCraftConfigurationInfo info = new TradeCraftConfigurationInfo();
                info.name = infoMatcher.group(1);
                info.id = Integer.parseInt(infoMatcher.group(2));

                if (infoMatcher.group(3) != null) {
                    info.sellAmount = info.buyAmount = Integer.parseInt(infoMatcher.group(3));
                    info.sellValue = info.buyValue = Integer.parseInt(infoMatcher.group(4));
                }

                if (infoMatcher.group(5) != null) {
                    info.sellAmount = Integer.parseInt(infoMatcher.group(5));
                    info.sellValue = Integer.parseInt(infoMatcher.group(6));
                }

                infos.put(info.name.toUpperCase(), info);
            }
            plugin.log.info("Loaded " + infos.size() + " configs");

            configurationFile.close();
        } catch (IOException e) {
            plugin.log.warning("Error reading " + filePath + File.separator + fileName);
        }
    }

    public String[] getNames() {
        String[] names = infos.keySet().toArray(new String[0]);
        Arrays.sort(names);
        return names;
    }

    public boolean isConfigured(String name) {
        return infos.containsKey(name.toUpperCase());
    }

    public TradeCraftConfigurationInfo get(String name) {
        return infos.get(name.toUpperCase());
    }
}