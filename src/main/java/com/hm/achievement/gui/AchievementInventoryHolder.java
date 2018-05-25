package com.hm.achievement.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class AchievementInventoryHolder implements InventoryHolder {
	
	public static final int MAIN_GUI_PAGE = -1;

	private final int pageIndex;
	private Inventory inventory;

	public AchievementInventoryHolder() {
		this.pageIndex = MAIN_GUI_PAGE;
	}

	public AchievementInventoryHolder(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

}
