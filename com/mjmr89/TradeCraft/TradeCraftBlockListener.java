package com.mjmr89.TradeCraft;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.block.SignChangeEvent;


public class TradeCraftBlockListener extends BlockListener{
	
	private TradeCraft plugin;
	
	TradeCraftBlockListener(TradeCraft plugin){
		this.plugin = plugin;
	}
	
	public void debug(String str){
		plugin.getServer().broadcastMessage(str);
	}
	
	@Override
	public void onBlockRightClick(BlockRightClickEvent e){
		Block blockClicked = e.getBlock();
		Player player = e.getPlayer();
		
        TradeCraftShop shop = plugin.getShopFromSignBlock(player, blockClicked);

        if (shop == null) {
            return;
        }

        shop.handleRightClick(player);
    }
	
	@Override
	public void onBlockBreak(BlockBreakEvent e) {
		Player player = e.getPlayer();
		Block block = e.getBlock();
		
        TradeCraftShop shop = plugin.getShopFromSignOrChestBlock(player, block);

        if (shop == null) {
            
            return;
        }

        if (shop.playerCanDestroy(player)) {
            if (!shop.shopCanBeWithdrawnFrom()) {
                
                return;
            }
 
            plugin.sendMessage(player, "All items and gold must be withdrawn before you can destroy this sign or chest!");

            e.setCancelled(true);
            return;
        }

        plugin.sendMessage(player, "You can't destroy this sign or chest!");

        e.setCancelled(true);
        return;
    }
	
	public void onSignChange(SignChangeEvent e) {
		Player player = e.getPlayer();
		Sign sign = (Sign) e.getBlock().getState();
		
        String ownerName = plugin.getOwnerName(sign);

        if (ownerName == null) {
            String itemName = plugin.getItemName(sign);

            if (itemName == null) {
            	
                return;
            }
            
            if (plugin.permissions.canMakeInfShops(player)){
            	
            	return;
            }

            plugin.sendMessage(player, "You can't create infinite shops!");

            
            return;
        }

        if ( plugin.permissions.canMakePlayerShops(player)){
            
            return;
        }

        if (player.getName().startsWith(ownerName)) {
            plugin.data.setOwnerOfSign(player.getName(), sign);
            
            return;
        }

        plugin.sendMessage(player, "You can't create signs with other players names on them!");

        
        return;
    }

}
