package com.hm.achievement.listener.statistics;

import static org.bukkit.enchantments.Enchantment.SILK_TOUCH;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;

/**
 * Listener class to deal with Breaks achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class BreaksListener extends AbstractListener {

	private Set<String> oreBlocks;

	private boolean disableSilkTouchBreaks;
	private boolean disableSilkTouchOreBreaks;

	@Inject
	public BreaksListener(@Named("main") YamlConfiguration mainConfig, AchievementMap achievementMap,
			CacheManager cacheManager) {
		super(MultipleAchievements.BREAKS, mainConfig, achievementMap, cacheManager);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		disableSilkTouchBreaks = mainConfig.getBoolean("DisableSilkTouchBreaks");
		disableSilkTouchOreBreaks = mainConfig.getBoolean("DisableSilkTouchOreBreaks");
		oreBlocks = new HashSet<>();
		for (String block : mainConfig.getStringList("OreBlocks")) {
			oreBlocks.add(block.toUpperCase());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (disableSilkTouchBreaks || disableSilkTouchOreBreaks) {
			if (player.getInventory().getItemInMainHand().containsEnchantment(SILK_TOUCH)
					&& (disableSilkTouchBreaks || oreBlocks.contains(block.getType().name()))) {
				return;
			}
		}

		String blockName = block.getType().name().toLowerCase();
		if (!player.hasPermission(category.toChildPermName(blockName))) {
			return;
		}

		Set<String> subcategories = new HashSet<>();
		addMatchingSubcategories(subcategories, blockName);
		updateStatisticAndAwardAchievementsIfAvailable(player, subcategories, 1);
	}
}
