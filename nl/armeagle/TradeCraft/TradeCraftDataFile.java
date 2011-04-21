package nl.armeagle.TradeCraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.block.Sign;

class TradeCraftDataFile {

	private static final String fileName = "plugins" + File.separator + TradeCraft.pluginName+ File.separator + TradeCraft.pluginName + ".data";
    private static final Pattern infoPattern1 = Pattern.compile(
            "^\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)" + // x,y,z
            "\\s*=\\s*" +
            "(\\d+)\\s*,\\s*(\\d+)\\s*$"); // itemAmount,currencyAmount
    private static final Pattern infoPattern2 = Pattern.compile(
            "^\\s*([^,]+)\\s*," + // ownerName
            "\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*," + // x,y,z
            "\\s*(\\d+(?:;\\d+)?)\\s*," + // itemType[!data]
            "\\s*(\\d+)\\s*," + // itemAmount
            "\\s*(\\d+)\\s*$"); // currencyAmount

    private final TradeCraft plugin;
    private final Map<String, TradeCraftDataInfo> data = new HashMap<String, TradeCraftDataInfo>();
    private final File dFile = new File(fileName);


    TradeCraftDataFile(TradeCraft plugin) {
        this.plugin = plugin;
    }

    public void load() {
//        if (!dFile.exists()) {
//            plugin.log.info("No " + fileName + " file to read.  Creating one now.");
//            try {
//				dFile.createNewFile();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//            return;
//        }

        try {
        	dFile.createNewFile();
            data.clear();

            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber += 1;

                Matcher infoMatcher2 = infoPattern2.matcher(line);

                if (infoMatcher2.matches()) {
                    String ownerName = infoMatcher2.group(1);
                    int x = Integer.parseInt(infoMatcher2.group(2));
                    int y = Integer.parseInt(infoMatcher2.group(3));
                    int z = Integer.parseInt(infoMatcher2.group(4));
                    int itemAmount = Integer.parseInt(infoMatcher2.group(6));
                    int currencyAmount = Integer.parseInt(infoMatcher2.group(7));
                    
                    TradeCraftDataInfo info = new TradeCraftDataInfo();
                    String key = getKey(x, y, z);

                    info.ownerName = ownerName;
                    info.itemAmount = itemAmount;
                    info.currencyAmount = currencyAmount;

                    data.put(key, info);
                    
                    Matcher IdSplitData = TradeCraft.itemPatternIdSplitData.matcher(infoMatcher2.group(5));
                    
                    if ( IdSplitData.matches() ) {
	                    int itemId = Integer.parseInt(IdSplitData.group(1));
	                    if ( IdSplitData.group(2) != null ) {
	                    	info.itemType = new TradeCraftItem(itemId, Short.parseShort(IdSplitData.group(2)));
	                    } else {
	                    	info.itemType = new TradeCraftItem(itemId);
	                    }
                    } else {
                    	plugin.log.warning(
                                "Failed to parse line number " + lineNumber +
                                " in " + fileName +
                                ": " + line);
                        continue;
                    }
                } else {
                    Matcher infoMatcher1 = infoPattern1.matcher(line);

                    if (!infoMatcher1.matches()) {
                        plugin.log.warning(
                                "Failed to parse line number " + lineNumber +
                                " in " + fileName +
                                ": " + line);
                        continue;
                    }

                    int x = Integer.parseInt(infoMatcher1.group(1));
                    int y = Integer.parseInt(infoMatcher1.group(2));
                    int z = Integer.parseInt(infoMatcher1.group(3));
                    int itemAmount = Integer.parseInt(infoMatcher1.group(4));
                    int currencyAmount = Integer.parseInt(infoMatcher1.group(5));

                    String key = getKey(x, y, z);

                    TradeCraftDataInfo info = new TradeCraftDataInfo();
                    info.ownerName = "unknown";
                    info.itemAmount = itemAmount;
                    info.currencyAmount = currencyAmount;

                    data.put(key, info);
                }
            }

            plugin.log.info("Loaded " + data.size() + " shops");
            reader.close();
        } catch (IOException e) {
            plugin.log.warning("Error reading " + fileName);
        }
    }

    public void save() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            for (String key : data.keySet()) {
                TradeCraftDataInfo info = data.get(key);
                writer.write(info.ownerName + "," +
                             key + "," +
                             info.itemType.toShortString() + "," +
                             info.itemAmount + "," +
                             info.currencyAmount);
                writer.newLine();
//                plugin.getServer().broadcastMessage(info.ownerName + "," +
//                             key + "," +
//                             info.itemType + "," +
//                             info.itemAmount + "," +
//                             info.currencyAmount);
            }

            writer.close();
        } catch (IOException e) {
            plugin.log.warning("Error writing " + fileName);
        }
    }
    
    public void deleteShop(TradeCraftShop shop){
    	Location l = shop.sign.getBlock().getLocation();
    	String key = getKey(l.getBlockX(),l.getBlockY(),l.getBlockZ());
    	if(data.containsKey(key)){
    		data.remove(key);
    		save();    		
    	}
    }
    
    public ArrayList<TradeCraftDataInfo> shopsOwned(String name){
    	ArrayList<TradeCraftDataInfo> list = new ArrayList<TradeCraftDataInfo>();
    	for (String key : data.keySet()) {
    		TradeCraftDataInfo info = data.get(key);
    		if ( this.plugin.properties.getStrictPlayerShopOwnerNameRequired() ) {
    			if(info.ownerName.equalsIgnoreCase(name)){
    				list.add(info);
    			}
    		} else {
    			if ( name.indexOf(info.ownerName) == 0 ) {
    				list.add(info);
    			}
    		}
    	}
    	
    	
    	return list;
    }

    public void setOwnerOfSign(String ownerName, Sign sign) {
        depositCurrency(ownerName, sign, 0);
    }

    public String getOwnerOfSign(Sign sign) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            return info.ownerName;
        }
        return null;
    }

    public int getItemAmount(Sign sign) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            return info.itemAmount;
        }
        return 0;
    }

    public int getCurrencyAmount(Sign sign) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            return info.currencyAmount;
        }
        return 0;
    }

    public void depositItems(String ownerName, Sign sign, TradeCraftItem itemType, int itemAmount) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            info.ownerName = ownerName; // For old entries that don't have the name.
            info.itemType = itemType; // For old entries that don't have the type.
            info.itemAmount += itemAmount;
        } else {
            TradeCraftDataInfo info = new TradeCraftDataInfo();
            info.ownerName = ownerName;
            info.itemType = itemType;
            info.itemAmount = itemAmount;
            data.put(key, info);
        }
        save();
    }

    public void depositCurrency(String ownerName, Sign sign, int currencyAmount) {
        String key = getKeyFromSign(sign);
        if (data.containsKey(key)) {
            TradeCraftDataInfo info = data.get(key);
            info.ownerName = ownerName; // For old entries that don't have the name.
            info.currencyAmount += currencyAmount;
        } else {
            TradeCraftDataInfo info = new TradeCraftDataInfo();
            info.ownerName = ownerName;
            info.currencyAmount = currencyAmount;
            data.put(key, info);
        }
        save();
    }

    public int withdrawItems(Sign sign) {
        String key = getKeyFromSign(sign);
        if (!data.containsKey(key)) {
            return 0;
        }
        TradeCraftDataInfo info = data.get(key);
        int itemAmount = info.itemAmount;
        if (itemAmount != 0) {
            info.itemAmount = 0;
            save();
        }
        return itemAmount;
    }

    public int withdrawCurrency(Sign sign) {
        String key = getKeyFromSign(sign);
        if (!data.containsKey(key)) {
            return 0;
        }
        TradeCraftDataInfo info = data.get(key);
        int currencyAmount = info.currencyAmount;
        if (currencyAmount != 0) {
            info.currencyAmount = 0;
            save();
        }
        return currencyAmount;
    }

    public void updateItemAndCurrencyAmounts(Sign sign, int itemAdjustment, int currencyAdjustment) {
        String key = getKeyFromSign(sign);
        if (!data.containsKey(key)) {
            return;
        }
        TradeCraftDataInfo info = data.get(key);
        info.itemAmount += itemAdjustment;
        info.currencyAmount += currencyAdjustment;
        save();
    }

    private String getKeyFromSign(Sign sign) {
        return getKey(sign.getX(), sign.getY(), sign.getZ());
    }

    private String getKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}