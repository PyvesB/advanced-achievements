package com.hm.achievement.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InventoryHelperTest {

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Player player;

	@Mock
	private ItemStack inputItemStack;

	@Mock
	private ItemStack existingItemStack;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Before
	public void setup() {
		HashMap itemStacks = new HashMap<>();
		itemStacks.put(1, existingItemStack);
		itemStacks.put(2, existingItemStack);
		when(player.getInventory().all(Material.STICK)).thenReturn(itemStacks);
		when(existingItemStack.getAmount()).thenReturn(3, 6);
		when(inputItemStack.getType()).thenReturn(Material.STICK);
		when(inputItemStack.isSimilar(existingItemStack)).thenReturn(true, false);
		when(inputItemStack.getMaxStackSize()).thenReturn(64);
	}

	@Test
	public void shouldReturnAvailableSpaceUsingMinecraft14() {
		InventoryHelper inventoryHelper = new InventoryHelper(14);
		when(player.getInventory().getStorageContents()).thenReturn(new ItemStack[] { null }); // Adds 64.

		int availableSpace = inventoryHelper.getAvailableSpace(player, inputItemStack);
		assertEquals(125, availableSpace);
	}

	@Test
	public void shouldReturnAvailableSpaceUsingMinecraft8() {
		InventoryHelper inventoryHelper = new InventoryHelper(8);
		when(player.getInventory().getContents()).thenReturn(new ItemStack[] { null }); // Adds 64.

		int availableSpace = inventoryHelper.getAvailableSpace(player, inputItemStack);
		assertEquals(125, availableSpace);
	}

}
