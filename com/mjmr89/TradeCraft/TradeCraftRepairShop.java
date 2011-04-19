package com.mjmr89.TradeCraft;

import java.util.List;

import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradeCraftRepairShop extends TradeCraftShop {

    public TradeCraftRepairShop(TradeCraft plugin, Sign sign, Chest chest) {
        super(plugin, sign, chest);
    }

    public void handleRightClick(Player player) {
        int currencyAmount = chest.getAmountOfCurrencyInChest();
        List<ItemStack> items = chest.getNonCurrencyItems();
        int repairCost = plugin.properties.getRepairCost();

        if (currencyAmount == 0 && items.size() == 0) {
            plugin.sendMessage(player, "It costs %d "+ TradeCraft.getCurrencyName() +" to repair an item.", repairCost);
            return;
        }

        int actualCost = items.size() * repairCost;

        if (items.size() == 0) {
            plugin.sendMessage(player, "With this much "+ TradeCraft.getCurrencyName() +", you can repair %d items.", currencyAmount / repairCost);
            return;
        }

        if (currencyAmount < actualCost) {
            if (currencyAmount > 0) {
                plugin.sendMessage(player, "That's not enough "+ TradeCraft.getCurrencyName() +".");
            }
            plugin.sendMessage(player, "You need %d "+ TradeCraft.getCurrencyName() +" to repair all this.", actualCost);
            return;
        }

        chest.clear();

        for (ItemStack item : items) {
            chest.add(new TradeCraftItem(item.getTypeId(), item.getData().getData()), 1);
        }

        chest.add(TradeCraft.currency, (currencyAmount - actualCost));

        chest.update();

        plugin.sendMessage(player, "You repaired %d items for %d "+ TradeCraft.getCurrencyName() +".", items.size(), actualCost);
    }

    public boolean playerCanDestroy(Player player) {
        return true;
    }

    public boolean shopCanBeWithdrawnFrom() {
        return false;
    }
}
