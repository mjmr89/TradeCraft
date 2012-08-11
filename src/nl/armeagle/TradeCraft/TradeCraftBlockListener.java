package nl.armeagle.TradeCraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class TradeCraftBlockListener implements Listener{
    
    private TradeCraft plugin;
    
    TradeCraftBlockListener(TradeCraft plugin){
        this.plugin = plugin;
    }
    
    public void debug(String str){
        plugin.getServer().broadcastMessage(str);
    }
        
    @EventHandler
    public void onNormalBlockBreak(BlockBreakEvent e) {
        this.onBlockBreak(e, EventPriority.NORMAL);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMonitorBlockBreak(BlockBreakEvent e) {
        this.onBlockBreak(e, EventPriority.MONITOR);
    }
    
    private void onBlockBreak(BlockBreakEvent e, EventPriority p) {
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
        if (EventPriority.NORMAL == p) {
            for ( TradeCraftShop shop : shops ) {
                if (!shop.playerCanDestroy(player) && !plugin.permissions.canDestroyShops(player) ||
                        shop.shopCanBeWithdrawnFrom() ) {
                    // cannot destroy this shop, so cancel destruction, use distinct error messages 
                    if ( shop.shopCanBeWithdrawnFrom() ) {
                        plugin.sendMessage(player, TradeCraftLocalization.get("ALL_ITEMS_MUST_BE_WITHDRAWN"));
                    } else {
                        if ( block.getType() == Material.WALL_SIGN ) {
                            plugin.sendMessage(player, TradeCraftLocalization.get("YOU_CANT_DESTROY_THIS_SIGN"));
                        } else if ( block.getType() == Material.CHEST ) {
                            plugin.sendMessage(player, TradeCraftLocalization.get("YOU_CANT_DESTROY_THIS_CHEST"));
                        } else {
                            plugin.sendMessage(player, TradeCraftLocalization.get("YOU_CANT_DESTROY_THIS_BLOCK_ATTACHED"));
                        }
                    }
                    stopDestruction(block,e);
                    return;
                }
            }
        }
        
        if (EventPriority.MONITOR == p && ! e.isCancelled()) {
            // player can destroy all shops, so proceed
            for ( TradeCraftShop shop : shops ) {
                plugin.data.deleteShop(shop);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onMonitorSignChange(SignChangeEvent e) {
        this.onSignChange(e, EventPriority.MONITOR);
    }

    @EventHandler
    public void onNormalSignChange(SignChangeEvent e) {
        this.onSignChange(e, EventPriority.NORMAL);
    }
    
    public void onSignChange(SignChangeEvent event, EventPriority priority) {
        if ( !this.plugin.isEnabled() ) {
            return;
        }
    
        // Check whether this block is (still) a sign. Can be revoked already in case the sign was temporary,
        // for example when the plugin SimpleSignEdit is being used.
        if ( event.getBlock().getType() != Material.SIGN_POST &&
             event.getBlock().getType() != Material.WALL_SIGN ) {
            return;
        }

        Sign sign = (Sign) event.getBlock().getState();

        Player player = event.getPlayer();
        String ownerName = player.getName();

        String itemName = plugin.getItemName(event.getLines());

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

        TradeCraftExchangeRate buyRate = new TradeCraftExchangeRate(event.getLine(1));
        TradeCraftExchangeRate sellRate = new TradeCraftExchangeRate(event.getLine(2));
        // no buy rate means this is an infinite shop
        if ( !buyRate.isValid() && !sellRate.isValid() ) {
            if (plugin.permissions.canMakeInfShops(player)){
                plugin.trace(player, "sign change, infinite chest of %s", itemName);
                return;
            }

            if (EventPriority.NORMAL == priority) {
                plugin.sendMessage(player, TradeCraftLocalization.get("YOU_CANT_CREATE_INF_SHOPS"));
                event.setCancelled(true);
                return;
            }
        }
        // there is a buy rate, so this is a player owned shop

        if (EventPriority.NORMAL == priority && !plugin.permissions.canMakePlayerShops(player)){
            plugin.sendMessage(player, TradeCraftLocalization.get("YOU_DONT_HAVE_PERM_CREATE_PLAYER_SHOP"));
            event.setCancelled(true);
            return;
        }

        // check whether the player doesn't have too many shops
        if (EventPriority.NORMAL == priority) {
            int totalShopLimit = TradeCraft.properties.getPlayerTotalShopLimit();
            int worldShopLimit = TradeCraft.properties.getPlayerWorldShopLimit();
            if (plugin.data.getPlayerShopCount(player) >= totalShopLimit) {
                plugin.sendMessage(player, TradeCraftLocalization.get("TOTAL_SHOP_LIMIT_X"), totalShopLimit);
                event.setCancelled(true);
                return;
            } else if (plugin.data.getPlayerShopCount(player, player.getWorld()) >= worldShopLimit) {
                plugin.sendMessage(player, TradeCraftLocalization.get("WORLD_SHOP_LIMIT_X"), worldShopLimit);
                event.setCancelled(true);
                return;
            }

        }

        if (EventPriority.MONITOR == priority && !event.isCancelled()) {
            plugin.trace(player, "Setting owner of sign to: %s", ownerName);
            // set the player name on the last line
            event.setLine(3, "-"+ ownerName.substring(0, Math.min(ownerName.length(), 15)) +"-");
            plugin.data.createNewSign(ownerName, itemInfo, sign);
        }
        
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
    }
    
    // prevent pistons from pushing away (the block behind) a sign. Block all retract and extend events that would move a block behind a sign, also for shop owners
    @EventHandler
    public void onNormalBlockPistonRetract(BlockPistonRetractEvent e) {
        if (e.isSticky()) {
            Block block = e.getRetractLocation().getBlock();
            ArrayList<TradeCraftShop> shops = plugin.getShopsFromBlock(null, block);
            
            if (shops.size() != 0) {
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onNormalBlockPistonExtend(BlockPistonExtendEvent e) {
        List<Block> blocks = e.getBlocks();
        for (Block block:blocks) {
            ArrayList<TradeCraftShop> shops = plugin.getShopsFromBlock(null, block);
            
            if (shops.size() != 0) {
                e.setCancelled(true);
                return;
            }
        }
    }
    
    public void stopDestruction(Block b, BlockBreakEvent e){
        if(b.getState() instanceof Sign){
            Sign sign = (Sign)b.getState();
            String[] lines = sign.getLines();
            for(int i = 0;i<4;i++){
                sign.setLine(i, lines[i]);
            }
            
            sign.update(true);
        }
        e.setCancelled(true);
    }
}
