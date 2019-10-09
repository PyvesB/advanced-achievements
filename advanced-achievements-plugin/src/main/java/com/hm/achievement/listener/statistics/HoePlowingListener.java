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

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with HoePlowings achievements.
 *
 * @author Pyves
 *
 */
@Singleton
public class HoePlowingListener extends AbstractListener {

	@Inject
	public HoePlowingListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(NormalAchievements.HOEPLOWING, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	@EventHandler(priority = EventPriority.MONITOR) // Do NOT set ignoreCancelled to true, deprecated for this event.
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.useItemInHand() == Result.DENY || event.getAction() != Action.RIGHT_CLICK_BLOCK
				|| !event.getMaterial().name().contains("HOE") || !canBePlowed(event.getClickedBlock())) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}

	/**
	 * Determines whether a material can be plowed with a hoe.
	 *
	 * @param block
	 *
	 * @return true if the block can be plowed, false otherwise
	 */
	private boolean canBePlowed(Block block) {
		return (serverVersion < 13 && block.getType() == Material.GRASS || block.getType() == Material.DIRT
				|| serverVersion >= 13 && block.getType() == Material.GRASS_BLOCK)
				&& block.getRelative(BlockFace.UP).getType() == Material.AIR;
	}
}
