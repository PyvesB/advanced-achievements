package com.hm.achievement.placeholder;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.entity.Player;

import com.hm.achievement.db.DatabaseCacheManager;

import codecrafter47.bungeetablistplus.api.bukkit.Variable;

/**
 * Class enabling usage of the achievement_count placeholder in BungeeTabListPlus configuration.
 * 
 * @author Pyves
 *
 */
@Singleton
public class AchievementCountBungeeTabListPlusVariable extends Variable {

	private final DatabaseCacheManager databaseCacheManager;

	@Inject
	public AchievementCountBungeeTabListPlusVariable(DatabaseCacheManager databaseCacheManager) {
		super("achievement_count");
		this.databaseCacheManager = databaseCacheManager;
	}

	@Override
	public String getReplacement(Player player) {
		return Integer.toString(databaseCacheManager.getPlayerTotalAchievements(player.getUniqueId()));
	}
}
