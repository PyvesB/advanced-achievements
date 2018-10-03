package com.hm.achievement.api;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Advanced Achievements API. Unless explicitly stated otherwise, implementations are expected to be thread-safe.
 *
 * @author Pyves
 */
public interface AdvancedAchievementsAPI {

	/**
	 * Formats Advanced Achievements's version as an integer. The version is computed as follows: 100 * major + 10 *
	 * minor + patch. For instance plugin version 5.5.0 will return 550; plugin version 5.1.2 will return 512.
	 *
	 * @deprecated use {@link #getAdvancedAchievementsVersion()} instead
	 * @return version code
	 * @since 1.0.0
	 */
	@Deprecated
	int getAdvancedAchievementsVersionCode();

	/**
	 * Returns Advanced Achievement's version as an object. Version 5.10.1 corresponds to major version 5, minor version
	 * 10 and patch version 1.
	 *
	 * @return version object
	 * @since 1.1.0
	 */
	Version getAdvancedAchievementsVersion();

	/**
	 * Checks whether player has received achievement {@code achievementName}. Implementation benefits from Advanced
	 * Achievements caching when player is online and if method called from server thread.
	 *
	 * @param player should not be null
	 * @param achievementName as defined by the Name parameter in Advanced Achievements config.yml, should not be empty
	 * @return true if player has received the achievement, false otherwise
	 * @since 1.0.0
	 */
	boolean hasPlayerReceivedAchievement(UUID player, String achievementName);

	/**
	 * Retrieves all achievements received by the player.
	 *
	 * @param player should not be null
	 * @return list of {@code Achievement} objects received by the player
	 * @since 1.0.0
	 */
	List<Achievement> getPlayerAchievementsList(UUID player);

	/**
	 * Retrieves the total number of achievements received by the player. Implementation benefits from Advanced
	 * Achievements caching when player is online.
	 *
	 * @param player should not be null
	 * @return total achievements by the player
	 * @since 1.0.0
	 */
	int getPlayerTotalAchievements(UUID player);

	/**
	 * Retrieves the {@code Rank} object of a player over a given period.
	 *
	 * @param player should not be null
	 * @param rankingPeriodStart time in millis since epoch; rank will be calculated for achievements received between
	 *            that starting point and now
	 * @return rank of the player; if no achievements were received over the period, his rank will be Integer.MAX_VALUE
	 * @since 1.0.0
	 */
	Rank getPlayerRank(UUID player, long rankingPeriodStart);

	/**
	 * Retrieves the players who have received the most achievements during a given period.
	 *
	 * @param numOfPlayers to return in the list
	 * @param rankingPeriodStart time in millis since epoch; ranks will be calculated for achievements received between
	 *            that starting point and now
	 * @return list of players, ordered from best to worst
	 * @since 1.0.0
	 */
	List<UUID> getTopPlayers(int numOfPlayers, long rankingPeriodStart);

	/**
	 * Retrieves a statistic for a normal category. Implementation benefits from Advanced Achievements caching when
	 * player is online and if method called from server thread.
	 *
	 * @param player should not be null
	 * @param category should not be null
	 * @return the statistic for the normal category
	 * @since 1.0.0
	 */
	long getStatisticForNormalCategory(UUID player, NormalAchievements category);

	/**
	 * Retrieves a statistic for a multiple category. Implementation benefits from Advanced Achievements caching when
	 * player is online and if method called from server thread.
	 *
	 * @param player should not be null
	 * @param category should not be null
	 * @param subcategory within the main multiple category
	 * @return the statistic for the multiple category
	 * @since 1.0.0
	 */
	long getStatisticForMultipleCategory(UUID player, MultipleAchievements category, String subcategory);

	/**
	 * Returns the DisplayName parameter for a given achievement Name parameter. If no DisplayName was found for the
	 * achievement {@code achievementName}, an empty String is returned. If the achievement {@code achievementName} was
	 * not found in Advanced Achievements's configuration, null is returned.
	 *
	 * @param achievementName as defined by the Name parameter in Advanced Achievements config.yml, should not be empty
	 * @return the DisplayName parameter of an achievement or "" or null
	 * @since 1.0.0
	 */
	String getDisplayNameForName(String achievementName);

	/**
	 * Retrieves the total numbers of achievements received by every player who has at least one achievement.
	 *
	 * @return map containing total achievements for every player
	 * @since 1.0.0
	 */
	Map<UUID, Integer> getPlayersTotalAchievements();

	final class Rank {

		public final int playerRank;
		public final int totalPlayers;

		public Rank(int playerRank, int totalPlayers) {
			this.playerRank = playerRank;
			this.totalPlayers = totalPlayers;
		}
	}

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
