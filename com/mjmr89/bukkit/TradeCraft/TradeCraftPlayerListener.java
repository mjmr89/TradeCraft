package com.mjmr89.bukkit.TradeCraft;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;


public class TradeCraftPlayerListener extends PlayerListener{

	private TradeCraft plugin;
	
	TradeCraftPlayerListener(TradeCraft plugin){
		this.plugin = plugin;
	}
	
	
    public void onPlayerChat(PlayerChatEvent e){
    //(Player player, String[] split) {
    	Player player = e.getPlayer();
    	String[] split = e.getMessage().split(" ");
    	
    	plugin.permissions.debug(player);
    	
        if (split[0].toLowerCase().equals("/tradecraft")) {
            if (split.length == 1) {
                displayHelp(player);
                return;
            } else {
                String command = split[1].toLowerCase();
                if (command.equals("version")) {
                    displayVersion(player);
                    return;
                } else if (command.equals("items")) {
                    displayItems(player);
                    return;
                } else if (command.equals("security")) {
                    displaySecurity(player);
                    return;
                }
            }
        }
        return;
    }

    private void displayHelp(Player player) {
        plugin.sendMessage(player, "/tradecraft version");
        plugin.sendMessage(player, "  - show the current version number");
        plugin.sendMessage(player, "/tradecraft items");
        plugin.sendMessage(player, "  - show item names that can appear on signs");
        plugin.sendMessage(player, "/tradecraft security");
        plugin.sendMessage(player, "  - show your permissions");
    }

    private void displayVersion(Player player) {
        plugin.sendMessage(player, "TradeCraft version %1$s", TradeCraft.version);
    }

    private void displayItems(Player player) {
        String[] names = plugin.configuration.getNames();
        StringBuilder sb = new StringBuilder(); 
        for (String name : names) {
            if (sb.length() + name.length() > 60) {
                plugin.sendMessage(player, sb.toString());
                sb = new StringBuilder();
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        if (sb.length() > 0) {
            plugin.sendMessage(player, sb.toString());
        }
    }

    private void displaySecurity(Player player) {
//        plugin.sendMessage(player,
//                "Can create infinite shops: %s",
//                plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToCreateInfiniteShops()));
//        plugin.sendMessage(player,
//                "Can create player-owned shops: %s",
//                plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToCreatePlayerOwnedShops()));
//        plugin.sendMessage(player,
//                "Can buy from shops: %s",
//                plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToBuyFromShops()));
//        plugin.sendMessage(player,
//                "Can sell to shops: %s",
//                plugin.playerIsInGroup(player, plugin.properties.getGroupRequiredToSellToShops()));
    }
	
}
