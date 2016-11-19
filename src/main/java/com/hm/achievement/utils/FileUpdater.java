package com.hm.achievement.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;

/**
 * Class in charge of updating the language and configuration files when a new version of the plugin is released.
 * 
 * @author Pyves
 */
public class FileUpdater {

	private AdvancedAchievements plugin;

	public FileUpdater(AdvancedAchievements plugin) {

		this.plugin = plugin;
	}

	/**
	 * Updates configuration file from older plugin versions by adding missing parameters. Upgrades from versions prior
	 * to 2.0 are not supported.
	 */
	public void updateOldConfiguration() {

		boolean updateDone = false;

		// Added in version 2.5.2 (put first to enable adding elements to it):
		if (!plugin.getPluginConfig().getKeys(false).contains("DisabledCategories")) {
			plugin.getPluginConfig().set("DisabledCategories", Collections.emptyList(),
					new String[] {
							"Don't show these categories in the achievement GUI or in the stats output (delete the [] before using).",
							"Also prevent obtaining achievements for these categories and prevent stats from increasing.",
							"If changed, do a full server reload, and not just /aach reload." });
			updateDone = true;
		}
		// Iterate through all categories to add missing ones.
		for (NormalAchievements category : NormalAchievements.values()) {
			if (!plugin.getPluginConfig().getKeys(false).contains(category.toString())) {
				plugin.getPluginConfig().set(category.toString(), Collections.emptyMap(), category.toConfigComment());
				// As no achievements are set, we initially disable this new category.
				List<String> disabledCategories = plugin.getPluginConfig().getList("DisabledCategories");
				disabledCategories.add(category.toString());
				plugin.getPluginConfig().set("DisabledCategories", disabledCategories);
				updateDone = true;
			}
		}

		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (!plugin.getPluginConfig().getKeys(false).contains(category.toString())) {
				plugin.getPluginConfig().set(category.toString(), Collections.emptyMap(), category.toConfigComment());
				// As no achievements are set, we initially disable this new category.
				List<String> disabledCategories = plugin.getPluginConfig().getList("DisabledCategories");
				disabledCategories.add(category.toString());
				plugin.getPluginConfig().set("DisabledCategories", disabledCategories);
				updateDone = true;
			}
		}

		// Added in version 2.1:
		if (!plugin.getPluginConfig().getKeys(false).contains("AdditionalEffects")) {
			plugin.getPluginConfig().set("AdditionalEffects", true,
					"Set to true to activate particle effects when receiving book and for players in top list.");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("FireworkStyle")) {
			plugin.getPluginConfig().set("FireworkStyle", "BALL_LARGE",
					"Choose BALL_LARGE, BALL, BURST, CREEPER or STAR.");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("ObfuscateNotReceived")) {
			plugin.getPluginConfig().set("ObfuscateNotReceived", true,
					"Obfuscate achievements that have not yet been received in /aach list.");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("HideNotReceivedCategories")) {
			plugin.getPluginConfig().set("HideNotReceivedCategories", false,
					"Hide categories with no achievements yet received in /aach list.");
			updateDone = true;
		}

		// Added in version 2.2:
		if (!plugin.getPluginConfig().getKeys(false).contains("TitleScreen")) {
			plugin.getPluginConfig().set("TitleScreen", true,
					"Display achievement name and description as screen titles.");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("Color")) {
			plugin.getPluginConfig().set("Color", "5", "Set the color of the plugin (default: 5, dark purple).");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("TimeBook")) {
			plugin.getPluginConfig().set("TimeBook", 900, "Time in seconds between each /aach book.");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("TimeList")) {
			plugin.getPluginConfig().set("TimeList", 0, "Time in seconds between each /aach list.");
			updateDone = true;
		}

		// Added in version 2.3.2:
		if (!plugin.getPluginConfig().getKeys(false).contains("AsyncPooledRequestsSender")) {
			plugin.getPluginConfig().set("AsyncPooledRequestsSender", true,
					"Enable multithreading for database write operations.");
			updateDone = true;
		}

