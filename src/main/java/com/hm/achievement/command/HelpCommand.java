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

	/**
	 * Display plugin help to the command sender.
	 */
	public void getHelp(CommandSender sender) {

		sender.sendMessage(plugin.getColor() + "-=-=-=-=-=-=-" + ChatColor.GRAY + "[" + plugin.getColor()
				+ plugin.getIcon() + "§lAdvanced Achievements" + plugin.getColor() + plugin.getIcon() + ChatColor.GRAY
				+ "]" + plugin.getColor() + "-=-=-=-=-=-=-");

		sendJsonClickableMessage(sender, plugin.getChatHeader() + plugin.getColor() + "/aach book" + ChatColor.GRAY
				+ " - " + Lang.AACH_COMMAND_BOOK, "/aach book");

		sendJsonClickableMessage(sender, plugin.getChatHeader() + plugin.getColor() + "/aach stats" + ChatColor.GRAY
				+ " - " + Lang.AACH_COMMAND_STATS, "/aach stats");

		sendJsonClickableMessage(sender, plugin.getChatHeader() + plugin.getColor() + "/aach list" + ChatColor.GRAY
				+ " - " + Lang.AACH_COMMAND_LIST, "/aach list");

		sendJsonClickableMessage(sender, plugin.getChatHeader() + plugin.getColor() + "/aach top" + ChatColor.GRAY
				+ " - " + Lang.AACH_COMMAND_TOP, "/aach top");

		sendJsonClickableMessage(sender,
				plugin.getChatHeader() + plugin.getColor() + "/aach give §oach name§r"
						+ ChatColor.GRAY + " - " + ChatColor.translateAlternateColorCodes('&', Lang.AACH_COMMAND_GIVE
								.toString().replace("ACH", "§oach§r&7").replace("NAME", "§oname§r&7")),
				"/aach give ach name");

		sendJsonClickableMessage(sender,
				plugin.getChatHeader() + plugin.getColor() + "/aach check §oach name§r"
						+ ChatColor.GRAY + " - " + ChatColor.translateAlternateColorCodes('&', Lang.AACH_COMMAND_CHECK
								.toString().replace("ACH", "§oach§r&7").replace("NAME", "§oname§r&7")),
				"/aach check ach name");

		sendJsonClickableMessage(sender,
				plugin.getChatHeader() + plugin.getColor() + "/aach delete §oach name§r"
						+ ChatColor.GRAY + " - " + ChatColor.translateAlternateColorCodes('&', Lang.AACH_COMMAND_DELETE
								.toString().replace("ACH", "§oach§r&7").replace("NAME", "§oname§r&7")),
				"/aach delete ach name");

		sendJsonClickableMessage(sender, plugin.getChatHeader() + plugin.getColor() + "/aach reload" + ChatColor.GRAY
				+ " - " + Lang.AACH_COMMAND_RELOAD, "/aach reload");

		sendJsonClickableMessage(sender, plugin.getChatHeader() + plugin.getColor() + "/aach info" + ChatColor.GRAY
				+ " - " + Lang.AACH_COMMAND_INFO, "/aach info");

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
		String json = "{\"text\":\"" + message + "\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + command + "\"}}";
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
