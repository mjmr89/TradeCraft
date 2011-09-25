package nl.armeagle.TradeCraft;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class TradeCraftPermissions {

	PermissionHandler permHandler = null;
	TradeCraft plugin;

	TradeCraftPermissions(TradeCraft plugin) {
		this.plugin = plugin;
	}

	public void setupPermissions() {
		Plugin test = plugin.getServer().getPluginManager().getPlugin("Permissions");

		if (permHandler == null) {
		    if (test != null) {
		        this.permHandler = ((Permissions)test).getHandler();
		        plugin.permEnabled = true;
		        plugin.log.info("[TradeCraft] has recognized Permissions");
		    } 
		}
	}
	
	public boolean canBuy(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canBuy");
		} else {
			return p.hasPermission("TradeCraft.canBuy");
		}
	}
	
	public boolean canSell(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canSell");
		} else {
			return p.hasPermission("TradeCraft.canSell");
		}
	}
	
	public boolean canMakeInfShops(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canMakeInfShops");
		} else {
			return p.hasPermission("TradeCraft.canMakeInfShops");
		}
	}
	
	public boolean canMakePlayerShops(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canMakePlayerShops");
		} else {
			return p.hasPermission("TradeCraft.canMakePlayerShops");
		}
	}
	
	public boolean canDestroyShops(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canDestroyShops");
		} else {
			return p.hasPermission("TradeCraft.canDestroyShops");
		}
	}
	
	public boolean canSetCurrency(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canSetCurrency");
		} else {
			return p.hasPermission("TradeCraft.canSetCurrency");
		}
	}
	
	public boolean canReload(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canReload");
		} else {
			return p.hasPermission("TradeCraft.canReload");
		}
	}
	
	public boolean canQueryOtherShops(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canQueryOtherShops");
		} else {
			return p.hasPermission("TradeCraft.canQueryOtherShops");
		}
	}
	
	public boolean canQueryPlayer(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canQueryPlayer");
		} else {
			return p.hasPermission("TradeCraft.canQueryPlayer");
		}
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
