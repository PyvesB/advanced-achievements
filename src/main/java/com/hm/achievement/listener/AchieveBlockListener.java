package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

public class AchieveBlockListener implements Listener {
	AdvancedAchievements plugin;

	public AchieveBlockListener(AdvancedAchievements instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {

		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.get")
				|| plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;
		Block block = event.getBlock();
		String blockName = block.getType().name().toLowerCase();
		if (!plugin.getConfig().isConfigurationSection("Breaks." + blockName))
			return;

		Integer breaks = 0;
		if (!DatabasePools.getBlockBreakHashMap().containsKey(
				player.getUniqueId().toString() + block.getTypeId()))
			breaks = plugin.getDb().getBreaks(player, block) + 1;
		else
			breaks = DatabasePools.getBlockBreakHashMap().get(
					player.getUniqueId().toString() + block.getTypeId()) + 1;

		DatabasePools.getBlockBreakHashMap().put(
				player.getUniqueId().toString() + block.getTypeId(), breaks);

		String configAchievement = "Breaks." + blockName + "." + breaks;
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
					"&0" + format.format(now));

			plugin.getReward().checkConfig(player, configAchievement);

		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.get")
				|| plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE
				|| plugin.isInExludedWorld(player))
			return;
		Block block = event.getBlock();
		String blockName = block.getType().name().toLowerCase();
		if (!plugin.getConfig().isConfigurationSection("Places." + blockName))
			return;

		Integer places = 0;
		if (!DatabasePools.getBlockPlaceHashMap().containsKey(
				player.getUniqueId().toString() + block.getTypeId()))
			places = plugin.getDb().getPlace(player, block) + 1;
		else
			places = DatabasePools.getBlockPlaceHashMap().get(
					player.getUniqueId().toString() + block.getTypeId()) + 1;

		DatabasePools.getBlockPlaceHashMap().put(
				player.getUniqueId().toString() + block.getTypeId(), places);

		String configAchievement = "Places." + blockName + "." + places;
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
					"&0" + format.format(now));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
