package com.hm.achievement.listener.statistics;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with HoePlowings achievements.
 *
 * @author Pyves
 *
 */
@Singleton
public class HoePlowingListener extends AbstractListener {

	private Set<String> hoePlowableBlocks;

	@Inject
	public HoePlowingListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(NormalAchievements.HOEPLOWING, mainConfig, achievementMap, cacheManager);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		hoePlowableBlocks = new HashSet<>();
		for (String block : mainConfig.getStringList("HoePlowableBlocks")) {
			hoePlowableBlocks.add(block.toUpperCase());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR) // Do NOT set ignoreCancelled to true, deprecated for this event.
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.useItemInHand() == Result.DENY
				|| event.getAction() != Action.RIGHT_CLICK_BLOCK
				|| !event.getMaterial().name().contains("HOE")
				|| !hoePlowableBlocks.contains(event.getClickedBlock().getType().name())
				|| event.getClickedBlock().getRelative(BlockFace.UP).getType() != Material.AIR) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(event.getPlayer(), 1);
	}
}