		// Added in version 2.5.2:
		if (!plugin.getPluginConfig().getKeys(false).contains("ListAchievementFormat")) {
			plugin.getPluginConfig().set("ListAchievementFormat", "%ICON% %NAME% %ICON%",
					"Set the format of the achievement name in /aach list.");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("HideRewardDisplayInList")) {
			plugin.getPluginConfig().set("HideRewardDisplayInList", false, "Hide the reward display in /aach list.");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("IgnoreVerticalDistance")) {
			plugin.getPluginConfig().set("IgnoreVerticalDistance", false,
					"Ignore vertical dimension (Y axis) when calculating distance statistics.");
			updateDone = true;
		}

		// Added in version 3.0:
		if (!plugin.getPluginConfig().getKeys(false).contains("TablePrefix")) {
			plugin.getPluginConfig().set("TablePrefix", "",
					new String[] {
							"Prefix added to the tables in the database. If you switch from the default tables names (no prefix),",
							"the plugin will attempt an automatic renaming. Otherwise you have to rename your tables manually.",
							"Do a full server reload or restart to make this effective." });
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("BookChronologicalOrder")) {
			plugin.getPluginConfig().set("BookChronologicalOrder", true,
					"Sort pages of the book in chronological order (false for reverse chronological order).");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("DisableSilkTouchBreaks")) {
			plugin.getPluginConfig().set("DisableSilkTouchBreaks", false,
					"Do not take into accound items broken with Silk Touch for the Breaks achievements.");
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("ObfuscateProgressiveAchievements")) {
			plugin.getPluginConfig().set("ObfuscateProgressiveAchievements", false,
					new String[] { "Obfuscate progressive achievements:",
							"For categories with a series of related achievements where the only thing changing is the number of times",
							"the event has occurred, show achievements that have been obtained and show the next obtainable achievement,",
							"but obfuscate the additional achievements. In order for this to work properly, achievements must be sorted",
							"in order of increasing difficulty. For example, under Places, stone, the first achievement could have a",
							"target of 100 stone,# the second 500 stone, and the third 1000 stone.  When ObfuscateProgressiveAchievements",
							"is true, initially only the 100 stone achievement will be readable in the GUI.  Once 100 stone have been placed,",
							"the 500 stone achievement will become legible." });
			updateDone = true;
		}

		// Added in version 3.0.2:
		if (!plugin.getPluginConfig().getKeys(false).contains("DisableSilkTouchOreBreaks")) {
			plugin.getPluginConfig().set("DisableSilkTouchOreBreaks", false,
					new String[] { "Do not take into account ores broken with Silk Touch for the Breaks achievements.",
							"DisableSilkTouchBreaks takes precedence over this." });
			updateDone = true;
		}

		if (!plugin.getPluginConfig().getKeys(false).contains("LanguageFileName")) {
			plugin.getPluginConfig().set("LanguageFileName", "lang.yml", "Name of the language file.");
			updateDone = true;
		}

