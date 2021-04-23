package com.hm.achievement.listener;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AchievementAdvancement;
import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with advancements for Minecraft 1.12+. This class uses delays processing of tasks to avoid
 * spamming a barely connected player.
 * 
 * @author Pyves
 *
 */
@Singleton
public class JoinListener implements Listener {

	private final int serverVersion;
	private final AdvancedAchievements advancedAchievements;
	private final CacheManager cacheManager;

	@Inject
	public JoinListener(int serverVersion, AdvancedAchievements advancedAchievements, CacheManager cacheManager) {
		this.serverVersion = serverVersion;
		this.advancedAchievements = advancedAchievements;
		this.cacheManager = cacheManager;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		scheduleReceivedCacheLoad(event.getPlayer());
		if (serverVersion >= 12) {
			scheduleAwardAdvancements(event.getPlayer());
		}
	}

	/**
	 * Schedules an asynchronous task to load the received achievement cache.
	 * 
	 * @param player
	 */
	private void scheduleReceivedCacheLoad(Player player) {
		Bukkit.getScheduler().runTaskAsynchronously(advancedAchievements,
				() -> cacheManager.getPlayerAchievements(player.getUniqueId()));

	}

	/**
	 * Schedules a delayed task to award advancements created by Advanced Achievements. This method can be seen as a
	 * synchronisation to give advancements which were generated after the corresponding achievement was received for a
	 * given player.
	 * 
	 * @param player
	 */
	private void scheduleAwardAdvancements(Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(advancedAchievements, () -> {
			// Check that the player is still connected.
			if (!player.isOnline()) {
				return;
			}
			Advancement advancement = Bukkit.getAdvancement(new NamespacedKey(advancedAchievements,
					AdvancementManager.ADVANCED_ACHIEVEMENTS_PARENT));
			// If no parent, /aach generate has not been run on server, do not do anything.
			if (advancement != null) {
				AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);
				if (!advancementProgress.isDone()) {
					advancementProgress.awardCriteria(AchievementAdvancement.CRITERIA_NAME);
				}
				for (String name : cacheManager.getPlayerAchievements(player.getUniqueId())) {
					// May be null if /aach generate has not been called since that achievement was added to the config.
					advancement = Bukkit.getAdvancement(new NamespacedKey(advancedAchievements,
							AdvancementManager.getKey(name)));
					if (advancement != null) {
						advancementProgress = player.getAdvancementProgress(advancement);
						if (!advancementProgress.isDone()) {
							advancementProgress.awardCriteria(AchievementAdvancement.CRITERIA_NAME);
						}
					}
				}
			}
		}, 200);
	}
}
