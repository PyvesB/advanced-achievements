package com.hm.achievement.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.bukkit.Bukkit;

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

	private final AdvancedAchievements pluginInstance;

	private AdvancedAchievementsBukkitAPI(AdvancedAchievements pluginInstance) {
		this.pluginInstance = pluginInstance;
	}

	/**
	 * Returns a ready to use API instance. The caller must previously check whether the plugin is enabled and has a
	 * minimum version of 5.0.
	 * 
	 * @return API instance
	 */
	public static AdvancedAchievementsAPI linkAdvancedAchievements() {
		return new AdvancedAchievementsBukkitAPI(
				(AdvancedAchievements) Bukkit.getPluginManager().getPlugin("AdvancedAchievements"));
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
		// Underlying structures do not support concurrent operations and are only used by the main server thread. Not
		// thread-safe to modify or read them asynchronously. Do not use cached data if player is offline.
		if (Bukkit.getServer().isPrimaryThread() && isPlayerOnline(player)) {
			return pluginInstance.getCacheManager().hasPlayerAchievement(player, achievementName);
		} else {
			return pluginInstance.getDatabaseManager().hasPlayerAchievement(player, achievementName);
		}
	}

	@Override
	public List<Achievement> getPlayerAchievementsList(UUID player) {
		List<String> rawList = pluginInstance.getDatabaseManager().getPlayerAchievementsList(player);
		List<Achievement> playerAchievements = new ArrayList<>(rawList.size() / 3);
		for (int i = 0; i < rawList.size(); i += 3) {
			playerAchievements.add(new Achievement(rawList.get(i), rawList.get(i + 1), rawList.get(i + 2)));
		}
		return playerAchievements;
	}

	@Override
	public int getPlayerTotalAchievements(final UUID player) {
		// Only use cached data if player is online.
		if (isPlayerOnline(player)) {
			return pluginInstance.getCacheManager().getPlayerTotalAchievements(player);
		} else {
			return pluginInstance.getDatabaseManager().getPlayerAchievementsAmount(player);
		}
	}

	@Override
	public Rank getPlayerRank(UUID player, long rankingPeriodStart) {
		int playerRank = pluginInstance.getDatabaseManager().getPlayerRank(player, rankingPeriodStart);
		int totalPlayers = pluginInstance.getDatabaseManager().getTotalPlayers(rankingPeriodStart);
		return new Rank(playerRank, totalPlayers);
	}

	@Override
	public List<UUID> getTopPlayers(int numOfPlayers, long rankingPeriodStart) {
		List<String> playersWithCounts = pluginInstance.getDatabaseManager().getTopList(numOfPlayers,
				rankingPeriodStart);
		List<UUID> topPlayers = new ArrayList<>(numOfPlayers);
		for (int i = 0; i < playersWithCounts.size(); i += 2) {
			topPlayers.add(UUID.fromString(playersWithCounts.get(i)));
		}
		return topPlayers;
	}

	@Override
	public long getStatisticForNormalCategory(UUID player, NormalAchievements category) {
		// Underlying structures do not support concurrent write operations and are only modified by the main server
		// thread. Do not use cache if player is offline.
		if (Bukkit.getServer().isPrimaryThread() && isPlayerOnline(player)) {
			return pluginInstance.getCacheManager().getAndIncrementStatisticAmount(category, player, 0);
		} else {
			return pluginInstance.getDatabaseManager().getNormalAchievementAmount(player, category);
		}
	}

	@Override
	public long getStatisticForMultipleCategory(UUID player, MultipleAchievements category, String subcategory) {
		// Underlying structures do not support concurrent write operations and are only modified by the main server
		// thread. Do not use cache if player is offline.
		if (Bukkit.getServer().isPrimaryThread() && isPlayerOnline(player)) {
			return pluginInstance.getCacheManager().getAndIncrementStatisticAmount(category, subcategory, player, 0);
		} else {
			return pluginInstance.getDatabaseManager().getMultipleAchievementAmount(player, category, subcategory);
		}
	}

	@Override
	public String getDisplayNameForName(String achievementName) {
		return pluginInstance.getAchievementsAndDisplayNames().get(achievementName);
	}

	@Override
	public Map<UUID, Integer> getPlayersTotalAchievements() {
		return pluginInstance.getDatabaseManager().getPlayersAchievementsAmount();
	}

	/**
	 * Checks whether the player is online by making a call on the server's main thread of execution.
	 * 
	 * @param player
	 * @return true if player is online, false otherwise
	 */
	private boolean isPlayerOnline(final UUID player) {
		if (Bukkit.getServer().isPrimaryThread()) {
			return Bukkit.getPlayer(player) != null;
		}
		// Called asynchronously. To ensure thread safety we must issue a call on the server's main thread of execution.
		Future<Boolean> onlineCheckFuture = Bukkit.getScheduler().callSyncMethod(pluginInstance,
				() -> Bukkit.getPlayer(player) != null);

		boolean playerOnline = true;
		try {
			playerOnline = onlineCheckFuture.get();
		} catch (InterruptedException e) {
			pluginInstance.getLogger().log(Level.SEVERE, "Thead interrupted while checking whether player online.", e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			pluginInstance.getLogger().log(Level.SEVERE,
					"Unexpected execution exception while checking whether player online.", e);
		} catch (CancellationException ignored) {
			// Task can be cancelled when plugin disabled.
		}
		return playerOnline;
	}
}
