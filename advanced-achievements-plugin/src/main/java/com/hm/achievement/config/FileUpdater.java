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
	 * to 2.5.2 are not supported.
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

		// Added in version 3.0:
		updateSetting(config, "TablePrefix", "",
				"Prefix added to the tables in the database. If you switch from the default tables names (no prefix),",
				"the plugin will attempt an automatic renaming. Otherwise you have to rename your tables manually.",
				"Do a full server reload or restart to make this effective.");
		updateSetting(config, "BookChronologicalOrder", true,
				"Sort pages of the book in chronological order (false for reverse chronological order).");
		updateSetting(config, "DisableSilkTouchBreaks", false,
				"Do not take into accound items broken with Silk Touch for the Breaks achievements.");
		updateSetting(config, "ObfuscateProgressiveAchievements", false,
				"For categories with a series of related achievements where the only thing changing is the number of times",
				"the event has occurred, show achievements that have been obtained and show the next obtainable achievement,",
				"but obfuscate the additional achievements.",
				"in order of increasing difficulty. For example, under Places, stone, the first achievement could have a",
				"target of 100 stone,# the second 500 stone, and the third 1000 stone.  When ObfuscateProgressiveAchievements",
				"is true, initially only the 100 stone achievement will be readable in the GUI.  Once 100 stone have been placed,",
				"the 500 stone achievement will become legible.");

		// Added in version 3.0.2:
		updateSetting(config, "DisableSilkTouchOreBreaks", false,
				"Do not take into account ores broken with Silk Touch for the Breaks achievements.",
				"DisableSilkTouchBreaks takes precedence over this.");
		updateSetting(config, "LanguageFileName", "lang.yml",
				" Name of the language file you want to use in your AdvancedAchievements directory.");

		// Added in version 4.0:
		updateSetting(config, "EnrichedListProgressBars", true,
				"Display precise statistic information in the /aach list progress bars.");
		Map<String, Integer> cooldownCategories = new HashMap<>();
		cooldownCategories.put("LavaBuckets", 10);
		cooldownCategories.put("WaterBuckets", 10);
		cooldownCategories.put("Milk", 10);
		cooldownCategories.put("Beds", 30);
		cooldownCategories.put("Brewing", 5);
		cooldownCategories.put("MusicDiscs", 30);
		updateSetting(config, "StatisticCooldown", cooldownCategories,
				"Time in seconds between each statistic count. Only the listed categories are currently supported.");
		updateSetting(config, "CooldownActionBar", true,
				"Display action bar message when player does an action while in the cooldown period.");

		// Added in version 4.1:
		updateSetting(config, "NumberedItemsInList", true,
				"Annotate each achievement displayed in a /aach list category with a number. Due to a Minecraft limitation,",
				"if you have more than 64 achievements for a category, the counting will start back at 1 after number 64.");
		updateSetting(config, "DateLocale", "en",
				"Locale used to format dates in /aach book and /aach list. You must select an ISO 639 language code.",
				"The list of possible language codes can be found here at www.loc.gov/standards/iso639-2/php/code_list.php");
		updateSetting(config, "DateDisplayTime", false,
				"Display reception time of achievements in /aach book and /aach list in addition to the date. For achievements",
				"received in plugin versions prior to 3.0, the precise time information is not available and will be displayed as midnight.");

		// Added in version 4.2:
		updateSetting(config, "RestrictSpectator", true,
				"Stop all stats from increasing when player in spectator mode, including PlayedTime.",
				"Connection achievements will only be handled once a player switches to a non-spectator mode.",
				"No effect if using Minecraft 1.7.9 or 1.7.10.");

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

		// Added in version 5.5:
		updateSetting(config, "ListColorNotReceived", 8,
				"Color used for Goals and progress bars in /aach list when an achievement is not yet received.");

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
	 * Updates language file from older plugin versions by adding missing parameters. Upgrades from versions prior to
	 * 2.5.2 are not supported.
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
				MultipleAchievements.values() }).flatMap(Arrays::stream).forEach(language -> updateLang(lang, language));

		// Not found in Enums (Possibly unused)
		updateSetting(lang, "list-custom", "Custom Categories");

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

		if (serverVersion < 13) {
			updateSetting(gui, "Breeding.Metadata", 0);
			updateSetting(gui, "AchievementNotStarted.Metadata", 14);
			updateSetting(gui, "AchievementStarted.Metadata", 4);
			updateSetting(gui, "AchievementReceived.Metadata", 5);
			updateSetting(gui, "BackButton.Metadata", 0);
			updateSetting(gui, "PreviousButton.Metadata", 0);
			updateSetting(gui, "NextButton.Metadata", 0);
			updateSetting(gui, "Custom.Metadata", 0);
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
