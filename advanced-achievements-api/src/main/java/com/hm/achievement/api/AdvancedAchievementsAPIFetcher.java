package com.hm.achievement.api;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

/**
 * Class allowing developers to easily retrieve instances of the Advanced Achievements API.
 * 
 * @author Pyves
 */
public class AdvancedAchievementsAPIFetcher {

	private static final String MAIN_CLASS = "com.hm.achievement.AdvancedAchievements";
	private static final String API_GETTER = "getAdvancedAchievementsAPI";

	/**
	 * Retrieves an AdvancedAchievementsAPI instance that can be used to integrate with Advanced Achievements. This
	 * methods will only work with plugin versions from 5.7.0 onwards.
	 * 
	 * @return an AdvancedAchievementsAPI instance wrapped in an optional or an empty optional if Advanced Achievements
	 *         could not be linked
	 */
	public static Optional<AdvancedAchievementsAPI> fetchInstance() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("AdvancedAchievements");
		if (plugin != null) {
			try {
				Object advancedAchievements = Class.forName(MAIN_CLASS).cast(plugin);
				Object apiInstance = Class.forName(MAIN_CLASS).getMethod(API_GETTER).invoke(advancedAchievements);
				return Optional.ofNullable((AdvancedAchievementsAPI) apiInstance);
			} catch (Exception e) {
				PluginLogger logger = new PluginLogger(plugin);
				logger.warning("Error whilst trying to fetch an Advanced Achievements API instance: " + e.getMessage());
			}
		}
		return Optional.empty();
	}

}
