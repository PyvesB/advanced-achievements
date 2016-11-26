package com.hm.achievement.particle;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.hm.achievement.particle.ReflectionUtils.PackageType;

/**
 * Class used to send packets to the player; can be titles, json chat messages or action bar messages.
 * 
 * @author Pyves
 *
 */
public final class PacketSender {

	public static final byte CHAT_MESSAGE_BYTE = 1;
	public static final byte ACTION_BAR_BYTE = 2;

	private static final String CLASS_CHAT_BASE_COMPONENT = "IChatBaseComponent";
	private static final String CLASS_CRAFT_PLAYER = "CraftPlayer";
	private static final String CLASS_ENTITY_PLAYER = "EntityPlayer";
	private static final String CLASS_PACKET = "Packet";
	private static final String CLASS_PACKET_PLAY_OUT_CHAT = "PacketPlayOutChat";
	private static final String CLASS_PACKET_PLAY_OUT_TITLE = "PacketPlayOutTitle";
	private static final String CLASS_PLAYER_CONNECTION = "PlayerConnection";
	private static final String ENUM_TITLE_ACTION = "EnumTitleAction";
	private static final String FIELD_PLAYER_CONNECTION = "playerConnection";
	private static final String METHOD_GET_HANDLE = "getHandle";
	private static final String METHOD_SEND_PACKET = "sendPacket";
	private static final String NESTED_CHAT_SERIALIZER = "ChatSerializer";
	private static final String PACKAGE_ENTITY = "entity";

	private PacketSender() {
		// Not called.
	}

	/**
	 * Sends the chat packet (hover and clickable messages, or action bar message).
	 * 
	 * @param player
	 * @param json
	 * @param type
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 */
	public static void sendChatPacket(Player player, String json, byte type)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException,
			InstantiationException, NoSuchFieldException {

		// Retrieve a CraftPlayer instance and its PlayerConnection instance.
		Object craftPlayer = PackageType.CRAFTBUKKIT.getClass(PACKAGE_ENTITY + "." + CLASS_CRAFT_PLAYER).cast(player);
		Object craftHandle = PackageType.CRAFTBUKKIT.getClass(PACKAGE_ENTITY + "." + CLASS_CRAFT_PLAYER)
				.getMethod(METHOD_GET_HANDLE).invoke(craftPlayer);
		Object playerConnection = PackageType.MINECRAFT_SERVER.getClass(CLASS_ENTITY_PLAYER)
				.getField(FIELD_PLAYER_CONNECTION).get(craftHandle);

		// Parse the json message.
		Object parsedMessage;
		try {
			// Since 1.8.3
			parsedMessage = Class
					.forName(PackageType.MINECRAFT_SERVER + "." + CLASS_CHAT_BASE_COMPONENT + "$"
							+ NESTED_CHAT_SERIALIZER)
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), json));
		} catch (ClassNotFoundException e) {
			// Older versions of the game.
			parsedMessage = PackageType.MINECRAFT_SERVER.getClass(NESTED_CHAT_SERIALIZER).getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), json));
		}

		Object packetPlayOutChat = PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_CHAT)
				.getConstructor(PackageType.MINECRAFT_SERVER.getClass(CLASS_CHAT_BASE_COMPONENT), byte.class)
				.newInstance(parsedMessage, type);

		// Send the message packet through the PlayerConnection.
		PackageType.MINECRAFT_SERVER.getClass(CLASS_PLAYER_CONNECTION)
				.getMethod(METHOD_SEND_PACKET, PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET))
				.invoke(playerConnection, packetPlayOutChat);
	}

	/**
	 * Sends the title packet (title and subtitle to appear on screen).
	 * 
	 * @param player
	 * @param mainJson
	 * @param subJson
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 */
	public static void sendTitlePacket(Player player, String mainJson, String subJson)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException,
			InstantiationException, NoSuchFieldException {

		// Retrieve a CraftPlayer instance and its PlayerConnection instance.
		Object craftPlayer = PackageType.CRAFTBUKKIT.getClass(PACKAGE_ENTITY + "." + CLASS_CRAFT_PLAYER).cast(player);
		Object craftHandle = PackageType.CRAFTBUKKIT.getClass(PACKAGE_ENTITY + "." + CLASS_CRAFT_PLAYER)
				.getMethod(METHOD_GET_HANDLE).invoke(craftPlayer);
		Object playerConnection = PackageType.MINECRAFT_SERVER.getClass(CLASS_ENTITY_PLAYER)
				.getField(FIELD_PLAYER_CONNECTION).get(craftHandle);

		// Parse the json message.
		Object parsedMainMessage;
		try {
			// Since 1.8.3
			parsedMainMessage = PackageType.MINECRAFT_SERVER
					.getClass(CLASS_CHAT_BASE_COMPONENT + "$" + NESTED_CHAT_SERIALIZER).getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), mainJson));
		} catch (ClassNotFoundException e) {
			// Older versions of the game.
			parsedMainMessage = PackageType.MINECRAFT_SERVER.getClass(NESTED_CHAT_SERIALIZER)
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), mainJson));
		}

		// Parse the json message.
		Object parsedSubMessage;
		try {
			// Since 1.8.3
			parsedSubMessage = PackageType.MINECRAFT_SERVER
					.getClass(CLASS_CHAT_BASE_COMPONENT + "$" + NESTED_CHAT_SERIALIZER).getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), subJson));
		} catch (ClassNotFoundException e) {
			parsedSubMessage = PackageType.MINECRAFT_SERVER.getClass(NESTED_CHAT_SERIALIZER)
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), subJson));
		}

		Class<?> titleClass;
		try {
			titleClass = PackageType.MINECRAFT_SERVER.getClass(CLASS_PACKET_PLAY_OUT_TITLE + "$" + ENUM_TITLE_ACTION);
		} catch (ClassNotFoundException e) {
			titleClass = PackageType.MINECRAFT_SERVER.getClass(ENUM_TITLE_ACTION);
		}
		// Retrieve parameters for titles and subtitles.
		Enum<?> mainTitleEnumValue = null;
		Enum<?> subTitleEnumValue = null;
		for (Object o : titleClass.getEnumConstants()) {
			Enum<?> e = (Enum<?>) o;
			if ("TITLE".equalsIgnoreCase(e.name()))
				mainTitleEnumValue = e;
			if ("SUBTITLE".equalsIgnoreCase(e.name()))
				subTitleEnumValue = e;
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
}
