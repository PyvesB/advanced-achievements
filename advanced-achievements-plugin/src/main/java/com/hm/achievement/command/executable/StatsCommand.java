package com.hm.achievement.command.executable;

import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.map.MinecraftFont;

import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.utils.SoundPlayer;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.ParticleEffect;

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

	private final Logger logger;
	private final int serverVersion;
	private final CacheManager cacheManager;
	private final Map<String, String> namesToDisplayNames;
	private final SoundPlayer soundPlayer;

	private ChatColor configColor;
	private String configIcon;
	private boolean configAdditionalEffects;
	private boolean configSound;
	private String configSoundStats;

	private String langNumberAchievements;

	@Inject
	public StatsCommand(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, StringBuilder pluginHeader, Logger logger,
			int serverVersion, CacheManager cacheManager, @Named("ntd") Map<String, String> namesToDisplayNames,
			SoundPlayer soundPlayer) {
		super(mainConfig, langConfig, pluginHeader);
		this.serverVersion = serverVersion;
		this.logger = logger;
		this.cacheManager = cacheManager;
		this.namesToDisplayNames = namesToDisplayNames;
		this.soundPlayer = soundPlayer;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		// Load configuration parameters.
		configColor = ChatColor.getByChar(mainConfig.getString("Color", "5"));
		configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon", "\u2618"));
		configAdditionalEffects = mainConfig.getBoolean("AdditionalEffects", true);
		configSound = mainConfig.getBoolean("Sound", true);
		configSoundStats = mainConfig.getString("SoundStats", "ENTITY_FIREWORK_ROCKET_BLAST").toUpperCase();

		langNumberAchievements = pluginHeader + LangHelper.get(CmdLang.NUMBER_ACHIEVEMENTS, langConfig) + " " + configColor;
	}

	@Override
	void onExecute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		int playerAchievements = cacheManager.getPlayerTotalAchievements(player.getUniqueId());
		int totalAchievements = namesToDisplayNames.size();

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
				if (serverVersion >= 13) {
					player.spawnParticle(Particle.SPELL_WITCH, player.getLocation(), 400, 0, 1, 0, 0.5f);
				} else {
					try {
						ParticleEffect.SPELL_WITCH.display(0, 1, 0, 0.5f, 400, player.getLocation(), 1);
					} catch (Exception e) {
						logger.warning("Failed to display additional particle effects for stats command.");
					}
				}
			}

			if (configSound) {
				soundPlayer.play(player, configSoundStats, "ENTITY_FIREWORK_ROCKET_BLAST", "ENTITY_FIREWORK_LARGE_BLAST",
						"FIREWORK_BLAST");
			}
		}
	}
}
