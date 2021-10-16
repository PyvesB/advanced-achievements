package com.hm.achievement.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryHelperTest {

	@Mock
	private Player player;

	@Mock
	private PlayerInventory playerInventory;

	@Mock
	private ItemStack inputItemStack;

	@Mock
	private ItemStack existingItemStack;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void shouldReturnAvailableSpace() {
		HashMap itemStacks = new HashMap<>();
		itemStacks.put(1, existingItemStack);
		itemStacks.put(2, existingItemStack);
		when(player.getInventory()).thenReturn(playerInventory);
		when(playerInventory.all(Material.STICK)).thenReturn(itemStacks);
		when(existingItemStack.getAmount()).thenReturn(3, 6);
		when(inputItemStack.getType()).thenReturn(Material.STICK);
		when(inputItemStack.isSimilar(existingItemStack)).thenReturn(true, false);
		when(inputItemStack.getMaxStackSize()).thenReturn(64);
		when(player.getInventory().getStorageContents()).thenReturn(new ItemStack[] { null }); // Adds 64.

		int availableSpace = InventoryHelper.getAvailableSpace(player, inputItemStack);
		assertEquals(125, availableSpace);
	}

}
