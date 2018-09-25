package com.hm.achievement.api;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

/**
 * Class allowing developers to easily retrieve instances of the Advanced Achievements API. It is only shipped with
 * plugin versions from 5.8.0 onwards; however, if shaded/copied within your own plugin, its methods will operate on
 * plugin versions 5.7.0 onwards. Open an issue on GitHub (https://github.com/PyvesB/AdvancedAchievements/issues) if you
 * need any help or have inquiries.
 * 
 * @author Pyves
 */
public class AdvancedAchievementsAPIFetcher {

	private static final String MAIN_CLASS = "com.hm.achievement.AdvancedAchievements";
	private static final String API_GETTER = "getAdvancedAchievementsAPI";

	/**
	 * Retrieves an AdvancedAchievementsAPI instance that can be used to integrate with Advanced Achievements.
	 * 
	 * @return an AdvancedAchievementsAPI instance wrapped in an optional or an empty optional if Advanced Achievements
	 *         could not be linked
	 */
	public static Optional<AdvancedAchievementsAPI> fetchInstance() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("AdvancedAchievements");
		if (plugin != null) {
			try {
				Class<?> mainClass = Class.forName(MAIN_CLASS);
				Object advancedAchievements = mainClass.cast(plugin);
				Object apiInstance = mainClass.getMethod(API_GETTER).invoke(advancedAchievements);
				return Optional.ofNullable((AdvancedAchievementsAPI) apiInstance);
			} catch (Exception e) {
				PluginLogger logger = new PluginLogger(plugin);
				logger.warning("Error whilst trying to fetch an Advanced Achievements API instance: " + e.getMessage());
			}
		}
		return Optional.empty();
	}

	private AdvancedAchievementsAPIFetcher() {
		// Not called.
	}

}
