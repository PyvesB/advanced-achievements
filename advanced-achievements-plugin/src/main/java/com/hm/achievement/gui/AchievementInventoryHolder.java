package com.hm.achievement.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Simple wrapper class to keep track of the plugin's inventories and their page numbers.
 * 
 * @author Pyves
 */
public class AchievementInventoryHolder implements InventoryHolder {

	public static final int MAIN_GUI_PAGE = -1;

	private final int pageIndex;
	private final ItemStack categoryItem;
	private Inventory inventory;

	public AchievementInventoryHolder() {
		this.pageIndex = MAIN_GUI_PAGE;
		this.categoryItem = null;
	}

	public AchievementInventoryHolder(int pageIndex, ItemStack categoryItem) {
		this.pageIndex = pageIndex;
		this.categoryItem = categoryItem;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public ItemStack getCategoryItem() {
		return categoryItem;
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

}
