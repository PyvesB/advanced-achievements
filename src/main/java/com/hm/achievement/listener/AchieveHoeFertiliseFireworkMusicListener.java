package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.particle.ReflectionUtils.PackageType;

/**
 * Listener class to deal with HoePlowings, Fertilising, Fireworks and MusicDiscs achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveHoeFertiliseFireworkMusicListener extends AbstractListener implements Listener {

	final private int version;

	public AchieveHoeFertiliseFireworkMusicListener(AdvancedAchievements plugin) {

		super(plugin);
		// Simple and fast check to compare versions. Might need to be updated in the future depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null)
			return;

		Player player = event.getPlayer();
		NormalAchievements category;

		if ((event.getItem().getType() == Material.DIAMOND_HOE || event.getItem().getType() == Material.IRON_HOE
				|| event.getItem().getType() == Material.STONE_HOE || event.getItem().getType() == Material.WOOD_HOE
				|| event.getItem().getType() == Material.GOLD_HOE)
				&& (event.getClickedBlock().getType() == Material.GRASS
						|| event.getClickedBlock().getType() == Material.DIRT)) {
			category = NormalAchievements.HOEPLOWING;
		} else if (event.getItem().isSimilar(new ItemStack(Material.INK_SACK, 1, (short) 15))
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
						|| event.getClickedBlock().getType() == Material.LONG_GRASS
						|| (version >= 9 && event.getClickedBlock().getType() == Material.BEETROOT_BLOCK))) {
			category = NormalAchievements.FERTILISING;
		} else if (event.getItem().getType() == Material.FIREWORK) {
			category = NormalAchievements.FIREWORKS;
		} else if (event.getItem().getType().name().contains("RECORD")
				&& event.getClickedBlock().getType() == Material.JUKEBOX) {
			category = NormalAchievements.MUSICDISCS;
		} else {
			return;
		}

		if (plugin.getDisabledCategorySet().contains(category.toString()))
			return;

		if (!shouldEventBeTakenIntoAccount(player, category))
			return;

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}
}
