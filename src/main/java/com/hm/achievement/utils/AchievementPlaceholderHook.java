package com.hm.achievement.utils;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

import me.clip.placeholderapi.external.EZPlaceholderHook;

/**
 * Class enabling usage of placeholder with PlaceholderAPI to get achievements
 * stats in others plugins.
 * 
 * @author Phoetrix
 *
 */
public class AchievementPlaceholderHook extends EZPlaceholderHook {

	private final AdvancedAchievements plugin;

	public AchievementPlaceholderHook(AdvancedAchievements plugin) {
		super(plugin, "aach");
		this.plugin = plugin;
	}

	@Override
	public String onPlaceholderRequest(Player p, String identifier) {

		if (identifier.equalsIgnoreCase("achievements")) {
			return String.valueOf(plugin.getCacheManager().getPlayerTotalAchievements(p.getUniqueId()));
		}

		if (identifier.equalsIgnoreCase("achievements_percentage")) {
			return String.format("%.1f",
					100 * (double) plugin.getCacheManager().getPlayerTotalAchievements(p.getUniqueId())
							/ plugin.getAchievementsAndDisplayNames().size())
					+ "%";
		}

		if (identifier.equalsIgnoreCase("total_achievements")) {
			return String.valueOf(plugin.getAchievementsAndDisplayNames().size());
		}

		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();

			if (identifier.equalsIgnoreCase(categoryName)) {
				return String
						.valueOf(plugin.getCacheManager().getAndIncrementStatisticAmount(category, p.getUniqueId(), 0));
			}
		}

		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();

			if (plugin.getDisabledCategorySet().contains(categoryName)) {
				continue;
			}

			for (String subcategory : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
				String categoryPath = categoryName + "_" + subcategory;

				if (identifier.equalsIgnoreCase(categoryPath)) {
					return String.valueOf(plugin.getCacheManager().getAndIncrementStatisticAmount(category, subcategory,
							p.getUniqueId(), 0));
				}
			}
		}
		return null;
	}
}
