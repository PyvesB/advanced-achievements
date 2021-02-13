package com.hm.achievement.utils;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.darkblade12.particleeffect.ReflectionUtils;
import com.darkblade12.particleeffect.ReflectionUtils.PackageType;

/**
 * Class used to send fancy messages to the player; can be titles, hoverable chat messages or action bar messages. All
 * methods are static and this class cannot be instanciated.
 * 
 * @author Pyves
 *
 */
public final class FancyMessageSender {

	private static final byte CHAT_MESSAGE_BYTE = 1;
	private static final byte ACTION_BAR_BYTE = 2;
	private static final int MINOR_VERSION_NUMBER = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	private static final String CLASS_CHAT_BASE_COMPONENT = "IChatBaseComponent";
	private static final String CLASS_CRAFT_PLAYER = "CraftPlayer";
	private static final String CLASS_ENTITY_PLAYER = "EntityPlayer";
	private static final String CLASS_PACKET = "Packet";
	private static final String CLASS_PACKET_PLAY_OUT_CHAT = "PacketPlayOutChat";
	private static final String CLASS_PACKET_PLAY_OUT_TITLE = "PacketPlayOutTitle";
	private static final String CLASS_PLAYER_CONNECTION = "PlayerConnection";
	private static final String ENUM_TITLE_ACTION = "EnumTitleAction";
	private static final String ENUM_CHAT_MESSAGE_TYPE = "ChatMessageType";
	private static final String FIELD_PLAYER_CONNECTION = "playerConnection";
	private static final String METHOD_GET_HANDLE = "getHandle";
	private static final String METHOD_SEND_PACKET = "sendPacket";
	private static final String NESTED_CHAT_SERIALIZER = "ChatSerializer";
	private static final String PACKAGE_ENTITY = "entity";

	private FancyMessageSender() {
		// Not called.
	}

	/**
	 * Sends a hoverable message to the player. Only supported in Minecraft 1.8+.
	 * 
	 * @param player Online player to send the message to.
	 * @param message The text to display in the chat.
	 * @param hover The text to display in the hover.
	 * @param color The color of the hover text.
	 * @throws Exception
	 */
	public static void sendHoverableMessage(Player player, String message, String hover, String color) throws Exception {
		sendChatPacket(player, constructHoverableMessageJson(message, hover, color), CHAT_MESSAGE_BYTE);
	}

	/**
	 * Sends a clickable and hoverable message to the player. Only supported in Minecraft 1.8+.
	 * 
	 * @param player Online player to send the message to.
	 * @param message The text to display in the chat.
	 * @param command The command that is entered when clicking on the message.
	 * @param hover The text to display in the hover.
	 * @param color The color of the hover text.
	 * @throws Exception
	 */
	public static void sendHoverableCommandMessage(Player player, String message, String command, String hover, String color)
			throws Exception {
		sendChatPacket(player, constructHoverableCommandMessageJson(message, command, hover, color), CHAT_MESSAGE_BYTE);
	}

	/**
	 * Sends an action bar chat message to the player. Only supported in Minecraft 1.8+.
	 * 
	 * @param player Online player to send the message to.
	 * @param message The text to display in the action bar.
	 * @throws Exception
	 */
	public static void sendActionBarMessage(Player player, String message) throws Exception {
		sendChatPacket(player, constructTextJson(message), ACTION_BAR_BYTE);
	}

