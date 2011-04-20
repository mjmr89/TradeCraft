package nl.armeagle.TradeCraft;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

class TradeCraftChest {
    private Inventory chest;
    public TradeCraftItem type = new TradeCraftItem(0);
    public int total = 0;
    public boolean diffFlag = false;
    
    

    public TradeCraftChest(Chest c) {
        chest = c.getInventory();
        
        for (ItemStack item : chest.getContents()) {
        	if(item != null){
        		// TODO | DEBUG  item.getData() always seems to return null
        		short itemData = item.getDurability(); //(item.getData() == null ? (short)0 : item.getData().getData());
        		if(type.id != 0 && (type.id != item.getTypeId() || type.data != itemData) ){
        			diffFlag = true;
        			return;
        		}
        		type.id = item.getTypeId();
        		type.data = itemData;
        		total += item.getAmount();
        	}
        	
//            if (item != null) {
//                addItem(item);
//            }
        }
    }

    public boolean containsOnlyOneItemType() {
        return diffFlag == false;
    }

    public void clear() {
        chest.clear();
    }

    public void add(TradeCraftItem type, int amount) {
        int maxStackSize = TradeCraft.getMaxStackSize(type.id);
        int blocks = amount / maxStackSize;

        for (int i = 0; i < blocks; i++) {
            chest.addItem(new ItemStack(type.id, maxStackSize, type.data));
        }

        int remainder = amount % maxStackSize;

        if (remainder > 0) {
            chest.addItem(new ItemStack(type.id, remainder, type.data));
        }
    }

    public void update() {
    }

    public void populateChest(TradeCraftItem type, int amount) {
        clear();
        add(type, amount);
        update();
    }

    public int getAmountOfCurrencyInChest() {
        int amount = 0;
        for (ItemStack item : ((Inventory)chest).getContents()) {
            if (item != null) {
        		// TODO | DEBUG  item.getData() always seems to return null
        		short itemData = item.getDurability(); //(item.getData() == null ? (short)0 : item.getData().getData());
                if (item.getTypeId() == TradeCraft.currency.id && itemData == TradeCraft.currency.data) {
                    amount += item.getAmount();
                }
            }
        }
        return amount;
    }

    public List<ItemStack> getNonCurrencyItems() {
        List<ItemStack> items = new ArrayList<ItemStack>();
        for (ItemStack item : chest.getContents()) {
            if (item != null) {
        		// TODO | DEBUG  item.getData() always seems to return null
        		short itemData = item.getDurability(); //(item.getData() == null ? (short)0 : item.getData().getData());
                if (item.getTypeId() != TradeCraft.currency.id || itemData != TradeCraft.currency.data) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}
