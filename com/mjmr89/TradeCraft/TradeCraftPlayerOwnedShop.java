package com.mjmr89.TradeCraft;

import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class TradeCraftPlayerOwnedShop extends TradeCraftItemShop {
    private final String ownerName;
    private final String itemName;
    private final int itemType;
    private final TradeCraftExchangeRate buyRate;
    private final TradeCraftExchangeRate sellRate;

    public TradeCraftPlayerOwnedShop(TradeCraft plugin, Sign sign, Chest chest, String ownerName) {
        super(plugin, sign, chest);

//        ownerName = plugin.data.getOwnerOfSign(sign);
        this.ownerName = ownerName;
        itemName = plugin.getItemName(sign);
        itemType = plugin.configuration.get(itemName).id;
        buyRate = plugin.getExchangeRate(sign, 1);
        sellRate = plugin.getExchangeRate(sign, 2);
        
//        plugin.getServer().broadcastMessage("owner" + ownerName + " itemname " + itemName + 
//        		" item type " + itemType + " buyrate " + buyRate + " sell rate" + sellRate);
    }

    public boolean playerCanDestroy(Player player) {
        return isOwnedByPlayer(player);
    }

    public boolean shopCanBeWithdrawnFrom() {
        return getItemsInShop() > 0 || getCurrencyInShop() > 0;
    }

    public boolean isOwnedByPlayer(Player player) {
    	if ( ownerName == null ) {
    		return false;
    	} else {
    		// option for less strict player name matching.
	    	if ( this.plugin.properties.getStrictPlayerShopOwnerNameRequired() ) {
	    		// strict is enforced, so use a perfect name match 
	    		return player.getName().equals(ownerName);
	    	} else {
	    		// strict is not enforced, so allow the playername to start with the set owner name
	    		return player.getName().indexOf(ownerName) == 0;
	    	}
    	}
    }

    public int getItemType() {
        return itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public boolean playerCanBuy() {
        return buyRate.amount != 0;
    }

    public boolean playerCanSell() {
        return sellRate.amount != 0;
    }

    public int getBuyAmount() {
        return buyRate.amount;
    }

    public int getBuyValue() {
        return buyRate.value;
    }

    public int getSellAmount() {
        return sellRate.amount;
    }

    public int getSellValue() {
        return sellRate.value;
    }

    public int getItemsInShop() {
        return plugin.data.getItemAmount(sign);
    }

    public int getCurrencyInShop() {
        return plugin.data.getCurrencyAmount(sign);
    }

    public void depositItems(int amount) {
        plugin.data.depositItems(ownerName, sign, itemType, amount);
    }

    public void depositCurrency(int amount) {
        plugin.data.depositCurrency(ownerName, sign, amount);
    }

    public int withdrawItems() {
        return plugin.data.withdrawItems(sign);
    }

    public int withdrawCurrency() {
        return plugin.data.withdrawCurrency(sign);
    }

    public void updateItemAndCurrencyAmounts(int itemAdjustment, int currencyAdjustment) {
        plugin.data.updateItemAndCurrencyAmounts(sign, itemAdjustment, currencyAdjustment);
    }

}
