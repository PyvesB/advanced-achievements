package com.hm.achievement.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of updating the language and configuration files when a new version of the plugin is released.
 * 
 * @author Pyves
 */
public class FileUpdater {

	private AdvancedAchievements plugin;
	private boolean updatePerformed;

	public FileUpdater(AdvancedAchievements plugin) {
		this.plugin = plugin;
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
		updateSetting(config, "LanguageFileName", "lang.yml", "Name of the language file.");

		// Added in version 4.0:
		updateSetting(config, "EnrichedListProgressBars", true,
				"Display precise statistic information in the /aach list progress bars.");
		updateSetting(config, "StatisticCooldown", 10, "LavaBuckets, WaterBuckets, Beds, Brewing, MusicDiscs.");
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
				"Display time of reception of achievements in /aach book and /aach list in addition to the date. For achievements",
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
		updateSetting(config, "NotifyOtherPlayers", false,
				"Notify other connected players when an achievement is received.",
				"Default behaviour, a player can override what he sees by using /aach toggle.");
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
				"When a player receives an achievement, the Name, Message and rewards of the achievement are displayed in",
				"the chat. If HoverableReceiverChatText is true, a single hoverable text will be displayed to the receiver.",
				"Otherwise texts will be displayed one after the other.");
		
		// Added in version 5.4.1:
		updateSetting(config, "ListColorNotReceived", 8,
				"Color used for Goals and progress bars in /aach list when an achievement is not yet received.");

		if (updatePerformed) {
			// Changes in the configuration: save and do a fresh load.
			try {
				config.saveConfiguration();
				config.loadConfiguration();
			} catch (IOException | InvalidConfigurationException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while saving changes to the configuration file: ", e);
				plugin.setSuccessfulLoad(false);
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
			if (!lang.getKeys(false).contains(category.toLangName())) {
				lang.set(category.toLangName(), category.toLangDefault());
				updatePerformed = true;
			}
		}
		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (!lang.getKeys(false).contains(category.toLangName())) {
				lang.set(category.toLangName(), category.toLangDefault());
				updatePerformed = true;
			}
		}

		// Added in version 3.0:
		updateSetting(lang, "book-date", "Book created on DATE.");
		updateSetting(lang, "list-back-message", "&7Back");
		updateSetting(lang, "week-achievement", "Weekly achievement rankings:");
		updateSetting(lang, "month-achievement", "Monthly achievement rankings:");
		updateSetting(lang, "aach-command-week", "Display weekly rankings.");
		updateSetting(lang, "aach-command-month", "Display monthly rankings.");
		updateSetting(lang, "not-ranked", "You are currently not ranked for this period.");
		updateSetting(lang, "aach-command-book-hover",
				"RP items you can collect and exchange with others! Time-based listing.");
		updateSetting(lang, "aach-command-stats-hover", "Progress bar. Gotta catch 'em all!");
		updateSetting(lang, "aach-command-list-hover",
				"Fancy GUI to get an overview of all achievements and your progress!");
		updateSetting(lang, "aach-command-top-hover", "Who are the server's leaders and how do you compare to them?");
		updateSetting(lang, "aach-command-reload-hover",
				"Player must be online; only Commands achievements can be used.");
		updateSetting(lang, "aach-command-give-hover", "Reload most settings in config.yml and lang.yml files.");
		updateSetting(lang, "aach-command-info-hover", "Some extra info about the plugin and its awesome author!");
		updateSetting(lang, "aach-command-check-hover", "Don't forget to add the colors defined in the config file.");
		updateSetting(lang, "aach-command-delete-hover",
				"Player must be online; does not reset any associated statistics.");
		updateSetting(lang, "aach-command-week-hover", "Best achievement hunters since the start of the week!");
		updateSetting(lang, "aach-command-month-hover", "Best achievement hunters since the start of the month!");
		updateSetting(lang, "aach-tip", "&lHINT &8You can &7&n&ohover &8or &7&n&oclick &8on the commands!");

		// Added in version 4.0:
		updateSetting(lang, "list-achievements-in-category-singular", "AMOUNT achievement");
		updateSetting(lang, "list-achievements-in-category-plural", "AMOUNT achievements");
		updateSetting(lang, "server-restart-reload",
				"DisabledCategories list was modified. Server must be fully reloaded or restarted for your changes to take effect.");
		updateSetting(lang, "statistic-cooldown",
				"Achievements cooldown, wait TIME seconds before this action counts again.");
		updateSetting(lang, "version-command-petmaster", "Pet Master integration:");

		// Added in version 4.1:
		updateSetting(lang, "list-description", "Description:");
		updateSetting(lang, "list-goal", "Goal:");
		updateSetting(lang, "list-reception", "Reception date:");
		updateSetting(lang, "list-progress", "Progress:");
		updateSetting(lang, "book-not-received", "You have not yet received any achievements.");

		// Added in version 4.2:
		updateSetting(lang, "aach-command-toggle", "Toggle achievements of other players.");
		updateSetting(lang, "aach-command-toggle-hover", "Your choice is saved until next server restart!");
		updateSetting(lang, "toggle-displayed", "You will now be notified when other players get achievements.");
		updateSetting(lang, "toggle-hidden", "You will no longer be notified when other players get achievements.");