	/**
	 * Sends a title and subtitle to the player. Only supported in Minecraft 1.8+.
	 * 
	 * @param player Online player to send the title and subtitle to.
	 * @param title The main text that will appear on the player's screen.
	 * @param subtitle The secondary text that will appear on the player's screen.
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static void sendTitle(Player player, String title, String subtitle) throws Exception {
		if (MINOR_VERSION_NUMBER >= 11) {
			player.sendTitle(title, subtitle, 10, 70, 20);
		} else if (MINOR_VERSION_NUMBER >= 9) {
			player.sendTitle(title, subtitle);
		} else {
			sendTitlePacket(player, constructTextJson(title), constructTextJson(subtitle));
		}
	}

	/**
	 * Sends a title and subtitle packet to the player.
	 * 
	 * @param player Online player to send the title and subtitle to.
	 * @param titleJson The title JSON format message to send to the player. See
	 *            http://minecraft.gamepedia.com/Commands#Raw_JSON_text for more information.
	 * @param subtitleJson The subtitle title JSON format message to send to the player. See
	 *            http://minecraft.gamepedia.com/Commands#Raw_JSON_text for more information.
	 * @throws ReflectiveOperationException
	 */
	private static void sendTitlePacket(Player player, String titleJson, String subtitleJson)
			throws ReflectiveOperationException {
		// Retrieve a CraftPlayer instance and its PlayerConnection instance.
		Object craftPlayer = PackageType.CRAFTBUKKIT.getClass(PACKAGE_ENTITY + "." + CLASS_CRAFT_PLAYER).cast(player);
		Object craftHandle = PackageType.CRAFTBUKKIT.getClass(PACKAGE_ENTITY + "." + CLASS_CRAFT_PLAYER)
				.getMethod(METHOD_GET_HANDLE).invoke(craftPlayer);
		Object playerConnection = PackageType.MINECRAFT_SERVER.getClass(CLASS_ENTITY_PLAYER)
				.getField(FIELD_PLAYER_CONNECTION).get(craftHandle);

		Object parsedMainMessage;
		Object parsedSubMessage;
		Class<?> titleClass;
		if (PackageType.getServerVersion().startsWith("1_8_R1")) {
			// Prior to version 1.8.3.
			parsedMainMessage = PackageType.MINECRAFT_SERVER.getClass(NESTED_CHAT_SERIALIZER)
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes('&', titleJson));
			parsedSubMessage = PackageType.MINECRAFT_SERVER.getClass(NESTED_CHAT_SERIALIZER)
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes('&', subtitleJson));
			titleClass = PackageType.MINECRAFT_SERVER.getClass(ENUM_TITLE_ACTION);
		} else {
			parsedMainMessage = PackageType.MINECRAFT_SERVER
					.getClass(CLASS_CHAT_BASE_COMPONENT + "$" + NESTED_CHAT_SERIALIZER).getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes('&', titleJson));
			parsedSubMessage = PackageType.MINECRAFT_SERVER
					.getClass(CLASS_CHAT_BASE_COMPONENT + "$" + NESTED_CHAT_SERIALIZER).getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes('&', subtitleJson));
			titleClass = PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_TITLE + "$" + ENUM_TITLE_ACTION);
		}

		// Retrieve parameters for titles and subtitles.
		Enum<?> mainTitleEnumValue = null;
		Enum<?> subTitleEnumValue = null;
		for (Object o : titleClass.getEnumConstants()) {
			Enum<?> e = (Enum<?>) o;
			if ("TITLE".equalsIgnoreCase(e.name())) {
				mainTitleEnumValue = e;
			} else if ("SUBTITLE".equalsIgnoreCase(e.name())) {
				subTitleEnumValue = e;
			}
		}

		Object packetPlayOutChatMainTitle = ReflectionUtils
				.getConstructor(PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_TITLE),
						PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_TITLE + "$" + ENUM_TITLE_ACTION),
						PackageType.MINECRAFT_SERVER.getClass(CLASS_CHAT_BASE_COMPONENT))
				.newInstance(mainTitleEnumValue, parsedMainMessage);

		// Send the message packet through the PlayerConnection (title).
		PackageType.MINECRAFT_SERVER.getClass(CLASS_PLAYER_CONNECTION)
				.getMethod(METHOD_SEND_PACKET, PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET))
				.invoke(playerConnection, packetPlayOutChatMainTitle);

		Object packetPlayOutChatSubTitle = ReflectionUtils
				.getConstructor(PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_TITLE),
						PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_TITLE + "$" + ENUM_TITLE_ACTION),
						PackageType.MINECRAFT_SERVER.getClass(CLASS_CHAT_BASE_COMPONENT))
				.newInstance(subTitleEnumValue, parsedSubMessage);

		// Send the message packet through the PlayerConnection (subtitle).
		PackageType.MINECRAFT_SERVER.getClass(CLASS_PLAYER_CONNECTION)
				.getMethod(METHOD_SEND_PACKET, PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET))
				.invoke(playerConnection, packetPlayOutChatSubTitle);
	}

	/**
	 * Sends a chat packet.
	 * 
	 * @param player Online player to send the title and subtitle to.
	 * @param json The message JSON format message to send to the player. See
	 *            http://minecraft.gamepedia.com/Commands#Raw_JSON_text for more information.
	 * @param type Either 1 (chat mesage) or 2 (action bar message).
	 * @throws ReflectiveOperationException
	 */
	private static void sendChatPacket(Player player, String json, byte type) throws ReflectiveOperationException {
		// Retrieve a CraftPlayer instance and its PlayerConnection instance.
		Object craftPlayer = PackageType.CRAFTBUKKIT.getClass(PACKAGE_ENTITY + "." + CLASS_CRAFT_PLAYER).cast(player);
		Object craftHandle = PackageType.CRAFTBUKKIT.getClass(PACKAGE_ENTITY + "." + CLASS_CRAFT_PLAYER)
				.getMethod(METHOD_GET_HANDLE).invoke(craftPlayer);
		Object playerConnection = PackageType.MINECRAFT_SERVER.getClass(CLASS_ENTITY_PLAYER)
				.getField(FIELD_PLAYER_CONNECTION).get(craftHandle);

		Object parsedMessage;
		if (PackageType.getServerVersion().startsWith("1_8_R1")) {
			// Prior to version 1.8.3.
			parsedMessage = PackageType.MINECRAFT_SERVER.getClass(NESTED_CHAT_SERIALIZER).getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes('&', json));
		} else {
			parsedMessage = Class
					.forName(PackageType.MINECRAFT_SERVER + "." + CLASS_CHAT_BASE_COMPONENT + "$" + NESTED_CHAT_SERIALIZER)
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes('&', json));
		}

		Object packetPlayOutChat;
		if (MINOR_VERSION_NUMBER < 12) {
			packetPlayOutChat = PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_CHAT)
					.getConstructor(PackageType.MINECRAFT_SERVER.getClass(CLASS_CHAT_BASE_COMPONENT), byte.class)
					.newInstance(parsedMessage, type);
		} else {
			// New method uses the ChatMessageType enum rather than a byte.
			Class<?> chatMessageTypeClass = PackageType.MINECRAFT_SERVER.getClass(ENUM_CHAT_MESSAGE_TYPE);
			Enum<?> chatType = null;
			for (Object chatMessageType : chatMessageTypeClass.getEnumConstants()) {
				Enum<?> e = (Enum<?>) chatMessageType;
				if ("SYSTEM".equalsIgnoreCase(e.name()) && type == CHAT_MESSAGE_BYTE
						|| "GAME_INFO".equalsIgnoreCase(e.name()) && type == ACTION_BAR_BYTE) {
					chatType = e;
					break;
				}
			}
			// Constructor signature Minecraft 1.7 - 1.15:
			// public PacketPlayOutChat(IChatBaseComponent ichatbasecomponent, ChatMessageType chatmessagetype)
			// Constructor signature Minecraft 1.16+:
			// public PacketPlayOutChat(IChatBaseComponent ichatbasecomponent, ChatMessageType chatmessagetype, UUID
			// uuid)
			if (MINOR_VERSION_NUMBER < 16) {
				packetPlayOutChat = PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_CHAT)
						.getConstructor(PackageType.MINECRAFT_SERVER.getClass(CLASS_CHAT_BASE_COMPONENT),
								chatMessageTypeClass)
						.newInstance(parsedMessage, chatType);
			} else {
				packetPlayOutChat = PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_CHAT)
						.getConstructor(PackageType.MINECRAFT_SERVER.getClass(CLASS_CHAT_BASE_COMPONENT),
								chatMessageTypeClass, UUID.class)
						.newInstance(parsedMessage, chatType, player.getUniqueId());
			}
		}

		// Send the message packet through the PlayerConnection.
		PackageType.MINECRAFT_SERVER.getClass(CLASS_PLAYER_CONNECTION)
				.getMethod(METHOD_SEND_PACKET, PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET))
				.invoke(playerConnection, packetPlayOutChat);
	}

	private static String constructTextJson(String text) {
		return "{\"text\":\"" + text.replace("\"", "\\\"") + "\"}";
	}

	private static String constructHoverableMessageJson(String message, String hover, String color) {
		return "{\"text\":\"" + message.replace("\"", "\\\"") + "\","
				+ "\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\""
				+ hover.replace("\"", "\\\"") + "\",\"color\":\"" + color + "\"}]}}";
	}

	private static String constructHoverableCommandMessageJson(String message, String command, String hover, String color) {
		return "{\"text\":\"" + message.replace("\"", "\\\"") + "\","
				+ "\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + command + "\"},"
				+ "\"hoverEvent\":{\"action\":\"show_text\",\"value\":[{\"text\":\""
				+ hover.replace("\"", "\\\"") + "\",\"color\":\"" + color + "\"}]}}";
	}
}
