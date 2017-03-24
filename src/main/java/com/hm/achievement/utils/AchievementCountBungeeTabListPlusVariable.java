package com.hm.achievement.utils;

import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;

/**
 * Class enabling usage of the achievement_count placeholder in BungeeTabListPlus configuration.
 * 
 * @author Pyves
 *
 */
public class AchievementCountBungeeTabListPlusVariable extends Variable {

	private final AdvancedAchievements plugin;

	public AchievementCountBungeeTabListPlusVariable(AdvancedAchievements plugin) {
		super("achievement_count");
		this.plugin = plugin;
	}

	@Override
	public String getReplacement(Player player) {
		return Integer.toString(plugin.getPoolsManager().getPlayerTotalAchievements(player.getUniqueId()));
	}
}
