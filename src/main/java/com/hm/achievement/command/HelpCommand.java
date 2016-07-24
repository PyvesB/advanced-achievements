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

		sender.sendMessage(plugin.getColor() + "------------ " + ChatColor.GRAY + plugin.getColor() + plugin.getIcon()
				+ ChatColor.translateAlternateColorCodes('&', " &lAdvanced Achievements ") + plugin.getColor()
				+ plugin.getIcon() + ChatColor.GRAY + plugin.getColor() + " ------------");

		if (sender.hasPermission("achievement.list"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach list" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-list",
									"Display received and missing achievements."),
					"/aach list", plugin.getPluginLang().getString("aach-command-list-hover",
							"Fancy GUI to get an overview of all achievements and your progress!"));

		if (sender.hasPermission("achievement.top"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach top" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-top",
									"Display personal and global rankings."),
					"/aach top", plugin.getPluginLang().getString("aach-command-top-hover",
							"Who are the server's leaders and how do you compare to them?"));

		sendJsonClickableMessage(sender,
				plugin.getChatHeader() + plugin.getColor() + "/aach info" + ChatColor.GRAY + " > "
						+ plugin.getPluginLang().getString("aach-command-info",
								"Display information about the plugin."),
				"/aach info", plugin.getPluginLang().getString("aach-command-info-hover",
						"Some extra info about the plugin and its awesome author!"));

		if (sender.hasPermission("achievement.book"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach book" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-book", "Receive your achievements book."),
					"/aach book", plugin.getPluginLang().getString("aach-command-book-hover",
							"RP items you can collect and exchange with others! Time-based listing."));

		if (sender.hasPermission("achievement.week"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach week" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-week", "Display weekly rankings."),
					"/aach week", plugin.getPluginLang().getString("aach-command-week-hover",
							"Best achievement hunters since the start of the week!"));

		if (sender.hasPermission("achievement.stats"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach stats" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-stats",
									"Display amount of received achievements."),
					"/aach stats",
					plugin.getPluginLang().getString("aach-command-stats-hover", "Progress bar. Gotta catch 'em all!"));

		if (sender.hasPermission("achievement.month"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach month" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-month", "Display monthly rankings."),
					"/aach month", plugin.getPluginLang().getString("aach-command-month-hover",
							"Best achievement hunters since the start of the month!"));

		if (sender.hasPermission("achievement.reload"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach reload" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-reload",
									"Reload the plugin's configuration."),
					"/aach reload", plugin.getPluginLang().getString("aach-command-reload-hover",
							"Reload most settings in config.yml and lang.yml files."));

		if (sender.hasPermission("achievement.give"))
			sendJsonClickableMessage(sender, plugin.getChatHeader() + plugin.getColor() + "/aach give &oach player&r"
					+ ChatColor.GRAY + " > "
					+ ChatColor.translateAlternateColorCodes('&',
							plugin.getPluginLang().getString("aach-command-give", "Give achievement ACH to &7NAME.")
									.replace("ACH", "&oach&r&7").replace("NAME", "&oplayer&r&7")),
					"/aach give ach name", plugin.getPluginLang().getString("aach-command-give-hover",
							"Player must be online; only Commands achievements can be used."));

		if (sender.hasPermission("achievement.check"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach check &oach player&r" + ChatColor.GRAY + " > "
							+ ChatColor.translateAlternateColorCodes('&',
									plugin.getPluginLang().getString("aach-command-check", "Check if NAME has ACH.")
											.replace("ACH", "&oach&r&7").replace("NAME", "&oplayer&r&7")),
					"/aach check ach name", plugin.getPluginLang().getString("aach-command-check-hover",
							"Don't forget to add the colors defined in the config file."));

		if (sender.hasPermission("achievement.delete"))
			sendJsonClickableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach delete &oach player&r" + ChatColor.GRAY + " > "
							+ ChatColor.translateAlternateColorCodes('&',
									plugin.getPluginLang().getString("aach-command-delete", "Delete ACH from NAME.")
											.replace("ACH", "&oach&r&7").replace("NAME", "&oplayer&r&7")),
					"/aach delete ach name", plugin.getPluginLang().getString("aach-command-delete-hover",
							"Player must be online; does not reset any associated statistics."));

		sender.sendMessage(plugin.getColor() + " ");

		sender.sendMessage(ChatColor.GRAY + "§lHINT§r" + ChatColor.DARK_GRAY + " You can " + ChatColor.GRAY
				+ "§n§ohover§r" + ChatColor.DARK_GRAY + " or " + ChatColor.GRAY + "§n§oclick§r" + ChatColor.DARK_GRAY
				+ " on the commands!");
	}

	/**
	 * Send a packet message to the server in order to display a clickable message. A suggestion command is then
	 * displayed in the chat. Parts of this method were extracted from ELCHILEN0's AutoMessage plugin, under MIT license
	 * (http://dev.bukkit.org/bukkit-plugins/automessage/). Thanks for his help on this matter.
	 */
	public void sendJsonClickableMessage(CommandSender sender, String message, String command, String hover) {

		// Build the json format string.
		String json = "{\"text\":\"" + message + "\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\""
				+ command + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\"" + hover
				+ "\",\"color\":\"dark_purple\"}]}}";
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
