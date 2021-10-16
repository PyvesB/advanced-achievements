package com.hm.achievement.utils;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

/**
 * Class used to send fancy messages to the player; can be titles, hoverable chat messages or action bar messages. All
 * methods are static and this class cannot be instanciated.
 * 
 * @author Pyves
 *
 */
@Singleton
public final class FancyMessageSender {

	private final int serverVersion;

	@Inject
	public FancyMessageSender(int serverVersion) {
		this.serverVersion = serverVersion;
	}

	/**
	 * Sends a hoverable message to the player.
	 * 
	 * @param player Online player to send the message to.
	 * @param message The text to display in the chat.
	 * @param hover The text to display in the hover.
	 * @param color The color of the hover text.
	 */
	@SuppressWarnings("deprecation")
	public void sendHoverableMessage(Player player, String message, String hover, String color) {
		TextComponent tc = new TextComponent();
		tc.setText(ChatColor.translateAlternateColorCodes('&', message));
		tc.setColor(ChatColor.valueOf(color.toUpperCase()).asBungee());

		if (serverVersion >= 16) {
			tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new Text(ChatColor.translateAlternateColorCodes('&', hover))));
		} else {
			tc.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
					new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hover)).create()));
		}
		player.spigot().sendMessage(tc);
	}

	/**
	 * Sends a clickable and hoverable message to the player.
	 * 
	 * @param player Online player to send the message to.
	 * @param message The text to display in the chat.
	 * @param command The command that is entered when clicking on the message.
	 * @param hover The text to display in the hover.
	 * @param color The color of the hover text.
	 */
	@SuppressWarnings("deprecation")
	public void sendHoverableCommandMessage(Player player, String message, String command, String hover,
			String color) {
		TextComponent tc = new TextComponent();
		tc.setText(ChatColor.translateAlternateColorCodes('&', message));
		tc.setColor(ChatColor.valueOf(color.toUpperCase()).asBungee());
		tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

		if (serverVersion >= 16) {
			tc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					new Text(ChatColor.translateAlternateColorCodes('&', hover))));
		} else {
			tc.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
					new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', hover)).create()));
		}
		player.spigot().sendMessage(tc);
	}
}
