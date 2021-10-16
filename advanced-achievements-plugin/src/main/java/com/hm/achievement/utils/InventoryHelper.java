package com.hm.achievement.utils;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryHelper {

	/**
	 * Calculates the space available to accommodate a new item stack. This method takes both empty slots and existing
	 * similar item stacks into account.
	 * 
	 * @param player
	 * @param newItemStack
	 * @return the available space for the item
	 */
	public static int getAvailableSpace(Player player, ItemStack newItemStack) {
		int availableSpace = 0;
		PlayerInventory inventory = player.getInventory();
		// Get all similar item stacks with a similar material in the player's inventory.
		HashMap<Integer, ? extends ItemStack> itemStacksWithSameMaterial = inventory.all(newItemStack.getType());
		for (ItemStack existingItemStack : itemStacksWithSameMaterial.values()) {
			// If matching item stack, add remaining space.
			if (newItemStack.isSimilar(existingItemStack)) {
				availableSpace += (newItemStack.getMaxStackSize() - existingItemStack.getAmount());
			}
		}

		// Get all empty slots in the player's inventory.
		for (ItemStack existingItemStack : inventory.getStorageContents()) {
			if (existingItemStack == null) {
				availableSpace += newItemStack.getMaxStackSize();
			}
		}

		return availableSpace;
	}

	private InventoryHelper() {
		// Not called.
	}
}
