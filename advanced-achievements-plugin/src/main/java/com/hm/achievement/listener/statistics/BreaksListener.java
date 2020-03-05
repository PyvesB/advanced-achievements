package com.hm.achievement.listener.statistics;

import static org.bukkit.enchantments.Enchantment.SILK_TOUCH;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Breaks achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class BreaksListener extends AbstractListener {

	private boolean disableSilkTouchBreaks;
	private boolean disableSilkTouchOreBreaks;

	@Inject
	public BreaksListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, CacheManager cacheManager, RewardParser rewardParser) {
		super(MultipleAchievements.BREAKS, mainConfig, serverVersion, sortedThresholds, cacheManager, rewardParser);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		disableSilkTouchBreaks = mainConfig.getBoolean("DisableSilkTouchBreaks");
		disableSilkTouchOreBreaks = mainConfig.getBoolean("DisableSilkTouchOreBreaks");
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (disableSilkTouchBreaks || disableSilkTouchOreBreaks) {
			ItemStack breakingTool = serverVersion >= 9 ? player.getInventory().getItemInMainHand() : player.getItemInHand();
			if (breakingTool.containsEnchantment(SILK_TOUCH) && (disableSilkTouchBreaks || isOre(block.getType().name()))) {
				return;
			}
		}

		String blockName = block.getType().name().toLowerCase();
		if (!player.hasPermission(category.toPermName() + '.' + blockName)) {
			return;
		}

		Set<String> foundAchievements = findAchievementsByCategoryAndName(
				blockName + ':' + block.getState().getData().toItemStack().getDurability());
		foundAchievements.addAll(findAchievementsByCategoryAndName(blockName));
		updateStatisticAndAwardAchievementsIfAvailable(player, foundAchievements, 1);
	}

	/**
	 * Determines whether the borken material is an ore.
	 * 
	 * @param materialName
	 * @return boolean if material is of type ore.
	 */
	private boolean isOre(String materialName) {
		switch (materialName) {
			case "COAL_ORE":
			case "DIAMOND_ORE":
			case "EMERALD_ORE":
			case "LAPIS_ORE":
			case "NETHER_QUARTZ_ORE":
			case "REDSTONE_ORE":
				// Pre Minecraft 1.13:
			case "QUARTZ_ORE":
			case "GLOWING_REDSTONE_ORE":
				return true;
			default:
				return false;
		}
	}
}