		// Added in version 5.0:
		updateSetting(lang, "aach-command-reset", "Reset statistic for category CAT.");
		updateSetting(lang, "aach-command-reset-hover",
				"Player must be online; for categories with subcategories, they are all reset!");
		updateSetting(lang, "reset-successful", " statistics were cleared for PLAYER.");
		updateSetting(lang, "category-does-not-exist", "The specified category does not exist.");
		updateSetting(lang, "version-command-btlp", "BungeeTabListPlus integration:");

		// Added in version 5.2:
		updateSetting(lang, "advancements-generated", "Advancements were successfully generated.");
		updateSetting(lang, "aach-command-generate", "Generate advancements.");
		updateSetting(lang, "aach-command-generate-hover", "Potentially slow command; use with care!");
		updateSetting(lang, "minecraft-not-supported",
				"Advancements not supported in your Minecraft version. Please update to 1.12+.");
		updateSetting(lang, "experience-reward-received", "You received: AMOUNT experience!");
		updateSetting(lang, "list-reward-experience", "receive AMOUNT experience");
		updateSetting(lang, "increase-max-health-reward-received", "Your max health has increased by AMOUNT!");
		updateSetting(lang, "list-reward-increase-max-health", "increase max health by AMOUNT");
		updateSetting(lang, "increase-max-oxygen-reward-received", "Your max oxygen has increased by AMOUNT!");
		updateSetting(lang, "list-reward-increase-max-oxygen", "increase max oxygen by AMOUNT");

		// Added in version 5.3:
		updateSetting(lang, "list-previous-message", "&7Previous");
		updateSetting(lang, "list-next-message", "&7Next");

		// Added in version 5.4:
		updateSetting(lang, "custom-command-reward", "You received your reward: MESSAGE");
		
		// Added in version 5.4.1:
		updateSetting(lang, "version-command-essentials", "Essentials integration:");
		updateSetting(lang, "version-command-placeholderapi", "PlaceholderAPI integration:");

		// Added in version 5.4.X:
		updateSetting(lang, "error-value", "The value VALUE must to be an integer!");
		updateSetting(lang, "achievement-increase", "Achievement ACH increase by AMOUNT for PLAYER!");
		updateSetting(lang, "achievement-unknown", "Achievement ACH is unknown!");
		updateSetting(lang, "aach-command-add", "Increase a statistic.");
		updateSetting(lang, "aach-command-add-hover", "Player must be online; mainly used for Customs achievements.");
		updateSetting(lang, "list-customs", "Customs Achievements");
		
		if (updatePerformed) {
			// Changes in the language file: save and do a fresh load.
			try {
				lang.saveConfiguration();
				lang.loadConfiguration();
			} catch (IOException | InvalidConfigurationException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while saving changes to the language file: ", e);
				plugin.setSuccessfulLoad(false);
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
		updateSetting(gui, "Breeding.Metadata", 0);

		// Added in version 5.3:
		updateSetting(gui, "AchievementNotStarted.Item", "stained_clay");
		updateSetting(gui, "AchievementNotStarted.Metadata", 14);
		updateSetting(gui, "AchievementStarted.Item", "stained_clay");
		updateSetting(gui, "AchievementStarted.Metadata", 4);
		updateSetting(gui, "AchievementReceived.Item", "stained_clay");
		updateSetting(gui, "AchievementReceived.Metadata", 5);
		updateSetting(gui, "BackButton.Item", "book");
		updateSetting(gui, "BackButton.Metadata", 0);
		updateSetting(gui, "PreviousButton.Item", "wood_button");
		updateSetting(gui, "PreviousButton.Metadata", 0);
		updateSetting(gui, "NextButton.Item", "stone_button");
		updateSetting(gui, "NextButton.Metadata", 0);
		
		// Added in version 5.4.X:
		updateSetting(gui, "Customs.Item", "feather");
		updateSetting(gui, "Customs.Metadata", 0);

		if (updatePerformed) {
			// Changes in the gui file: save and do a fresh load.
			try {
				gui.saveConfiguration();
				gui.loadConfiguration();
			} catch (IOException | InvalidConfigurationException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while saving changes to the gui file: ", e);
				plugin.setSuccessfulLoad(false);
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

	/**
	 * Adds a new category to the configuration file, and includes it in the DisabledCategories list.
	 * 
	 * @param config
	 * @param categoryName
	 * @param categoryComment
	 */
	private void addNewCategory(CommentedYamlConfiguration config, String categoryName, String categoryComment) {
		if (!config.getKeys(false).contains(categoryName)) {
			Map<Object, Object> emptyMap = new HashMap<>();
			config.set(categoryName, emptyMap, categoryComment);
			// As no achievements are set, we initially disable this new category.
			List<String> disabledCategories = config.getList("DisabledCategories");
			disabledCategories.add(categoryName);
			config.set("DisabledCategories", disabledCategories);
			updatePerformed = true;
		}
	}
}
