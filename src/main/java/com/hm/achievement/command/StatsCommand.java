package com.hm.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.particle.ParticleEffect;
import com.hm.achievement.particle.ReflectionUtils.PackageType;

/**
 * Class in charge of handling the /aach stats command, which creates and displays a progress bar of the player's
 * achievements
 * 
 * @author Pyves
 */
public class StatsCommand {

	private AdvancedAchievements plugin;
	private int totalAchievements;
	private boolean additionalEffects;
	private boolean sound;
	private int version;

	public StatsCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;

		// Calculate the total number of achievements in the config file.
		for (String type : AdvancedAchievements.NORMAL_ACHIEVEMENTS) {
			if (plugin.getDisabledCategorySet().contains(type))
				continue; // ignore this type
			totalAchievements += plugin.getPluginConfig().getConfigurationSection(type).getKeys(false).size();
		}
		for (String type : AdvancedAchievements.MULTIPLE_ACHIEVEMENTS) {
			if (plugin.getDisabledCategorySet().contains(type))
				continue; // ignore this type
			for (String item : plugin.getPluginConfig().getConfigurationSection(type).getKeys(false))
				totalAchievements += plugin.getPluginConfig().getConfigurationSection(type + '.' + item).getKeys(false)
						.size();
		}
		// Load configuration parameters.
		additionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		sound = plugin.getPluginConfig().getBoolean("Sound", true);
		// Simple and fast check to compare versions. Might need to be updated in the future depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);
	}

	/**
	 * Get statistics of the player by displaying number of achievements received and total number of achievements.
	 * 
	 * @param player
	 */
	public void getStats(Player player) {

		// Retrieve total number of achievements received by the player.
		int achievements = plugin.getDb().getPlayerAchievementsAmount(player);

		// Display number of achievements received and total achievements.
		player.sendMessage(plugin.getChatHeader()
				+ plugin.getPluginLang().getString("number-achievements", "Achievements received:") + " "
				+ plugin.getColor() + achievements + ChatColor.GRAY + "/" + plugin.getColor() + totalAchievements);

		// Display progress bar.
		if (totalAchievements > 0) {
			// Size initialised to 150; might require more or slightly less depending on the filling of the progress
			// bar.
			StringBuilder barDisplay = new StringBuilder(150);
			for (int i = 1; i <= 146 - plugin.getIcon().length(); i++) {
				if (i < ((146 - plugin.getIcon().length()) * achievements) / totalAchievements)
					barDisplay.append(plugin.getColor()).append('|');
				else
					barDisplay.append("&8|");
			}
			player.sendMessage(plugin.getChatHeader() + "["
					+ ChatColor.translateAlternateColorCodes('&', barDisplay.toString()) + ChatColor.GRAY + "]");
		}

		// Player has received all achievement; play special effect and sound.
		if (achievements >= totalAchievements) {
			try {
				// Play special effect.
				if (additionalEffects)
					ParticleEffect.SPELL_WITCH.display(0, 1, 0, 0.5f, 400, player.getLocation(), 1);
			} catch (Exception e) {
				plugin.getLogger().severe("Error while displaying additional particle effects.");
			}

			// Play special sound.
			if (sound) {
				if (version < 9) {
					// Old enum for versions prior to Minecraft 1.9. Retrieving it by name as it does no longer exist in
					// newer versions.
					player.getWorld().playSound(player.getLocation(), Sound.valueOf("FIREWORK_BLAST"), 1, 0.6f);
				} else {
					// Play sound with enum for newer versions.
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LARGE_BLAST, 1, 0.9f);
				}
			}
		}
	}
}
