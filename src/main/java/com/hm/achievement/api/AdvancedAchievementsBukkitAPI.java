package com.hm.achievement.api;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Underlying implementation of the AdvancedAchievementsAPI interface.
 * 
 * @author Pyves
 *
 */
public class AdvancedAchievementsBukkitAPI implements AdvancedAchievementsAPI {

	private static final String PLUGIN_NAME = "AdvancedAchievements";

	private final AdvancedAchievements pluginInstance;

	private AdvancedAchievementsBukkitAPI(AdvancedAchievements pluginInstance) {
		this.pluginInstance = pluginInstance;
	}

	/**
	 * Checks whether Advanced Achievements 5.0+ is running, and return a ready to use API instance if it is.
	 * 
	 * @return a ready-to-use object if Advanced Achievements 5.0+ was successfully linked, null otherwise
	 */
	public static AdvancedAchievementsAPI linkAdvancedAchievements() {
		if (Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME)) {
			Plugin pluginInstance = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
			if (Integer.parseInt(Character.toString(pluginInstance.getDescription().getVersion().charAt(0))) >= 5) {
				return new AdvancedAchievementsBukkitAPI((AdvancedAchievements) pluginInstance);
			}
		}
		return null;
	}

	@Override
	public int getAdvancedAchievementsVersionCode() {
		String version = pluginInstance.getDescription().getVersion();
		int versionCode = 100
				* Integer.parseInt(Character.toString(pluginInstance.getDescription().getVersion().charAt(0)))
				+ 10 * Integer.parseInt(Character.toString(pluginInstance.getDescription().getVersion().charAt(2)));
		if (version.length() > 4) {
			versionCode += Integer.parseInt(Character.toString(pluginInstance.getDescription().getVersion().charAt(4)));
		}
		return versionCode;
	}

	@Override
	public boolean hasPlayerReceivedAchievement(UUID player, String achievementName) {
		return pluginInstance.getPoolsManager().hasPlayerAchievement(player, achievementName);
	}

	@Override
	public List<Achievement> getPlayerAchievementsList(UUID player) {
		List<String> rawList = pluginInstance.getDb().getPlayerAchievementsList(player);
		List<Achievement> playerAchievements = new ArrayList<>(rawList.size() / 3);
		for (int i = 0; i < rawList.size(); i += 3) {
			playerAchievements.add(new Achievement(rawList.get(i), rawList.get(i + 1), rawList.get(i + 2)));
		}
		return playerAchievements;
	}

	@Override
	public int getPlayerTotalAchievements(UUID player) {
		return pluginInstance.getPoolsManager().getPlayerTotalAchievements(player);
	}

	@Override
	public Rank getPlayerRank(UUID player, long rankingPeriodStart) {
		int playerRank = pluginInstance.getDb().getPlayerRank(player, rankingPeriodStart);
		int totalPlayers = pluginInstance.getDb().getTotalPlayers(rankingPeriodStart);
		return new Rank(playerRank, totalPlayers);
	}

	@Override
	public List<UUID> getTopPlayers(int numOfPlayers, long rankingPeriodStart) {
		List<String> playersWithCounts = pluginInstance.getDb().getTopList(numOfPlayers, rankingPeriodStart);
		List<UUID> topPlayers = new ArrayList<>(numOfPlayers);
		for (int i = 0; i < playersWithCounts.size(); i += 2) {
			topPlayers.add(UUID.fromString(playersWithCounts.get(i)));
		}
		return topPlayers;
	}

	@Override
	public long getStatisticForNormalCategory(UUID player, NormalAchievements category) {
		return pluginInstance.getPoolsManager().getAndIncrementStatisticAmount(category, player, 0);
	}

	@Override
	public long getStatisticForMultipleCategory(UUID player, MultipleAchievements category, String subcategory) {
		return pluginInstance.getPoolsManager().getAndIncrementStatisticAmount(category, subcategory, player, 0);
	}
}
