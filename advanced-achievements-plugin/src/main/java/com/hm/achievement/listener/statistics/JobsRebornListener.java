package com.hm.achievement.listener.statistics;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.gamingmesh.jobs.api.JobsLevelUpEvent;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;

/**
 * Listener class to deal with Jobs Reborn achievements.
 */
@Singleton
public class JobsRebornListener extends AbstractListener {

	@Inject
	public JobsRebornListener(@Named("main") YamlConfiguration mainConfig, int serverVersion, AchievementMap achievementMap,
			CacheManager cacheManager, RewardParser rewardParser) {
		super(MultipleAchievements.JOBSREBORN, mainConfig, serverVersion, achievementMap, cacheManager, rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onJob(JobsLevelUpEvent event) {
		if (event.getPlayer() == null) {
			return;
		}

		// Grab the player from the JobsPlayer
		Player player = event.getPlayer().getPlayer();
		if (player == null) {
			return;
		}

		String jobName = event.getJobName().toLowerCase();
		if (!player.hasPermission(category.toChildPermName(jobName))) {
			return;
		}

		Set<String> foundAchievements = findAchievementsByCategoryAndName(jobName);
		updateStatisticAndAwardAchievementsIfAvailable(player, foundAchievements, 1);
	}
}
