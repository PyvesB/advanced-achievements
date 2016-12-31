package com.hm.achievement.command;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.mcshared.particle.PacketSender;

/**
 * Class in charge of displaying the plugin's help (/aach help).
 * 
 * @author Pyves
 */
public class HelpCommand extends AbstractCommand {

	public HelpCommand(AdvancedAchievements plugin) {

		super(plugin);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {

		// Header.
		sender.sendMessage(plugin.getColor() + "------------ " + plugin.getColor() + plugin.getIcon()
				+ ChatColor.translateAlternateColorCodes('&', " &lAdvanced Achievements ") + plugin.getColor()
				+ plugin.getIcon() + plugin.getColor() + " ------------");

		if (sender.hasPermission("achievement.list")) {
			sendJsonClickableHoverableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach list" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-list",
									"Display received and missing achievements."),
					"/aach list", plugin.getPluginLang().getString("aach-command-list-hover",
							"Fancy GUI to get an overview of all achievements and your progress!"));
		}

		if (sender.hasPermission("achievement.top")) {
			sendJsonClickableHoverableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach top" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-top",
									"Display personal and global rankings."),
					"/aach top", plugin.getPluginLang().getString("aach-command-top-hover",
							"Who are the server's leaders and how do you compare to them?"));
		}

		sendJsonClickableHoverableMessage(sender,
				plugin.getChatHeader() + plugin.getColor() + "/aach info" + ChatColor.GRAY + " > "
						+ plugin.getPluginLang().getString("aach-command-info",
								"Display information about the plugin."),
				"/aach info", plugin.getPluginLang().getString("aach-command-info-hover",
						"Some extra info about the plugin and its awesome author!"));

		if (sender.hasPermission("achievement.book")) {
			sendJsonClickableHoverableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach book" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-book", "Receive your achievements book."),
					"/aach book", plugin.getPluginLang().getString("aach-command-book-hover",
							"RP items you can collect and exchange with others! Time-based listing."));
		}

		if (sender.hasPermission("achievement.week")) {
			sendJsonClickableHoverableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach week" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-week", "Display weekly rankings."),
					"/aach week", plugin.getPluginLang().getString("aach-command-week-hover",
							"Best achievement hunters since the start of the week!"));
		}

		if (sender.hasPermission("achievement.stats")) {
			sendJsonClickableHoverableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach stats" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-stats",
									"Display amount of received achievements."),
					"/aach stats",
					plugin.getPluginLang().getString("aach-command-stats-hover", "Progress bar. Gotta catch 'em all!"));
		}

		if (sender.hasPermission("achievement.month")) {
			sendJsonClickableHoverableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach month" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-month", "Display monthly rankings."),
					"/aach month", plugin.getPluginLang().getString("aach-command-month-hover",
							"Best achievement hunters since the start of the month!"));
		}

		if (sender.hasPermission("achievement.reload")) {
			sendJsonClickableHoverableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach reload" + ChatColor.GRAY + " > "
							+ plugin.getPluginLang().getString("aach-command-reload",
									"Reload the plugin's configuration."),
					"/aach reload", plugin.getPluginLang().getString("aach-command-reload-hover",
							"Reload most settings in config.yml and lang.yml files."));
		}

		if (sender.hasPermission("achievement.give")) {
			sendJsonClickableHoverableMessage(sender,
					plugin.getChatHeader() + plugin.getColor() + "/aach give &oach player" + ChatColor.GRAY + " > "
							+ ChatColor.translateAlternateColorCodes('&',
									StringUtils.replaceEach(
											plugin.getPluginLang().getString("aach-command-give",
													"Give achievement ACH to &7NAME."),
											new String[] { "ACH", "NAME" }, new String[] { "&oach&7", "&oplayer&7" })),
					"/aach give ach name", plugin.getPluginLang().getString("aach-command-give-hover",
							"Player must be online; only Commands achievements can be used."));
		}

		if (sender.hasPermission("achievement.check")) {
			sendJsonClickableHoverableMessage(sender, plugin.getChatHeader() + plugin.getColor()
					+ "/aach check &oach player" + ChatColor.GRAY + " > "
					+ ChatColor.translateAlternateColorCodes('&',
							StringUtils.replaceEach(
									plugin.getPluginLang().getString("aach-command-check", "Check if NAME has ACH."),
									new String[] { "ACH", "NAME" }, new String[] { "&oach&7", "&oplayer&7" })),
					"/aach check ach name", plugin.getPluginLang().getString("aach-command-check-hover",
							"Use the Name parameter specified in the config."));
		}

		if (sender.hasPermission("achievement.delete")) {
			sendJsonClickableHoverableMessage(sender, plugin.getChatHeader() + plugin.getColor()
					+ "/aach delete &oach player" + ChatColor.GRAY + " > "
					+ ChatColor.translateAlternateColorCodes('&',
							StringUtils.replaceEach(
									plugin.getPluginLang().getString("aach-command-delete", "Delete ACH from NAME."),
									new String[] { "ACH", "NAME" }, new String[] { "&oach&7", "&oplayer&7" })),
					"/aach delete ach name", plugin.getPluginLang().getString("aach-command-delete-hover",
							"Player must be online; does not reset any associated statistics."));
		}

		// Empty line.
		sender.sendMessage(plugin.getColor() + " ");

		// Tip message.
		sender.sendMessage(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', plugin.getPluginLang()
				.getString("aach-tip", "&lHINT &8You can &7&n&ohover &8or &7&n&oclick &8on the commands!")));
	}

	/**
	 * Sends a packet message to the server in order to display a clickable and hoverable message. A suggested command
	 * is displayed in the chat when clicked on, and an additional help message appears when a command is hovered.
	 * 
	 * @param sender
	 * @param message
	 * @param command
	 * @param hover
	 */
	public void sendJsonClickableHoverableMessage(CommandSender sender, String message, String command, String hover) {

		// Build the json format string.
		String json = "{\"text\":\"" + message + "\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\""
				+ command + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\"" + hover
				+ "\",\"color\":\"dark_purple\"}]}}";

		// Send clickable and hoverable message if sender is a player and if no exception is caught.
		if (sender instanceof Player) {
			try {
				PacketSender.sendChatMessagePacket((Player) sender, json);
			} catch (Exception e) {
				plugin.getLogger().warning(
						"Errors while trying to display clickable and hoverable message in /aach help command. Displaying standard message instead.");
				sender.sendMessage(message);
			}
		} else {
			sender.sendMessage(message);
		}
	}
}
