package com.hm.achievement;

import java.util.Random;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import com.google.common.base.Strings;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.PacketSender;
import com.hm.mcshared.particle.ParticleEffect;

/**
 * Class in charge of displaying of achievements (title, firework, chat messages).
 * 
 * @author Pyves
 */
public class AchievementDisplay {

	private final AdvancedAchievements plugin;
	private final String fireworkStyle;
	private final boolean fireworks;
	private final boolean chatNotify;
	private final boolean titleScreen;

	private static final Random RANDOM = new Random();

	public AchievementDisplay(AdvancedAchievements achievement) {

		this.plugin = achievement;
		// Load configuration parameters.
		CommentedYamlConfiguration config = plugin.getPluginConfig();
		fireworkStyle = config.getString("FireworkStyle", "BALL_LARGE");
		fireworks = config.getBoolean("Firework", true);
		chatNotify = config.getBoolean("ChatNotify", false);
		titleScreen = config.getBoolean("TitleScreen", true);
	}

	/**
	 * Displays chat messages, screen title and launches a firework when a player receives an achievement.
	 * 
	 * @param player
	 * @param configAchievement
	 */
	public void displayAchievement(Player player, String configAchievement) {

		CommentedYamlConfiguration config = plugin.getPluginConfig();

		String achievementName = config.getString(configAchievement + ".Name");
		String displayName = config.getString(configAchievement + ".DisplayName", "");
		String message = config.getString(configAchievement + ".Message", "");
		String nameToShowUser;

		if (Strings.isNullOrEmpty(displayName)) {
			// Use the achievement key name (this name is used in the achievements table in the database).
			nameToShowUser = ChatColor.translateAlternateColorCodes('&', achievementName);
			plugin.getLogger().info("Player " + player.getName() + " received the achievement: " + achievementName);
		} else {
			// Display name is defined; use it.
			nameToShowUser = ChatColor.translateAlternateColorCodes('&', displayName);
			plugin.getLogger().info("Player " + player.getName() + " received the achievement: " + achievementName
					+ " (" + displayName + ")");
		}

		String msg = ChatColor.translateAlternateColorCodes('&', message);

		player.sendMessage(
				plugin.getChatHeader() + plugin.getPluginLang().getString("achievement-new", "New Achievement:") + " "
						+ ChatColor.WHITE + nameToShowUser);

		// Notify other online players that the player has received an achievement.
		if (chatNotify) {
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (!p.getName().equals(player.getName())) {
					p.sendMessage(
							plugin.getChatHeader()
									+ plugin.getPluginLang()
											.getString("achievement-received", "PLAYER received the achievement:")
											.replace("PLAYER", player.getName())
									+ " " + ChatColor.WHITE + nameToShowUser);
				}
			}
		}
		player.sendMessage(plugin.getChatHeader() + ChatColor.WHITE + msg);

		if (fireworks) {
			displayFirework(player);
		}

		if (titleScreen) {
			displayTitle(player, nameToShowUser, msg);
		}
	}

	/**
	 * Displays title when receiving an achievement.
	 * 
	 * @param player
	 * @param nameToShowUser
	 * @param msg
	 */
	private void displayTitle(Player player, String nameToShowUser, String msg) {

		try {
			PacketSender.sendTitlePacket(player, "{\"text\":\"" + nameToShowUser + "\"}", "{\"text\":\"" + msg + "\"}");
		} catch (Exception e) {
			plugin.getLogger().log(Level.SEVERE,
					"Errors while trying to display achievement screen title. Is your server up-to-date? ", e);
		}
	}

	/**
	 * Launches firework when receiving an achievement.
	 * 
	 * @param player
	 */
	private void displayFirework(Player player) {

		Location location = player.getLocation();
		try {
			// Set firework to launch beneath user.
			location.setY(location.getY() - 1);

			Firework firework = player.getWorld().spawn(location, Firework.class);
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			Builder effectBuilder = FireworkEffect.builder().flicker(false).trail(false)
					.withColor(Color.WHITE.mixColors(Color.BLUE.mixColors(Color.NAVY))).withFade(Color.PURPLE);
			if ("RANDOM".equalsIgnoreCase(fireworkStyle)) {
				effectBuilder.with(getRandomFireworkType());
			} else {
				try {
					// Firework style must be one of the following: BALL_LARGE, BALL, BURST, CREEPER or STAR.
					effectBuilder.with(Type.valueOf(fireworkStyle.toUpperCase()));
				} catch (Exception e) {
					effectBuilder.with(Type.BALL_LARGE);
					plugin.getLogger().warning(
							"Error while loading FireworkStyle. Please check your config. Loading default style.");
				}
			}
			fireworkMeta.addEffects(effectBuilder.build());
			firework.setVelocity(player.getLocation().getDirection().multiply(0));
			firework.setFireworkMeta(fireworkMeta);
		} catch (Exception e) {
			// Particle effect workaround to handle bug in early Spigot 1.9 and 1.11 releases. We try to simulate a
			// firework.
			player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_LAUNCH, 1, 0.6f);
			ParticleEffect.FIREWORKS_SPARK.display(0, 3, 0, 0.1f, 500, location, 1);
			player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_BLAST, 1, 0.6f);
			player.getWorld().playSound(location, Sound.ENTITY_FIREWORK_TWINKLE, 1, 0.6f);
		}
	}

	/**
	 * Returns a random firework type.
	 * 
	 * @return
	 */
	private Type getRandomFireworkType() {

		Type[] fireworkTypes = Type.values();
		return fireworkTypes[RANDOM.nextInt(fireworkTypes.length)];
	}
}
