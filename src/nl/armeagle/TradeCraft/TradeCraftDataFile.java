package nl.armeagle.TradeCraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

class TradeCraftDataFile {
    /*
     * As of version 1.0.5 there is support for multiple worlds.
     * Newly created shops will add the world name to the information stored.
     * Old shops will be converted when first interacted with. 
     */

    private static final String fileName = "plugins" + File.separator + TradeCraft.pluginName+ File.separator + TradeCraft.pluginName + ".data";
    private static final Pattern infoPatternNoWorld = Pattern.compile(
            "^\\s*([^,]+)\\s*," + // ownerName
            "\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*," + // x,y,z
            "\\s*(\\d+(?:;\\d+)?)\\s*," + // itemType[!data]
            "\\s*(\\d+)\\s*," + // itemAmount
            "\\s*(\\d+)\\s*$"); // currencyAmount
    private static final Pattern infoPatternWorld = Pattern.compile(
            "^\\s*([^,]+)\\s*," + // ownerName
            "\\s*([^,]+)\\s*," + // world name
            "\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*," + // x,y,z
            "\\s*(\\d+(?:;\\d+)?)\\s*," + // itemType[!data]
            "\\s*(\\d+)\\s*," + // itemAmount
            "\\s*(\\d+)\\s*$"); // currencyAmount

    private final TradeCraft plugin;
    private final Map<String, TradeCraftDataInfo> data = new HashMap<String, TradeCraftDataInfo>();
    private final File dFile = new File(fileName);
    public boolean wasLoaded = false;


    TradeCraftDataFile(TradeCraft plugin) {
        this.plugin = plugin;
    }

