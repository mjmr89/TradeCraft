package nl.armeagle.TradeCraft;

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
            int currencyAmount = withdrawCurrency();
            if (currencyAmount > 0) {
                populateChest(TradeCraft.currency, currencyAmount);
                plugin.sendMessage(player, "Withdrew %1$d "+ plugin.getCurrencyName() +".", currencyAmount);
            } else {
                int itemAmount = withdrawItems();
                if (itemAmount > 0) {
                    populateChest(getItemType(), itemAmount);
                    plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, getItemName());
                } else {
                    plugin.sendMessage(player, "There is nothing to withdraw.");
                }
            }
        } else if (getChestItemType() == TradeCraft.currency) {
            depositCurrency(getChestItemCount());
            plugin.sendMessage(player, "Deposited %1$d "+ plugin.getCurrencyName() +".", getChestItemCount());
            populateChest(new TradeCraftItem(0), 0);
            int itemAmount = withdrawItems();
            if (itemAmount > 0) {
                populateChest(getItemType(), itemAmount);
                plugin.sendMessage(player, "Withdrew %1$d %2$s.", itemAmount, getItemName());
            }
        } else if (getChestItemType() == getItemType()) {
            depositItems(getChestItemCount());
            populateChest(new TradeCraftItem(0), 0);
            plugin.sendMessage(player, "Deposited %1$d %2$s.", getChestItemCount(), getItemName());
        } else {
            plugin.sendMessage(player, "You can't deposit that here!");
        }
    }

    private void handlePatronClick(Player player) {
    	
    	
        boolean playerCanBuy= (plugin.permissions.canBuy(player));
        boolean playerCanSell = plugin.permissions.canSell(player);

        getChestItemCount();
        
        if (!chestContentsAreOK()) {
            plugin.sendMessage(player, "The chest has more than one type of item in it!");
            return;
        }
        
        if (getChestItemCount() == 0) {
            if (playerCanBuy && playerCanBuy()) {
                plugin.sendMessage(player,
                        "You can buy %1$d %2$s for %3$d "+ plugin.getCurrencyName() +".",
                        getBuyAmount(),
                        getItemName(),
                        getBuyValue());
            }

            if (playerCanSell && playerCanSell()) {
                plugin.sendMessage(player,
                        "You can sell %1$d %2$s for %3$d "+ plugin.getCurrencyName() +".",
                        getSellAmount(),
                        getItemName(),
                        getSellValue());
            }

            plugin.sendMessage(player, "The chest is empty.");
            return;
        }

        plugin.log.info(getChestItemType() +" "+ TradeCraft.currency +" "+ getItemType());
        if ( getChestItemType().compareTo(TradeCraft.currency) == 0 ) {
            if (!playerCanBuy) {
                plugin.sendMessage(player, "You are not allowed to buy from shops!");
            } else {
                playerWantsToBuy(player);
            }
        } else if ( getChestItemType().compareTo(getItemType()) == 0 ) {
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
                        "You need to spend at least %1$d "+ plugin.getCurrencyName() +" to get any %2$s.",
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

        int requiredCurrencyForThatAmount = amountPlayerWantsToBuy * getBuyValue() / getBuyAmount();

        updateItemAndCurrencyAmounts(-amountPlayerWantsToBuy, requiredCurrencyForThatAmount);

        chest.clear();
        chest.add(TradeCraft.currency, currencyPlayerWantsToSpend - requiredCurrencyForThatAmount);
        chest.add(getItemType(), amountPlayerWantsToBuy);
        chest.update();

        plugin.sendMessage(player,
                    "You bought %1$d %2$s for %3$d "+ plugin.getCurrencyName() +".",
                    amountPlayerWantsToBuy,
                    getItemName(),
                    requiredCurrencyForThatAmount);
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
                        "You need to sell at least %1$d %2$s to get any "+ plugin.getCurrencyName() +".",
                        getSellAmount(),
                        getItemName());
            return;
        }

        if (currencyPlayerShouldReceive > getCurrencyInShop()) {
            plugin.sendMessage(player,
                    "Cannot sell. This shop only has %1$d "+ plugin.getCurrencyName() +".",
                    getCurrencyInShop());
            return;
        }

        int amountThatCanBeSold = currencyPlayerShouldReceive * getSellAmount() / getSellValue();

        updateItemAndCurrencyAmounts(amountThatCanBeSold, -currencyPlayerShouldReceive);

        chest.clear();
        chest.add(getItemType(), amountPlayerWantsToSell - amountThatCanBeSold);
        chest.add(TradeCraft.currency, currencyPlayerShouldReceive);
        chest.update();

        plugin.sendMessage(player,
                    "You sold %1$d %2$s for %3$d "+ plugin.getCurrencyName() +".",
                    amountThatCanBeSold,
                    getItemName(),
                    currencyPlayerShouldReceive);
    }

    public TradeCraftItem getChestItemType() {
        return chest.type;
    }

    public int getChestItemCount() {
        return chest.total;
    }

    public boolean chestContentsAreOK() {
        return chest.containsOnlyOneItemType();
    }

    public void populateChest(TradeCraftItem type, int amount) {
        chest.populateChest(type, amount);
    }

    public abstract boolean isOwnedByPlayer(Player player);

    public abstract TradeCraftItem getItemType();

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