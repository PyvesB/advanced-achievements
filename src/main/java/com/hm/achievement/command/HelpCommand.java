package com.hm.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.language.Lang;
import com.hm.achievement.particle.PacketSender;

public class HelpCommand {

	private AdvancedAchievements plugin;

	public HelpCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	public void getHelp(CommandSender sender) {

		sender.sendMessage((new StringBuilder()).append(ChatColor.DARK_PURPLE).append("-=-=-=-=-=-=-")
				.append(ChatColor.GRAY).append("[").append(ChatColor.DARK_PURPLE).append(plugin.getIcon())
				.append("§lAdvanced Achievements").append(ChatColor.DARK_PURPLE).append(plugin.getIcon())
				.append(ChatColor.GRAY).append("]").append(ChatColor.DARK_PURPLE).append("-=-=-=-=-=-=-").toString());

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(ChatColor.DARK_PURPLE + "/aach book")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_BOOK).toString(), "/aach book");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(ChatColor.DARK_PURPLE + "/aach stats")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_STATS).toString(), "/aach stats");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(ChatColor.DARK_PURPLE + "/aach list")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_LIST).toString(), "/aach list");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(ChatColor.DARK_PURPLE + "/aach top")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_TOP).toString(), "/aach top");

		sendJsonClickableMessage(
				sender,
				(new StringBuilder())
						.append(plugin.getChatHeader())
						.append(ChatColor.DARK_PURPLE + "/aach give §oach name§r")
						.append(ChatColor.GRAY)
						.append(" - "
								+ ChatColor.translateAlternateColorCodes('&', Lang.AACH_COMMAND_GIVE.toString()
										.replace("ACH", "§oach§r&7").replace("NAME", "§oname§r&7"))).toString(),
				"/aach give ach name");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(ChatColor.DARK_PURPLE + "/aach reload")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_RELOAD).toString(), "/aach reload");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(ChatColor.DARK_PURPLE + "/aach info")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_INFO).toString(), "/aach info");

		sender.sendMessage((new StringBuilder()).append(ChatColor.DARK_PURPLE)
				.append("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-").toString());
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
		String json = "{text:\"" + message + "\",clickEvent:{action:suggest_command,value:\"" + command + "\"}}";

		// Get the string corresponding to the server Minecraft version.
		if (sender instanceof Player)
			try {
				PacketSender.sendPacket((Player) sender, json);
			} catch (Exception ex) {

				plugin.getLogger()
						.severe("Errors while trying to display clickable in /aach help command. Displaying standard message instead.");
				sender.sendMessage(message);
			}
		else
			sender.sendMessage(message);
	}
}
