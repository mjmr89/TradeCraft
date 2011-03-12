package com.mjmr89.TradeCraft;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public abstract class TradeCraftItemShop extends TradeCraftShop {

    public TradeCraftItemShop(TradeCraft plugin, Sign sign, Chest chest) {
        super(plugin, sign, chest);
    }

    public void handleRightClick(Player player) {
        if (isOwnedByPlayer(player)) {
            handleOwnerClick(player);
        } else {
            handlePatronClick(player);
        }
    }

    private void handleOwnerClick(Player player) {
        if (!chestContentsAreOK()) {
            plugin.sendMessage(player, "The chest has more than one type of item in it!");
            return;
        }

        if (getChestItemCount() == 0) {
            int goldAmount = withdrawCurrency();
            if (goldAmount > 0) {
                populateChest(TradeCraft.currency.getId(), goldAmount);
                plugin.sendMessage(player, "Withdrew %1$d gold.", goldAmount);
            } else {
                int itemAmount = withdrawItems();
                if (itemAmount > 0) {
                    populateChest(getItemType(), itemAmount);
                    plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, getItemName());
                } else {
                    plugin.sendMessage(player, "There is nothing to withdraw.");
                }
            }
        } else if (getChestItemType() == TradeCraft.currency.getId()) {
            depositCurrency(getChestItemCount());
            plugin.sendMessage(player, "Deposited %1$d gold.", getChestItemCount());
            populateChest(0, 0);
            int itemAmount = withdrawItems();
            if (itemAmount > 0) {
                populateChest(getItemType(), itemAmount);
                plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, getItemName());
            }
        } else if (getChestItemType() == getItemType()) {
            depositItems(getChestItemCount());
            populateChest(0, 0);
            plugin.sendMessage(player, "Deposited %1$d %2$s.", getChestItemCount(), getItemName());
        } else {
            plugin.sendMessage(player, "You can't deposit that here!");
        }
    }

    private void handlePatronClick(Player player) {
    	
    	
        boolean playerCanBuy= (TradeCraftPermissions.permEnabled &&plugin.permissions.canBuy(player)) ||
        						(!TradeCraftPermissions.permEnabled && player.isOp());
        boolean playerCanSell = (TradeCraftPermissions.permEnabled &&plugin.permissions.canSell(player)) ||
		(!TradeCraftPermissions.permEnabled && player.isOp());

        getChestItemCount();
        
        if (!chestContentsAreOK()) {
            plugin.sendMessage(player, "The chest has more than one type of item in it!");
            return;
        }
        
        if (getChestItemCount() == 0) {
            if (playerCanBuy && playerCanBuy()) {
                plugin.sendMessage(player,
                        "You can buy %1$d %2$s for %3$d gold.",
                        getBuyAmount(),
                        getItemName(),
                        getBuyValue());
            }

            if (playerCanSell && playerCanSell()) {
                plugin.sendMessage(player,
                        "You can sell %1$d %2$s for %3$d gold.",
                        getSellAmount(),
                        getItemName(),
                        getSellValue());
            }

            plugin.sendMessage(player, "The chest is empty.");
            return;
        }

        

        if (getChestItemType() == TradeCraft.currency.getId()) {
            if (!playerCanBuy) {
                plugin.sendMessage(player, "You are not allowed to buy from shops!");
            } else {
                playerWantsToBuy(player);
            }
        } else if (getChestItemType() == getItemType()) {
            if (!playerCanSell) {
                plugin.sendMessage(player, "You are not allowed to sell to shops!");
            } else { 
                playerWantsToSell(player);
            }
        } else {
            plugin.sendMessage(player, "You can't sell that here!");
        }
    }

    private void playerWantsToBuy(Player player) {
        if (!playerCanBuy()) {
            plugin.sendMessage(player, "You can't buy that here!");
            return;
        }

        int currencyPlayerWantsToSpend = getChestItemCount();
        int amountPlayerWantsToBuy = currencyPlayerWantsToSpend * getBuyAmount() / getBuyValue();

        if (amountPlayerWantsToBuy == 0) {
            plugin.sendMessage(player,
                        "You need to spend at least %1$d gold to get any %2$s.",
                        getBuyValue(),
                        getItemName());
            return;
        }

        if (amountPlayerWantsToBuy > getItemsInShop()) {
            plugin.sendMessage(player,
                    "Cannot buy. This shop only has %1$d %2$s.",
                    getItemsInShop(),
                    getItemName());
            return;
        }

        int requiredGoldForThatAmount = amountPlayerWantsToBuy * getBuyValue() / getBuyAmount();

        updateItemAndCurrencyAmounts(-amountPlayerWantsToBuy, requiredGoldForThatAmount);

        chest.clear();
        chest.add(TradeCraft.currency.getId(), currencyPlayerWantsToSpend - requiredGoldForThatAmount);
        chest.add(getItemType(), amountPlayerWantsToBuy);
        chest.update();

        plugin.sendMessage(player,
                    "You bought %1$d %2$s for %3$d gold.",
                    amountPlayerWantsToBuy,
                    getItemName(),
                    requiredGoldForThatAmount);
    }

    private void playerWantsToSell(Player player) {
        if (!playerCanSell()) {
            plugin.sendMessage(player, "You can't sell that here!");
            return;
        }

        int amountPlayerWantsToSell = getChestItemCount();
        int currencyPlayerShouldReceive = amountPlayerWantsToSell * getSellValue() / getSellAmount();

        if (currencyPlayerShouldReceive == 0) {
            plugin.sendMessage(player,
                        "You need to sell at least %1$d %2$s to get any gold.",
                        getSellAmount(),
                        getItemName());
            return;
        }

        if (currencyPlayerShouldReceive > getCurrencyInShop()) {
            plugin.sendMessage(player,
                    "Cannot sell. This shop only has %1$d gold.",
                    getCurrencyInShop());
            return;
        }

        int amountThatCanBeSold = currencyPlayerShouldReceive * getSellAmount() / getSellValue();

        updateItemAndCurrencyAmounts(amountThatCanBeSold, -currencyPlayerShouldReceive);

        chest.clear();
        chest.add(getItemType(), amountPlayerWantsToSell - amountThatCanBeSold);
        chest.add(TradeCraft.currency.getId(), currencyPlayerShouldReceive);
        chest.update();

        plugin.sendMessage(player,
                    "You sold %1$d %2$s for %3$d gold.",
                    amountThatCanBeSold,
                    getItemName(),
                    currencyPlayerShouldReceive);
    }

    public int getChestItemType() {
        return chest.id;
    }

    public int getChestItemCount() {
        return chest.total;
    }

    public boolean chestContentsAreOK() {
        return chest.containsOnlyOneItemType();
    }

    public void populateChest(int id, int amount) {
        chest.populateChest(id, amount);
    }

    public abstract boolean isOwnedByPlayer(Player player);

    public abstract int getItemType();

    public abstract String getItemName();

    public abstract boolean playerCanBuy();

    public abstract boolean playerCanSell();

    public abstract int getBuyAmount();

    public abstract int getBuyValue();

    public abstract int getSellAmount();

    public abstract int getSellValue();

    public abstract int getItemsInShop();

    public abstract int getCurrencyInShop();

    public abstract void depositItems(int amount);

    public abstract void depositCurrency(int amount);

    public abstract int withdrawItems();

    public abstract int withdrawCurrency();

    public abstract void updateItemAndCurrencyAmounts(int itemAdjustment, int CurrencyAdjustment);
}