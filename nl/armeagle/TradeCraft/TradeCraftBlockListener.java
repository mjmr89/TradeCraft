package nl.armeagle.TradeCraft;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
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
	public void onBlockBreak(BlockBreakEvent e) {
		if ( !this.plugin.isEnabled() ) {
			return;
		}
		
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
                    plugin.sendMessage(player, "All items and currency must be withdrawn before you can destroy this sign or chest!");
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
		if ( !this.plugin.isEnabled() ) {
			return;
		}
		
		Player player = e.getPlayer();
		Sign sign = (Sign) e.getBlock().getState();
        String ownerName = player.getName();

        String itemName = plugin.getItemName(e.getLines());

        if (itemName == null) {
            plugin.trace(player, "sign change, no item name, ignore");
            return;
        }
        // Check whether this is an existing item. Try to prevent as little normal sign placement as possible.
        // Only treat signs with "[name]" as item line where "name" is found in the configuration (.txt) file.
        TradeCraftConfigurationInfo itemInfo = plugin.configuration.get(itemName); 
        if ( itemInfo == null ) {
            plugin.trace(player, "sign change, %s is not a valid item name, ignore this sign", itemName);
            return;
        }

        TradeCraftExchangeRate buyRate = new TradeCraftExchangeRate(e.getLine(1));
        TradeCraftExchangeRate sellRate = new TradeCraftExchangeRate(e.getLine(2));
        // no buy rate means this is an infinite shop
        if ( !buyRate.isValid() && !sellRate.isValid() ) {
            if (plugin.permissions.canMakeInfShops(player)){
                plugin.trace(player, "sign change, infinite chest of %s", itemName);
            	return;
            }
            
            plugin.sendMessage(player, "You can't create infinite shops!");
            e.setCancelled(true);
            return;
        }
        // there is a buy rate, so this is a player owned shop
        if ( !plugin.permissions.canMakePlayerShops(player)){
	        plugin.sendMessage(player, "You do not have the permission to create a player shop!");
	        e.setCancelled(true);
	        return;
        }

        plugin.trace(player, "Setting owner of sign to: %s", ownerName);
        // set the player name on the last line
        e.setLine(3, "-"+ ownerName.substring(0, Math.min(ownerName.length(), 15)) +"-");
        plugin.data.createNewSign(ownerName, itemInfo, sign);
        /*
        if ( this.plugin.properties.getStrictPlayerShopOwnerNameRequired() ) {
        	if (player.getName().equalsIgnoreCase(ownerName)) {
        		plugin.data.setOwnerOfSign(player.getName(), sign);
        		return;
        	}
        } else {
        	if (player.getName().startsWith(ownerName)) {
        		plugin.data.setOwnerOfSign(player.getName(), sign);
        		return;
        	}
        }
        */
        
        return;
    }
}
