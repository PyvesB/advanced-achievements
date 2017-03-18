package com.hm.achievement.listener;

import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;

/**
 * Listener class to deal with Breaks achievements.
 * 
 * @author Pyves
 *
 */
public class AchieveBlockBreakListener extends AbstractListener implements Listener {

	private final boolean disableSilkTouchBreaks;
	private final boolean disableSilkTouchOreBreaks;

	public AchieveBlockBreakListener(AdvancedAchievements plugin) {

		super(plugin);
		// Load configuration parameter.
		disableSilkTouchBreaks = plugin.getPluginConfig().getBoolean("DisableSilkTouchBreaks", false);
		disableSilkTouchOreBreaks = plugin.getPluginConfig().getBoolean("DisableSilkTouchOreBreaks", false);
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {

		Player player = event.getPlayer();
		boolean silkTouchBreak = (version >= 9
				&& player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))
				|| version < 9 && player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH);

		if (!shouldEventBeTakenIntoAccountNoPermission(player) || disableSilkTouchBreaks && silkTouchBreak) {
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
		if (player.hasPermission(category.toPermName() + '.' + blockName + '.' + block.getData()) && plugin
				.getPluginConfig().isConfigurationSection(category + "." + blockName + ':' + block.getData())) {
			blockName += ":" + block.getData();
		} else {
			if (!player.hasPermission(category.toPermName() + '.' + blockName)) {
				return;
			}
			if (!plugin.getPluginConfig().isConfigurationSection(category + "." + blockName)) {
				return;
			}
		}

		updateStatisticAndAwardAchievementsIfAvailable(player, category, blockName, 1);
	}
}
