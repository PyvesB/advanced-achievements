package com.hm.achievement.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Listener class to deal with HoePlowings, Fertilising, Fireworks and MusicDiscs achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveHoeFertiliseFireworkMusicListener extends AbstractListener implements Listener {

	public AchieveHoeFertiliseFireworkMusicListener(AdvancedAchievements plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null) {
			return;
		}

		Player player = event.getPlayer();
		NormalAchievements category;

		Material clickedMaterial = event.getClickedBlock().getType();
		if (event.getItem().getType().name().contains("HOE")
				&& (clickedMaterial == Material.GRASS || clickedMaterial == Material.DIRT)
				&& event.getClickedBlock().getRelative(BlockFace.UP).getType() == Material.AIR) {
			category = NormalAchievements.HOEPLOWING;
		} else if (event.getItem().isSimilar(new ItemStack(Material.INK_SACK, 1, (short) 15))
				&& canBeFertilised(clickedMaterial, event.getClickedBlock())) {
			category = NormalAchievements.FERTILISING;
		} else if (event.getItem().getType() == Material.FIREWORK) {
			category = NormalAchievements.FIREWORKS;
		} else if (event.getItem().getType().isRecord() && clickedMaterial == Material.JUKEBOX) {
			category = NormalAchievements.MUSICDISCS;
		} else {
			return;
		}

		if (plugin.getDisabledCategorySet().contains(category.toString())) {
			return;
		}

		if (!shouldEventBeTakenIntoAccount(player, category)
				|| category == NormalAchievements.MUSICDISCS && isInCooldownPeriod(player)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, 1);
	}

	/**
	 * Determines whether clickedMaterial can be fertilised.
	 * 
	 * @param clickedMaterial
	 * @param block
	 * @return
	 */
	private boolean canBeFertilised(Material clickedMaterial, Block block) {
		short durability = block.getState().getData().toItemStack().getDurability();
		if (clickedMaterial == Material.DOUBLE_PLANT) {
			if (durability == 10) {
				// Upper part of double plant. We must look at the lower part to get the double plant type.
				durability = block.getRelative(BlockFace.DOWN).getState().getData().toItemStack().getDurability();
			}
			// Fertilisation does not work on double tallgrass and large fern.
			return durability != 2 && durability != 3;
		}
		return clickedMaterial == Material.GRASS || clickedMaterial == Material.SAPLING
				|| clickedMaterial == Material.POTATO && durability < 7
				|| clickedMaterial == Material.CARROT && durability < 7
				|| clickedMaterial == Material.CROPS && durability < 7
				|| clickedMaterial == Material.PUMPKIN_STEM && durability < 7
				|| clickedMaterial == Material.MELON_STEM && durability < 7
				|| clickedMaterial == Material.BROWN_MUSHROOM || clickedMaterial == Material.RED_MUSHROOM
				|| clickedMaterial == Material.COCOA && durability < 9 || clickedMaterial == Material.LONG_GRASS
				|| (version >= 9 && clickedMaterial == Material.BEETROOT_BLOCK && durability < 3);
	}
}
