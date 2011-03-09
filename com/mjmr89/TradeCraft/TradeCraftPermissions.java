package com.mjmr89.TradeCraft;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class TradeCraftPermissions {

	static boolean permEnabled = false;
	PermissionHandler permHandler;
	TradeCraft plugin;

	TradeCraftPermissions(TradeCraft plugin) {
		this.plugin = plugin;
	}

	public void setupPermissions() {
		Plugin test = plugin.getServer().getPluginManager().getPlugin(
		"Permissions");

		if (plugin.permissions == null) {
			if(test != null){
				permHandler = ((Permissions) test).getHandler();
				permEnabled = true;
			}else{
				plugin.log.info("Permission system not detected, defaulting to OP");
			}
			
		}
	}
	
	public boolean canBuy(Player p){
		return permHandler.has(p, "TradeCraft.canBuy");
	}
	
	public boolean canSell(Player p){
		return permHandler.has(p, "TradeCraft.canSell");
	}
	
	public boolean canMakeInfShops(Player p){
		return permHandler.has(p, "TradeCraft.canMakeInfShops");
	}
	
	public boolean canMakePlayerShops(Player p){
		return permHandler.has(p, "TradeCraft.canMakePlayerShops");
	}
	
	public boolean canDestroyShops(Player p){
		return permHandler.has(p, "TradeCraft.canDestroy");
	}
	
	public void debug(Player p){
		String name = p.getName();
		
		plugin.log.info("" + name + " has:");
		plugin.log.info("canbuy " + canBuy(p));
		plugin.log.info("cansell " + canSell(p));
		plugin.log.info("canmakeinf " + canMakeInfShops(p));
		plugin.log.info("canmakepersonal " + canMakePlayerShops(p));
		plugin.log.info("candestroy " + canDestroyShops(p));

		
	}
	
}
