package nl.armeagle.TradeCraft;

import java.io.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.armeagle.Configuration.StatefulYamlConfiguration;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TradeCraft extends JavaPlugin {
    public static enum MessageTypes {WITHDRAW, DEPOSIT};

    // The plugin name.
    static final String pluginName = "TradeCraft";

    public static final Pattern itemPatternIdSplitData = Pattern.compile("^(\\d+)(?:;(\\d+))?$");

    private static enum Commands {
        tcsetcurrency,
        tcgetcurrency,
        tcshops,
        tcpshops,
        tcreload,
        tcplayerperms,
        tchelp,
        tc
    };

    // Stuff used to interact with the server.
    final Logger log = Logger.getLogger("Minecraft");
    final Server server = this.getServer();

    protected BufferedWriter usageLog = null;
    // Objects used by the plugin.
    static TradeCraftItem currency;
    static TradeCraftPropertiesFile properties;
    TradeCraftConfigurationFile configuration;
    public TradeCraftLocalization localization;
    TradeCraftDataFile data;

    private HashMap<String, StatefulYamlConfiguration> configs = new HashMap<String, StatefulYamlConfiguration>();

    private final TradeCraftBlockListener blockListener = new TradeCraftBlockListener(this);
    private final TradeCraftPlayerListener playerListener = new TradeCraftPlayerListener(this);
    public TradeCraftPermissions permissions = new TradeCraftPermissions(this);

    // prevent the script from registering the event listeners multiple times (by dis-/enable)
    public static boolean hasRegisteredEventListeners = false;

    public void onDisable() {
        this.disable();
    }
    private void disable() {
        if ( this.usageLog != null ) {
            try {
                this.usageLog.close();
            } catch (IOException e) {
                this.log(Level.WARNING, "Failed to close shop usage log file");
            }
        }
        properties = null;
        configuration = null;
        this.localization = null;
        data.save();
        data = null;
    }

    public void onEnable() {
        this.enable();
    }
    private void enable() {
        properties = new TradeCraftPropertiesFile(this);
        configuration = new TradeCraftConfigurationFile(this);
        data = new TradeCraftDataFile(this);
        this.localization = new TradeCraftLocalization(this);

        if ( TradeCraft.properties.logShopUse() ) {
            File usageLogFile = new File(this.getDataFolder(), "shopUsage.log");
            try {
                if ( !usageLogFile.exists() ) {
                    usageLogFile.createNewFile();
                }
                if ( usageLogFile.canWrite() ) {
                    this.usageLog = new BufferedWriter(new FileWriter(usageLogFile, true));
                    this.log(Level.INFO, "Writing shop usage to log file: "+ usageLogFile.toString());
                } else {
                    this.log(Level.WARNING, "Error opening shop usage log file: "+ usageLogFile.toString());
                }
            } catch (IOException e) {
                this.log(Level.WARNING, "Failed to open shop usage log file: "+ usageLogFile.toString());
            }
        }

        configuration.load();
        data.load();
        currency = properties.getCurrencyType();

        if ( !TradeCraft.hasRegisteredEventListeners ) {
            PluginManager pm = this.getServer().getPluginManager();
            pm.registerEvents(playerListener, this);
            pm.registerEvents(blockListener, this);
            TradeCraft.hasRegisteredEventListeners = true;
        }

        PluginDescriptionFile pdfFile = this.getDescription();
        this.log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        try {
            TradeCraft.Commands command = TradeCraft.Commands.valueOf(cmd.getName());
            if (sender instanceof Player) {
                Player p = (Player) sender;
                switch (command) {
                case tcsetcurrency:
                    if ( args.length == 1 && this.permissions.canSetCurrency(p) ) {
                        TradeCraftItem testCurrency = null;
                        // try to split ID and Data, separated by a semicolon mark
                        Matcher IdSplitData = TradeCraft.itemPatternIdSplitData.matcher(args[0]);

                        if ( !IdSplitData.matches() ) {
                            // try to match the parameter to item names from the configuration
                            TradeCraftConfigurationInfo setCurr = this.configuration.get(args[0]);
                            if ( setCurr == null ) {
                                this.sendMessage(p, TradeCraftLocalization.get("IS_NO_VALID_CURRENCY_USE_INSTEAD"),
                                                 args[0]);
                                return false;
                            } else {
                                currency = setCurr.type;
                            }
                        } else {
                            try {
                                int cid = Integer.parseInt(IdSplitData.group(1));
                                if ( IdSplitData.group(2) != null ) {
                                    testCurrency = new TradeCraftItem(cid, Short.parseShort(IdSplitData.group(2)));
                                } else {
                                    testCurrency = new TradeCraftItem(cid);
                                }
                            } catch ( NumberFormatException e ) {
                                 this.sendMessage(p, TradeCraftLocalization.get("INVALID_CURRENCY"),
                                                     args[0]);
                                return false;
                            }
                            if ( this.configuration.get(testCurrency) != null ) {
                                currency = testCurrency;
                            } else {
                                this.sendMessage(p, TradeCraftLocalization.get("INVALID_CURRENCY"),
                                        args[0]);
                                return false;
                            }
                        }

                        TradeCraft.properties.setCurrencyType(currency);
                        this.sendMessage(p, TradeCraftLocalization.get("CURRENCY_IS_SET_TO_A_IDDATA"),
                                            this.getCurrencyName(),
                                            currency.toShortString());
                        return true;
                    }
                    return true;
                case tcgetcurrency:
                    this.sendMessage(p, TradeCraftLocalization.get("CURRENCY_IS_A_IDDATA"),
                            this.getCurrencyName(),
                            currency.toShortString());
                    return true;
                case tcshops:
                    // lookup own shows
                    displayShops(p.getName(), p, false);
                    return true;
                case tcpshops:
                    // Check whether another parameter is passed, if so check whether
                    // the player can get information about other player's shops.
                    if ( this.permissions.canQueryOtherShops(p) ) {
                        if ( args.length == 1 ) {
                            // lookup other player's shops
                            displayShops(args[0], p, true);
                        } else {
                            this.sendMessage(p, cmd.getUsage());
                        }
                    }
                    return true;
                case tcreload:
                    if ( this.permissions.canReload(p)) {
                        this.sendMessage(p, TradeCraftLocalization.get("RESTARTING_PLUGIN"), TradeCraft.pluginName);
                        this.disable();
                        this.enable();
                        this.sendMessage(p, TradeCraftLocalization.get("RESTARTING_PLUGIN_DONE"), TradeCraft.pluginName);
                    }
                    return true;
                case tcplayerperms:
                    if ( this.permissions.canQueryPlayer(p)) {
                        if ( args.length == 1 ) {
                            permissions.debug(sender, args[0]);
                        } else {
                            sender.sendMessage(cmd.getUsage());
                        }
                    }
                    return true;
                default:
                    displayCommandHelpText(p);
                    return true;
                }
            } else if ( sender instanceof ConsoleCommandSender ) {
                switch (command) {
                case tcplayerperms:
                    if ( args.length == 1 ) {
                        permissions.debug(sender, args[0]);
                    } else {
                        sender.sendMessage(cmd.getUsage());
                    }
                    return true;
                case tcpshops:
                    // Check whether another parameter is passed, if so check whether
                    // the player can get information about other player's shops.
                    if ( args.length == 1 ) {
                        // lookup other player's shops
                        displayShops(args[0], sender, true);
                    } else {
                        sender.sendMessage(cmd.getUsage());
                    }
                    return true;
                case tcreload:
                    sender.sendMessage(String.format(TradeCraftLocalization.get("RESTARTING_PLUGIN"),
                                                      TradeCraft.pluginName));
                    this.disable();
                    this.enable();
                    sender.sendMessage(String.format(TradeCraftLocalization.get("RESTARTING_PLUGIN_DONE"),
                                                      TradeCraft.pluginName));
                    return true;
                default:
                    displayCommandHelpText(null);
                    return true;
                }
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return false;
    }

    void displayShops(String infoPlayerName, CommandSender displayTo, boolean otherQuery) {
        Map<String, TradeCraftDataInfo> list = data.shopsOwned(infoPlayerName);
        if (list.size() == 0) {
            if ( otherQuery ) {
                // elevated player looking for other player's shops
                displayTo.sendMessage(String.format(TradeCraftLocalization.get("A_DOES_NOT_OWN_ANY_SHOPS"),
                                                    infoPlayerName));
            } else {
                displayTo.sendMessage(TradeCraftLocalization.get("YOU_DONT_OWN_ANY_SHOPS"));
            }
            return;
        }

        if ( otherQuery ) {
            displayTo.sendMessage(String.format(TradeCraftLocalization.get("SHOPS_OF_A"),
                                                 infoPlayerName));
        } else {
            displayTo.sendMessage(TradeCraftLocalization.get("YOUR_SHOPS"));
        }
        for (Map.Entry<String, TradeCraftDataInfo> entry : list.entrySet()) {
            TradeCraftDataInfo info = entry.getValue();
            String message = "";
            if (TradeCraft.properties.showShopLocation()) {
                String location = entry.getKey().replaceFirst(",", "(") +")";
                message += ChatColor.GRAY + location +" "+ ChatColor.WHITE;
            }
            message += TradeCraftLocalization.get("ITEM") +": "+ this.configuration.get(info.itemType).name +"("+ info.itemType.toShortString() +")"
                +" "+ TradeCraftLocalization.get("AMOUNT") +": "+ info.itemAmount +" "
                + this.getCurrencyName() +": "+ info.currencyAmount;

            displayTo.sendMessage(message);
        }

    }

    void sendMessage(Player player, TradeCraft.MessageTypes messageType, String format, Object... args) {
        try {
            String message = String.format(format, args);
            player.sendMessage(TradeCraft.properties.getMessageTypeColor(messageType) + message);
        } catch ( IllegalFormatException e ) {
            player.sendMessage(TradeCraftLocalization.get("ERROR_IN_FORMAT_STRING") +" "+ format);
        }
    }

    void sendMessage(Player player, String format, Object... args) {
        try {
            String message = String.format(format, args);
            player.sendMessage(message);
        } catch ( IllegalFormatException e ) {
            player.sendMessage(TradeCraftLocalization.get("ERROR_IN_FORMAT_STRING") +" "+ format);
        }
    }

    void trace(Player player, String format, Object... args) {
        if (properties.getEnableDebugMessages()) {
            if (null != player) {
                sendMessage(player, format, args);
            } else {
                this.log(Level.INFO, format, args);
            }
        }
    }
    /*
     * When a block behind a shop sign is destroyed, the sign would be destroyed too.
     * Check all side faces of this block, for a sign attached to this block. Then
     * pass that sign block to getShopFromSignBlock.
     *
     * This should only be used for checking whether a normal block can be destroyed, for there not being any
     * signs attached to it, or this block being a chest or sign itself.
     * Since one block can
     */
    ArrayList<TradeCraftShop> getShopsFromBlock(Player player, Block block) {
        ArrayList<TradeCraftShop> shops = new ArrayList<TradeCraftShop>();
        // use other function(s) directly if applicable
        if ( block.getType() == Material.CHEST || block.getType() == Material.WALL_SIGN ) {
            TradeCraftShop shop = getShopFromSignOrChestBlock(player, block);
            if ( shop != null ) {
                shops.add(shop);
            }
        } else {
            // go through all 4 faces of this block to check for a sign attached to this block
            final BlockFace[] sides = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
            for ( int index_sides = 0; index_sides < sides.length; index_sides++ ) {
                BlockFace side = sides[index_sides];
                // get the block on that side
                Block sideBlock = block.getRelative(side);
                // check for it being a wall sign
                if ( sideBlock.getType() == Material.WALL_SIGN ) {
                    // get the sign (extending MaterialData) for clean attached face checking
                    org.bukkit.material.Sign materialSign = new org.bukkit.material.Sign(Material.WALL_SIGN, sideBlock.getData());
                    /* Now, for easy comparison, we'll compare to the direction the sign is facing. If that's
                     * the same as the current face (of the 4 we're looping through), then this sign is attached
                     * to the block that is being destroyed.
                     */
                    if ( materialSign.getFacing() == side ) {
                        TradeCraftShop shop = this.getShopFromSignBlock(player, sideBlock);
                        if ( shop != null ) {
                            shops.add(shop);
                        }
                    }

                }
            }
        }
        return shops;
    }
    TradeCraftShop getShopFromSignOrChestBlock(Player player, Block block) {
        if (block.getType() == Material.CHEST) {
            block = block.getWorld().getBlockAt(
                block.getX(),
                block.getY() + 1,
                block.getZ()
            );
        }

        return getShopFromSignBlock(player, block);
    }

    TradeCraftShop getShopFromSignBlock(Player player, Block block) {
        if (block.getType() != Material.WALL_SIGN) {
            return null;
        }

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        trace(player, "You clicked a sign at %d, %d, %d in world: %s.", x, y, z, block.getWorld().getName());

        Sign sign = (Sign) block.getWorld().getBlockAt(x, y, z).getState();

        // The sign at this location can be null if it was just destroyed.
        if (sign == null) {
            trace(player, "The sign is no longer there.");

            return null;
        }

        String itemName = getItemName(sign.getLines());

        if (itemName == null) {
            trace(player, "There is no item name on the sign.");

            return null;
        }

        trace(player, "The item name on the sign is %s.", itemName);

        Block blockBelowSign = block.getRelative(0, -1, 0);

        if (blockBelowSign.getType() != Material.CHEST) {
            trace(player, "There is no chest beneath the sign.");
            return null;
        }

        Chest chest = (Chest) blockBelowSign.getState();

        if (itemName.toLowerCase().equals("repair")) {
            if (!properties.getRepairShopsEnabled()) {
                trace(player, "Repair shops are not enabled.");
                return null;
            }

            if (player == null || !player.isOp()) {
                trace(player, "You can't use repair shops.");
                return null;
            }

            trace(player, "This is a repair shop.");
            return new TradeCraftRepairShop(this, sign, chest);
        }

        if (!configuration.isConfigured(itemName)) {
            trace(player,
                  "The item name %s is not configured in the config file.",
                  itemName);
            return null;
        }

        // TODO change to use chest getOwner
        //String ownerName = getOwnerName(sign.getLine(3));
        String ownerName = data.getOwnerOfSign(sign);

        if (ownerName == null) {
            trace(player, "There is no owner name on the sign.");

            if (!properties.getInfiniteShopsEnabled()) {
                trace(player, "Ininite shops are not enabled.");
                return null;
            }

            trace(player, "This is an infinite shop.");
            try {
                return new TradeCraftInfiniteShop(this, sign, chest);
            } catch (Exception e) {
                trace(player, e.getMessage());
            }
            return null;
        }

        trace(player, "The owner name on the sign is %s.", ownerName);

        if (!properties.getPlayerOwnedShopsEnabled()) {
            trace(player, "Player-owned shops are not enabled.");
            return null;
        }

        trace(player, "This is a player-owned shop.");
        return new TradeCraftPlayerOwnedShop(this, sign, chest, ownerName);
    }

    String getItemName(String[] signLines) {
        return getSpecialText(signLines, "[", "]");
    }

    String getOwnerName(String signLine) {
        return getSpecialTextOnLine(signLine, "-", "-");
    }

    private String getSpecialText(String[] signLines, String prefix, String suffix) {
        for (int i = 0; i < 4; i++) {
            String text = getSpecialTextOnLine(signLines[i], prefix, suffix);

            if (text != null) {
                return text;
            }
        }

        return null;
    }

    private String getSpecialTextOnLine(String signLine, String prefix, String suffix) {
        if (signLine == null) {
            return null;
        }

        signLine = signLine.trim();

        if (signLine.startsWith(prefix) && signLine.endsWith(suffix)
                && signLine.length() > 2) {

            String text = signLine.substring(1, signLine.length() - 1);
            text = text.trim();
            if (text.equals("")) {
                return null;
            }

            return text;
        }

        return null;
    }

    TradeCraftExchangeRate getExchangeRate(String[] signLines, int lineNumber) {
        if ( lineNumber < 0 || lineNumber >= signLines.length ) {
            return null;
        }
        return getExchangeRate(signLines[lineNumber]);
    }
    TradeCraftExchangeRate getExchangeRate(String signLine) {
        return new TradeCraftExchangeRate(signLine);    }

    static int getMaxStackSize(int itemType) {
        if ( TradeCraft.properties.getNormalStackSizeUsed() ) {
            int stackSize = Material.getMaterial(itemType).getMaxStackSize();
            return (stackSize == 0 ? 64 : Material.getMaterial(itemType).getMaxStackSize());
        } else {
            return 64;
        }
    }

    /**
     * Get a CamelCased string based on the current currency.
     *
     * @return a string representing the currency.
     */
    public String getCurrencyName() {
        // Try to get the name from the configuration file first
        TradeCraftConfigurationInfo configInfo = this.configuration.get(TradeCraft.currency);
        if ( configInfo != null ) {
            return configInfo.name;
        } else {

            ItemStack currencyStack = new ItemStack(TradeCraft.currency.id, 1, TradeCraft.currency.data); // weird that there's no Material.getMaterial(id, short/byte)
            MaterialData currencyData = currencyStack.getType().getNewData((byte)TradeCraft.currency.data);
            String currencyString;
            if ( currencyData == null ) {
                currencyString = currencyStack.getType().name();
            } else {
                currencyString = currencyData.toString();
            }

            //String baseName = stack.getType().name();
            String[] words = currencyString.replace("null ", "").split("\\(")[0].split("[ _]{1}");
            String name = "";
            for ( int word_ind = 0; word_ind < words.length; word_ind++ ) {
                String word = words[word_ind];
                if ( word_ind > 0 ) {
                    name = name.concat(" ");
                }
                name = name.concat(word.substring(0, 1).toUpperCase()).concat(word.substring(1).toLowerCase());
            }
            return name;
        }
    }

    private void displayCommandHelpText(Player player) {
        if ( player != null ) {
            this.sendMessage(player, TradeCraftLocalization.get("POSSIBLE_COMMANDS_FOR_THE_PLUGIN"),
                                     TradeCraft.pluginName);
            this.sendMessage(player, "/tc[help]"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_HELP_THIS_TEXT"));
            this.sendMessage(player, "/tcshops"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_SHOPS"));
            if ( this.permissions.canQueryOtherShops(player) ) {
                this.sendMessage(player, "/tcpshops [player]"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_PSHOPS"));
            }
            this.sendMessage(player, "/tcgetcurrency"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_CURRENCY_GET_CURRENCY"));
            if ( this.permissions.canSetCurrency(player) ) {
                this.sendMessage(player, "/tcsetcurrency [id[;data]]"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_CURRENCY_OPT_PARAM_GETSET_CURRENCY"));
            }
            if ( this.permissions.canReload(player) ) {
                this.sendMessage(player, "/tcreload"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_RELOAD"));
            }
            if ( this.permissions.canQueryPlayer(player) ) {
                this.sendMessage(player, "/tcplayerperms"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_CAN_PLAYER"));
            }
        } else {
            // console command help
            this.log(Level.INFO, TradeCraftLocalization.get("POSSIBLE_COMMANDS_FOR_THE_PLUGIN"),
                                 TradeCraft.pluginName);
            this.log(Level.INFO, "tc[help]: "+ TradeCraftLocalization.get("TC_HELP_THIS_TEXT"));
            this.log(Level.INFO, "tcplayerperms playername: "+ TradeCraftLocalization.get("TC_CAN_PLAYER"));
            this.log(Level.INFO, "tcpshops [player]: "+ TradeCraftLocalization.get("TC_PSHOPS"));
            this.log(Level.INFO, "tcreload: "+ TradeCraftLocalization.get("TC_RELOAD"));
        }
    }

    public void saveConfig() {
        Logger.getLogger(JavaPlugin.class.getName()).log(Level.INFO, "saving config to: "+ this.getConfig("config").getFile().getAbsolutePath());
        try {
            this.getConfig("config").save();
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + this.getConfig("config").getFile().getName(), ex);
        }
    }
    public StatefulYamlConfiguration getConfig() {
        return this.getConfig("config");
    }
    public StatefulYamlConfiguration getConfig(String name) {
        if (name.indexOf(".") < 0) {
            name += ".yml";
        }
        if (this.configs.containsKey(name)) {
            return this.configs.get(name);
        } else {
            File configFile = new File(this.getDataFolder(), name);
            StatefulYamlConfiguration config = new StatefulYamlConfiguration(configFile);
            this.configs.put(name, config);
            return config;
        }
    }

    public void log(Level level, String format, Object... args) {
        this.log.log(level, TradeCraft.pluginName +": "+ String.format(format, args));
    }

    public void useLog(Player player, TradeCraftShop shop, String format, Object... args) {
        if ( TradeCraft.properties.logShopUse() ) {
            if ( this.usageLog != null ) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    this.usageLog.write(String.format("%s %s \t%s %s\n",
                            formatter.format(new Date()),
                            shop.toString(),
                            player.getDisplayName(),
                            String.format(format, args)));
                    this.usageLog.flush();
                } catch (IOException e) {
                    this.log(Level.WARNING, "Failed to write to shop use log file");
                }
            } else {
                this.log(Level.INFO, "not written to log file");
            }
        }
    }

}