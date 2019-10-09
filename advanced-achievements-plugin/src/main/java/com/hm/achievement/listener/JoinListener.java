package com.hm.achievement.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.lifecycle.Cleanable;

/**
 * Listener class to deal with advancements for Minecraft 1.12+. This class uses delays processing of tasks to avoid
 * spamming a barely connected player.
 * 
 * @author Pyves
 *
 */
@Singleton
public class JoinListener implements Listener, Cleanable {

	private final Set<UUID> playersConnectionProcessed = new HashSet<>();
	private final AdvancedAchievements advancedAchievements;
	private final AbstractDatabaseManager databaseManager;

	private final int serverVersion;

	@Inject
	public JoinListener(int serverVersion, AdvancedAchievements advancedAchievements,
			AbstractDatabaseManager databaseManager) {
		this.serverVersion = serverVersion;
		this.advancedAchievements = advancedAchievements;
		this.databaseManager = databaseManager;
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		playersConnectionProcessed.remove(uuid);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (serverVersion >= 12) {
			scheduleAwardAdvancements(event.getPlayer());
		}
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
			// If no parent, user has not used /aach generate, do not do anything.
			if (advancement != null) {
				AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);
				if (!advancementProgress.isDone()) {
					advancementProgress.awardCriteria(AchievementAdvancement.CRITERIA_NAME);
				}
				for (String achName : databaseManager.getPlayerAchievementNamesList(player.getUniqueId())) {
					advancement = Bukkit.getAdvancement(new NamespacedKey(advancedAchievements,
							AdvancementManager.getKey(achName)));
					// Matching advancement might not exist if user has not called /aach generate.
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
