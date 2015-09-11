package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;


public class AchieveDropListener implements Listener {
	
		private AdvancedAchievements plugin;

		public AchieveDropListener(AdvancedAchievements plugin) {
			this.plugin = plugin;
		}

		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void onDropEvent(PlayerDropItemEvent  event) {

			Player player = event.getPlayer();
			if (!player.hasPermission("achievement.get")
					|| plugin.isRestrictCreative()
					&& player.getGameMode() == GameMode.CREATIVE
					|| plugin.isInExludedWorld(player))
				return;

			String configAchievement = "";
				Integer drops = 0;
				if (!DatabasePools.getDropHashMap().containsKey(
						player.getUniqueId().toString()))
					drops = plugin.getDb().getDrops(player) + 1;
				else
					drops = DatabasePools.getDropHashMap().get(
							player.getUniqueId().toString()) + 1;

				DatabasePools.getDropHashMap().put(
						player.getUniqueId().toString(), drops);

				configAchievement = "ItemDrops." + drops;
			if (plugin.getReward().checkAchievement(configAchievement)) {
				String name = plugin.getConfig().getString(
						configAchievement + ".Name");
				String msg = plugin.getConfig().getString(
						configAchievement + ".Message");
				plugin.getAchievementDisplay()
						.displayAchievement(player, name, msg);
				Date now = new Date();
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
				plugin.getDb().registerAchievement(
						player,
						plugin.getConfig().getString(configAchievement + ".Name"),
						plugin.getConfig()
								.getString(configAchievement + ".Message"),
						format.format(now));

				plugin.getReward().checkConfig(player, configAchievement);
			}
		}
	}