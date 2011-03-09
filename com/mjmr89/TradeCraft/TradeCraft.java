package com.mjmr89.TradeCraft;

import java.io.File;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;

public class TradeCraft extends JavaPlugin {

	// The plugin name.
	static final String pluginName = "TradeCraft";

	// The plugin version. The first part is the version of hMod this is built
	// against.
	// The second part is the release number built against that version of hMod.
	// A "+" at the end means this is a development version that hasn't been
	// released yet.
	static final String version = "133.3+";

	private static final Pattern ratePattern = Pattern
			.compile("\\s*(\\d+)\\s*:\\s*(\\d+)\\s*");

	// Stuff used to interact with the server.
	final Logger log = Logger.getLogger("Minecraft");
	final Server server = this.getServer();

	// Objects used by the plugin.
	TradeCraftPropertiesFile properties = new TradeCraftPropertiesFile();
	TradeCraftConfigurationFile configuration = new TradeCraftConfigurationFile(
			this);
	TradeCraftDataFile data = new TradeCraftDataFile(this);

	private final TradeCraftBlockListener blockListener = new TradeCraftBlockListener(
			this);
	private final TradeCraftPlayerListener playerListener = new TradeCraftPlayerListener(
			this);
	public TradeCraftPermissions permissions = new TradeCraftPermissions(this);

	public void onDisable() {
	}

	public void onEnable() {
		log.info(pluginName + " " + version + " initialized");

		properties = new TradeCraftPropertiesFile();
		configuration = new TradeCraftConfigurationFile(this);
		data = new TradeCraftDataFile(this);

		configuration.load();
		buildConfiguration();
		data.load();
		permissions.setupPermissions();

		PluginManager pm = this.getServer().getPluginManager();

		pm.registerEvent(Type.BLOCK_RIGHTCLICKED, blockListener,
				Priority.Normal, this);

		pm.registerEvent(Type.BLOCK_DAMAGED,// block broken
				blockListener, Priority.Normal, this);

		pm
				.registerEvent(Type.SIGN_CHANGE, blockListener,
						Priority.Normal, this);

		pm.registerEvent(Type.PLAYER_CHAT, playerListener, Priority.Low, this);
		pm.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Low, this);

		
	}

	public void buildConfiguration() {
		Configuration c = getConfiguration();
		if (c != null)
			addItem(c, "Cobblestone", 4, "64:1", "64:1");

		if (!c.save()) {
			getServer()
					.getLogger()
					.warning(
							"Unable to persist configuration files, changes will not be saved.");
		}

	}

	public void addItem(Configuration c, String iName, int id, String buyStr,
			String sellStr) {

		// c.setProperty("Items.",iName);
		c.setProperty("Items." + iName + ".ID", id);
		c.setProperty("Items." + iName + ".Buy", buyStr == null ? sellStr
				: buyStr);
		c.setProperty("Items." + iName + ".Sell", sellStr == null ? buyStr
				: sellStr);

	}

	void sendMessage(Player player, String format, Object... args) {
		String message = String.format(format, args);
		player.sendMessage(message);
	}

	void trace(Player player, String format, Object... args) {
		if (true) {// properties.getEnableDebugMessages()) {
			sendMessage(player, format, args);
		}
	}

	// public boolean playerIsInGroup(Player player, String group) {
	// if (group.equals("*")) {
	// return true;
	// }
	// return player.isInGroup(group);
	// }

	TradeCraftShop getShopFromSignOrChestBlock(Player player, Block block) {
		if (block.getType() == Material.CHEST) {
			block = player.getWorld().getBlockAt(block.getX(),
					block.getY() + 1, block.getZ());
		}

		return getShopFromSignBlock(player, block);
	}

	TradeCraftShop getShopFromSignBlock(Player player, Block block) {
		if (block.getType() != Material.WALL_SIGN) {
			blockListener.debug("Thinks material is not a wall sign");
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

		String itemName = getItemName(sign);

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

		Chest chest = (Chest) player.getWorld().getBlockAt(x, y - 1, z)
				.getState();

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

		String ownerName = getOwnerName(sign);
		player.sendMessage("Owner is : " + ownerName);

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

	String getItemName(Sign sign) {
		return getSpecialText(sign, "[", "]");
	}

	String getOwnerName(Sign sign) {
		return getSpecialTextOnLine(sign, "-", "-", 3);
	}

	private String getSpecialText(Sign sign, String prefix, String suffix) {
		for (int i = 0; i < 4; i++) {
			String text = getSpecialTextOnLine(sign, prefix, suffix, i);

			if (text != null) {
				return text;
			}
		}

		return null;
	}

	private String getSpecialTextOnLine(Sign sign, String prefix,
			String suffix, int lineNumber) {
		String signText = sign.getLine(lineNumber);

		if (signText == null) {
			return null;
		}

		signText = signText.trim();

		if (signText.startsWith(prefix) && signText.endsWith(suffix)
				&& signText.length() > 2) {

			String text = signText.substring(1, signText.length() - 1);
			text = text.trim();

			if (text.equals("")) {
				return null;
			}

			return text;
		}

		return null;
	}

	TradeCraftExchangeRate getExchangeRate(Sign sign, int lineNumber) {
		TradeCraftExchangeRate rate = new TradeCraftExchangeRate();

		String signText = sign.getLine(lineNumber);

		Matcher matcher = ratePattern.matcher(signText);

		if (matcher.find()) {
			rate.amount = Integer.parseInt(matcher.group(1));
			rate.value = Integer.parseInt(matcher.group(2));
		}

		return rate;
	}

	static int getMaxStackSize(int itemType) {
		/*
		 * switch (Material.getMaterial(itemType)) { case APPLE: case
		 * Golden_Apple: case Pork: case Grilled_Pork: case Bread: case Bucket:
		 * case Water_Bucket: case Lava_Bucket: case Milk_Bucket: case
		 * Wood_Sword: case Wood_Spade: case Wood_Pickaxe: case Wood_Axe: case
		 * Wood_Hoe: case Stone_Sword: case Stone_Spade: case Stone_Pickaxe:
		 * case Stone_Axe: case Stone_Hoe: case Iron_Sword: case Iron_Spade:
		 * case Iron_Pickaxe: case Iron_Axe: case Iron_Hoe: case Diamond_Sword:
		 * case Diamond_Spade: case Diamond_Pickaxe: case Diamond_Axe: case
		 * Diamond_Hoe: case Gold_Sword: case Gold_Spade: case Gold_Pickaxe:
		 * case Gold_Axe: case Gold_Hoe: case Leather_Helmet: case
		 * Leather_Chestplate: case Leather_Leggings: case Leather_Boots: case
		 * Iron_Helmet: case Iron_Chestplate: case Iron_Leggings: case
		 * Iron_Boots: case Diamond_Helmet: case Diamond_Chestplate: case
		 * Diamond_Leggings: case Diamond_Boots: case Gold_Helmet: case
		 * Gold_Chestplate: case Gold_Leggings: case Gold_Boots: return 1; case
		 * SnowBall: return 16; } return 64;
		 */
		return 64;
	}

}