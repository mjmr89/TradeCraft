package nl.armeagle.TradeCraft;

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
	              System.out.println("[TradeCraft] has recognized Permissions");
	          } 
	      }
	}
	
	public boolean canBuy(Player p){
		if(plugin.permEnabled == true){
			return permHandler.has(p, "TradeCraft.canBuy");
		}else
			return true;
	}
	
	public boolean canSell(Player p){
		if(plugin.permEnabled == true){
			return permHandler.has(p, "TradeCraft.canSell");
		}else
				return true;
	}
	
	public boolean canMakeInfShops(Player p){
		if(plugin.permEnabled == true){
			return permHandler.has(p, "TradeCraft.canMakeInfShops");
		}else
				return p.isOp();
	}
	
	public boolean canMakePlayerShops(Player p){
		if(plugin.permEnabled == true){
			return permHandler.has(p, "TradeCraft.canMakePlayerShops");
		}else
				return p.isOp();
	}
	
	public boolean canDestroyShops(Player p){
		if(plugin.permEnabled == true){
			return permHandler.has(p, "TradeCraft.canDestroyShops");
		}else
				return p.isOp();
	}
	
	public boolean canSetCurrency(Player p) {
		if ( plugin.permEnabled ) {
			return permHandler.has(p, "TradeCraft.canSetCurrency");
		} else {
			return p.isOp();
		}
	}
	
	public void debug(String n){
		Player p = plugin.getServer().getPlayer(n);
		if(p == null){
			plugin.getServer().broadcastMessage("/canPlayer used with a name of player who is not online.");
			return;
		}
		String name = p.getName();
		plugin.log.info("" + name + " has:");
		plugin.log.info("canbuy " + canBuy(p));
		plugin.log.info("cansell " + canSell(p));
		plugin.log.info("canmakeinf " + canMakeInfShops(p));
		plugin.log.info("canmakepersonal " + canMakePlayerShops(p));
		plugin.log.info("candestroy " + canDestroyShops(p));

		
	}
	
}
