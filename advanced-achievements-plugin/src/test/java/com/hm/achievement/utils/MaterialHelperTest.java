package com.hm.achievement.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaterialHelperTest {

	@Mock
	private PotionMeta potionMeta;

	@Mock
	private ItemStack itemStack;

	@Test
	void shouldReturnFalseForWaterPotion() {
		when(potionMeta.getBasePotionData()).thenReturn(new PotionData(PotionType.WATER));
		when(itemStack.getItemMeta()).thenReturn(potionMeta);
		when(itemStack.getType()).thenReturn(Material.POTION);
		MaterialHelper underTest = new MaterialHelper(null);

		assertFalse(underTest.isAnyPotionButWater(itemStack));
	}

	@Test
	void shouldReturnTrueForOtherPotion() {
		when(potionMeta.getBasePotionData()).thenReturn(new PotionData(PotionType.INSTANT_DAMAGE));
		when(itemStack.getItemMeta()).thenReturn(potionMeta);
		when(itemStack.getType()).thenReturn(Material.POTION);
		MaterialHelper underTest = new MaterialHelper(null);

		assertTrue(underTest.isAnyPotionButWater(itemStack));
	}

	@Test
	void shouldReturnFalseForOtherMaterial() {
		when(itemStack.getType()).thenReturn(Material.SPLASH_POTION);
		MaterialHelper underTest = new MaterialHelper(null);

		assertFalse(underTest.isAnyPotionButWater(itemStack));
	}
}
