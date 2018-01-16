package com.hm.achievement.command;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.lang.command.HelpLang;
import com.hm.mcshared.particle.PacketSender;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
	private String langCommandReloadHover;
	private String langCommandGenerate;
	private String langCommandGenerateHover;
	private String langCommandGive;
	private String langCommandGiveHover;
	private String langCommandAdd;
	private String langCommandAddHover;
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
		langCommandList = header("/aach list") + Lang.get(HelpLang.LIST, plugin);
		langCommandListHover = Lang.get(HelpLang.Hover.LIST, plugin);
		langCommandTop = header("/aach top") + Lang.get(HelpLang.TOP, plugin);
		langCommandTopHover = Lang.get(HelpLang.Hover.TOP, plugin);
		langCommandInfo = header("/aach info") + Lang.get(HelpLang.INFO, plugin);
		langCommandInfoHover = Lang.get(HelpLang.Hover.INFO, plugin);
		langCommandBook = header("/aach book") + Lang.get(HelpLang.BOOK, plugin);
		langCommandBookHover = Lang.get(HelpLang.Hover.BOOK, plugin);
		langCommandWeek = header("/aach week") + Lang.get(HelpLang.WEEK, plugin);
		langCommandWeekHover = Lang.get(HelpLang.Hover.WEEK, plugin);
		langCommandStats = header("/aach stats") + Lang.get(HelpLang.STATS, plugin);
		langCommandStatsHover = Lang.get(HelpLang.Hover.STATS, plugin);
		langCommandMonth = header("/aach month") + Lang.get(HelpLang.MONTH, plugin);
		langCommandMonthHover = Lang.get(HelpLang.Hover.MONTH, plugin);
		langCommandToggle = header("/aach toggle") + Lang.get(HelpLang.TOGGLE, plugin);
		langCommandToggleHover = Lang.get(HelpLang.Hover.TOGGLE, plugin);
		langCommandReload = header("/aach reload") + Lang.get(HelpLang.RELOAD, plugin);
		langCommandReloadHover = Lang.get(HelpLang.Hover.RELOAD, plugin);
		langCommandGenerate = header("/aach generate") + Lang.get(HelpLang.GENERATE, plugin);
		langCommandGenerateHover = Lang.get(HelpLang.Hover.GENERATE, plugin);
		langCommandGive = header("/aach give &oach player")
				+ translateColorCodes(
				Lang.getEachReplaced(
						HelpLang.GIVE, plugin,
						new String[]{"ACH", "NAME"}, new String[]{"&oach&7", "&oplayer&7"}
				)
		);
		langCommandGiveHover = Lang.get(HelpLang.Hover.GIVE, plugin);
		langCommandAdd = header("/aach add &ox cat player") + Lang.get(HelpLang.ADD, plugin);
		langCommandAddHover = Lang.get(HelpLang.Hover.ADD, plugin);
		langCommandReset = header("/aach reset &ocat player")
				+ Lang.getReplacedOnce(HelpLang.RESET, "CAT", "&ocat&7", plugin);
		langCommandResetHover = Lang.get(HelpLang.Hover.RESET, plugin);
		langCommandCheck = header("/aach check &oach player")
				+ translateColorCodes(
				Lang.getEachReplaced(
						HelpLang.CHECK, plugin,
						new String[]{"ACH", "NAME"}, new String[]{"&oach&7", "&oplayer&7"}
				));
		langCommandCheckHover = Lang.get(HelpLang.Hover.CHECK, plugin);
		langCommandDelete = header("/aach delete &oach player")
				+ translateColorCodes(
				Lang.getEachReplaced(
						HelpLang.DELETE, plugin,
						new String[]{"ACH", "NAME"}, new String[]{"&oach&7", "&oplayer&7"}
				));
		langCommandDeleteHover = Lang.get(HelpLang.Hover.DELETE, plugin);
		langTip = ChatColor.GRAY + translateColorCodes(Lang.get(CmdLang.AACH_TIP, plugin));
	}

	private String header(String command) {
		return plugin.getChatHeader() + configColor + command + ChatColor.GRAY + " > ";
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		// Header.
		sender.sendMessage(configColor + "------------ " + configIcon
				+ translateColorCodes(" &lAdvanced Achievements ") + configColor + configIcon
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
			sendJsonClickableHoverableMessage(sender, langCommandReload, "/aach reload", langCommandReloadHover);
		}

		if (plugin.getServerVersion() >= 12 && sender.hasPermission("achievement.generate")) {
			sendJsonClickableHoverableMessage(sender, langCommandGenerate, "/aach generate", langCommandGenerateHover);
		}

		if (sender.hasPermission("achievement.give")) {
			sendJsonClickableHoverableMessage(sender, langCommandGive, "/aach give ach name", langCommandGiveHover);
		}

		if (sender.hasPermission("achievement.add")) {
			sendJsonClickableHoverableMessage(sender, langCommandAdd, "/aach add x cat name", langCommandAddHover);
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
		// Send clickable and hoverable message if sender is a player and if supported by the Minecraft version.
		if (sender instanceof Player && plugin.getServerVersion() > 7) {
			// Build the json format string.
			String json = "{\"text\":\"" + message + "\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\""
					+ command + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\"" + hover
					+ "\",\"color\":\"" + configColor.name().toLowerCase() + "\"}]}}";
			try {
				PacketSender.sendChatMessagePacket((Player) sender, json);
			} catch (Exception e) {
				plugin.getLogger().warning(
						"Failed to display clickable and hoverable message in /aach help command. Displaying standard message instead.");
				sender.sendMessage(message);
			}
		} else {
			sender.sendMessage(message);
		}
	}
}
