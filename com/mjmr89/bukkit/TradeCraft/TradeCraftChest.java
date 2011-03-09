package com.mjmr89.bukkit.TradeCraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

class TradeCraftChest {
    private Inventory chest;
    public int id = 0;
    public int total = 0;
    public boolean diffFlag = false;
    
    

    public TradeCraftChest(Chest c) {
        chest = c.getInventory();
        
        
        for (ItemStack item : chest.getContents()) {
        	if(!item.getType().equals(Material.AIR)){
        		if(id != 0 && id != item.getTypeId()){
        			diffFlag = true;
        			return;
        		}
        		id = item.getTypeId();
        		total += item.getAmount();
        	}
        	
//            if (item != null) {
//                addItem(item);
//            }
        }
    }

    private void addItem(ItemStack item) {
    	
        if (total == 0) {
            id = item.getTypeId();
        } else if (id != item.getTypeId()) {
            id = -1;
        }
        total += item.getAmount();
    }

    public boolean containsOnlyOneItemType() {
        return diffFlag == false;
    }

    public void clear() {
        chest.clear();
    }

    public void add(int id, int amount) {
        int maxStackSize = TradeCraft.getMaxStackSize(id);
        int blocks = amount / maxStackSize;

        for (int i = 0; i < blocks; i++) {
            chest.addItem(new ItemStack(id, maxStackSize));
        }

        int remainder = amount % maxStackSize;

        if (remainder > 0) {
            chest.addItem(new ItemStack(id, remainder));
        }
    }

    public void update() {
    }

    public void populateChest(int id, int amount) {
        clear();
        add(id, amount);
        update();
    }

    public int getAmountOfCurrencyInChest() {
        int amount = 0;
        for (ItemStack item : ((Inventory)chest).getContents()) {
            if (item != null) {
                if (item.getType() == Material.GOLD_INGOT) {
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
                if (item.getType() != Material.GOLD_INGOT) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}
