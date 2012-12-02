package nl.armeagle.TradeCraft;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.Listener;


public class TradeCraftPlayerListener implements Listener{

    private TradeCraft plugin;

    TradeCraftPlayerListener(TradeCraft plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if ( !this.plugin.isEnabled() ) {
            return;
        }

        Player player = e.getPlayer();
        if ( player == null ) {
            return;
        }

        Block blockClicked = null;
        if ( e.getAction() == Action.RIGHT_CLICK_BLOCK ) {
            blockClicked = e.getClickedBlock();
        } else if ( e.getAction() == Action.RIGHT_CLICK_AIR ) {
            // If player is holding a solid block and in the way of placing the block, a click air event will be fired instead.
            // Look for up to two blocks away.
            blockClicked = player.getTargetBlock(null, 2);
        }

        if ( blockClicked != null ) {
            TradeCraftShop shop = plugin.getShopFromSignBlock(player, blockClicked);

            if (shop == null) {
                return;
            }

            shop.handleRightClick(player);
            e.setCancelled(true);
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
}
