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

public class AchievementDisplay {

	private AdvancedAchievements plugin;

	public AchievementDisplay(AdvancedAchievements achievement) {

		this.plugin = achievement;
	}

	/**
	 * When a player receives an achievement, display chat messages and set
	 * firework.
	 */
	public void displayAchievement(Player player, String configAchievement) {

		String name = plugin.getConfig().getString(configAchievement + ".Name");
		String msg = plugin.getConfig().getString(configAchievement + ".Message");

		name = ChatColor.translateAlternateColorCodes('&', name);
		msg = ChatColor.translateAlternateColorCodes('&', msg);

		player.sendMessage(plugin.getChatHeader() + Lang.ACHIVEMENT_NEW + " " + ChatColor.WHITE + name);
		if (plugin.isChatMessage()) {
			for (Player p : plugin.getServer().getOnlinePlayers()) {
				if (!p.getName().equals(player.getName())) {
					p.sendMessage(plugin.getChatHeader()
							+ Lang.ACHIEVEMENT_RECEIVED.toString().replace("PLAYER", player.getName()) + " "
							+ ChatColor.WHITE + name);

				}
			}
		}

		player.sendMessage(plugin.getChatHeader() + ChatColor.WHITE + msg);

		if (plugin.isFirework()) {

			Location location = player.getLocation();
			location.setY(location.getY() - 1);

			Firework firework = player.getWorld().spawn(location, Firework.class);
			FireworkMeta fireworkMeta = firework.getFireworkMeta();
			FireworkEffect effect;
			try {
				effect = FireworkEffect.builder().flicker(false).trail(false)
						.withColor(Color.WHITE.mixColors(Color.BLUE.mixColors(Color.NAVY)))
						.with(Type.valueOf(plugin.getFireworkStyle().toUpperCase())).withFade(Color.PURPLE).build();
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

		}
	}

}
