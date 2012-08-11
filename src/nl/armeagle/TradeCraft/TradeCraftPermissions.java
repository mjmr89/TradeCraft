package nl.armeagle.TradeCraft;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TradeCraftPermissions {
    TradeCraft plugin;

    TradeCraftPermissions(TradeCraft plugin) {
        this.plugin = plugin;
    }
    
    public boolean canBuy(Player p) {
        return p.hasPermission("TradeCraft.canBuy");
    }
    
    public boolean canSell(Player p) {
        return p.hasPermission("TradeCraft.canSell");
    }
    
    public boolean canMakeInfShops(Player p) {
        return p.hasPermission("TradeCraft.canMakeInfShops");
    }
    
    public boolean canMakePlayerShops(Player p) {
        return p.hasPermission("TradeCraft.canMakePlayerShops");
    }
    
    public boolean canDestroyShops(Player p) {
        return p.hasPermission("TradeCraft.canDestroyShops");
    }
    
    public boolean canSetCurrency(Player p) {
        return p.hasPermission("TradeCraft.canSetCurrency");
    }
    
    public boolean canReload(Player p) {
        return p.hasPermission("TradeCraft.canReload");
    }
    
    public boolean canQueryOtherShops(Player p) {
        return p.hasPermission("TradeCraft.canQueryOtherShops");
    }
    
    public boolean canQueryPlayer(Player p) {
        return p.hasPermission("TradeCraft.canQueryPlayer");
    }
    
    public void debug(CommandSender sender, String n){
        Player p = plugin.getServer().getPlayer(n);
        if(p == null){
            plugin.getServer().broadcastMessage("/tc canPlayer used with a name of player who is not online.");
            return;
        }
        String name = p.getName();
        sender.sendMessage("" + name + " has:");
        sender.sendMessage("canBuy " + canBuy(p));
        sender.sendMessage("canSell " + canSell(p));
        sender.sendMessage("canMakeInf " + canMakeInfShops(p));
        sender.sendMessage("canMakePersonal " + canMakePlayerShops(p));
        sender.sendMessage("canDestroy " + canDestroyShops(p));
        sender.sendMessage("canSetCurrency " + canSetCurrency(p));
        sender.sendMessage("canReload " + canReload(p));
        sender.sendMessage("canQueryOtherShops " + canQueryOtherShops(p));        
    }
    
}
