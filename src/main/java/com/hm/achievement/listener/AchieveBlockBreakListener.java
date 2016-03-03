package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

public class AchieveBlockBreakListener implements Listener {

	AdvancedAchievements plugin;

	public AchieveBlockBreakListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {

		Player player = event.getPlayer();
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;
		Block block = event.getBlock();
		String blockName = block.getType().name().toLowerCase();
		if (player.hasPermission("achievement.count.breaks." + blockName + ":" + block.getData()) && plugin.getConfig().isConfigurationSection("Breaks." + blockName + ":" + block.getData()))
			blockName += ":" + block.getData();
		else {
			if (!player.hasPermission("achievement.count.breaks." + blockName))
				return;
			if (!plugin.getConfig().isConfigurationSection("Breaks." + blockName))
				return;
		}

		int breaks;
		if (!DatabasePools.getBlockBreakHashMap().containsKey(player.getUniqueId().toString() + blockName))
			breaks = plugin.getDb().getBreaks(player, blockName) + 1;
		else
			// Concatenate player name and block ID to put in HashMap.
			breaks = DatabasePools.getBlockBreakHashMap().get(player.getUniqueId().toString() + blockName) + 1;

		DatabasePools.getBlockBreakHashMap().put(player.getUniqueId().toString() + blockName, breaks);

		String configAchievement = "Breaks." + blockName + '.' + breaks;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);

		}
	}

}
