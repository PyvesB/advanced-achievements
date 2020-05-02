package com.hm.achievement.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.configuration.InvalidConfigurationException;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.lang.ListenerLang;
import com.hm.achievement.lang.RewardLang;
import com.hm.achievement.lang.command.CmdLang;
import com.hm.achievement.lang.command.HelpLang;
import com.hm.achievement.lang.command.InfoLang;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of updating the language and configuration files when a new version of the plugin is released.
 *
 * @author Pyves
 */
@Singleton
public class FileUpdater {

	private final Logger logger;
	private final int serverVersion;

	private boolean updatePerformed;

	@Inject
	public FileUpdater(Logger logger, int serverVersion) {
		this.logger = logger;
		this.serverVersion = serverVersion;
	}

	/**
	 * Updates configuration file from older plugin versions by adding missing parameters. Upgrades from versions prior
	 * to 5.0 are not supported.
	 *
	 * @param config
	 */
	public void updateOldConfiguration(CommentedYamlConfiguration config) {
		updatePerformed = false;

		// Make sure DisabledCategories exists so elements can then be added to it:
		if (!config.getKeys(false).contains("DisabledCategories")) {
			List<String> emptyList = new ArrayList<>();
			config.set("DisabledCategories", emptyList,
					"Don't show these categories in the achievement GUI or in the stats output (delete the [] before using).",
					"Also prevent obtaining achievements for these categories and prevent stats from increasing.",
					"If changed, do a full server reload, and not just /aach reload.");
			updatePerformed = true;
		}
		// Iterate through all categories to add missing ones.
		for (NormalAchievements category : NormalAchievements.values()) {
			addNewCategory(config, category.toString(), category.toConfigComment());
		}
		for (MultipleAchievements category : MultipleAchievements.values()) {
			addNewCategory(config, category.toString(), category.toConfigComment());
		}
		addNewCategory(config, CommandAchievements.COMMANDS.toString(), CommandAchievements.COMMANDS.toConfigComment());

		// Added in version 5.0:
		updateSetting(config, "SimplifiedReception", false,
				"Set to true to activate simpler effects and a calm sound when a player receives an achievement.",
				"Ignored if Firework parameter is set to true.");

		// Added in version 5.1:
		updateSetting(config, "NotifyOtherPlayers", false, "Notify other connected players when an achievement is received.",
				"This defines the default behaviour, a player can override what he sees by using /aach toggle.");
		updateSetting(config, "ActionBarNotify", true,
				"When NotifyOtherPlayers is enabled, notifications are done using action bars when ActionBarNotify is true.",
				"When ActionBarNotify is false, chat messages are used.");

		// Added in version 5.1.1:
		updateSetting(config, "RestrictAdventure", false,
				"Stop all stats from increasing when player in adventure mode, including PlayedTime.",
				"Connection achievements will only be handled once a player switches to a non-adventure mode.");

		// Added in version 5.2:
		updateSetting(config, "RegisterAdvancementDescriptions", true,
				"Register advancements with a description corresponding to the Goal parameter of each achievement.",
				"If changed, run /aach generate to regenerate advancements with the new parameter value taken into account.",
				"No effect if using Minecraft versions prior to 1.12.");
		updateSetting(config, "HideNoPermissionCategories", false,
				"Hide categories for which the player does not have the corresponding count permissions.");

		// Added in version 5.2.2:
		updateSetting(config, "HideAdvancements", false,
				"If true, hide advancements from the advancement GUI. Advancement notifications will still appear when receiving achievements.",
				"No effect if using Minecraft versions prior to 1.12.");
		updateSetting(config, "IgnoreAFKPlayedTime", false,
				"If true, PlayedTime will no longer increase when the player is AFK. Requires Essentials to work.");

		// Added in version 5.3:
		updateSetting(config, "ChatHeader", "&7[%ICON%&7]",
				"Set the format of the header used for most chat messages (default: \"&7[%ICON%&7]\").");

		// Added in version 5.3.3:
		updateSetting(config, "AdditionalConnectionOptions", "",
				"Specify additional options when opening a connection to a MySQL/PostgreSQL database. Start each option with &,",
				"for instance \"&useUnicode=yes&characterEncoding=UTF-8\".");
		updateSetting(config, "HoverableReceiverChatText", false,
				"When a player receives an achievement, the DisplayName, Message and rewards of the achievement are displayed in",
				"the chat. If HoverableReceiverChatText is true, a single hoverable text will be displayed to the receiver.",
				"Otherwise texts will be displayed one after the other.");

		// Added in version 5.4:
		Map<String, Integer> cooldownCategories = new HashMap<>();
		cooldownCategories.put("LavaBuckets", 10);
		cooldownCategories.put("WaterBuckets", 10);
		cooldownCategories.put("Milk", 10);
		cooldownCategories.put("Beds", 30);
		cooldownCategories.put("Brewing", 5);
		cooldownCategories.put("MusicDiscs", 30);
		updateSetting(config, "StatisticCooldown", cooldownCategories,
				"Time in seconds between each statistic count. Only the listed categories are currently supported.");

		// Added in version 5.5:
		updateSetting(config, "ListColorNotReceived", 8, "Color used for not yet received achievements in /aach list.");

		// Added in version 5.6:
		if (!config.isConfigurationSection("AllAchievementsReceivedRewards")) {
			updateSetting(config, "AllAchievementsReceivedRewards.Money", 30,
					"Awarded when a player has received all the achievements. Use the same reward pattern as with achievements.",
					"See https://github.com/PyvesB/AdvancedAchievements/wiki/Rewards");
		}

		// Added in 5.9.0:
		updateSetting(config, "RootAdvancementTitle", "Advanced Achievements", "Title shown on the root advancement.");
		updateSetting(config, "AdvancementsBackground", "minecraft:textures/item/book.png",
				"Background shown on the Advanced Achievements advancement tab.",
				"Must be a resource location to any image in a resource pack.");

		// Added in 5.10.0:
		String bookDefault = serverVersion < 9 ? "level_up" : "entity_player_levelup";
		updateSetting(config, "SoundBook", bookDefault,
				"For /aach book. Possible values: github.com/PyvesB/AdvancedAchievements/wiki/Sound-names");
		String statsRankingDefault = serverVersion < 9 ? "firework_blast"
				: serverVersion < 13 ? "entity_firework_large_blast" : "entity_firework_rocket_blast";
		updateSetting(config, "SoundStats", statsRankingDefault,
				"For /aach stats with all achievements. Possible values: github.com/PyvesB/AdvancedAchievements/wiki/Sound-names");
		updateSetting(config, "SoundRanking", statsRankingDefault,
				"For /aach top, week, month when ranked in the top list. Possible values: github.com/PyvesB/AdvancedAchievements/wiki/Sound-names");

		// Added in 5.14.0:
		updateSetting(config, "ReceiverChatMessages", true, "Display chat messages when a player receives an achievement.");

		// Added in 6.0.0:
		updateSetting(config, "ListItaliciseNotReceived", true,
				"Italicise not yet received achievements in /aach list. Obfuscated achievements are not affected.");

		// Added in 6.1.0:
		updateSetting(config, "HideProgressiveAchievements", false,
				"Similar to ObfuscateProgressiveAchievements, but displays not received achievements as locked in /aach list.");

		if (updatePerformed) {
			// Changes in the configuration: save and do a fresh load.
			try {
				config.saveConfiguration();
				config.loadConfiguration();
			} catch (IOException | InvalidConfigurationException e) {
				logger.log(Level.SEVERE, "Error while saving changes to the configuration file:", e);
			}
		}
	}

