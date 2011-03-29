package com.mjmr89.TradeCraft;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;


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
        ArrayList<TradeCraftShop> shops = plugin.getShopsFromBlock(player, block);
                
        if (shops.size() == 0) {
            return;
        }

        // Go through all shops in the list and check whether the player can destroy them all first.
        // Only if that is the case proceed to destroy.
        for ( TradeCraftShop shop : shops ) {
        	if (!shop.playerCanDestroy(player) && !plugin.permissions.canDestroyShops(player) ||
        			shop.shopCanBeWithdrawnFrom() ) {
        		// cannot destroy this shop, so cancel destruction, use distinct error messages 
        		if ( shop.shopCanBeWithdrawnFrom() ) {
                    plugin.sendMessage(player, "All items and gold must be withdrawn before you can destroy this sign or chest!");
        		} else {
        			if ( block.getType() == Material.WALL_SIGN ) {
        				plugin.sendMessage(player, "You can't destroy this sign!");
        			} else if ( block.getType() == Material.CHEST ) {
        				plugin.sendMessage(player, "You can't destroy this chest!");
        			} else {
        				plugin.sendMessage(player, "You can't destroy this block because there are signs attached to it!");
        			}
        		}
        		stopDestruction(block,e);
                return;
        	}
        }
        // player can destroy all shops, so proceed
        for ( TradeCraftShop shop : shops ) {
        	plugin.data.deleteShop(shop);
            return;
        }
    }
	
	public void stopDestruction(Block b, BlockBreakEvent e){
		if(b.getState() instanceof Sign){
			Sign sign = (Sign)b.getState();
			String[] lines = sign.getLines();
			e.setCancelled(true);
			for(int i = 0;i<4;i++){
				sign.setLine(i, lines[i]);
			}
			
			sign.update(true);
			return;
		} else {
			e.setCancelled(true);			
		}
		
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
