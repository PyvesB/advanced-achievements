package com.hm.achievement;

import com.google.common.base.Strings;
import com.hm.achievement.utils.YamlManager;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import com.hm.achievement.particle.PacketSender;
import com.hm.achievement.particle.ParticleEffect;

public class AchievementDisplay {

	private AdvancedAchievements plugin;
	private String fireworkStyle;
	private boolean firework;
	private boolean chatNotify;
	private boolean titleScreen;

	public AchievementDisplay(AdvancedAchievements achievement) {

		this.plugin = achievement;

		YamlManager config = plugin.getPluginConfig();
		fireworkStyle = config.getString("FireworkStyle", "BALL_LARGE");
		firework = config.getBoolean("Firework", true);
		chatNotify = config.getBoolean("ChatNotify", false);
		titleScreen = config.getBoolean("TitleScreen", true);
	}

	/**
	 * Display chat messages, screen title and set firework when a player
	 * receives an achievement,
	 */
	public void displayAchievement(Player player, String configAchievement) {
		YamlManager config = plugin.getPluginConfig();

		String achievementName = config.getString(configAchievement + ".Name");
		String displayName = config.getString(configAchievement + ".DisplayName", "");
		String message = config.getString(configAchievement + ".Message", "");
		String nameToShowUser;

		if (Strings.isNullOrEmpty(displayName)) {
			// Use the achievement key name (this name is used in the achievements table in the database)
			nameToShowUser = ChatColor.translateAlternateColorCodes('&', achievementName);
			plugin.getLogger().info("Player " + player.getName() +
					" received the achievement: " + achievementName);
		} else {
			// Display name is defined; use it
			nameToShowUser = ChatColor.translateAlternateColorCodes('&', displayName);
			plugin.getLogger().info("Player " + player.getName() +
					" received the achievement: " + achievementName + " (" + displayName + ")");
		}

		String msg = ChatColor.translateAlternateColorCodes('&', message);

		player.sendMessage(
				plugin.getChatHeader() + plugin.getPluginLang().getString("achievement-new", "New Achievement:") + " "
						+ ChatColor.WHITE + nameToShowUser);

		// Notify other online players that the player has received an achievement.
		if (chatNotify) {
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (!p.getName().equals(player.getName())) {
					p.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
							.getString("achievement-received", "PLAYER received the achievement:")
							.replace("PLAYER", player.getName()) + " " + ChatColor.WHITE + nameToShowUser);

				}
			}
		}

		player.sendMessage(plugin.getChatHeader() + ChatColor.WHITE + msg);

		if (firework) {

			Location location = player.getLocation();

			try {
				location.setY(location.getY() - 1);

				Firework firework = player.getWorld().spawn(location, Firework.class);
				FireworkMeta fireworkMeta = firework.getFireworkMeta();
				FireworkEffect effect;
				// Firework style must be one of the following: BALL_LARGE,
				// BALL,
				// BURST, CREEPER or STAR.
				try {
					effect = FireworkEffect.builder().flicker(false).trail(false)
							.withColor(Color.WHITE.mixColors(Color.BLUE.mixColors(Color.NAVY)))
							.with(Type.valueOf(fireworkStyle.toUpperCase())).withFade(Color.PURPLE).build();
				} catch (Exception ex) {
					effect = FireworkEffect.builder().flicker(false).trail(false)
							.withColor(Color.WHITE.mixColors(Color.BLUE.mixColors(Color.NAVY))).with(Type.BALL_LARGE)
							.withFade(Color.PURPLE).build();
					plugin.getLogger().severe(
							"Error while loading FireworkStyle. Please check your config. Loading default style.");
				}
				fireworkMeta.addEffects(effect);
				firework.setVelocity(player.getLocation().getDirection().multiply(0));
				firework.setFireworkMeta(fireworkMeta);

			} catch (IllegalArgumentException e) {

				player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_LAUNCH, 1, 0.6f);
				ParticleEffect.FIREWORKS_SPARK.display(0, 3, 0, 0.1f, 500, location, 1);
				player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_BLAST, 1, 0.6f);
				player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_TWINKLE, 1, 0.6f);
			}
		}
		if (titleScreen) {
			try {
				PacketSender.sendTitlePacket(player, "{\"text\":\"" + nameToShowUser + "\"}", "{\"text\":\"" + msg + "\"}");
			} catch (Exception ex) {

				plugin.getLogger()
						.severe("Errors while trying to display achievement screen title. Is your server up-to-date?");
				ex.printStackTrace();
			}
		}
	}

}
