package com.mjmr89.bukkit.TradeCraft;

import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public abstract class TradeCraftShop {
    protected final TradeCraft plugin;
    protected final Sign sign;
    protected final TradeCraftChest chest;

    public TradeCraftShop(TradeCraft plugin, Sign sign, Chest chest) {
        this.plugin = plugin;
        this.sign = sign;
        this.chest = new TradeCraftChest(chest);
    }

    public abstract void handleRightClick(Player player);

    public abstract boolean playerCanDestroy(Player player);

    public abstract boolean shopCanBeWithdrawnFrom();
}
