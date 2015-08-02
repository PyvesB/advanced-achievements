package com.hm.achievement;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.hm.achievement.AdvancedAchievements;

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

			if (plugin.getLanguage().equals("fr"))
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
						+ plugin.getIcon() + ChatColor.GRAY + "] "
						+ "Le joueur " + args[2] + " n'est pas en ligne!");
			else
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
						+ plugin.getIcon() + ChatColor.GRAY + "] "
						+ "The player " + args[2] + " is offline!");

			return;
		}
		if (plugin.getReward().checkAchievement(configAchievement)) {
			String name = plugin.getConfig().getString(
					configAchievement + ".Name");
			if (!plugin.isMultiCommand()
					&& plugin.getDb().hasAchievement(player, name)) {
				if (plugin.getLanguage().equals("fr"))
					sender.sendMessage(ChatColor.GRAY + "["
							+ ChatColor.DARK_PURPLE + plugin.getIcon()
							+ ChatColor.GRAY + "] " + "Le joueur " + args[2]
							+ " possède déjà ce succès !");
				else
					sender.sendMessage(ChatColor.GRAY + "["
							+ ChatColor.DARK_PURPLE + plugin.getIcon()
							+ ChatColor.GRAY + "] " + "The player " + args[2]
							+ " has already received this achievement!");
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
					"&0" + format.format(now));

			plugin.getReward().checkConfig(player, configAchievement);

			if (plugin.getLanguage().equals("fr"))
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
						+ plugin.getIcon() + ChatColor.GRAY + "] "
						+ "Succès donné!");
			else
				sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
						+ plugin.getIcon() + ChatColor.GRAY + "] "
						+ "Achievement given!");
		}
	}
}