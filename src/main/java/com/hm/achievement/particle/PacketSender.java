package com.hm.achievement.particle;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.hm.achievement.particle.ReflectionUtils.PackageType;

public class PacketSender {

	/**
	 * Send the packet.
	 */
	public static void sendPacket(Player player, String json) throws IllegalAccessException, InvocationTargetException,
			NoSuchMethodException, ClassNotFoundException, InstantiationException, NoSuchFieldException {

		// Retrieve a CraftPlayer instance and its PlayerConnection
		// instance.
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
			parsedMessage = Class.forName(PackageType.MINECRAFT_SERVER + ".ChatSerializer")
					.getMethod("a", String.class)
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

}
