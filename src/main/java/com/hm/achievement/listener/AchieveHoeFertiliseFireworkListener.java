package com.hm.achievement.listener;

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

public class AchieveHoeFertiliseFireworkListener implements Listener {

	private AdvancedAchievements plugin;

	public AchieveHoeFertiliseFireworkListener(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null)
			return;
		
		Player player = event.getPlayer();
		
		if (plugin.isRestrictCreative() && player.getGameMode() == GameMode.CREATIVE || plugin.isInExludedWorld(player))
			return;
		
		String configAchievement;
		if (player.hasPermission("achievement.count.hoeplowings")
				&& (event.getItem().getType() == Material.DIAMOND_HOE || event.getItem().getType() == Material.IRON_HOE
						|| event.getItem().getType() == Material.STONE_HOE
						|| event.getItem().getType() == Material.WOOD_HOE
						|| event.getItem().getType() == Material.GOLD_HOE)
				&& (event.getClickedBlock().getType() == Material.GRASS
						|| event.getClickedBlock().getType() == Material.DIRT)) {
			int plowings;
			if (!DatabasePools.getHoePlowingHashMap().containsKey(player.getUniqueId().toString()))
				plowings = plugin.getDb().getNormalAchievementAmount(player, "hoeplowing") + 1;
			else
				plowings = DatabasePools.getHoePlowingHashMap().get(player.getUniqueId().toString()) + 1;

			DatabasePools.getHoePlowingHashMap().put(player.getUniqueId().toString(), plowings);

			configAchievement = "HoePlowings." + plowings;

		} else if (player.hasPermission("achievement.count.fertilising")
				&& event.getItem().isSimilar(new ItemStack(Material.INK_SACK, 1, (short) 15))
				&& (event.getClickedBlock().getType() == Material.GRASS
						|| event.getClickedBlock().getType() == Material.SAPLING
						|| event.getClickedBlock().getType() == Material.DOUBLE_PLANT
						|| event.getClickedBlock().getType() == Material.POTATO
						|| event.getClickedBlock().getType() == Material.CARROT
						|| event.getClickedBlock().getType() == Material.CROPS
						|| event.getClickedBlock().getType() == Material.PUMPKIN_STEM
						|| event.getClickedBlock().getType() == Material.MELON_STEM
						|| event.getClickedBlock().getType() == Material.BROWN_MUSHROOM
						|| event.getClickedBlock().getType() == Material.RED_MUSHROOM
						|| event.getClickedBlock().getType() == Material.COCOA
						|| event.getClickedBlock().getType() == Material.LONG_GRASS)) {
			int fertilising;
			if (!DatabasePools.getFertiliseHashMap().containsKey(player.getUniqueId().toString()))
				fertilising = plugin.getDb().getNormalAchievementAmount(player, "fertilising") + 1;
			else
				fertilising = DatabasePools.getFertiliseHashMap().get(player.getUniqueId().toString()) + 1;

			DatabasePools.getFertiliseHashMap().put(player.getUniqueId().toString(), fertilising);

			configAchievement = "Fertilising." + fertilising;
		} else if (player.hasPermission("achievement.count.fireworks")
				&& event.getItem().getType() == Material.FIREWORK) {
			int fireworks;
			if (!DatabasePools.getFireworkHashMap().containsKey(player.getUniqueId().toString()))
				fireworks = plugin.getDb().getNormalAchievementAmount(player, "fireworks") + 1;
			else
				fireworks = DatabasePools.getFireworkHashMap().get(player.getUniqueId().toString()) + 1;

			DatabasePools.getFireworkHashMap().put(player.getUniqueId().toString(), fireworks);

			configAchievement = "Fireworks." + fireworks;

		} else
			return;

		if (plugin.getReward().checkAchievement(configAchievement)) {

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"));
			plugin.getReward().checkConfig(player, configAchievement);
		}
	}
}
