package com.hm.achievement.listener;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.api.AdvancedAchievementsAPI;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

import me.clip.placeholderapi.external.EZPlaceholderHook;

public class PlaceholderAchievementListener extends EZPlaceholderHook
{
	AdvancedAchievements plugin;
	AdvancedAchievementsAPI api;
	
	public PlaceholderAchievementListener(AdvancedAchievements plugin, AdvancedAchievementsAPI api)
	{
		super(plugin, "aach");
		this.plugin = plugin;
		this.api = api;
	}
	
	@Override
	public String onPlaceholderRequest(Player p, String identifier)
	{
		for (NormalAchievements category : NormalAchievements.values())
		{
			String categoryName = category.toString();
			
			if (identifier.equalsIgnoreCase(categoryName))
			{
				return String.valueOf(api.getStatisticForNormalCategory(p.getUniqueId(), category));
			}
		}
		
		for (MultipleAchievements category : MultipleAchievements.values())
		{
			String categoryName = category.toString();

			for (String section : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false))
			{
				String categoryPath = categoryName+"_"+section;
				
				if (identifier.equalsIgnoreCase(categoryPath))
				{
					return String.valueOf(api.getStatisticForMultipleCategory(p.getUniqueId(), category, section));
				}
			}
			
			
		}
		
		return null;
	}
}
