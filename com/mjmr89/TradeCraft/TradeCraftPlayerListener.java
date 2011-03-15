package com.mjmr89.TradeCraft;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;


public class TradeCraftPlayerListener extends PlayerListener{

	private TradeCraft plugin;
	
	TradeCraftPlayerListener(TradeCraft plugin){
		this.plugin = plugin;
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
        plugin.permissions.debug(player.getName());
    }
	
}
