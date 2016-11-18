package com.hm.achievement.listener;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import com.google.common.collect.HashMultimap;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with MaxLevel achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveXPListener extends AbstractListener implements Listener {

	// Multimaps corresponding to the players who have received max level achievements.
	// Each key in the multimap corresponds to one achievement threshold, and has its associated player Set.
	// Used as pseudo-caching system to reduce load on database as we cannot rely on the fact that each unique integer
	// value will be reached for this achievement, and we can therefore not give the achievement when a precise value is
	// met; we have to do threshold comparisons, which require extra reads from the database.
	private HashMultimap<Integer, String> achievementsCache;

	public AchieveXPListener(AdvancedAchievements plugin) {

		super(plugin);
		extractAchievementsFromConfig();
	}

	/**
	 * Loads achievements from configuration.
	 * 
	 */
	public void extractAchievementsFromConfig() {

		Set<String> configKeys = plugin.getConfig().getConfigurationSection(NormalAchievements.LEVELS.toString())
				.getKeys(false);

		achievementsCache = HashMultimap.create(configKeys.size(), 1);

		// Populate the multimap with the different threshold keys and null values. This is used to easily iterate
		// through the thresholds without referring to the config file again.
		for (String level : configKeys) {
			achievementsCache.put(Integer.valueOf(level), null);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerExpChange(PlayerLevelChangeEvent event) {

		Player player = event.getPlayer();
		String uuid = player.getUniqueId().toString();

		NormalAchievements category = NormalAchievements.LEVELS;
		if (!shouldEventBeTakenIntoAccount(player, category)) {
			return;
		}

		int levels = plugin.getPoolsManager().getStatisticAmount(category, player);

		if (event.getNewLevel() > levels) {
			plugin.getPoolsManager().getHashMap(category).put(player.getUniqueId().toString(), event.getNewLevel());
		} else {
			return;
		}

		for (Integer achievementThreshold : achievementsCache.keySet()) {
			if (event.getNewLevel() >= achievementThreshold
					&& !achievementsCache.get(achievementThreshold).contains(uuid)) {
				String configAchievement = category + "." + achievementThreshold;
				if (!plugin.getDb().hasPlayerAchievement(player,
						plugin.getPluginConfig().getString(configAchievement + ".Name"))) {
					awardAchievementIfAvailable(player, configAchievement + achievementThreshold);
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
