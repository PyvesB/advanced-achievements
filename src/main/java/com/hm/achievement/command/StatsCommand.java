package com.hm.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.particle.ParticleEffect;

/**
 * Class in charge of handling the /aach stats command, which creates and displays a progress bar of the player's
 * achievements
 *
 * @author Pyves
 */
public class StatsCommand extends AbstractCommand {

	// Minecraft font, used to get size information in the progress bar.
	private static final MinecraftFont FONT = MinecraftFont.Font;

	private boolean configAdditionalEffects;
	private boolean configSound;
	private String langNumberAchievements;

	public StatsCommand(AdvancedAchievements plugin) {
		super(plugin);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		// Load configuration parameters.
		configAdditionalEffects = plugin.getPluginConfig().getBoolean("AdditionalEffects", true);
		configSound = plugin.getPluginConfig().getBoolean("Sound", true);

		langNumberAchievements = Lang.getWithChatHeader(CmdLang.NUMBER_ACHIEVEMENTS, plugin) + " " + configColor;
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		int playerAchievements = plugin.getCacheManager().getPlayerTotalAchievements(player.getUniqueId());
		int totalAchievements = plugin.getAchievementsAndDisplayNames().size();

		player.sendMessage(
				langNumberAchievements + String.format("%.1f", 100 * (double) playerAchievements / totalAchievements) + "%");

		String middleText = " " + playerAchievements + "/" + totalAchievements + " ";
		int verticalBarsToDisplay = 150 - configIcon.length() - FONT.getWidth(middleText);
		boolean hasDisplayedMiddleText = false;
		StringBuilder barDisplay = new StringBuilder();
		int i = 1;
		while (i < verticalBarsToDisplay) {
			if (!hasDisplayedMiddleText && i >= verticalBarsToDisplay / 2) {
				// Middle reached: append number of achievements information.
				barDisplay.append(ChatColor.GRAY).append(middleText);
				// Do not display middleText again.
				hasDisplayedMiddleText = true;
				// Iterate a number of times equal to the number of iterations so far to have the same number of
				// vertical bars left and right from the middle text.
				i = verticalBarsToDisplay - i;
			} else if (i < ((verticalBarsToDisplay - 1) * playerAchievements) / totalAchievements) {
				// Color: progress by user.
				barDisplay.append(configColor).append('|');
				i++;
			} else {
				// Grey: amount not yet reached by user.
				barDisplay.append("&8|");
				i++;
			}
		}
		// Display enriched progress bar.
		player.sendMessage(plugin.getChatHeader() + "["
				+ translateColorCodes(barDisplay.toString()) + ChatColor.GRAY + "]");

		// Player has received all achievement; play special effect and sound.
		if (playerAchievements >= totalAchievements) {
			if (configAdditionalEffects) {
				try {
					// Play special effect.
					ParticleEffect.SPELL_WITCH.display(0, 1, 0, 0.5f, 400, player.getLocation(), 1);
				} catch (Exception e) {
					plugin.getLogger().warning("Failed to display additional particle effects for stats command.");
				}
			}

			// Play special sound.
			if (configSound) {
				playFireworkSound(player);
			}
		}
	}
}
