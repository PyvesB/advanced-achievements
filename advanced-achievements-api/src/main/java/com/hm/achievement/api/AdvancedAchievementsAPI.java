package com.hm.achievement.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.domain.AwardedAchievement;

/**
 * Advanced Achievements API. Unless explicitly stated otherwise, implementations are expected to be thread-safe.
 *
 * @author Pyves
 */
public interface AdvancedAchievementsAPI {

	/**
	 * Returns Advanced Achievement's version as an object. Version 5.10.1 corresponds to major version 5, minor version
	 * 10 and patch version 1.
	 *
	 * @return version object
	 * @since 5.10.0
	 */
	Version getAdvancedAchievementsVersion();

	/**
	 * Checks whether player has received achievement {@code achievementName}. The underlying implementation of this API
	 * method benefits from Advanced Achievements caching if method called from the main server thread.
	 *
	 * @param player should not be null
	 * @param achievementName as defined by the Name parameter in Advanced Achievements config.yml, should not be empty
	 * @return true if player has received the achievement, false otherwise
	 * @since 5.8.0
	 */
	boolean hasPlayerReceivedAchievement(UUID player, String achievementName);

	/**
	 * Retrieves all achievements currently registered with the plugin.
	 *
	 * @return list of {@code com.hm.achievement.domain.Achievement} objects received by the player
	 * @since 7.0.0
	 */
	List<com.hm.achievement.domain.Achievement> getAllAchievements();

	/**
	 * Retrieves all achievements received by the player.
	 *
	 * @param player should not be null
	 * @return list of {@code AwardedAchievement} objects received by the player
	 * @since 7.0.0
	 */
	List<AwardedAchievement> getPlayerAchievements(UUID player);

	/**
	 * Retrieves all achievements received by the player.
	 *
	 * @param player should not be null
	 * @return list of {@code Achievement} objects received by the player
	 * @since 5.8.0
	 * @deprecated use {@link AdvancedAchievementsAPI#getPlayerAchievements(UUID)} instead
	 */
	@Deprecated
	List<Achievement> getPlayerAchievementsList(UUID player);

	/**
	 * Retrieves the total number of achievements received by the player.
	 *
	 * @param player should not be null
	 * @return total achievements by the player
	 * @since 5.8.0
	 */
	int getPlayerTotalAchievements(UUID player);

	/**
	 * Retrieves the {@code Rank} object of a player over a given period.
	 *
	 * @param player should not be null
	 * @param rankingPeriodStart time in millis since epoch; rank will be calculated for achievements received between
	 *            that starting point and now
	 * @return rank of the player; if no achievements were received over the period, his rank will be Integer.MAX_VALUE
	 * @since 5.8.0
	 */
	Rank getPlayerRank(UUID player, long rankingPeriodStart);

	/**
	 * Retrieves the players who have received the most achievements during a given period.
	 *
	 * @param numOfPlayers to return in the list
	 * @param rankingPeriodStart time in millis since epoch; ranks will be calculated for achievements received between
	 *            that starting point and now
	 * @return list of players, ordered from best to worst
	 * @since 5.8.0
	 */
	List<UUID> getTopPlayers(int numOfPlayers, long rankingPeriodStart);

	/**
	 * Retrieves a statistic for a normal category. The underlying implementation of this API method benefits from
	 * Advanced Achievements caching if method called from the main server thread.
	 *
	 * @param player should not be null
	 * @param category should not be null
	 * @return the statistic for the normal category
	 * @since 5.8.0
	 */
	long getStatisticForNormalCategory(UUID player, NormalAchievements category);

	/**
	 * Retrieves a statistic for a multiple category. The underlying implementation of this API method benefits from
	 * Advanced Achievements caching if method called from the main server thread.
	 *
	 * @param player should not be null
	 * @param category should not be null
	 * @param subcategory within the main multiple category
	 * @return the statistic for the multiple category
	 * @since 5.8.0
	 */
	long getStatisticForMultipleCategory(UUID player, MultipleAchievements category, String subcategory);

	/**
	 * Returns the DisplayName parameter for a given achievement Name parameter. If no DisplayName was found for the
	 * achievement {@code achievementName}, an empty String is returned. If the achievement {@code achievementName} was
	 * not found in Advanced Achievements's configuration, null is returned.
	 *
	 * @param achievementName as defined by the Name parameter in Advanced Achievements config.yml, should not be empty
	 * @return the DisplayName parameter of an achievement or "" or null
	 * @since 5.8.0
	 */
	String getDisplayNameForName(String achievementName);

	/**
	 * Retrieves the total numbers of achievements received by every player who has at least one achievement.
	 *
	 * @return map containing total achievements for every player
	 * @since 5.8.0
	 */
	Map<UUID, Integer> getPlayersTotalAchievements();

	/**
	 * Increments the given category for the given player.
	 *
	 * @param category should not be null
	 * @param player should not be null
	 * @param valueToAdd should be positive
	 * @since 6.6.0
	 */
	void incrementCategoryForPlayer(NormalAchievements category, Player player, int valueToAdd);

	/**
	 * Increments the given category for the given player.
	 *
	 * @param category should not be null
	 * @param subcategory within the main multiple category
	 * @param player should not be null
	 * @param valueToAdd should be positive
	 * @since 6.6.0
	 */
	void incrementCategoryForPlayer(MultipleAchievements category, String subcategory, Player player, int valueToAdd);

	final class Rank {

		public final int playerRank;
		public final int totalPlayers;

		public Rank(int playerRank, int totalPlayers) {
			this.playerRank = playerRank;
			this.totalPlayers = totalPlayers;
		}
	}

	@Deprecated
	final class Achievement {

		public final String name;
		public final String message;
		public final String formattedDate;

		public Achievement(String name, String message, String formattedDate) {
			this.name = name;
			this.message = message;
			this.formattedDate = formattedDate;
		}
	}

	final class Version {

		public final int major;
		public final int minor;
		public final int patch;

		public Version(int major, int minor, int patch) {
			this.major = major;
			this.minor = minor;
			this.patch = patch;
		}
	}

}
