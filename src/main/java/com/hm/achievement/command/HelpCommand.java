package com.hm.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.particle.PacketSender;

public class HelpCommand {

	private AdvancedAchievements plugin;

	public HelpCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Display plugin help to the command sender.
	 */
	public void getHelp(CommandSender sender) {

		sender.sendMessage(plugin.getColor() + "-=-=-=-=-=-=-" + ChatColor.GRAY + "[" + plugin.getColor()
				+ plugin.getIcon() + ChatColor.translateAlternateColorCodes('&', "&lAdvanced Achievements")
				+ plugin.getColor() + plugin.getIcon() + ChatColor.GRAY + "]" + plugin.getColor() + "-=-=-=-=-=-=-");

		if (sender.hasPermission("achievement.book"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach book" + ChatColor.GRAY + " - "
							+ plugin.getPluginLang().getString("aach-command-book", "Receive your achievements book."),
					"/aach book");

		sendJsonClickableMessage(sender,
				plugin.getChatHeader() + plugin.getColor()
						+ "/aach stats" + ChatColor.GRAY + " - " + plugin.getPluginLang()
								.getString("aach-command-stats", "Amount of achievements you have received."),
				"/aach stats");

		if (sender.hasPermission("achievement.list"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor()
							+ "/aach list" + ChatColor.GRAY + " - " + plugin.getPluginLang()
									.getString("aach-command-list", "Display received and missing achievements."),
					"/aach list");

		sendJsonClickableMessage(sender,
				plugin.getChatHeader() + plugin.getColor() + "/aach top" + ChatColor.GRAY + " - "
						+ plugin.getPluginLang().getString("aach-command-top", "Display personal and global rankings."),
				"/aach top");

		if (sender.hasPermission("achievement.give"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach give &oach name&r" + ChatColor.GRAY + " - "
							+ ChatColor
									.translateAlternateColorCodes('&',
											plugin.getPluginLang()
													.getString("aach-command-give",
															"Give achievement ACH to player &7NAME.")
													.replace("ACH", "&oach&r&7").replace("NAME", "&oname&r&7")),
					"/aach give ach name");

		if (sender.hasPermission("achievement.check"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach check &oach name&r" + ChatColor.GRAY + " - "
							+ ChatColor
									.translateAlternateColorCodes('&',
											plugin.getPluginLang()
													.getString("aach-command-check",
															"Check if player NAME has &7received ACH.")
													.replace("ACH", "&oach&r&7").replace("NAME", "&oname&r&7")),
					"/aach check ach name");

		if (sender.hasPermission("achievement.delete"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach delete &oach name&r" + ChatColor.GRAY + " - "
							+ ChatColor.translateAlternateColorCodes('&',
									plugin.getPluginLang()
											.getString("aach-command-delete",
													"Delete achievement ACH from &7player NAME.")
											.replace("ACH", "&oach&r&7").replace("NAME", "&oname&r&7")),
					"/aach delete ach name");

		if (sender.hasPermission("achievement.reload"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor()
							+ "/aach reload" + ChatColor.GRAY + " - " + plugin.getPluginLang()
									.getString("aach-command-reload", "Reload the plugin's configuration."),
					"/aach reload");

		sendJsonClickableMessage(sender,
				plugin.getChatHeader() + plugin.getColor() + "/aach info" + ChatColor.GRAY + " - "
						+ plugin.getPluginLang().getString("aach-command-info",
								"Display various information about the plugin."),
				"/aach info");

		sender.sendMessage(plugin.getColor() + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
	}

	/**
	 * Send a packet message to the server in order to display a clickable
	 * message. A suggestion command is then displayed in the chat. Parts of
	 * this method were extracted from ELCHILEN0's AutoMessage plugin, under MIT
	 * license (http://dev.bukkit.org/bukkit-plugins/automessage/). Thanks for
	 * his help on this matter.
	 */
	public void sendJsonClickableMessage(CommandSender sender, String message, String command) {

		// Build the json format string.
		String json = "{\"text\":\"" + message + "\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\""
				+ command + "\"}}";
		if (sender instanceof Player)
			try {
				PacketSender.sendChatPacket((Player) sender, json);
			} catch (Exception ex) {

				plugin.getLogger().severe(
						"Errors while trying to display clickable in /aach help command. Displaying standard message instead.");
				sender.sendMessage(message);
			}
		else
			sender.sendMessage(message);
	}
}
