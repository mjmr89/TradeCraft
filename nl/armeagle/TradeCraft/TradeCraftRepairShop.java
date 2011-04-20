package nl.armeagle.TradeCraft;

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
            plugin.sendMessage(player, "It costs %d "+ plugin.getCurrencyName() +" to repair an item.", repairCost);
            return;
        }

        int actualCost = items.size() * repairCost;

        if (items.size() == 0) {
            plugin.sendMessage(player, "With this much "+ plugin.getCurrencyName() +", you can repair %d items.", currencyAmount / repairCost);
            return;
        }

        if (currencyAmount < actualCost) {
            if (currencyAmount > 0) {
                plugin.sendMessage(player, "That's not enough "+ plugin.getCurrencyName() +".");
            }
            plugin.sendMessage(player, "You need %d "+ plugin.getCurrencyName() +" to repair all this.", actualCost);
            return;
        }

        chest.clear();

        for (ItemStack item : items) {
    		// TODO | DEBUG  item.getData() always seems to return null
    		short itemData = item.getDurability(); //(item.getData() == null ? (short)0 : item.getData().getData());

            chest.add(new TradeCraftItem(item.getTypeId(), itemData), 1);
        }

        chest.add(TradeCraft.currency, (currencyAmount - actualCost));

        chest.update();

        plugin.sendMessage(player, "You repaired %d items for %d "+ plugin.getCurrencyName() +".", items.size(), actualCost);
    }

    public boolean playerCanDestroy(Player player) {
        return true;
    }

    public boolean shopCanBeWithdrawnFrom() {
        return false;
    }
}
