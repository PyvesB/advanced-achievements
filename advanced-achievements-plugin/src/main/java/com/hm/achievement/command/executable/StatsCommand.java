package com.hm.achievement.command.executable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;

import com.darkblade12.particleeffect.ParticleEffect;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.utils.SoundPlayer;

/**
 * Class in charge of handling the /aach stats command, which creates and displays a progress bar of the player's
 * achievements
 *
 * @author Pyves
 */
@Singleton
@CommandSpec(name = "stats", permission = "stats", minArgs = 1, maxArgs = 1)
public class StatsCommand extends AbstractCommand {

	// Minecraft font, used to get size information in the progress bar.
	private static final MinecraftFont FONT = MinecraftFont.Font;

	private final int serverVersion;
	private final CacheManager cacheManager;
	private final AchievementMap achievementMap;
	private final SoundPlayer soundPlayer;

	private ChatColor configColor;
	private String configIcon;
	private boolean configAdditionalEffects;
	private boolean configSound;
	private String configSoundStats;

	private String langNumberAchievements;

	@Inject
	public StatsCommand(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			StringBuilder pluginHeader, int serverVersion, CacheManager cacheManager, AchievementMap achievementMap,
			SoundPlayer soundPlayer) {
		super(mainConfig, langConfig, pluginHeader);
		this.serverVersion = serverVersion;
		this.cacheManager = cacheManager;
		this.achievementMap = achievementMap;
		this.soundPlayer = soundPlayer;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		// Load configuration parameters.
		configColor = ChatColor.getByChar(mainConfig.getString("Color"));
		configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon"));
		configAdditionalEffects = mainConfig.getBoolean("AdditionalEffects");
		configSound = mainConfig.getBoolean("Sound");
		configSoundStats = mainConfig.getString("SoundStats").toUpperCase();

		langNumberAchievements = pluginHeader + langConfig.getString("number-achievements") + " " + configColor;
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		int playerAchievements = cacheManager.getPlayerAchievements(player.getUniqueId()).size();
		int totalAchievements = achievementMap.getAll().size();

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
				if (serverVersion >= 9) {
					player.spawnParticle(Particle.SPELL_WITCH, player.getLocation(), 400, 0, 1, 0, 0.5f);
				} else {
					ParticleEffect.SPELL_WITCH.display(0, 1, 0, 0.5f, 400, player.getLocation(), 1);
				}
			}

			if (configSound) {
				soundPlayer.play(player, configSoundStats, "ENTITY_FIREWORK_ROCKET_BLAST", "ENTITY_FIREWORK_LARGE_BLAST",
						"FIREWORK_BLAST");
			}
		}
	}
}