	/**
	 * Updates language file from older plugin versions by adding missing parameters.
	 *
	 * @param lang
	 */
	public void updateOldLanguage(CommentedYamlConfiguration lang) {
		updatePerformed = false;

		// Iterate through all categories to add missing ones.
		for (NormalAchievements category : NormalAchievements.values()) {
			if (!lang.getKeys(false).contains(category.toLangKey())) {
				lang.set(category.toLangKey(), category.toLangDefault());
				updatePerformed = true;
			}
		}
		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (!lang.getKeys(false).contains(category.toLangKey())) {
				lang.set(category.toLangKey(), category.toLangDefault());
				updatePerformed = true;
			}
		}

		// Iterate through all Lang implementation keys & default values
		Arrays.stream(new Lang[][] { CmdLang.values(), HelpLang.values(), HelpLang.Hover.values(), InfoLang.values(),
				GuiLang.values(), ListenerLang.values(), RewardLang.values(), NormalAchievements.values(),
				MultipleAchievements.values(), new Lang[] { CommandAchievements.COMMANDS } })
				.flatMap(Arrays::stream)
				.forEach(language -> updateLang(lang, language));

		if (updatePerformed) {
			// Changes in the language file: save and do a fresh load.
			try {
				lang.saveConfiguration();
				lang.loadConfiguration();
			} catch (IOException | InvalidConfigurationException e) {
				logger.log(Level.SEVERE, "Error while saving changes to the language file:", e);
			}
		}
	}

	/**
	 * Updates GUI file from older plugin versions by adding missing parameters. New configuration file introduced in
	 * version 5.0 of the plugin.
	 *
	 * @param gui
	 */
	public void updateOldGUI(CommentedYamlConfiguration gui) {
		updatePerformed = false;

		// Added in version 5.2.5:
		updateSetting(gui, "Breeding.Item", "wheat");

		// Added in version 5.3:
		updateSetting(gui, "AchievementNotStarted.Item", "stained_clay");
		updateSetting(gui, "AchievementStarted.Item", "stained_clay");
		updateSetting(gui, "AchievementReceived.Item", "stained_clay");
		updateSetting(gui, "BackButton.Item", "book");
		updateSetting(gui, "PreviousButton.Item", "wood_button");
		updateSetting(gui, "NextButton.Item", "stone_button");

		// Added in version 5.5:
		updateSetting(gui, "Custom.Item", "feather");

		// Added in version 5.12.0:
		updateSetting(gui, "TargetsShot.Item", serverVersion >= 13 ? "firework_star" : "firework_charge");

		// Added in version 5.13.0:
		updateSetting(gui, "RaidsWon.Item", "gray_banner");
		updateSetting(gui, "Riptides.Item", "trident");

		// Added in version 5.15.0:
		updateSetting(gui, "AdvancementsCompleted.Item", "gold_ingot");

		// Added in version 6.1.0:
		updateSetting(gui, "AchievementLock.Item", serverVersion >= 13 ? "black_terracotta" : "stained_clay");
		updateSetting(gui, "CategoryLock.Item", serverVersion >= 13 ? "barrier" : "bedrock");

		if (serverVersion < 13) {
			updateSetting(gui, "Breeding.Metadata", 0);
			updateSetting(gui, "AchievementNotStarted.Metadata", 14);
			updateSetting(gui, "AchievementStarted.Metadata", 4);
			updateSetting(gui, "AchievementReceived.Metadata", 5);
			updateSetting(gui, "BackButton.Metadata", 0);
			updateSetting(gui, "PreviousButton.Metadata", 0);
			updateSetting(gui, "NextButton.Metadata", 0);
			updateSetting(gui, "Custom.Metadata", 0);
			updateSetting(gui, "TargetsShot.Metadata", 0);
			updateSetting(gui, "AchievementLock.Metadata", 15);
			updateSetting(gui, "CategoryLock.Metadata", 0);
		}

		if (updatePerformed) {
			// Changes in the gui file: save and do a fresh load.
			try {
				gui.saveConfiguration();
				gui.loadConfiguration();
			} catch (IOException | InvalidConfigurationException e) {
				logger.log(Level.SEVERE, "Error while saving changes to the gui file:", e);
			}
		}
	}

	/**
	 * Updates the configuration file to include a new setting with its default value and its comments (each comment
	 * String corresponding to a separate line).
	 *
	 * @param file
	 * @param name
	 * @param value
	 * @param comments
	 */
	private void updateSetting(CommentedYamlConfiguration file, String name, Object value, String... comments) {
		if (!file.getKeys(true).contains(name)) {
			file.set(name, value, comments);
			updatePerformed = true;
		}
	}

	private void updateLang(CommentedYamlConfiguration file, Lang lang) {
		updateSetting(file, lang.toLangKey(), lang.toLangDefault());
	}

	/**
	 * Adds a new category to the configuration file, and includes it in the DisabledCategories list.
	 *
	 * @param config
	 * @param categoryName
	 * @param categoryComments
	 */
	private void addNewCategory(CommentedYamlConfiguration config, String categoryName, String... categoryComments) {
		if (!config.getKeys(false).contains(categoryName)) {
			Map<Object, Object> emptyMap = new HashMap<>();
			config.set(categoryName, emptyMap, categoryComments);
			// As no achievements are set, we initially disable this new category.
			List<String> disabledCategories = config.getList("DisabledCategories");
			if (!disabledCategories.contains(categoryName)) {
				disabledCategories.add(categoryName);
				config.set("DisabledCategories", disabledCategories);
			}
			updatePerformed = true;
		}
	}
}