    public void load() {
        try {
            dFile.createNewFile();
            data.clear();

            BufferedReader reader = new BufferedReader(new FileReader(fileName));

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber += 1;

                String ownerName;
                String worldName;
                int x;
                int y;
                int z;
                int itemAmount;
                int currencyAmount;
                String itemIdData;
                
                Matcher infoMatcher2 = infoPatternNoWorld.matcher(line);

                if (infoMatcher2.matches()) {
                    ownerName = infoMatcher2.group(1);
                    worldName = null;
                    x = Integer.parseInt(infoMatcher2.group(2));
                    y = Integer.parseInt(infoMatcher2.group(3));
                    z = Integer.parseInt(infoMatcher2.group(4));
                    itemIdData = infoMatcher2.group(5);
                    itemAmount = Integer.parseInt(infoMatcher2.group(6));
                    currencyAmount = Integer.parseInt(infoMatcher2.group(7));
                } else {
                     // support for multiple worlds 
                     Matcher infoMatcher3 = infoPatternWorld.matcher(line);
                     if ( !infoMatcher3.matches()) {
                         plugin.log.warning(
                                "Failed to parse line number " + lineNumber +
                                " in " + fileName +
                                ": " + line);
                        continue;
                     }

                    ownerName = infoMatcher3.group(1);
                    worldName = infoMatcher3.group(2);
                    x = Integer.parseInt(infoMatcher3.group(3));
                    y = Integer.parseInt(infoMatcher3.group(4));
                    z = Integer.parseInt(infoMatcher3.group(5));
                    itemIdData = infoMatcher3.group(6);
                    itemAmount = Integer.parseInt(infoMatcher3.group(7));
                    currencyAmount = Integer.parseInt(infoMatcher3.group(8));
                }
                    
                TradeCraftDataInfo info = new TradeCraftDataInfo();
                info.ownerName = ownerName;
                info.worldName = worldName;
                info.itemAmount = itemAmount;
                info.currencyAmount = currencyAmount;

                String key = getKey(worldName, x, y, z);
                data.put(key, info);
                
                Matcher IdSplitData = TradeCraft.itemPatternIdSplitData.matcher(itemIdData);
                
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
            }

            plugin.log.info("Loaded " + data.size() + " shops");
            reader.close();
        } catch (IOException e) {
            plugin.log.warning("Error reading " + fileName);
        }
        this.wasLoaded = true;
    }

    public void save() {
        if ( ! this.wasLoaded ) {
            this.plugin.log.severe("TradeCraft: failed to load data file when plugin was enabled, will not save to prevent loss of items.");
            // The failure should have been such that no interaction with shops would have been possible, so no items should have been lost since the plugin was
            // loaded till this save point. TODO, make sure that no items are lost, when save is actually called after motations, even though that situation
            // should never possibly occur.
            return;
        }
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
        String key = getKey(shop.sign.getWorld().getName(), l.getBlockX(),l.getBlockY(),l.getBlockZ());
        if(data.containsKey(key)){
            data.remove(key);
            save();            
        }
    }
    
    public Map<String, TradeCraftDataInfo> shopsOwned(String playerName){
        Map<String, TradeCraftDataInfo> list = new HashMap<String, TradeCraftDataInfo>();
        for (String key : data.keySet()) {
            TradeCraftDataInfo info = data.get(key);
              if(info.ownerName.equalsIgnoreCase(playerName)){
                   list.put(key, info);
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
        TradeCraftDataInfo info;
        String key = getKeyFromSign(sign);
        
        if (data.containsKey(key)) {
            info = data.get(key);
        } else {
            info = new TradeCraftDataInfo();
        }
        
        info.ownerName = ownerName;
        info.worldName = sign.getWorld().getName();
        info.itemType = itemType;
        info.itemAmount += itemAmount;
        data.put(key, info);
        
        save();
    }

    public void depositCurrency(String ownerName, Sign sign, int currencyAmount) {
        TradeCraftDataInfo info;        
        String key = getKeyFromSign(sign);
        
        if (data.containsKey(key)) {
            info = data.get(key);
        } else {
            info = new TradeCraftDataInfo();
        }
        
        info.ownerName = ownerName;
        info.worldName = sign.getWorld().getName();
        info.currencyAmount += currencyAmount;
        data.put(key, info);
        
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
        String keyWithWorld = getKey(sign.getWorld().getName(), sign.getX(), sign.getY(), sign.getZ());
        // convert old style keys (without world name) to new style and return the new key
        if ( !data.containsKey(keyWithWorld) ) {
            // try the old style key, without the world part
            String keyWithoutWorld = getKey(null, sign.getX(), sign.getY(), sign.getZ());
            if ( data.containsKey(keyWithoutWorld) ) {
                TradeCraftDataInfo shopInfo = data.get(keyWithoutWorld);
                data.remove(keyWithoutWorld);
                data.put(keyWithWorld, shopInfo);
            }
        }
        return keyWithWorld;
    }

    private String getKey(String world, int x, int y, int z) {
        // support for multiple words now, optionally accepting a world passed on.
        if ( world == null ) {
            return x + "," + y + "," + z;
        } else {
            return world + "," + x + "," + y + "," + z;
        }
    }

    public void createNewSign(String ownerName, TradeCraftConfigurationInfo itemInfo, Sign sign) {
        TradeCraftDataInfo info;
        String key = getKeyFromSign(sign);
        
        if (data.containsKey(key)) {
            info = data.get(key);
        } else {
            info = new TradeCraftDataInfo();
        }
        
        info.ownerName = ownerName;
        info.worldName = sign.getWorld().getName();
        info.currencyAmount = 0;
        info.itemAmount = 0;
        info.itemType = itemInfo.type;
        data.put(key, info);

        save();
    }
    
    public int getPlayerShopCount(Player player, World world) {
        Map<String, TradeCraftDataInfo> list = this.shopsOwned(player.getName());
        int shopsOwnedCount = 0;
        
        for (String key : list.keySet()) {
            TradeCraftDataInfo info = data.get(key);
            if (info.worldName.equalsIgnoreCase(world.getName())) {
                shopsOwnedCount++;
            }
        }
        
        return shopsOwnedCount;
    }
    public int getPlayerShopCount(Player player) {
        return this.shopsOwned(player.getName()).size();
    }
}