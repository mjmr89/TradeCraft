package nl.armeagle.TradeCraft;

import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class TradeCraftInfiniteShop extends TradeCraftItemShop {
    private final TradeCraftConfigurationInfo configurationInfo;

    public TradeCraftInfiniteShop(TradeCraft plugin, Sign sign, Chest chest) throws Exception {
        super(plugin, sign, chest);

        String itemName = plugin.getItemName(sign.getLines());
        configurationInfo = plugin.configuration.get(itemName);
        if (null == configurationInfo) {
            throw new Exception("Invalid item name on sign: "+ itemName);
        }
    }

    public boolean playerCanDestroy(Player player) {
        return plugin.permissions.canDestroyShops(player);
    }

    public boolean shopCanBeWithdrawnFrom() {
        return false;
    }

    public boolean isOwnedByPlayer(Player player) {
        return false;
    }

    public TradeCraftItem getItemType() {
        return configurationInfo.type;
    }

    public String getItemName() {
        return configurationInfo.name;
    }

    public boolean playerCanBuy() {
        return configurationInfo.buyAmount != 0;
    }

    public boolean playerCanSell() {
        return configurationInfo.sellAmount != 0;
    }

    public int getBuyAmount() {
        return configurationInfo.buyAmount;
    }

    public int getBuyValue() {
        return configurationInfo.buyValue;
    }

    public int getSellAmount() {
        return configurationInfo.sellAmount;
    }

    public int getSellValue() {
        return configurationInfo.sellValue;
    }

    public int getItemsInShop() {
        return Integer.MAX_VALUE;
    }

    public int getCurrencyInShop() {
        return Integer.MAX_VALUE;
    }

    public void depositItems(int amount) {
    }

    public void depositCurrency(int amount) {
    }

    public int withdrawItems() {
        return 0;
    }

    public int withdrawCurrency() {
        return 0;
    }

    public void updateItemAndCurrencyAmounts(int itemAdjustment, int currencyAdjustment) {
    }
}
