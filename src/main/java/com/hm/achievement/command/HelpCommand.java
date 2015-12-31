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

		sender.sendMessage((new StringBuilder()).append(plugin.getColor()).append("-=-=-=-=-=-=-")
				.append(ChatColor.GRAY).append("[").append(plugin.getColor()).append(plugin.getIcon())
				.append("§lAdvanced Achievements").append(plugin.getColor()).append(plugin.getIcon())
				.append(ChatColor.GRAY).append("]").append(plugin.getColor()).append("-=-=-=-=-=-=-").toString());

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(plugin.getColor() + "/aach book")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_BOOK).toString(),
				"/aach book");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(plugin.getColor() + "/aach stats")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_STATS).toString(),
				"/aach stats");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(plugin.getColor() + "/aach list")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_LIST).toString(),
				"/aach list");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(plugin.getColor() + "/aach top")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_TOP).toString(),
				"/aach top");

		sendJsonClickableMessage(sender, (new StringBuilder()).append(plugin.getChatHeader())
				.append(plugin.getColor() + "/aach give §oach name§r").append(ChatColor.GRAY)
				.append(" - " + ChatColor.translateAlternateColorCodes('&',
						Lang.AACH_COMMAND_GIVE.toString().replace("ACH", "§oach§r&7").replace("NAME", "§oname§r&7")))
				.toString(), "/aach give ach name");

		sendJsonClickableMessage(sender, (new StringBuilder()).append(plugin.getChatHeader())
				.append(plugin.getColor() + "/aach check §oach name§r").append(ChatColor.GRAY)
				.append(" - " + ChatColor.translateAlternateColorCodes('&',
						Lang.AACH_COMMAND_CHECK.toString().replace("ACH", "§oach§r&7").replace("NAME", "§oname§r&7")))
				.toString(), "/aach check ach name");
		
		sendJsonClickableMessage(sender, (new StringBuilder()).append(plugin.getChatHeader())
				.append(plugin.getColor() + "/aach delete §oach name§r").append(ChatColor.GRAY)
				.append(" - " + ChatColor.translateAlternateColorCodes('&',
						Lang.AACH_COMMAND_DELETE.toString().replace("ACH", "§oach§r&7").replace("NAME", "§oname§r&7")))
				.toString(), "/aach delete ach name");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(plugin.getColor() + "/aach reload")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_RELOAD).toString(),
				"/aach reload");

		sendJsonClickableMessage(sender,
				(new StringBuilder()).append(plugin.getChatHeader()).append(plugin.getColor() + "/aach info")
						.append(ChatColor.GRAY).append(" - " + Lang.AACH_COMMAND_INFO).toString(),
				"/aach info");

		sender.sendMessage((new StringBuilder()).append(plugin.getColor())
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
