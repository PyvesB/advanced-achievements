package com.hm.achievement;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class AchievementDisplay {

	private AdvancedAchievements plugin;

	public AchievementDisplay(AdvancedAchievements achievement) {
		this.plugin = achievement;
	}

	public void displayAchievement(Player player, String name, String msg) {

		name = ChatColor.translateAlternateColorCodes('&', name);
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		if (plugin.getLanguage().equals("fr")) {
			player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
					+ plugin.getIcon() + ChatColor.GRAY + "] "
					+ "Nouveau succès : " + ChatColor.WHITE + name);
			if (plugin.isChatMessage()) {
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (!p.getName().equals(player.getName())) {
						p.sendMessage(ChatColor.GRAY + "["
								+ ChatColor.DARK_PURPLE + plugin.getIcon()
								+ ChatColor.GRAY + "] " + player.getName()
								+ " a obtenu le succès : " + ChatColor.WHITE
								+ name);
					}
				}
			}
		}

		else {
			player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
					+ plugin.getIcon() + ChatColor.GRAY + "] "
					+ "Получено новое достижение: " + ChatColor.WHITE + name);
			if (plugin.isChatMessage()) {
				for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (!p.getName().equals(player.getName())) {
						p.sendMessage(ChatColor.GRAY + "["
								+ ChatColor.DARK_PURPLE + plugin.getIcon()
								+ ChatColor.GRAY + "] " + player.getName()
								+ " получил(а) достижение: "
								+ ChatColor.WHITE + name);
					}
				}
			}
		}

		player.sendMessage(ChatColor.GRAY + "[" + ChatColor.DARK_PURPLE
				+ plugin.getIcon() + ChatColor.GRAY + "] " + ChatColor.WHITE
				+ msg);

		if (plugin.isFirework()) {
			Location location = player.getLocation();
			location.setY(location.getY() - 1);

			Firework fw = player.getWorld().spawn(location, Firework.class);
			FireworkMeta fwm = fw.getFireworkMeta();
			FireworkEffect effect = FireworkEffect
					.builder()
					.flicker(false)
					.trail(false)
					.withColor(
							Color.WHITE.mixColors(Color.BLUE
									.mixColors(Color.NAVY)))
					.with(Type.BALL_LARGE).withFade(Color.PURPLE).build();
			fwm.addEffects(effect);
			fw.setVelocity(player.getLocation().getDirection().multiply(0));
			fw.setFireworkMeta(fwm);
		}
	}

}
