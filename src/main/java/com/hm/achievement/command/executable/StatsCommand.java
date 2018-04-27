package com.hm.achievement.command.executable;

import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;

import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.ParticleEffect;

/**
 * Class in charge of handling the /aach stats command, which creates and displays a progress bar of the player's
 * achievements
 *
 * @author Pyves
 */
@Singleton
public class StatsCommand extends AbstractCommand {

	// Minecraft font, used to get size information in the progress bar.
	private static final MinecraftFont FONT = MinecraftFont.Font;

	private final Logger logger;
	private final int serverVersion;
	private final CacheManager cacheManager;
	private final Map<String, String> achievementsAndDisplayNames;

	private ChatColor configColor;
	private String configIcon;
	private boolean configAdditionalEffects;
	private boolean configSound;

	private String langNumberAchievements;

	@Inject
	public StatsCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, ReloadCommand reloadCommand,
			Logger logger, int serverVersion, CacheManager cacheManager, Map<String, String> achievementsAndDisplayNames) {
		super(mainConfig, langConfig, pluginHeader, reloadCommand, "stats");
		this.serverVersion = serverVersion;
		this.logger = logger;
		this.cacheManager = cacheManager;
		this.achievementsAndDisplayNames = achievementsAndDisplayNames;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		// Load configuration parameters.
		configColor = ChatColor.getByChar(mainConfig.getString("Color", "5").charAt(0));
		configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon", "\u2618"));
		configAdditionalEffects = mainConfig.getBoolean("AdditionalEffects", true);
		configSound = mainConfig.getBoolean("Sound", true);

		langNumberAchievements = pluginHeader + Lang.get(CmdLang.NUMBER_ACHIEVEMENTS, langConfig) + " " + configColor;
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		int playerAchievements = cacheManager.getPlayerTotalAchievements(player.getUniqueId());
		int totalAchievements = achievementsAndDisplayNames.size();

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
		player.sendMessage(pluginHeader + "[" + translateColorCodes(barDisplay.toString()) + ChatColor.GRAY + "]");

		// Player has received all achievement; play special effect and sound.
		if (playerAchievements >= totalAchievements) {
			if (configAdditionalEffects) {
				try {
					// Play special effect.
					ParticleEffect.SPELL_WITCH.display(0, 1, 0, 0.5f, 400, player.getLocation(), 1);
				} catch (Exception e) {
					logger.warning("Failed to display additional particle effects for stats command.");
				}
			}

			// Play special sound.
			if (configSound) {
				// If old version, retrieving sound by name as it no longer exists in newer versions.
				Sound sound = serverVersion < 9 ? Sound.valueOf("FIREWORK_BLAST") : Sound.ENTITY_FIREWORK_LARGE_BLAST;
				player.getWorld().playSound(player.getLocation(), sound, 1, 0.7f);
			}
		}
	}
}
