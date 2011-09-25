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
        int repairCost = TradeCraft.properties.getRepairCost();

        if (currencyAmount == 0 && items.size() == 0) {
            plugin.sendMessage(player, TradeCraftLocalization.get("IT_COSTS_X_A_TO_REPAIR_AN_ITEM"),
            				   repairCost,
            				   plugin.getCurrencyName());
            return;
        }

        int actualCost = items.size() * repairCost;

        if (items.size() == 0) {
            plugin.sendMessage(player, TradeCraftLocalization.get("WITH_THIS_MUCH_A_YOU_CAN_REPAIR_Y_ITEMS"),
            				   plugin.getCurrencyName(),
            				   currencyAmount / repairCost);
            return;
        }

        if (currencyAmount < actualCost) {
            if (currencyAmount > 0) {
                plugin.sendMessage(player, TradeCraftLocalization.get("THAT_IS_NOT_ENOUGH_A"),
                		  	       plugin.getCurrencyName());
            }
            plugin.sendMessage(player, TradeCraftLocalization.get("YOU_NEED_X_A_TO_REPAIR_ALL_THIS"),
            		           actualCost,
            		           plugin.getCurrencyName());
            return;
        }

        chest.clear();

        for (ItemStack item : items) {
        	short itemData = (item.getData() == null ? (short)0 : item.getData().getData());

            chest.add(new TradeCraftItem(item.getTypeId(), itemData), 1);
        }

        chest.add(TradeCraft.currency, (currencyAmount - actualCost));

        chest.update();

        plugin.sendMessage(player, TradeCraftLocalization.get("YOU_REPAIRED_X_ITEMS_FOR_Y_B"),
        				   items.size(),
        				   actualCost,
        				   plugin.getCurrencyName());
    }

    public boolean playerCanDestroy(Player player) {
        return true;
    }

    public boolean shopCanBeWithdrawnFrom() {
        return false;
    }
}
