package com.hm.achievement.listener;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

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
		if (!player.hasPermission("achievement.count.places." + blockName))
			return;
		if (!plugin.getConfig().isConfigurationSection("Places." + blockName))
			return;

		Integer places = 0;
		if (!DatabasePools.getBlockPlaceHashMap().containsKey(player.getUniqueId().toString() + block.getTypeId()))
			places = plugin.getDb().getPlaces(player, block) + 1;
		else
			places = DatabasePools.getBlockPlaceHashMap().get(player.getUniqueId().toString() + block.getTypeId()) + 1;

		DatabasePools.getBlockPlaceHashMap().put(player.getUniqueId().toString() + block.getTypeId(), places);

		String configAchievement = "Places." + blockName + '.' + places;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
