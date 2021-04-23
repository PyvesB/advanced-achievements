package com.hm.achievement.placeholder;

import javax.inject.Inject;

import org.bukkit.entity.Player;

import com.hm.achievement.db.CacheManager;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;

/**
 * Class enabling usage of the achievement_count placeholder in BungeeTabListPlus configuration.
 * 
 * @author Pyves
 *
 */
public class AchievementCountBungeeTabListPlusVariable extends Variable {

	private final CacheManager cacheManager;

	@Inject
	public AchievementCountBungeeTabListPlusVariable(CacheManager cacheManager) {
		super("achievement_count");
		this.cacheManager = cacheManager;
	}

	@Override
	public String getReplacement(Player player) {
		return Integer.toString(cacheManager.getPlayerAchievements(player.getUniqueId()).size());
	}
}
