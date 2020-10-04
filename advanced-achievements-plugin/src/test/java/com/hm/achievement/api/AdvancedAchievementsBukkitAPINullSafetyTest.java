package com.hm.achievement.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class for testing the Bukkit API (null safety).
 *
 * @author Pyves
 */
@RunWith(MockitoJUnitRunner.class)
public class AdvancedAchievementsBukkitAPINullSafetyTest {

	@InjectMocks
	private AdvancedAchievementsBukkitAPI underTest;

	@Test
	public void itShouldThrowExceptionWhenCheckingForAchievementWithNullPlayer() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.hasPlayerReceivedAchievement(null, "achievement-name"));
		assertEquals("Player cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenCheckingForAchievementWithEmptyName() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.hasPlayerReceivedAchievement(UUID.randomUUID(), ""));
		assertEquals("Achievement Name cannot be empty.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenGettingAchievementListWithNullPlayer() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> underTest.getPlayerAchievementsList(null));
		assertEquals("Player cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenGettingTotalAchievementsWithNullPlayer() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> underTest.getPlayerTotalAchievements(null));
		assertEquals("Player cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenGettingRankWithNullPlayer() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> underTest.getPlayerRank(null, 0));
		assertEquals("Player cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenGettingNormalStatisticWithNullPlayer() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.getStatisticForNormalCategory(null, NormalAchievements.ARROWS));
		assertEquals("Player cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenGettingNormalStatisticWithNullCategory() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.getStatisticForNormalCategory(UUID.randomUUID(), null));
		assertEquals("Category cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenGettingMultipleStatisticWithNullPlayer() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.getStatisticForMultipleCategory(null, MultipleAchievements.KILLS, "skeleton"));
		assertEquals("Player cannot be null.", e.getMessage());

	}

	@Test
	public void itShouldThrowExceptionWhenGettingMultipleStatisticWithNullCategory() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.getStatisticForMultipleCategory(UUID.randomUUID(), null, "skeleton"));
		assertEquals("Category cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenGettingMultipleStatisticWithEmptySubcategory() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.getStatisticForMultipleCategory(UUID.randomUUID(), MultipleAchievements.KILLS, ""));
		assertEquals("Sub-category cannot be empty.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenGettingDisplayNameWithEmptyName() {
		Exception e = assertThrows(IllegalArgumentException.class, () -> underTest.getDisplayNameForName(""));
		assertEquals("Achievement Name cannot be empty.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenIncrementingNormalCategoryWithNullCategory() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.incrementCategoryForPlayer(null, null, 1));
		assertEquals("category cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenIncrementingNormalCategoryWithNullPlayer() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.incrementCategoryForPlayer(NormalAchievements.ANVILS, null, 1));
		assertEquals("player cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenIncrementingMultipleCategoryWithNullCategory() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.incrementCategoryForPlayer(null, "skeleton", null, 1));
		assertEquals("category cannot be null.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenIncrementingMultipleCategoryWithEmptySubcategory() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.incrementCategoryForPlayer(MultipleAchievements.KILLS, "", null, 1));
		assertEquals("subcategory cannot be empty.", e.getMessage());
	}

	@Test
	public void itShouldThrowExceptionWhenIncrementingMultipleCategoryWithNullPlayer() {
		Exception e = assertThrows(IllegalArgumentException.class,
				() -> underTest.incrementCategoryForPlayer(MultipleAchievements.KILLS, "skeleton", null, 1));
		assertEquals("player cannot be null.", e.getMessage());
	}
}
