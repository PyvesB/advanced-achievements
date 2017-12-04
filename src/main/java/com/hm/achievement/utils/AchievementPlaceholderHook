package com.hm.achievement.utils;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

import me.clip.placeholderapi.external.EZPlaceholderHook;

/**
 * Class enabling usage of placeholder with PlaceholderAPI to get achievements stats in others plugins.
 * 
 * @author Phoetrix
 *
 */
public class AchievementPlaceholderHook extends EZPlaceholderHook
{
	private final AdvancedAchievements plugin;
	
	public AchievementPlaceholderHook(AdvancedAchievements plugin)
	{
		super(plugin, "aach");
		this.plugin = plugin;
	}
	
	@Override
	public String onPlaceholderRequest(Player p, String identifier)
	{
		for (NormalAchievements category : NormalAchievements.values())
		{
			String categoryName = category.toString();
			
			if (identifier.equalsIgnoreCase(categoryName))
			{
				return String.valueOf(plugin.getCacheManager().getAndIncrementStatisticAmount(category, p.getUniqueId(), 0));
			}
		}
		
		for (MultipleAchievements category : MultipleAchievements.values())
		{
			String categoryName = category.toString();

			for (String subcategory : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false))
			{
				String categoryPath = categoryName+"_"+subcategory;
				
				if (identifier.equalsIgnoreCase(categoryPath))
				{
					return String.valueOf(plugin.getCacheManager().getAndIncrementStatisticAmount(category, subcategory, p.getUniqueId(), 0));
				}
			}
		}
		
		return null;
	}
}
