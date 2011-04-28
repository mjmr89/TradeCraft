package nl.armeagle.TradeCraft;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.bukkit.Permissions.Permissions;

public class TradeCraft extends JavaPlugin {
	// The plugin name.
	static final String pluginName = "TradeCraft";

	public static final Pattern itemPatternIdSplitData = Pattern.compile("^(\\d+)(?:;(\\d+))?$");

	private static final String CommandString = "tc";

	// Stuff used to interact with the server.
	final Logger log = Logger.getLogger("Minecraft");
	final Server server = this.getServer();

	// Objects used by the plugin.
	static TradeCraftItem currency;
	TradeCraftPropertiesFile properties;
	TradeCraftConfigurationFile configuration;
	public TradeCraftLocalization localization;
	TradeCraftDataFile data;

	private final TradeCraftBlockListener blockListener = new TradeCraftBlockListener(this);
	private final TradeCraftPlayerListener playerListener = new TradeCraftPlayerListener(this);
	public TradeCraftPermissions permissions = new TradeCraftPermissions(this);
	public Permissions permissionsPlugin = null;
	public boolean permEnabled = false;
	
	// prevent the script from registering the event listeners multiple times (by dis-/enable)
	public static boolean hasRegisteredEventListeners = false;

	public void onDisable() {
		this.disable();
	}
	private void disable() {
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
		
		configuration.load();
		data.load();
		currency = properties.getCurrencyType();
		permissions.setupPermissions();

		if ( !TradeCraft.hasRegisteredEventListeners ) {
			PluginManager pm = this.getServer().getPluginManager();
			pm.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
			pm.registerEvent(Type.SIGN_CHANGE, blockListener,Priority.Normal, this);
			pm.registerEvent(Type.BLOCK_BREAK, blockListener,Priority.Normal, this);
			TradeCraft.hasRegisteredEventListeners = true;
		}

		PluginDescriptionFile pdfFile = this.getDescription();
		this.log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String name = command.getName();
		Player p;

		if (sender instanceof Player) {
			p = (Player) sender;

			if ( name.equalsIgnoreCase(TradeCraft.CommandString) ) {
				if ( args.length == 0 || args[0].compareToIgnoreCase("help") == 0 ) {
					displayCommandHelpText(p);
				} else if ( args[0].compareToIgnoreCase("currency") == 0 ) {
					if ( args.length == 2 && this.permissions.canSetCurrency(p) ) {
						TradeCraftItem testCurrency = null;
						// try to split ID and Data, separated by a semicolon mark
		                Matcher IdSplitData = TradeCraft.itemPatternIdSplitData.matcher(args[1]);
		                
		                if ( !IdSplitData.matches() ) {
		                	// try to match the parameter to item names from the configuration
		                	TradeCraftConfigurationInfo setCurr = this.configuration.get(args[1]);
		                	if ( setCurr == null ) {
		                		this.sendMessage(p, TradeCraftLocalization.get("IS_NO_VALID_CURRENCY_USE_INSTEAD"),
		                						 args[1]);
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
		 											args[1]);
			                	return false;
			                }
		                	if ( this.configuration.get(testCurrency) != null ) {
		                		currency = testCurrency;
		                	} else {
								this.sendMessage(p, TradeCraftLocalization.get("INVALID_CURRENCY"),
										args[1]);
								return false;
		                	}
		                }
		                
						this.properties.setCurrencyType(currency);
						this.sendMessage(p, TradeCraftLocalization.get("CURRENCY_IS_SET_TO_A_IDDATA"),
											this.getCurrencyName(),
											currency.toShortString());
					} else {
						this.sendMessage(p, TradeCraftLocalization.get("CURRENCY_IS_A_IDDATA"),
										    this.getCurrencyName(),
										    currency.toShortString());
					}
				} else if ( args[0].equalsIgnoreCase("shops") ) {
					displayShops(p);
				} else if ( args[0].equalsIgnoreCase("reload") && this.permissions.canReload(p) ) {
					this.sendMessage(p, TradeCraftLocalization.get("RESTARTING_PLUGIN"),
										TradeCraft.pluginName);
					this.disable();
					this.enable();
					this.sendMessage(p, TradeCraftLocalization.get("RESTARTING_PLUGIN_DONE"),
										TradeCraft.pluginName);
				} else {
					return false;
				}
			}
		} else if ( sender instanceof ConsoleCommandSender ) {
			if ( args.length == 0 || args[0].compareToIgnoreCase("help") == 0 ) {
				displayCommandHelpText(null);
				return true;
			} else if ( name.equalsIgnoreCase(TradeCraft.CommandString) ) {
				if ( args[0].equalsIgnoreCase("canplayer") && args.length == 2) {
					permissions.debug(args[1]);
					return true;
				} else if ( args[0].equalsIgnoreCase("reload") ) {
					this.log(Level.INFO, TradeCraftLocalization.get("RESTARTING_PLUGIN"),
										 TradeCraft.pluginName);
					this.disable();
					this.enable();
					this.log(Level.INFO, TradeCraftLocalization.get("RESTARTING_PLUGIN_DONE"),
										 TradeCraft.pluginName);
				}
				return true;
			}
			return false;
		} else {
			return false;
		}

