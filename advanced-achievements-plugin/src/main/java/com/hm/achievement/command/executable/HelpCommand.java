package com.hm.achievement.command.executable;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.lang.command.HelpLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.FancyMessageSender;

/**
 * Class in charge of displaying the plugin's help (/aach help).
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "help", permission = "", minArgs = 0, maxArgs = Integer.MAX_VALUE)
public class HelpCommand extends AbstractCommand {

	private final int serverVersion;
	private final Logger logger;

	private ChatColor configColor;
	private String configIcon;

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
	private String langCommandInspect;
	private String langCommandInspectHover;
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

	@Inject
	public HelpCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, int serverVersion,
			Logger logger) {
		super(mainConfig, langConfig, pluginHeader);
		this.serverVersion = serverVersion;
		this.logger = logger;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configColor = ChatColor.getByChar(mainConfig.getString("Color", "5"));
		configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon", "\u2618"));

		langCommandList = header("/aach list") + LangHelper.get(HelpLang.LIST, langConfig);
		langCommandListHover = LangHelper.get(HelpLang.Hover.LIST, langConfig);
		langCommandTop = header("/aach top") + LangHelper.get(HelpLang.TOP, langConfig);
		langCommandTopHover = LangHelper.get(HelpLang.Hover.TOP, langConfig);
		langCommandInfo = header("/aach info") + LangHelper.get(HelpLang.INFO, langConfig);
		langCommandInfoHover = LangHelper.get(HelpLang.Hover.INFO, langConfig);
		langCommandBook = header("/aach book") + LangHelper.get(HelpLang.BOOK, langConfig);
		langCommandBookHover = LangHelper.get(HelpLang.Hover.BOOK, langConfig);
		langCommandWeek = header("/aach week") + LangHelper.get(HelpLang.WEEK, langConfig);
		langCommandWeekHover = LangHelper.get(HelpLang.Hover.WEEK, langConfig);
		langCommandStats = header("/aach stats") + LangHelper.get(HelpLang.STATS, langConfig);
		langCommandStatsHover = LangHelper.get(HelpLang.Hover.STATS, langConfig);
		langCommandMonth = header("/aach month") + LangHelper.get(HelpLang.MONTH, langConfig);
		langCommandMonthHover = LangHelper.get(HelpLang.Hover.MONTH, langConfig);
		langCommandToggle = header("/aach toggle") + LangHelper.get(HelpLang.TOGGLE, langConfig);
		langCommandToggleHover = LangHelper.get(HelpLang.Hover.TOGGLE, langConfig);
		langCommandReload = header("/aach reload") + LangHelper.get(HelpLang.RELOAD, langConfig);
		langCommandReloadHover = LangHelper.get(HelpLang.Hover.RELOAD, langConfig);
		langCommandGenerate = header("/aach generate") + LangHelper.get(HelpLang.GENERATE, langConfig);
		langCommandGenerateHover = LangHelper.get(HelpLang.Hover.GENERATE, langConfig);
		langCommandGive = header("/aach give &oach player") + translateColorCodes(LangHelper.getEachReplaced(HelpLang.GIVE,
				langConfig, new String[] { "ACH", "NAME" }, new String[] { "&oach&7", "&oplayer&7" }));
		langCommandInspect = header("/aach inspect &oach")
				+ translateColorCodes(LangHelper.getReplacedOnce(HelpLang.INSPECT, "ACH", "&oach&7", langConfig));
		langCommandInspectHover = LangHelper.get(HelpLang.Hover.INSPECT, langConfig);
		langCommandGiveHover = LangHelper.get(HelpLang.Hover.GIVE, langConfig);
		langCommandAdd = header("/aach add &ox cat player") + LangHelper.get(HelpLang.ADD, langConfig);
		langCommandAddHover = LangHelper.get(HelpLang.Hover.ADD, langConfig);
		langCommandReset = header("/aach reset &ocat player")
				+ LangHelper.getReplacedOnce(HelpLang.RESET, "CAT", "&ocat&7", langConfig);
		langCommandResetHover = LangHelper.get(HelpLang.Hover.RESET, langConfig);
		langCommandCheck = header("/aach check &oach player")
				+ translateColorCodes(LangHelper.getEachReplaced(HelpLang.CHECK, langConfig, new String[] { "ACH", "NAME" },
						new String[] { "&oach&7", "&oplayer&7" }));
		langCommandCheckHover = LangHelper.get(HelpLang.Hover.CHECK, langConfig);
		langCommandDelete = header("/aach delete &oach player")
				+ translateColorCodes(LangHelper.getEachReplaced(HelpLang.DELETE,
						langConfig, new String[] { "ACH", "NAME" }, new String[] { "&oach&7", "&oplayer&7" }));
		langCommandDeleteHover = LangHelper.get(HelpLang.Hover.DELETE, langConfig);
		langTip = ChatColor.GRAY + translateColorCodes(LangHelper.get(CmdLang.AACH_TIP, langConfig));
	}

	private String header(String command) {
		return pluginHeader.toString() + configColor + command + ChatColor.GRAY + " > ";
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		// Header.
		sender.sendMessage(configColor + "------------ " + configIcon + translateColorCodes(" &lAdvanced Achievements ")
				+ configColor + configIcon + configColor + " ------------");

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

		if (serverVersion >= 12 && sender.hasPermission("achievement.generate")) {
			sendJsonClickableHoverableMessage(sender, langCommandGenerate, "/aach generate", langCommandGenerateHover);
		}

		if (sender.hasPermission("achievement.inspect")) {
			sendJsonClickableHoverableMessage(sender, langCommandInspect, "/aach inspect ach", langCommandInspectHover);
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
			sendJsonClickableHoverableMessage(sender, langCommandDelete, "/aach delete ach name", langCommandDeleteHover);
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
		if (sender instanceof Player && serverVersion > 7) {
			try {
				FancyMessageSender.sendHoverableCommandMessage((Player) sender, message, command, hover,
						configColor.name().toLowerCase());
			} catch (Exception e) {
				logger.warning(
						"Failed to display clickable and hoverable message in /aach help command. Displaying standard message instead.");
				sender.sendMessage(message);
			}
		} else {
			sender.sendMessage(message);
		}
	}
}
