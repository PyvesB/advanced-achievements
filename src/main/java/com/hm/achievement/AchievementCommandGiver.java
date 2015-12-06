package com.hm.achievement;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.language.Lang;

public class AchievementCommandGiver implements Listener {

	private AdvancedAchievements plugin;

	public AchievementCommandGiver(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Give an achievement with an in game or console command.
	 */
	public void achievementGive(CommandSender sender, String args[]) {

		String configAchievement = "Commands." + args[1];
		Player player = null;
		for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
			if (currentPlayer.getName().equalsIgnoreCase(args[2])) {
				player = currentPlayer;
				break;
			}
		}

		// If player not found or is offline.
		if (player == null) {

			sender.sendMessage(plugin.getChatHeader() + Lang.PLAYER_OFFLINE.toString().replaceAll("PLAYER", args[2]));

			return;
		}
		if (plugin.getReward().checkAchievement(configAchievement)) {

			if (!plugin.isMultiCommand()
					&& plugin.getDb().hasAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"))) {

				sender.sendMessage(plugin.getChatHeader()
						+ Lang.ACHIEVEMENT_ALREADY_RECEIVED.toString().replace("PLAYER", args[2]));
				return;
			}

			plugin.getAchievementDisplay().displayAchievement(player, configAchievement);
			Date now = new Date();
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			plugin.getDb().registerAchievement(player, plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig().getString(configAchievement + ".Message"), format.format(now));

			plugin.getReward().checkConfig(player, configAchievement);

			sender.sendMessage(plugin.getChatHeader() + Lang.ACHIEVEMENT_GIVEN);
		} else {
			sender.sendMessage(plugin.getChatHeader() + Lang.ACHIEVEMENT_NOT_FOUND.toString().replace("PLAYER", args[2]));
		}
	}
}
