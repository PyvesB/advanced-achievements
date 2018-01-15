package com.hm.achievement.listener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AchievementAdvancement;
import com.hm.achievement.advancement.AdvancementManager;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.utils.Cleanable;
import com.hm.achievement.utils.PlayerAdvancedAchievementEvent.PlayerAdvancedAchievementEventBuilder;

/**
 * Listener class to deal with Connections achievements and advancements for Minecraft 1.12+.
 * 
 * @author Pyves
 *
 */
public class AchieveConnectionListener extends AbstractListener implements Cleanable {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	// Contains UUIDs of players for which a AchieveConnectionRunnable ran successfully without returning.
	private final Set<String> playersProcessingRan;

	public AchieveConnectionListener(AdvancedAchievements plugin) {
		super(plugin);

		playersProcessingRan = new HashSet<>();
	}

	@Override
	public void cleanPlayerData(UUID uuid) {
		playersProcessingRan.remove(uuid.toString());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		scheduleTask(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorldChanged(PlayerChangedWorldEvent event) {
		if (playersProcessingRan.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		scheduleTask(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onGameModeChange(PlayerGameModeChangeEvent event) {
		if (playersProcessingRan.contains(event.getPlayer().getUniqueId().toString())) {
			return;
		}

		scheduleTask(event.getPlayer());
	}

	/**
	 * Schedules a delayed task to deal with Connection achievements.
	 * 
	 * @param player
	 */
	private void scheduleTask(Player player) {
		// Schedule delayed task to check if player should receive a Connections achievement or advancements he is
		// missing. This processing is delayed to avoid spamming a barely connected player.
		Bukkit.getServer().getScheduler()
				.scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin(plugin.getDescription().getName()), () -> {
					// Check reception conditions and whether player is still connected, as he could have left in
					// the meantime.
					if (!player.isOnline()
							|| !shouldIncreaseBeTakenIntoAccount(player, NormalAchievements.CONNECTIONS)) {
						return;
					}

					// Check whether another runnable has already done the work (even though this method is intended
					// to run once per player per connection instance, it might happen with some server settings).
					if (playersProcessingRan.contains(player.getUniqueId().toString())) {
						return;
					}

					if (plugin.getServerVersion() >= 12) {
						awardAdvancements(player);
					}
					handleConnectionAchievements(player);

					// Ran successfully to completion: no need to re-run while player is connected.
					playersProcessingRan.add(player.getUniqueId().toString());
				}, 100);
	}

	/**
	 * Updates Connection statistics and awards an achievement if need-be.
	 * 
	 * @param player
	 */
	private void handleConnectionAchievements(Player player) {
		String dateString = LocalDate.now().format(DATE_TIME_FORMATTER);
		if (!dateString.equals(plugin.getDatabaseManager().getPlayerConnectionDate(player.getUniqueId()))) {
			int connections = plugin.getDatabaseManager().updateAndGetConnection(player.getUniqueId(), dateString);
			String achievementPath = NormalAchievements.CONNECTIONS + "." + connections;
			String rewardPath = achievementPath + ".Reward";
			if (plugin.getPluginConfig().getString(achievementPath + ".Message", null) != null) {
				// Fire achievement event.
				PlayerAdvancedAchievementEventBuilder playerAdvancedAchievementEventBuilder = new PlayerAdvancedAchievementEventBuilder()
						.player(player).name(plugin.getPluginConfig().getString(achievementPath + ".Name"))
						.displayName(plugin.getPluginConfig().getString(achievementPath + ".DisplayName"))
						.message(plugin.getPluginConfig().getString(achievementPath + ".Message"))
						.commandRewards(plugin.getRewardParser().getCommandRewards(rewardPath, player))
						.commandMessage(plugin.getRewardParser().getCustomCommandMessage(rewardPath))
						.itemReward(plugin.getRewardParser().getItemReward(rewardPath))
						.moneyReward(plugin.getRewardParser().getRewardAmount(rewardPath, "Money"))
						.experienceReward(plugin.getRewardParser().getRewardAmount(rewardPath, "Experience"))
						.maxHealthReward(plugin.getRewardParser().getRewardAmount(rewardPath, "IncreaseMaxHealth"))
						.maxOxygenReward(plugin.getRewardParser().getRewardAmount(rewardPath, "IncreaseMaxOxygen"));

				Bukkit.getServer().getPluginManager().callEvent(playerAdvancedAchievementEventBuilder.build());
			}
		}
	}

	/**
	 * Awards advancements created by Advanced Achievements. This method can be seen as a synchronisation to give
	 * advancements which were generated after the corresponding achievement was received for a given player.
	 * 
	 * @param player
	 */
	private void awardAdvancements(Player player) {
		Advancement advancement = Bukkit.getServer()
				.getAdvancement(new NamespacedKey(plugin, AdvancementManager.ADVANCED_ACHIEVEMENTS_PARENT));
		// If no parent, user has not used /aach generate, do not do anything.
		if (advancement != null) {
			AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);
			if (!advancementProgress.isDone()) {
				advancementProgress.awardCriteria(AchievementAdvancement.CRITERIA_NAME);
			}
			for (String achName : plugin.getDatabaseManager().getPlayerAchievementNamesList(player.getUniqueId())) {
				advancement = Bukkit.getServer()
						.getAdvancement(new NamespacedKey(plugin, AdvancementManager.getKey(achName)));
				// Matching advancement might not exist if user has not called /aach generate.
				if (advancement != null) {
					advancementProgress = player.getAdvancementProgress(advancement);
					if (!advancementProgress.isDone()) {
						advancementProgress.awardCriteria(AchievementAdvancement.CRITERIA_NAME);
					}
				}
			}
		}
	}
}
