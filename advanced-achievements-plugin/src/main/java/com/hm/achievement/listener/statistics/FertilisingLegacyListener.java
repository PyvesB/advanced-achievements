package com.hm.achievement.listener.statistics;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Fertilising achievements for Minecraft 1.7.9-1.12.2.
 *
 * @author Pyves
 *
 */
@Singleton
public class FertilisingLegacyListener extends AbstractListener {

	@Inject
	public FertilisingLegacyListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR) // Do NOT set ignoreCancelled to true, see SPIGOT-4793.
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.useItemInHand() == Result.DENY || event.getAction() != Action.RIGHT_CLICK_BLOCK
				|| !isBoneMeal(event.getItem()) || !canBeFertilised(event.getClickedBlock())) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), NormalAchievements.FERTILISING, 1);
	}

	/**
	 * Determines whether the used item is bone meal.
	 *
	 * @param itemStack
	 * @return true if the item is bone meal, false otherwise
	 */
	@SuppressWarnings("deprecation")
	private boolean isBoneMeal(ItemStack itemStack) {
		return itemStack != null && itemStack.isSimilar(new ItemStack(Material.valueOf("INK_SACK"), 1, (short) 15));
	}

	/**
	 * Determines whether a material can be fertilised on the land.
	 *
	 * @param block
	 *
	 * @return true if the block can be fertilised, false otherwise
	 */
	@SuppressWarnings("deprecation")
	private boolean canBeFertilised(Block block) {
		short durability = block.getState().getData().toItemStack(0).getDurability();
		Material material = block.getType();
		if ("DOUBLE_PLANT".equals(material.name())) {
			if (durability == 10) {
				// Upper part of double plant. We must look at the lower part to get the double plant type.
				durability = block.getRelative(BlockFace.DOWN).getState().getData().toItemStack(0).getDurability();
			}
			// Fertilisation does not work on double tallgrass and large fern.
			return durability != 2 && durability != 3;
		}
		return material == Material.GRASS || material.name().endsWith("SAPLING")
				|| material == Material.POTATO && durability < 7
				|| material == Material.CARROT && durability < 7
				|| "CROPS".equals(material.name()) && durability < 7
				|| material == Material.PUMPKIN_STEM && durability < 7
				|| material == Material.MELON_STEM && durability < 7 || material == Material.BROWN_MUSHROOM
				|| material == Material.RED_MUSHROOM || material == Material.COCOA && durability < 9
				|| serverVersion >= 9 && "BEETROOT_BLOCK".equals(material.name()) && durability < 3;
	}

}