		return true;
	}

	void displayShops(Player p) {
		String name = p.getName();
		ArrayList<TradeCraftDataInfo> list = data.shopsOwned(name);
		if (list.size() == 0) {
			p.sendMessage(TradeCraftLocalization.get("YOU_DONT_OWN_ANY_SHOPS"));
			return;
		}
		p.sendMessage(TradeCraftLocalization.get("YOUR_SHOPS"));
		for (TradeCraftDataInfo info : list) {
			p.sendMessage(TradeCraftLocalization.get("ITEM") +": "+ this.configuration.get(info.itemType).name +"("+ info.itemType.toShortString() +")"
					+" "+ TradeCraftLocalization.get("AMOUNT") +": "+ info.itemAmount +" "
					+ this.getCurrencyName() +": "+ info.currencyAmount);

		}

	}

	void sendMessage(Player player, String format, Object... args) {
		String message = String.format(format, args);
		player.sendMessage(message);
	}

	void trace(Player player, String format, Object... args) {
		if (properties.getEnableDebugMessages()) {
			sendMessage(player, format, args);
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
				Block sideBlock = block.getFace(side);
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
			block = player.getWorld().getBlockAt(block.getX(),
					block.getY() + 1, block.getZ());
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

		trace(player, "You clicked a sign at %d, %d, %d.", x, y, z);

		Sign sign = (Sign) player.getWorld().getBlockAt(x, y, z).getState();

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

		Block blockBelowSign = player.getWorld().getBlockAt(x, y - 1, z);

		if (blockBelowSign.getType() != Material.CHEST) {
			trace(player, "There is no chest beneath the sign.");
			return null;
		}

		Chest chest = (Chest) player.getWorld().getBlockAt(x, y - 1, z).getState();

		if (itemName.toLowerCase().equals("repair")) {
			if (!properties.getRepairShopsEnabled()) {
				trace(player, "Repair shops are not enabled.");
				return null;
			}

			if (!player.isOp()) {
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
			return new TradeCraftInfiniteShop(this, sign, chest);
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
		return new TradeCraftExchangeRate(signLine);	}

	static int getMaxStackSize(int itemType) {
		return Material.getMaterial(itemType).getMaxStackSize();
	}

	public void onLoad() {
		// TODO Auto-generated method stub

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
			this.sendMessage(player, "/tc [help]"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_HELP_THIS_TEXT"));
			this.sendMessage(player, "/tc shops"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_SHOPS"));
			if ( this.permissions.canSetCurrency(player) ) {
				this.sendMessage(player, "/tc currency [id[;data]]"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_CURRENCY_OPT_PARAM_GETSET_CURRENCY"));
			} else {
				this.sendMessage(player, "/tc currency"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_CURRENCY_GET_CURRENCY"));
			}
			if ( this.permissions.canReload(player) ) {
				this.sendMessage(player, "/tc reload"+ ChatColor.GRAY +" "+ TradeCraftLocalization.get("TC_RELOAD"));
			}
		} else {
			// console command help
			this.log(Level.INFO, TradeCraftLocalization.get("POSSIBLE_COMMANDS_FOR_THE_PLUGIN"),
								 TradeCraft.pluginName);
			this.log(Level.INFO, "tc [help]: "+ TradeCraftLocalization.get("TC_HELP_THIS_TEXT"));
			this.log(Level.INFO, "tc canPlayer playername: "+ TradeCraftLocalization.get("TC_CAN_PLAYER"));
			this.log(Level.INFO, "tc reload: "+ TradeCraftLocalization.get("TC_RELOAD"));
		}
	}

	
	/**
	 * Return the last modified time stamp of a resource with the given file path.
	 * @param filePath
	 * @return -1 in case of errors, or else the seconds since epoch at which point this resource was last modified. 
	 */
	public static long resourceLastModified(String filePath) {
		URL resource = TradeCraft.class.getResource(filePath);
		if ( resource == null) {
			return -1;
		}
		URLConnection resConn;
		try {
			resConn = resource.openConnection();
		} catch ( IOException ioe ) {
			return -1;
		}
		if ( resConn == null ) {
			return -1;
		}
		long lastModified = resConn.getLastModified();
		// calling getLastModified apparently opens an InputStream that should be closed
		try {
			resConn.getInputStream().close();
		} catch ( Exception e ) {}
		// finally return the last modified time code
		return lastModified;
	}
	
	public void log(Level level, String format, Object... args) {
		this.log.log(level, TradeCraft.pluginName +": "+ String.format(format, args));
	}

}