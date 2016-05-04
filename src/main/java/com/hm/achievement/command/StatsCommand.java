package com.hm.achievement.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.particle.ParticleEffect;

public class StatsCommand {

	private AdvancedAchievements plugin;
	private int totalAchievements;
	private boolean additionalEffects;
	private boolean sound;
	private Integer version;

	public StatsCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		// Ignore this category if it's in the disabled list
		List<String> disabled = plugin.getConfig().getStringList("DisabledCategories");
		// Calculate the total number of achievements in the config file.
		for (String type : AdvancedAchievements.NORMAL_ACHIEVEMENTS) {
			if ((disabled != null) && (disabled.contains(type)))
				continue; // ignore this type
			totalAchievements += plugin.getPluginConfig().getConfigurationSection(type).getKeys(false).size();
		}
		for (String type : AdvancedAchievements.MULTIPLE_ACHIEVEMENTS) {
			if ((disabled != null) && (disabled.contains(type)))
				continue; // ignore this type
			for (String item : plugin.getPluginConfig().getConfigurationSection(type).getKeys(false))
				totalAchievements += plugin.getPluginConfig().getConfigurationSection(type + '.' + item).getKeys(false)
						.size();
		}

		additionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		sound = plugin.getPluginConfig().getBoolean("Sound", true);
		// Simple and fast check to compare versions. Might need to
		// be updated in the future depending on how the Minecraft
		// versions change in the future.
		version = Integer.valueOf(Bukkit.getBukkitVersion().charAt(2) + "");
	}

	/**
	 * Get statistics of the player by displaying number of achievements
	 * received and total number of achievements.
	 */
	public void getStats(Player player) {

		int achievements = plugin.getDb().getPlayerAchievementsAmount(player);

		// Display number of achievements received and total achievements.
		player.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("number-achievements", "Achievements received:") + " "
				+ plugin.getColor() + achievements + ChatColor.GRAY + "/" + plugin.getColor() + totalAchievements);

		// Display progress bar.
		if (totalAchievements > 0) {
			String barDisplay = "";
			for (int i = 1; i <= 146 - plugin.getIcon().length(); i++) {
				if (i < ((146 - plugin.getIcon().length()) * achievements) / totalAchievements)
					barDisplay = barDisplay + plugin.getColor() + "|";
				else {
					barDisplay = barDisplay + "&8|";
				}
			}
			player.sendMessage(plugin.getChatHeader() + "[" + ChatColor.translateAlternateColorCodes('&', barDisplay)
					+ ChatColor.GRAY + "]");
		}

		if (achievements >= totalAchievements) {
			try {
				if (additionalEffects)
					// Play special effect when in top list.
					ParticleEffect.SPELL_WITCH.display(0, 1, 0, 0.5f, 400, player.getLocation(), 1);

			} catch (Exception ex) {
				plugin.getLogger().severe("Error while displaying additional particle effects.");
			}

			// Play special sound when in top list.
			if (sound) {
				if (version < 9) // Old enum for versions prior to Minecraft
									// 1.9.
					player.getWorld().playSound(player.getLocation(), Sound.valueOf("FIREWORK_BLAST"), 1, 0.6f);
				else
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LARGE_BLAST, 1, 0.9f);
			}
		}
	}
}
