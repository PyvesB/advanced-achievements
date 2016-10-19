package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

import com.hm.achievement.AdvancedAchievements;

/**
 * Listener class to deal with Fish achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveFishListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveFishListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerFish(PlayerFishEvent event) {

		if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH)
			return;
		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.count.fish")
				|| plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;

		int fish = plugin.getPoolsManager().getPlayerFishAmount(player) + 1;

		plugin.getPoolsManager().getFishHashMap().put(player.getUniqueId().toString(), fish);

		String configAchievement = "Fish." + fish;
		if (plugin.getPluginConfig().getString(configAchievement + ".Message", null) != null) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);

		}
	}
}
