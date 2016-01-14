package com.hm.achievement;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import com.hm.achievement.language.Lang;
import com.hm.achievement.particle.PacketSender;

public class AchievementDisplay {

	private AdvancedAchievements plugin;
	private String fireworkStyle;
	private boolean firework;
	private boolean chatNotify;
	private boolean titleScreen;

	public AchievementDisplay(AdvancedAchievements achievement) {

		this.plugin = achievement;
		fireworkStyle = plugin.getConfig().getString("FireworkStyle", "BALL_LARGE");
		firework = plugin.getConfig().getBoolean("Firework", true);
		chatNotify = plugin.getConfig().getBoolean("ChatNotify", false);
		titleScreen = plugin.getConfig().getBoolean("TitleScreen", true);
	}

	/**
	 * When a player receives an achievement, display chat messages and set
	 * firework.
	 */
	public void displayAchievement(Player player, String configAchievement) {

		String name = ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString(configAchievement + ".Name"));
		String msg = ChatColor.translateAlternateColorCodes('&',
				plugin.getConfig().getString(configAchievement + ".Message"));

		plugin.getLogger().info("Player " + player.getName() + " received the achievement: " + name);

		player.sendMessage(plugin.getChatHeader() + Lang.ACHIEVEMENT_NEW + " " + ChatColor.WHITE + name);
		if (chatNotify) {
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (!p.getName().equals(player.getName())) {
					p.sendMessage(plugin.getChatHeader()
							+ Lang.ACHIEVEMENT_RECEIVED.toString().replace("PLAYER", player.getName()) + " "
							+ ChatColor.WHITE + name);

				}
			}
		}

		player.sendMessage(plugin.getChatHeader() + ChatColor.WHITE + msg);

		if (firework) {

			Location location = player.getLocation();
			location.setY(location.getY() - 1);

			Firework firework = player.getWorld().spawn(location, Firework.class);
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			FireworkEffect effect;
			try {
				effect = FireworkEffect.builder().flicker(false).trail(false)
						.withColor(Color.WHITE.mixColors(Color.BLUE.mixColors(Color.NAVY)))
						.with(Type.valueOf(fireworkStyle.toUpperCase())).withFade(Color.PURPLE).build();
			} catch (Exception ex) {
				effect = FireworkEffect.builder().flicker(false).trail(false)
						.withColor(Color.WHITE.mixColors(Color.BLUE.mixColors(Color.NAVY))).with(Type.BALL_LARGE)
						.withFade(Color.PURPLE).build();
				plugin.getLogger()
						.severe("Error while loading FireworkStyle. Please check your config. Loading default style.");
			}
			fireworkMeta.addEffects(effect);
			firework.setVelocity(player.getLocation().getDirection().multiply(0));
			firework.setFireworkMeta(fireworkMeta);

		}
		if (titleScreen) {
			try {
				PacketSender.sendTitlePacket(player, "{text:\"" + name + "\"}", "{text:\"" + msg + "\"}");
			} catch (Exception ex) {

				plugin.getLogger()
						.severe("Errors while trying to display achievement screen title. Is your server up-to-date?");
				ex.printStackTrace();
			}
		}
	}

}
