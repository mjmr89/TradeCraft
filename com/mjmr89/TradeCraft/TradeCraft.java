package com.mjmr89.TradeCraft;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class TradeCraft extends JavaPlugin {

	// The plugin name.
	static final String pluginName = "TradeCraft";

	private static final Pattern ratePattern = Pattern
			.compile("\\s*(\\d+)\\s*:\\s*(\\d+)\\s*");

	// Stuff used to interact with the server.
	final Logger log = Logger.getLogger("Minecraft");
	final Server server = this.getServer();

	// Objects used by the plugin.
	static Material currency;
	TradeCraftPropertiesFile properties = new TradeCraftPropertiesFile();
	TradeCraftConfigurationFile configuration = new TradeCraftConfigurationFile(
			this);
	TradeCraftDataFile data = new TradeCraftDataFile(this);

	private final TradeCraftBlockListener blockListener = new TradeCraftBlockListener(
			this);
	private final TradeCraftPlayerListener playerListener = new TradeCraftPlayerListener(
			this);
	public TradeCraftPermissions permissions = new TradeCraftPermissions(this);
	public Permissions permissionsPlugin = null;
	public boolean permEnabled = false;

	public void onDisable() {
	}

	public void onEnable() {

		properties = new TradeCraftPropertiesFile();
		configuration = new TradeCraftConfigurationFile(this);
		data = new TradeCraftDataFile(this);

		currency = Material.getMaterial(properties.getCurrencyTypeId());
		configuration.load();
		data.load();
		permissions.setupPermissions();

		PluginManager pm = this.getServer().getPluginManager();

		pm.registerEvent(Type.PLAYER_INTERACT, playerListener,
				Priority.Normal, this);

		pm
				.registerEvent(Type.SIGN_CHANGE, blockListener,
						Priority.Normal, this);
		pm
				.registerEvent(Type.BLOCK_BREAK, blockListener,
						Priority.Normal, this);

		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println(pdfFile.getName() + " version "
				+ pdfFile.getVersion() + " is enabled!");

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		String name = command.getName();
		Player p;

		if (sender instanceof Player) {
			p = (Player) sender;

			if (name.equalsIgnoreCase("setcurrency") && args.length == 1) {
				try {
					int cid = Integer.parseInt(args[0]);
					currency = Material.getMaterial(cid);
					p.sendMessage("Currency is set to " + currency);
				} catch (NumberFormatException nfe) {
					Material m = Material.getMaterial(args[0]);
					if (m != null) {
						currency = m;
						p.sendMessage("Currency is set to " + currency);

					}
				} finally {
					return true;
				}
			} else if (name.equalsIgnoreCase("displaycurrency")
					&& args.length == 0) {
				p.sendMessage("Currency is: " + currency);
			} else if (name.equalsIgnoreCase("canplayer") && args.length == 1) {
				permissions.debug(args[0]);
			} else if (name.equalsIgnoreCase("myshops")) {
				displayShops(p);
			}

		} else {
			return false;
		}

		return true;
	}

	void displayShops(Player p) {
		String name = p.getName();
		ArrayList<TradeCraftDataInfo> list = data.shopsOwned(name);
		if (list.size() == 0) {
			p.sendMessage("You don't own any shops!");
			return;
		}
		p.sendMessage("Your shops:");
		for (TradeCraftDataInfo info : list) {

			p.sendMessage("Item: " + Material.getMaterial(info.itemType)
					+ " Amount: " + info.itemAmount + "Gold: "
					+ info.currencyAmount);

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

	public void onLoad() {
		// TODO Auto-generated method stub

	}

}