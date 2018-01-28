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

		int achievements = plugin.getCacheManager().getPlayerTotalAchievements(p.getUniqueId());

		if (identifier.equalsIgnoreCase("achievements")) {
			return String.valueOf(achievements);
		}

		int totalAchievements = 0;

		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();

			if (plugin.getDisabledCategorySet().contains(categoryName)) {
				continue;
			}

			if (identifier.equalsIgnoreCase(categoryName)) {
				return String
						.valueOf(plugin.getCacheManager().getAndIncrementStatisticAmount(category, p.getUniqueId(), 0));
			}

			totalAchievements += plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false).size();
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

				totalAchievements += plugin.getPluginConfig().getConfigurationSection(categoryName + '.' + subcategory)
						.getKeys(false).size();
			}
		}

		if (!plugin.getDisabledCategorySet().contains("Commands")) {
			totalAchievements += plugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false).size();
		}

		if (identifier.equalsIgnoreCase("achievements_percentage")) {
			return String.format("%.1f", 100 * (double) achievements / totalAchievements) + "%";
		}

		if (identifier.equalsIgnoreCase("total_achievements")) {
			return String.valueOf(totalAchievements);
		}

		return null;
	}
}
