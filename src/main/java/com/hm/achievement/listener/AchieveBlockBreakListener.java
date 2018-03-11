package com.hm.achievement.listener;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.command.ReloadCommand;
import com.hm.achievement.db.DatabaseCacheManager;
import com.hm.achievement.utils.RewardParser;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Listener class to deal with Breaks achievements.
 * 
 * @author Pyves
 *
 */
@Singleton
public class AchieveBlockBreakListener extends AbstractListener {

	private boolean disableSilkTouchBreaks;
	private boolean disableSilkTouchOreBreaks;

	@Inject
	public AchieveBlockBreakListener(@Named("main") CommentedYamlConfiguration mainConfig, int serverVersion,
			Map<String, List<Long>> sortedThresholds, DatabaseCacheManager databaseCacheManager, RewardParser rewardParser,
			ReloadCommand reloadCommand) {
		super(mainConfig, serverVersion, sortedThresholds, databaseCacheManager, rewardParser, reloadCommand);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		disableSilkTouchBreaks = mainConfig.getBoolean("DisableSilkTouchBreaks", false);
		disableSilkTouchOreBreaks = mainConfig.getBoolean("DisableSilkTouchOreBreaks", false);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		boolean silkTouchBreak = (serverVersion >= 9
				&& player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))
				|| serverVersion < 9 && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH);

		if (!shouldIncreaseBeTakenIntoAccountNoPermissions(player) || disableSilkTouchBreaks && silkTouchBreak) {
			return;
		}

		Block block = event.getBlock();
		if (disableSilkTouchOreBreaks && silkTouchBreak) {
			switch (block.getType()) {
				case COAL_ORE:
				case DIAMOND_ORE:
				case EMERALD_ORE:
				case LAPIS_ORE:
				case QUARTZ_ORE:
				case REDSTONE_ORE:
				case GLOWING_REDSTONE_ORE:
					return;
				default:
					break;
			}
		}

		MultipleAchievements category = MultipleAchievements.BREAKS;

		String blockName = block.getType().name().toLowerCase();
		if (!player.hasPermission(category.toPermName() + '.' + blockName)) {
			return;
		}
		if (mainConfig.isConfigurationSection(
				category + "." + blockName + ':' + block.getState().getData().toItemStack().getDurability())) {
			blockName += ":" + block.getState().getData().toItemStack().getDurability();
		} else if (!mainConfig.isConfigurationSection(category + "." + blockName)) {
			return;
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, blockName, 1);
	}
}
