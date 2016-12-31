package com.hm.achievement.listener;

import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;

public class AchievePlayerCommandListener extends AbstractListener implements Listener {

	public AchievePlayerCommandListener(AdvancedAchievements plugin) {

		super(plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {

		Player player = event.getPlayer();

		if (!shouldEventBeTakenIntoAccountNoPermission(player)) {
			return;
		}

		MultipleAchievements category = MultipleAchievements.PLAYERCOMMANDS;

		// Strip down the command to a lowercase version without initial slash.
		String command = event.getMessage().substring(1).toLowerCase();

		for (String commandPrefix : plugin.getPluginConfig().getConfigurationSection(category.toString())
				.getKeys(false)) {
			if (command.startsWith(commandPrefix)) {
				if (player.hasPermission(category.toPermName() + '.' + StringUtils.replace(commandPrefix, " ", ""))) {
					updateStatisticAndAwardAchievementsIfAvailable(player, category, commandPrefix, 1);
				}
				break;
			}
		}
	}
}
