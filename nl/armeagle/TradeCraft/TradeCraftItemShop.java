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
            plugin.sendMessage(player, TradeCraftLocalization.get("THE_CHEST_HAS_MORE_THAN_ONE_TYPE"));
            return;
        }

        if (getChestItemCount() == 0) {
            int currencyAmount = withdrawCurrency();
            if (currencyAmount > 0) {
            	// limit amount of currency dropped into the chest to the max amount it can hold
            	int maxCurrencyChestCanHold = chest.getSize() * TradeCraft.getMaxStackSize(TradeCraft.currency.id); 
            	if ( currencyAmount > maxCurrencyChestCanHold ) {
            		populateChest(TradeCraft.currency, maxCurrencyChestCanHold); // fill to the max
            		depositCurrency(currencyAmount - maxCurrencyChestCanHold); // return remaining money back to shop data 
            		plugin.sendMessage(player, TradeCraftLocalization.get("WITHDREW_X_CURRENCY_SHOP_STILL_HOLDS_Y_CURRENCY"),
            								   maxCurrencyChestCanHold,
            								   plugin.getCurrencyName(),
            								   currencyAmount - maxCurrencyChestCanHold);
            	} else {
            		populateChest(TradeCraft.currency, currencyAmount);
            		plugin.sendMessage(player, "%1$s %2$d %3$s.", TradeCraftLocalization.get("WITHDREW"), currencyAmount, plugin.getCurrencyName());
            	}
            } else {
                int itemAmount = withdrawItems();
                if (itemAmount > 0) {
                	int maxItemsChestCanHold = chest.getSize() * TradeCraft.getMaxStackSize(getChestItemType().id);
                	if ( itemAmount > maxItemsChestCanHold ) {
                		populateChest(getItemType(), maxItemsChestCanHold);
                		depositItems(itemAmount - maxItemsChestCanHold);
	                    plugin.sendMessage(player, TradeCraftLocalization.get("WITHDREW_X_A_SHOP_STILL_HOLDS_Y_A"),
	                    						   itemAmount - maxItemsChestCanHold,
	                    						   getItemName(),
	                    						   itemAmount - maxItemsChestCanHold);
                	} else {
	                    populateChest(getItemType(), itemAmount);
	                    plugin.sendMessage(player, "%1$s %2$d %3$s.", TradeCraftLocalization.get("WITHDREW"), itemAmount, getItemName());
                	}
                } else {
                    plugin.sendMessage(player, TradeCraftLocalization.get("THERE_IS_NOTHING_TO_WITHDRAW"));
                }
            }
        } else if ( getChestItemType().compareTo(TradeCraft.currency) == 0 ) {
            depositCurrency(getChestItemCount());
            plugin.sendMessage(player, "%1$s %2$d %3$s.", TradeCraftLocalization.get("DEPOSITED"), getChestItemCount(), plugin.getCurrencyName());
            populateChest(new TradeCraftItem(0), 0);
            int itemAmount = withdrawItems();
            if (itemAmount > 0) {
            	int maxItemsChestCanHold = chest.getSize() * TradeCraft.getMaxStackSize(getChestItemType().id);
            	if ( itemAmount > maxItemsChestCanHold ) {
            		populateChest(getItemType(), maxItemsChestCanHold);
            		depositItems(itemAmount - maxItemsChestCanHold);
                    plugin.sendMessage(player, TradeCraftLocalization.get("WITHDREW_X_A_SHOP_STILL_HOLDS_Y_A"),
                    						   itemAmount - maxItemsChestCanHold,
                    						   getItemName(),
                    						   itemAmount - maxItemsChestCanHold);
            	} else {
                    populateChest(getItemType(), itemAmount);
                    plugin.sendMessage(player, "%1$s %2$d %3$s.", TradeCraftLocalization.get("WITHDREW"), itemAmount, getItemName());
            	}
            }
        } else if ( getChestItemType().compareTo(getItemType()) == 0 ) {
            depositItems(getChestItemCount());
            populateChest(new TradeCraftItem(0), 0);
            plugin.sendMessage(player, "%1$s %2$d %3$s.", TradeCraftLocalization.get("DEPOSITED"), getChestItemCount(), getItemName());
        } else {
            plugin.sendMessage(player, TradeCraftLocalization.get("YOU_CANT_DEPOSIT_THAT_HERE"));
        }
    }

    private void handlePatronClick(Player player) {
    	
    	
        boolean playerCanBuy= (plugin.permissions.canBuy(player));
        boolean playerCanSell = plugin.permissions.canSell(player);

        getChestItemCount();
        
        if (!chestContentsAreOK()) {
            plugin.sendMessage(player, TradeCraftLocalization.get("THE_CHEST_HAS_MORE_THAN_ONE_TYPE"));
            return;
        }
        
        if (getChestItemCount() == 0) {
            if (playerCanBuy && playerCanBuy()) {
            	if ( this instanceof TradeCraftInfiniteShop ) {
	                plugin.sendMessage(player,
	                        TradeCraftLocalization.get("YOU_CAN_BUY_Y_A_FOR_X_B"),
	                        getBuyAmount(),
	                        getItemName(),
	                        getBuyValue(),
	                        plugin.getCurrencyName());            		
            	} else {
	                plugin.sendMessage(player,
	                        TradeCraftLocalization.get("YOU_CAN_BUY_X_A_FOR_Y_B_UP_TO_Z"),
	                        getBuyAmount(),
	                        getItemName(),
	                        getBuyValue(),
	                        plugin.getCurrencyName(),
	                        this.getItemsInShop());
            	}
            }

            if (playerCanSell && playerCanSell()) {
            	if ( this instanceof TradeCraftInfiniteShop ) {
	                plugin.sendMessage(player,
	                        TradeCraftLocalization.get("YOU_CAN_SELL_X_A_FOR_Y_B"),
	                        getSellAmount(),
	                        getItemName(),
	                        getSellValue(),
	                        plugin.getCurrencyName());
            	} else {
	                plugin.sendMessage(player,
	                        TradeCraftLocalization.get("YOU_CAN_SELL_X_A_FOR_Y_B_UP_TO_Z"),
	                        getSellAmount(),
	                        getItemName(),
	                        getSellValue(),
	                        plugin.getCurrencyName(),
	                        this.getCurrencyInShop());
            	}
            }
            
            if ( this instanceof TradeCraftInfiniteShop ) {
            	plugin.sendMessage(player, TradeCraftLocalization.get("THIS_IS_AN_INFINITE_SHOP"));
            }
            return;
        }

        plugin.trace(player, "%s %s %s", getChestItemType(), TradeCraft.currency, getItemType());
        if ( getChestItemType().compareTo(TradeCraft.currency) == 0 ) {
            if (!playerCanBuy) {
                plugin.sendMessage(player, TradeCraftLocalization.get("YOU_ARE_NOT_ALLOWED_TO_BUY"));
            } else {
                playerWantsToBuy(player);
            }
        } else if ( getChestItemType().compareTo(getItemType()) == 0 ) {
            if (!playerCanSell) {
                plugin.sendMessage(player, TradeCraftLocalization.get("YOU_ARE_NO_ALLOWED_TO_SELL"));
            } else { 
                playerWantsToSell(player);
            }
        } else {
            plugin.sendMessage(player, TradeCraftLocalization.get("YOU_CANT_SELL_THAT"));
        }
    }

    private void playerWantsToBuy(Player player) {
        if (!playerCanBuy()) {
            plugin.sendMessage(player, TradeCraftLocalization.get("YOU_CANT_BUY_HERE"));
            return;
        }

        int currencyPlayerWantsToSpend = getChestItemCount();
        int amountPlayerWantsToBuy = ((currencyPlayerWantsToSpend - (currencyPlayerWantsToSpend % getBuyValue()) ) / getBuyValue()) * getBuyAmount();
    	if ( getBuyAmount() > this.chest.getSize() * TradeCraft.getMaxStackSize(getItemType().id) ) {
    		plugin.sendMessage(player, TradeCraftLocalization.get("THIS_SHOP_ALWAYS_RETURNS_TOO_MUCH"));
    		return;
    	}
        
        if (amountPlayerWantsToBuy == 0) {
            plugin.sendMessage(player,
                        TradeCraftLocalization.get("YOU_NEED_TO_SPEND_AT_LEAST_X_A_TO_GET_ANY_B"),
                        plugin.getCurrencyName(),
                        getBuyValue(),
                        getItemName());
            return;
        }

        if (amountPlayerWantsToBuy > getItemsInShop()) {
        	if ( getItemsInShop() == 0 ) {
	            plugin.sendMessage(player,
	                    TradeCraftLocalization.get("CANT_BUY_SHOP_HAS_NO_A_LEFT"),
	                    getItemName());
        	} else {
	            plugin.sendMessage(player,
	                    TradeCraftLocalization.get("CANNOT_BUY_SHOP_ONLY_HAS_X_A"),
	                    getItemsInShop(),
	                    getItemName());
        	}
        	return;
        }

        int requiredCurrencyForThatAmount = amountPlayerWantsToBuy * getBuyValue() / getBuyAmount();

        if ( Math.ceil( (currencyPlayerWantsToSpend - requiredCurrencyForThatAmount) / TradeCraft.getMaxStackSize(TradeCraft.currency.id))
        	 + Math.ceil( amountPlayerWantsToBuy / TradeCraft.getMaxStackSize(getItemType().id))
        	 > this.chest.getSize() ) {
        	plugin.sendMessage(player, TradeCraftLocalization.get("THIS_SHOP_WOULD_RETURN_TOO_MANY_BUY_LESS"));
        	return;
        }
        
        updateItemAndCurrencyAmounts(-amountPlayerWantsToBuy, requiredCurrencyForThatAmount);

        chest.clear();
        chest.add(TradeCraft.currency, currencyPlayerWantsToSpend - requiredCurrencyForThatAmount);
        chest.add(getItemType(), amountPlayerWantsToBuy);
        chest.update();

        plugin.sendMessage(player,
                    "You bought %1$d %2$s for %3$d %4$s.",
                    amountPlayerWantsToBuy,
                    getItemName(),
                    requiredCurrencyForThatAmount,
                    plugin.getCurrencyName());
    }

    private void playerWantsToSell(Player player) {
        if (!playerCanSell()) {
            plugin.sendMessage(player, TradeCraftLocalization.get("YOU_CANT_SELL_THAT"));
            return;
        }

        int amountPlayerWantsToSell = getChestItemCount();
        int currencyPlayerShouldReceive = ((amountPlayerWantsToSell - (amountPlayerWantsToSell % getSellAmount())) / getSellAmount()) * getSellValue();
        // prevent too much currency (more than can fit in a chest) to be given to the customer
       	if ( getSellValue() > this.chest.getSize() * TradeCraft.getMaxStackSize(TradeCraft.currency.id) ) {
       		plugin.sendMessage(player, TradeCraftLocalization.get("THIS_SHOP_ALWAYS_RETURNS_TOO_MUCH_CURRENCY"), this.plugin.getCurrencyName());
       		return;
       	}
        
        if (currencyPlayerShouldReceive == 0) {
            plugin.sendMessage(player,
                        TradeCraftLocalization.get("YOU_NEED_TO_SELL_AT_LEAST_X_A_TO_GET_ANY_B"),
                        getSellAmount(),
                        getItemName(),
                        plugin.getCurrencyName());
            return;
        }

        if (currencyPlayerShouldReceive > getCurrencyInShop()) {
            plugin.sendMessage(player,
                    TradeCraftLocalization.get("CANNOT_SELL_SHOP_ONLY_HAS_X_A"),
                    getCurrencyInShop(),
                    plugin.getCurrencyName());
            return;
        }

        int amountThatCanBeSold = currencyPlayerShouldReceive * getSellAmount() / getSellValue();

        // prevent too much items+currency stacks to end up in the chest
        if ( Math.ceil( (amountPlayerWantsToSell - amountThatCanBeSold)/ TradeCraft.getMaxStackSize(getItemType().id) )
        	 + Math.ceil( currencyPlayerShouldReceive / TradeCraft.getMaxStackSize(TradeCraft.currency.id) ) 
             > chest.getSize() ) {
    		plugin.sendMessage(player, TradeCraftLocalization.get("THIS_SHOP_WOULD_RETURN_TOO_MUCH_CURRENCY_SELL_LESS"), this.plugin.getCurrencyName());
    		return;
        }
        
        updateItemAndCurrencyAmounts(amountThatCanBeSold, -currencyPlayerShouldReceive);

        chest.clear();
        chest.add(getItemType(), amountPlayerWantsToSell - amountThatCanBeSold);
        chest.add(TradeCraft.currency, currencyPlayerShouldReceive);
        chest.update();

        plugin.sendMessage(player,
                    TradeCraftLocalization.get("YOU_SOLD_X_A_FOR_Y_B"),
                    amountThatCanBeSold,
                    getItemName(),
                    currencyPlayerShouldReceive,
                    plugin.getCurrencyName());
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