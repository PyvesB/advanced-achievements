package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveMilkListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveMilkListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerBucketFillEvent(PlayerBucketFillEvent event) {

		if (!(event.getItemStack().getType() == Material.MILK_BUCKET))
			return;
		Player player = (Player) event.getPlayer();
		if (!player.hasPermission("achievement.get") || plugin.isInExludedWorld(player))
			return;

		Integer milks = plugin.getDb().incrementAndGetNormalAchievement(player, "milks");
		String configAchievement = "Milk." + milks;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
