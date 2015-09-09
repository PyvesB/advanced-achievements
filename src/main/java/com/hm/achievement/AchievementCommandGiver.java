package com.hm.achievement;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

	public void achievementGive(CommandSender sender, String args[]) {

		String configAchievement = "Commands." + args[1];
		Player player = null;
		for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
			if (currentPlayer.getName().equalsIgnoreCase(args[2])) {
				player = currentPlayer;
				break;
			}
		}

		if (player == null) {

			sender.sendMessage(ChatColor.GRAY
					+ "["
					+ ChatColor.DARK_PURPLE
					+ plugin.getIcon()
					+ ChatColor.GRAY
					+ "] "
					+ Lang.PLAYER_OFFLINE.toString().replaceAll("PLAYER",
							args[2]));

			return;
		}
		if (plugin.getReward().checkAchievement(configAchievement)) {
			String name = plugin.getConfig().getString(
					configAchievement + ".Name");
			if (!plugin.isMultiCommand()
					&& plugin.getDb().hasAchievement(player, name)) {

				sender.sendMessage(ChatColor.GRAY
						+ "["
						+ ChatColor.DARK_PURPLE
						+ plugin.getIcon()
						+ ChatColor.GRAY
						+ "] "
						+ Lang.ACHIEVEMENT_ALREADY_RECEIVED.toString().replace(
								"PLAYER", args[2]));
				return;
			}

			String msg = plugin.getConfig().getString(
					configAchievement + ".Message");
			plugin.getAchievementDisplay()
					.displayAchievement(player, name, msg);
			Date now = new Date();
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			plugin.getDb().registerAchievement(
					player,
					plugin.getConfig().getString(configAchievement + ".Name"),
					plugin.getConfig()
							.getString(configAchievement + ".Message"),
					format.format(now));

			plugin.getReward().checkConfig(player, configAchievement);

			sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
					+ plugin.getIcon() + ChatColor.GRAY + "] "
					+ Lang.ACHIEVEMENT_GIVEN);
		} else {
			sender.sendMessage(ChatColor.GRAY
					+ "["
					+ ChatColor.DARK_PURPLE
					+ plugin.getIcon()
					+ ChatColor.GRAY
					+ "] "
					+ Lang.ACHIEVEMENT_NOT_FOUND.toString().replace("PLAYER",
							args[2]));
		}
	}
}