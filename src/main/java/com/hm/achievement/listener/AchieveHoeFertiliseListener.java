package com.hm.achievement.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.db.DatabasePools;

public class AchieveHoeFertiliseListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveHoeFertiliseListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDropEvent(PlayerInteractEvent event) {

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		Player player = event.getPlayer();
		if (!player.hasPermission("achievement.get") || plugin.isRestrictCreative()
				&& player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;
		String configAchievement = "";
		if ((player.getItemInHand().getType() == Material.DIAMOND_HOE
				|| player.getItemInHand().getType() == Material.GOLD_HOE
				|| player.getItemInHand().getType() == Material.IRON_HOE
				|| player.getItemInHand().getType() == Material.WOOD_HOE || player.getItemInHand().getType() == Material.STONE_HOE)
				&& (event.getClickedBlock().getType() == Material.GRASS || event.getClickedBlock().getType() == Material.DIRT)) {
			Integer plowings = 0;
			if (!DatabasePools.getHoePlowingHashMap().containsKey(player.getUniqueId().toString()))
				plowings = plugin.getDb().getHoePlowing(player) + 1;
			else
				plowings = DatabasePools.getHoePlowingHashMap().get(player.getUniqueId().toString()) + 1;

			DatabasePools.getHoePlowingHashMap().put(player.getUniqueId().toString(), plowings);

			configAchievement = "HoePlowings." + plowings;

		} else if (player.getItemInHand().isSimilar(new ItemStack(Material.INK_SACK, 1, (short) 15))
				&& (event.getClickedBlock().getType() == Material.LONG_GRASS
						|| event.getClickedBlock().getType() == Material.SAPLING
						|| event.getClickedBlock().getType() == Material.CARROT
						|| event.getClickedBlock().getType() == Material.POTATO
						|| event.getClickedBlock().getType() == Material.DOUBLE_PLANT
						|| event.getClickedBlock().getType() == Material.CROPS
						|| event.getClickedBlock().getType() == Material.PUMPKIN_STEM
						|| event.getClickedBlock().getType() == Material.MELON_STEM
						|| event.getClickedBlock().getType() == Material.BROWN_MUSHROOM
						|| event.getClickedBlock().getType() == Material.RED_MUSHROOM
						|| event.getClickedBlock().getType() == Material.COCOA || event.getClickedBlock().getType() == Material.GRASS)) {
			Integer fertilising = 0;
			if (!DatabasePools.getFertiliseHashMap().containsKey(player.getUniqueId().toString()))
				fertilising = plugin.getDb().getFertilising(player) + 1;
			else
				fertilising = DatabasePools.getFertiliseHashMap().get(player.getUniqueId().toString()) + 1;

			DatabasePools.getFertiliseHashMap().put(player.getUniqueId().toString(), fertilising);

			configAchievement = "Fertilising." + fertilising;
		} else
			return;
		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"), format.format(new Date()));

			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
