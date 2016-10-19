package com.hm.achievement.particle;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.hm.achievement.particle.ReflectionUtils.PackageType;

/**
 * Class used to send packets to the player; can be titles or json messages.
 * 
 * @author Pyves
 *
 */
public class PacketSender {

	/**
	 * Send the chat packet (hover and clickable messages).
	 * 
	 * @param player
	 * @param json
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws NoSuchFieldException
	 */
	public static void sendChatPacket(Player player, String json)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException,
			InstantiationException, NoSuchFieldException {

		// Retrieve a CraftPlayer instance and its PlayerConnection instance.
		Object craftPlayer = Class.forName(PackageType.CRAFTBUKKIT + ".entity.CraftPlayer").cast(player);
		Object craftHandle = Class.forName(PackageType.CRAFTBUKKIT + ".entity.CraftPlayer").getMethod("getHandle")
				.invoke(craftPlayer);
		Object playerConnection = Class.forName(PackageType.MINECRAFT_SERVER + ".EntityPlayer")
				.getField("playerConnection").get(craftHandle);

		// Parse the json message.
		Object parsedMessage;
		try {
			// Since 1.8.3
			parsedMessage = Class.forName(PackageType.MINECRAFT_SERVER + ".IChatBaseComponent$ChatSerializer")
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), json));
		} catch (ClassNotFoundException e) {
			// Older versions of the game.
			parsedMessage = Class.forName(PackageType.MINECRAFT_SERVER + ".ChatSerializer").getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), json));
		}
		Object packetPlayOutChat = Class.forName(PackageType.MINECRAFT_SERVER + ".PacketPlayOutChat")
				.getConstructor(Class.forName(PackageType.MINECRAFT_SERVER + ".IChatBaseComponent"))
				.newInstance(parsedMessage);

		// Send the message packet through the PlayerConnection.
		Class.forName(PackageType.MINECRAFT_SERVER + ".PlayerConnection")
				.getMethod("sendPacket", Class.forName(PackageType.MINECRAFT_SERVER + ".Packet"))
				.invoke(playerConnection, packetPlayOutChat);
	}

	/**
	 * Send the title packet (title and subtitle to appear on screen).
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

		// Retrieve a CraftPlayer instance and its PlayerConnection
		// instance.
		Object craftPlayer = Class.forName(PackageType.CRAFTBUKKIT + ".entity.CraftPlayer").cast(player);
		Object craftHandle = Class.forName(PackageType.CRAFTBUKKIT + ".entity.CraftPlayer").getMethod("getHandle")
				.invoke(craftPlayer);
		Object playerConnection = Class.forName(PackageType.MINECRAFT_SERVER + ".EntityPlayer")
				.getField("playerConnection").get(craftHandle);

		// Parse the json message.
		Object parsedMainMessage;
		try {
			// Since 1.8.3
			parsedMainMessage = Class.forName(PackageType.MINECRAFT_SERVER + ".IChatBaseComponent$ChatSerializer")
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), mainJson));
		} catch (ClassNotFoundException e) {
			// Older versions of the game.
			parsedMainMessage = Class.forName(PackageType.MINECRAFT_SERVER + ".ChatSerializer")
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), mainJson));
		}

		// Parse the json message.
		Object parsedSubMessage;
		try {
			// Since 1.8.3
			parsedSubMessage = Class.forName(PackageType.MINECRAFT_SERVER + ".IChatBaseComponent$ChatSerializer")
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), subJson));
		} catch (ClassNotFoundException e) {
			parsedSubMessage = Class.forName(PackageType.MINECRAFT_SERVER + ".ChatSerializer")
					.getMethod("a", String.class)
					.invoke(null, ChatColor.translateAlternateColorCodes("&".charAt(0), subJson));
		}

		Class<?> titleClass;
		try {
			titleClass = PackageType.MINECRAFT_SERVER.getClass("PacketPlayOutTitle$EnumTitleAction");
		} catch (ClassNotFoundException e) {
			titleClass = PackageType.MINECRAFT_SERVER.getClass("EnumTitleAction");
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
				.getConstructor(Class.forName(PackageType.MINECRAFT_SERVER + ".PacketPlayOutTitle"),
						Class.forName(PackageType.MINECRAFT_SERVER + ".PacketPlayOutTitle$EnumTitleAction"),
						Class.forName(PackageType.MINECRAFT_SERVER + ".IChatBaseComponent"))
				.newInstance(mainTitleEnumValue, parsedMainMessage);

		// Send the message packet through the PlayerConnection (title).
		Class.forName(PackageType.MINECRAFT_SERVER + ".PlayerConnection")
				.getMethod("sendPacket", Class.forName(PackageType.MINECRAFT_SERVER + ".Packet"))
				.invoke(playerConnection, packetPlayOutChatMainTitle);

		Object packetPlayOutChatSubTitle = ReflectionUtils
				.getConstructor(Class.forName(PackageType.MINECRAFT_SERVER + ".PacketPlayOutTitle"),
						Class.forName(PackageType.MINECRAFT_SERVER + ".PacketPlayOutTitle$EnumTitleAction"),
						Class.forName(PackageType.MINECRAFT_SERVER + ".IChatBaseComponent"))
				.newInstance(subTitleEnumValue, parsedSubMessage);

		// Send the message packet through the PlayerConnection (subtitle).
		Class.forName(PackageType.MINECRAFT_SERVER + ".PlayerConnection")
				.getMethod("sendPacket", Class.forName(PackageType.MINECRAFT_SERVER + ".Packet"))
				.invoke(playerConnection, packetPlayOutChatSubTitle);
	}
}
