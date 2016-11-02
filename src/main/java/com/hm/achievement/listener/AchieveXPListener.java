package com.hm.achievement.listener;

import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import com.google.common.collect.HashMultimap;
import com.hm.achievement.AdvancedAchievements;

/**
 * Listener class to deal with MaxLevel achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveXPListener implements Listener {

	private AdvancedAchievements plugin;

	// Multimaps corresponding to the players who have received max level achievements.
	// Each key in the multimap corresponds to one achievement threshold, and has its associated player Set.
	// Used as pseudo-caching system to reduce load on database as we cannot rely on the fact that each unique integer
	// value will be reached for this achievement, and we can therefore not give the achievement when a precise value is
	// met; we have to do threshold comparisons, which require extra reads from the database.
	private HashMultimap<Integer, String> achievementsCache;

	public AchieveXPListener(AdvancedAchievements plugin) {

		this.plugin = plugin;

		extractAchievementsFromConfig();
	}

	public void extractAchievementsFromConfig() {

		Set<String> configKeys = plugin.getConfig().getConfigurationSection("MaxLevel").getKeys(false);

		achievementsCache = HashMultimap.create(configKeys.size(), 1);

		// Populate the multimap with the different threshold keys and null values. This is used to easily iterate
		// through the thresholds without referring to the config file again.
		for (String level : configKeys)
			achievementsCache.put(Integer.valueOf(level), null);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerExpChange(PlayerLevelChangeEvent event) {

		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();

		if (!player.hasPermission("achievement.count.maxlevel")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		int levels = plugin.getPoolsManager().getPlayerXPAmount(player);

		if (event.getNewLevel() > levels)
			plugin.getPoolsManager().getXpHashMap().put(player.getUniqueId().toString(), event.getNewLevel());
		else
			return;

		for (Integer achievementThreshold : achievementsCache.keySet()) {
			if (event.getNewLevel() >= achievementThreshold
					&& !achievementsCache.get(achievementThreshold).contains(uuid)) {
				if (!plugin.getDb().hasPlayerAchievement(player,
						plugin.getPluginConfig().getString("MaxLevel." + achievementThreshold + ".Name"))) {
					plugin.getAchievementDisplay().displayAchievement(player, "MaxLevel." + achievementThreshold);
					plugin.getDb().registerAchievement(player,
							plugin.getPluginConfig().getString("MaxLevel." + achievementThreshold + ".Name"),
							plugin.getPluginConfig().getString("MaxLevel." + achievementThreshold + ".Message"));
					plugin.getReward().checkConfig(player, "MaxLevel." + achievementThreshold);
				}
				// Player has received this achievement.
				achievementsCache.put(achievementThreshold, uuid);
			}
		}
	}

	public HashMultimap<Integer, String> getAchievementsCache() {

		return achievementsCache;
	}

}
