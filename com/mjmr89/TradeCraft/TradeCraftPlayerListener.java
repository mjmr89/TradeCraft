package com.mjmr89.TradeCraft;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;


public class TradeCraftPlayerListener extends PlayerListener{

	private TradeCraft plugin;
	
	TradeCraftPlayerListener(TradeCraft plugin){
		this.plugin = plugin;
	}
	@Override
	public void onPlayerInteract(PlayerInteractEvent e) {
		if ( !this.plugin.isEnabled() ) {
			return;
		}
		
		if ( e.getAction() == Action.RIGHT_CLICK_BLOCK ) {
			Block blockClicked = e.getClickedBlock();
			Player player = e.getPlayer();
			
	        TradeCraftShop shop = plugin.getShopFromSignBlock(player, blockClicked);

	        if (shop == null) {
	            return;
	        }

	        shop.handleRightClick(player);
		}
	}
	
    @SuppressWarnings("unused")
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
    
    @SuppressWarnings("unused")
	private void displaySecurity(Player player) {
        plugin.permissions.debug(player.getName());
    }
	
}
