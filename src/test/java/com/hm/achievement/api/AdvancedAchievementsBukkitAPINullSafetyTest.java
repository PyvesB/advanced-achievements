package com.hm.achievement.api;

import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

import utilities.MockUtility;

/**
 * Class for testing the Bukkit API (null safety).
 *
 * @author Pyves
 */
@RunWith(MockitoJUnitRunner.class)
public class AdvancedAchievementsBukkitAPINullSafetyTest {

	private AdvancedAchievementsBukkitAPI underTest;

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {
		AdvancedAchievements pluginMock = MockUtility.setUp().getPluginMock();

		underTest = new AdvancedAchievementsBukkitAPI(pluginMock);
	}

	@Test
	public void itShouldThrowExceptionWhenCheckingForAchievementWithNullPlayer() {
		expectIllegalArgumentExceptionWithMessage("Player cannot be null.");
		underTest.hasPlayerReceivedAchievement(null, "achievement-name");
	}

	@Test
	public void itShouldThrowExceptionWhenCheckingForAchievementWithEmptyName() {
		expectIllegalArgumentExceptionWithMessage("Achievement Name cannot be empty.");
		underTest.hasPlayerReceivedAchievement(UUID.randomUUID(), "");
	}

	@Test
	public void testAchievementListForNullPlayer() {
		expectIllegalArgumentExceptionWithMessage("Player cannot be null.");
		underTest.getPlayerAchievementsList(null);
	}

	@Test
	public void testTotalAchievementsForNullPlayer() {
		expectIllegalArgumentExceptionWithMessage("Player cannot be null.");
		underTest.getPlayerTotalAchievements(null);
	}

	@Test
	public void testRankForNullPlayer() {
		expectIllegalArgumentExceptionWithMessage("Player cannot be null.");
		underTest.getPlayerRank(null, 0);
	}

	@Test
	public void testNormalStatisticForNullPlayer() {
		expectIllegalArgumentExceptionWithMessage("Player cannot be null.");
		underTest.getStatisticForNormalCategory(null, NormalAchievements.ARROWS);
	}

	@Test
	public void testNormalStatisticForNullCategory() {
		expectIllegalArgumentExceptionWithMessage("Category cannot be null.");
		underTest.getStatisticForNormalCategory(UUID.randomUUID(), null);
	}

	@Test
	public void testMultipleStatisticForNullPlayer() {
		expectIllegalArgumentExceptionWithMessage("Player cannot be null.");
		underTest.getStatisticForMultipleCategory(null, MultipleAchievements.KILLS, "skeleton");
	}

	@Test
	public void testMultipleStatisticForNullCategory() {
		expectIllegalArgumentExceptionWithMessage("Category cannot be null.");
		underTest.getStatisticForMultipleCategory(UUID.randomUUID(), null, "skeleton");
	}

	@Test
	public void testMultipleStatisticForEmptySubcategory() {
		expectIllegalArgumentExceptionWithMessage("Sub-category cannot be empty.");
		underTest.getStatisticForMultipleCategory(UUID.randomUUID(), MultipleAchievements.KILLS, "");
	}

	@Test
	public void testAchievementDisplayName() {
		expectIllegalArgumentExceptionWithMessage("Achievement Name cannot be empty.");
		underTest.getDisplayNameForName("");
	}

	private void expectIllegalArgumentExceptionWithMessage(String message) {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(message);
	}
}
