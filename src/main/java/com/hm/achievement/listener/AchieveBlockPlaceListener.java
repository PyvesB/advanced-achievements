package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.hm.achievement.AdvancedAchievements;

public class AchieveBlockPlaceListener implements Listener {

	AdvancedAchievements plugin;

	public AchieveBlockPlaceListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		Player player = event.getPlayer();
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;
		Block block = event.getBlock();

		String blockName = block.getType().name().toLowerCase();
		if (player.hasPermission("achievement.count.places." + blockName + ":" + block.getData())
				&& plugin.getPluginConfig().isConfigurationSection("Places." + blockName + ":" + block.getData()))
			blockName += ":" + block.getData();
		else {
			if (!player.hasPermission("achievement.count.places." + blockName))
				return;
			if (!plugin.getPluginConfig().isConfigurationSection("Places." + blockName))
				return;
		}

		int places = plugin.getPoolsManager().getPlayerBlockPlaceAmount(player, blockName) + 1;

		plugin.getPoolsManager().getBlockPlaceHashMap().put(player.getUniqueId().toString() + blockName, places);

		String configAchievement = "Places." + blockName + '.' + places;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getPluginConfig().getString(configAchievement + ".Name"),
					plugin.getPluginConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
