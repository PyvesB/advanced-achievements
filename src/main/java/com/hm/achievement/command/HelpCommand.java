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

	private String langCommandList;
	private String langCommandListHover;
	private String langCommandTop;
	private String langCommandTopHover;
	private String langCommandInfo;
	private String langCommandInfoHover;
	private String langCommandBook;
	private String langCommandBookHover;
	private String langCommandWeek;
	private String langCommandWeekHover;
	private String langCommandStats;
	private String langCommandStatsHover;
	private String langCommandMonth;
	private String langCommandMonthHover;
	private String langCommandToggleHover;
	private String langCommandToggle;
	private String langCommandReload;
	private String langCommandReloadgover;
	private String langCommandGive;
	private String langCommandGiveHover;
	private String langCommandReset;
	private String langCommandResetHover;
	private String langCommandCheck;
	private String langCommandCheckHover;
	private String langCommandDelete;
	private String langCommandDeleteHover;
	private String langTip;

	public HelpCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		langCommandList = plugin.getChatHeader() + configColor + "/aach list" + ChatColor.GRAY + " > "
				+ plugin.getPluginLang().getString("aach-command-list", "Display received and missing achievements.");
		langCommandListHover = plugin.getPluginLang().getString("aach-command-list-hover",
				"Fancy GUI to get an overview of all achievements and your progress!");
		langCommandTop = plugin.getChatHeader() + configColor + "/aach top" + ChatColor.GRAY + " > "
				+ plugin.getPluginLang().getString("aach-command-top", "Display personal and global rankings.");
		langCommandTopHover = plugin.getPluginLang().getString("aach-command-top-hover",
				"Who are the server's leaders and how do you compare to them?");
		langCommandInfo = plugin.getChatHeader() + configColor + "/aach info" + ChatColor.GRAY + " > "
				+ plugin.getPluginLang().getString("aach-command-info", "Display information about the plugin.");
		langCommandInfoHover = plugin.getPluginLang().getString("aach-command-info-hover",
				"Some extra info about the plugin and its awesome author!");
		langCommandBook = plugin.getChatHeader() + configColor + "/aach book" + ChatColor.GRAY + " > "
				+ plugin.getPluginLang().getString("aach-command-book", "Receive your achievements book.");
		langCommandBookHover = plugin.getPluginLang().getString("aach-command-book-hover",
				"RP items you can collect and exchange with others! Time-based listing.");
		langCommandWeek = plugin.getChatHeader() + configColor + "/aach week" + ChatColor.GRAY + " > "
				+ plugin.getPluginLang().getString("aach-command-week", "Display weekly rankings.");
		langCommandWeekHover = plugin.getPluginLang().getString("aach-command-week-hover",
				"Best achievement hunters since the start of the week!");
		langCommandStats = plugin.getChatHeader() + configColor + "/aach stats" + ChatColor.GRAY + " > "
				+ plugin.getPluginLang().getString("aach-command-stats", "Display amount of received achievements.");
		langCommandStatsHover = plugin.getPluginLang().getString("aach-command-stats-hover",
				"Progress bar. Gotta catch 'em all!");
		langCommandMonth = plugin.getChatHeader() + configColor + "/aach month" + ChatColor.GRAY + " > "
				+ plugin.getPluginLang().getString("aach-command-month", "Display monthly rankings.");
		langCommandMonthHover = plugin.getPluginLang().getString("aach-command-month-hover",
				"Best achievement hunters since the start of the month!");
		langCommandToggle = plugin.getChatHeader() + configColor + "/aach toggle" + ChatColor.GRAY + " > "
				+ plugin.getPluginLang().getString("aach-command-toggle", "Toggle achievements of other players.");
		langCommandToggleHover = plugin.getPluginLang().getString("aach-command-toggle-hover",
				"Your choice is saved until next server restart!");
		langCommandReload = plugin.getChatHeader() + configColor + "/aach reload" + ChatColor.GRAY + " > "
				+ plugin.getPluginLang().getString("aach-command-reload", "Reload the plugin's configuration.");
		langCommandReloadgover = plugin.getPluginLang().getString("aach-command-reload-hover",
				"Reload most settings in config.yml and lang.yml files.");
		langCommandGive = plugin.getChatHeader() + configColor + "/aach give &oach player" + ChatColor.GRAY + " > "
				+ ChatColor.translateAlternateColorCodes('&',
						StringUtils.replaceEach(
								plugin.getPluginLang().getString("aach-command-give",
										"Give achievement ACH to &7NAME."),
								new String[] { "ACH", "NAME" }, new String[] { "&oach&7", "&oplayer&7" }));
		langCommandGiveHover = plugin.getPluginLang().getString("aach-command-give-hover",
				"Player must be online; only Commands achievements can be used.");
		langCommandReset = plugin.getChatHeader() + configColor + "/aach reset &ocat player" + ChatColor.GRAY + " > "
				+ StringUtils.replaceOnce(
						plugin.getPluginLang().getString("aach-command-reset", "Reset statistic for category CAT."),
						"CAT", "&ocat&7");
		langCommandResetHover = plugin.getPluginLang().getString("aach-command-reset-hover",
				"Player must be online; for categories with subcategories, they are all reset!");
		langCommandCheck = plugin.getChatHeader() + configColor + "/aach check &oach player" + ChatColor.GRAY + " > "
				+ ChatColor.translateAlternateColorCodes('&',
						StringUtils.replaceEach(
								plugin.getPluginLang().getString("aach-command-check", "Check if NAME has ACH."),
								new String[] { "ACH", "NAME" }, new String[] { "&oach&7", "&oplayer&7" }));
		langCommandCheckHover = plugin.getPluginLang().getString("aach-command-check-hover",
				"Use the Name parameter specified in the config.");
		langCommandDelete = plugin.getChatHeader() + configColor + "/aach delete &oach player" + ChatColor.GRAY + " > "
				+ ChatColor.translateAlternateColorCodes('&',
						StringUtils.replaceEach(
								plugin.getPluginLang().getString("aach-command-delete", "Delete ACH from NAME."),
								new String[] { "ACH", "NAME" }, new String[] { "&oach&7", "&oplayer&7" }));
		langCommandDeleteHover = plugin.getPluginLang().getString("aach-command-delete-hover",
				"Player must be online; does not reset any associated statistics.");
		langTip = ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', plugin.getPluginLang()
				.getString("aach-tip", "&lHINT &8You can &7&n&ohover &8or &7&n&oclick &8on the commands!"));
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		// Header.
		sender.sendMessage(configColor + "------------ " + configIcon
				+ ChatColor.translateAlternateColorCodes('&', " &lAdvanced Achievements ") + configColor + configIcon
				+ configColor + " ------------");

		if (sender.hasPermission("achievement.list")) {
			sendJsonClickableHoverableMessage(sender, langCommandList, "/aach list", langCommandListHover);
		}

		if (sender.hasPermission("achievement.top")) {
			sendJsonClickableHoverableMessage(sender, langCommandTop, "/aach top", langCommandTopHover);
		}

		sendJsonClickableHoverableMessage(sender, langCommandInfo, "/aach info", langCommandInfoHover);

		if (sender.hasPermission("achievement.book")) {
			sendJsonClickableHoverableMessage(sender, langCommandBook, "/aach book", langCommandBookHover);
		}

		if (sender.hasPermission("achievement.week")) {
			sendJsonClickableHoverableMessage(sender, langCommandWeek, "/aach week", langCommandWeekHover);
		}

		if (sender.hasPermission("achievement.stats")) {
			sendJsonClickableHoverableMessage(sender, langCommandStats, "/aach stats", langCommandStatsHover);
		}

		if (sender.hasPermission("achievement.month")) {
			sendJsonClickableHoverableMessage(sender, langCommandMonth, "/aach month", langCommandMonthHover);
		}

		if (sender.hasPermission("achievement.toggle")) {
			sendJsonClickableHoverableMessage(sender, langCommandToggle, "/aach toggle", langCommandToggleHover);
		}

		if (sender.hasPermission("achievement.reload")) {
			sendJsonClickableHoverableMessage(sender, langCommandReload, "/aach reload", langCommandReloadgover);
		}

		if (sender.hasPermission("achievement.give")) {
			sendJsonClickableHoverableMessage(sender, langCommandGive, "/aach give ach name", langCommandGiveHover);
		}

		if (sender.hasPermission("achievement.reset")) {
			sendJsonClickableHoverableMessage(sender, langCommandReset, "/aach reset cat name", langCommandResetHover);
		}

		if (sender.hasPermission("achievement.check")) {
			sendJsonClickableHoverableMessage(sender, langCommandCheck, "/aach check ach name", langCommandCheckHover);
		}

		if (sender.hasPermission("achievement.delete")) {
			sendJsonClickableHoverableMessage(sender, langCommandDelete, "/aach delete ach name",
					langCommandDeleteHover);
		}

		// Empty line.
		sender.sendMessage(configColor + " ");

		sender.sendMessage(langTip);
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
	private void sendJsonClickableHoverableMessage(CommandSender sender, String message, String command, String hover) {
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
