package com.hm.achievement.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("deprecation")
public class MaterialHelperTest {

	@Mock
	private PotionMeta potionMeta;

	@Mock
	private ItemStack itemStack;

	@Test
	public void shouldReturnFalseForWaterPotionWhenUsingMinecraft14() {
		when(potionMeta.getBasePotionData()).thenReturn(new PotionData(PotionType.WATER));
		when(itemStack.getItemMeta()).thenReturn(potionMeta);
		when(itemStack.getType()).thenReturn(Material.POTION);
		MaterialHelper underTest = new MaterialHelper(null, 14);

		assertFalse(underTest.isAnyPotionButWater(itemStack));
	}

	@Test
	public void shouldReturnTrueForOtherPotionWhenUsingMinecraft14() {
		when(potionMeta.getBasePotionData()).thenReturn(new PotionData(PotionType.INSTANT_DAMAGE));
		when(itemStack.getItemMeta()).thenReturn(potionMeta);
		when(itemStack.getType()).thenReturn(Material.POTION);
		MaterialHelper underTest = new MaterialHelper(null, 14);

		assertTrue(underTest.isAnyPotionButWater(itemStack));
	}

	@Test
	public void shouldReturnFalseForWaterPotionWhenUsingMinecraft8() {
		when(itemStack.getType()).thenReturn(Material.POTION);
		when(itemStack.getDurability()).thenReturn((short) 0);
		MaterialHelper underTest = new MaterialHelper(null, 8);

		assertFalse(underTest.isAnyPotionButWater(itemStack));
	}

	@Test
	public void shouldReturnTrueForOtherPotionWhenUsingMinecraft8() {
		when(itemStack.getType()).thenReturn(Material.POTION);
		when(itemStack.getDurability()).thenReturn((short) 1);
		MaterialHelper underTest = new MaterialHelper(null, 8);

		assertTrue(underTest.isAnyPotionButWater(itemStack));
	}

	@Test
	public void shouldReturnFalseForOtherMaterial() {
		when(itemStack.getType()).thenReturn(Material.SPLASH_POTION);
		MaterialHelper underTest = new MaterialHelper(null, 12);

		assertFalse(underTest.isAnyPotionButWater(itemStack));
	}
}