		if (updateDone) {
			// Changes in the configuration: save and do a fresh load.
			try {
				plugin.getPluginConfig().saveConfig();
				plugin.getPluginConfig().reloadConfig();
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while saving changes to the configuration file: ", e);
				plugin.setSuccessfulLoad(false);
			}
		}
	}

	/**
	 * Updates language file from older plugin versions by adding missing parameters. Upgrades from versions prior to
	 * 2.3 are not supported.
	 */
	public void updateOldLanguage() {

		boolean updateDone = false;

		// Iterate through all categories to add missing ones.
		for (NormalAchievements category : NormalAchievements.values()) {
			if (!plugin.getPluginLang().getKeys(false).contains(category.toLangName())) {
				plugin.getPluginLang().set(category.toLangName(), category.toLangDefault());
				updateDone = true;
			}
		}
		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (!plugin.getPluginLang().getKeys(false).contains(category.toLangName())) {
				plugin.getPluginLang().set(category.toLangName(), category.toLangDefault());
				updateDone = true;
			}
		}

		// Added in version 2.5.2:
		if (!plugin.getPluginLang().getKeys(false).contains("list-achievement-received")) {
			plugin.getPluginLang().set("list-achievement-received", "&a\u2713&f ");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("list-achievement-not-received")) {
			plugin.getPluginLang().set("list-achievement-not-received", "&4\u2717&8 ");
			updateDone = true;
		}

		// Added in version 3.0:
		if (!plugin.getPluginLang().getKeys(false).contains("book-date")) {
			plugin.getPluginLang().set("book-date", "Book created on DATE.");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("list-back-message")) {
			plugin.getPluginLang().set("list-back-message", "&7Back");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("week-achievement")) {
			plugin.getPluginLang().set("week-achievement", "Weekly achievement rankings:");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("month-achievement")) {
			plugin.getPluginLang().set("month-achievement", "Monthly achievement rankings:");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-week")) {
			plugin.getPluginLang().set("aach-command-week", "Display weekly rankings.");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-month")) {
			plugin.getPluginLang().set("aach-command-month", "Display monthly rankings.");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("not-ranked")) {
			plugin.getPluginLang().set("not-ranked", "You are currently not ranked for this period.");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-book-hover")) {
			plugin.getPluginLang().set("aach-command-book-hover",
					"RP items you can collect and exchange with others! Time-based listing.");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-stats-hover")) {
			plugin.getPluginLang().set("aach-command-stats-hover", "Progress bar. Gotta catch 'em all!");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-list-hover")) {
			plugin.getPluginLang().set("aach-command-list-hover",
					"Fancy GUI to get an overview of all achievements and your progress!");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-top-hover")) {
			plugin.getPluginLang().set("aach-command-top-hover",
					"Who are the server's leaders and how do you compare to them?");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-give-hover")) {
			plugin.getPluginLang().set("aach-command-give-hover",
					"Player must be online; only Commands achievements can be used.");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-reload-hover")) {
			plugin.getPluginLang().set("aach-command-reload-hover",
					"Reload most settings in config.yml and lang.yml files.");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-info-hover")) {
			plugin.getPluginLang().set("aach-command-info-hover",
					"Some extra info about the plugin and its awesome author!");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-check-hover")) {
			plugin.getPluginLang().set("aach-command-check-hover",
					"Don't forget to add the colors defined in the config file.");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-delete-hover")) {
			plugin.getPluginLang().set("aach-command-delete-hover",
					"Player must be online; does not reset any associated statistics.");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-week-hover")) {
			plugin.getPluginLang().set("aach-command-week-hover",
					"Best achievement hunters since the start of the week!");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-command-month-hover")) {
			plugin.getPluginLang().set("aach-command-month-hover",
					"Best achievement hunters since the start of the month!");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("aach-tip")) {
			plugin.getPluginLang().set("aach-tip",
					"&lHINT&r &8You can &7&n&ohover&r &8or &7&n&oclick&r &8on the commands!");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("list-achievements-in-category-singular")) {
			plugin.getPluginLang().set("list-achievements-in-category", "AMOUNT achievement");
			updateDone = true;
		}

		if (!plugin.getPluginLang().getKeys(false).contains("list-achievements-in-category-plural")) {
			plugin.getPluginLang().set("list-achievements-in-category-plural", "AMOUNT achievements");
			updateDone = true;
		}

		if (updateDone) {
			// Changes in the language file: save and do a fresh load.
			try {
				plugin.getPluginLang().saveConfig();
				plugin.getPluginLang().reloadConfig();
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while saving changes to the language file: ", e);
				plugin.setSuccessfulLoad(false);
			}
		}
	}

}
