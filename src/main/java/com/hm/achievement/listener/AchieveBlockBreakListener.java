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
		if (!player.hasPermission("achievement.get") || plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;
		Block block = event.getBlock();
		String blockName = block.getType().name().toLowerCase();
		if (!plugin.getConfig().isConfigurationSection("Breaks." + blockName))
			return;

		Integer breaks = 0;
		if (!DatabasePools.getBlockBreakHashMap().containsKey(player.getUniqueId().toString() + block.getTypeId()))
			breaks = plugin.getDb().getBreaks(player, block) + 1;
		else
			breaks = DatabasePools.getBlockBreakHashMap().get(player.getUniqueId().toString() + block.getTypeId()) + 1;

		DatabasePools.getBlockBreakHashMap().put(player.getUniqueId().toString() + block.getTypeId(), breaks);

		String configAchievement = "Breaks." + blockName + "." + breaks;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);

		}
	}

}
