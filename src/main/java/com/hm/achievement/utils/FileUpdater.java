package com.hm.achievement.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	 * to 2.5.2 are not supported.
	 * 
	 * @param configFile
	 */
	public void updateOldConfiguration(YamlManager configFile) {

		boolean updateDone = false;

		// Make sure DisabledCategories exists so elements can then be added to it:
		if (!configFile.getKeys(false).contains("DisabledCategories")) {
			List<String> emptyList = new ArrayList<>();
			configFile.set("DisabledCategories", emptyList,
					new String[] {
							"Don't show these categories in the achievement GUI or in the stats output (delete the [] before using).",
							"Also prevent obtaining achievements for these categories and prevent stats from increasing.",
							"If changed, do a full server reload, and not just /aach reload." });
			updateDone = true;
		}

		// Iterate through all categories to add missing ones.
		for (NormalAchievements category : NormalAchievements.values()) {
			if (!configFile.getKeys(false).contains(category.toString())) {
				Map<Object, Object> emptyMap = new HashMap<>();
				configFile.set(category.toString(), emptyMap, category.toConfigComment());
				// As no achievements are set, we initially disable this new category.
				List<String> disabledCategories = configFile.getList("DisabledCategories");
				disabledCategories.add(category.toString());
				configFile.set("DisabledCategories", disabledCategories);
				updateDone = true;
			}
		}
		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (!configFile.getKeys(false).contains(category.toString())) {
				Map<Object, Object> emptyMap = new HashMap<>();
				configFile.set(category.toString(), emptyMap, category.toConfigComment());
				// As no achievements are set, we initially disable this new category.
				List<String> disabledCategories = configFile.getList("DisabledCategories");
				disabledCategories.add(category.toString());
				configFile.set("DisabledCategories", disabledCategories);
				updateDone = true;
			}
		}

		// Added in version 3.0:
		if (!configFile.getKeys(false).contains("TablePrefix")) {
			configFile.set("TablePrefix", "",
					new String[] {
							"Prefix added to the tables in the database. If you switch from the default tables names (no prefix),",
							"the plugin will attempt an automatic renaming. Otherwise you have to rename your tables manually.",
							"Do a full server reload or restart to make this effective." });
			updateDone = true;
		}

		if (!configFile.getKeys(false).contains("BookChronologicalOrder")) {
			configFile.set("BookChronologicalOrder", true,
					"Sort pages of the book in chronological order (false for reverse chronological order).");
			updateDone = true;
		}

		if (!configFile.getKeys(false).contains("DisableSilkTouchBreaks")) {
			configFile.set("DisableSilkTouchBreaks", false,
					"Do not take into accound items broken with Silk Touch for the Breaks achievements.");
			updateDone = true;
		}

		if (!configFile.getKeys(false).contains("ObfuscateProgressiveAchievements")) {
			configFile.set("ObfuscateProgressiveAchievements", false,
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
		if (!configFile.getKeys(false).contains("DisableSilkTouchOreBreaks")) {
			configFile.set("DisableSilkTouchOreBreaks", false,
					new String[] { "Do not take into account ores broken with Silk Touch for the Breaks achievements.",
							"DisableSilkTouchBreaks takes precedence over this." });
			updateDone = true;
		}

		if (!configFile.getKeys(false).contains("LanguageFileName")) {
			configFile.set("LanguageFileName", "lang.yml", "Name of the language file.");
			updateDone = true;
		}

		// Added in version 4.0:
		if (!configFile.getKeys(false).contains("EnrichedListProgressBars")) {
			configFile.set("EnrichedListProgressBars", true,
					"Display precise statistic information in the /aach list progress bars.");
			updateDone = true;
		}

		if (!configFile.getKeys(false).contains("StatisticCooldown")) {
			configFile.set("StatisticCooldown", 10,
					new String[] { "Time in seconds between each statistic count for the following categories.",
							"LavaBuckets, WaterBuckets, Beds, Brewing, MusicDiscs." });
			updateDone = true;
		}

		if (!configFile.getKeys(false).contains("CooldownActionBar")) {
			configFile.set("CooldownActionBar", true,
					"Display action bar message when player does an action while in the cooldown period.");
			updateDone = true;
		}

		// Added in version 4.1:
		if (!configFile.getKeys(false).contains("NumberedItemsInList")) {
			configFile.set("NumberedItemsInList", true, new String[] {
					"Annotate each achievement displayed in a /aach list category with a number. Due to a Minecraft limitation,",
					"if you have more than 64 achievements for a category, the counting will start back at 1 after number 64." });
			updateDone = true;
		}

		if (!configFile.getKeys(false).contains("DateLocale")) {
			configFile.set("DateLocale", "en", new String[] {
					"Locale used to format dates in /aach book and /aach list. You must select an ISO 639 language code.",
					"The list of possible language codes can be found here at www.loc.gov/standards/iso639-2/php/code_list.php" });
			updateDone = true;
		}

		if (!configFile.getKeys(false).contains("DateDisplayTime")) {
			configFile.set("DateDisplayTime", false, new String[] {
					"Display time of reception of achievements in /aach book and /aach list in addition to the date. For achievements",
					"received in plugin versions prior to 3.0, the precise time information is not available and will be displayed as midnight." });
			updateDone = true;
		}

		if (updateDone) {
			// Changes in the configuration: save and do a fresh load.
			try {
				configFile.saveConfig();
				configFile.reloadConfig();
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while saving changes to the configuration file: ", e);
				plugin.setSuccessfulLoad(false);
			}
		}
	}

	/**
	 * Updates language file from older plugin versions by adding missing parameters. Upgrades from versions prior to
	 * 2.5.2 are not supported.
	 * 
	 * @param langFile
	 */
	public void updateOldLanguage(YamlManager langFile) {

		boolean updateDone = false;

		// Iterate through all categories to add missing ones.
		for (NormalAchievements category : NormalAchievements.values()) {
			if (!langFile.getKeys(false).contains(category.toLangName())) {
				langFile.set(category.toLangName(), category.toLangDefault());
				updateDone = true;
			}
		}
		for (MultipleAchievements category : MultipleAchievements.values()) {
			if (!langFile.getKeys(false).contains(category.toLangName())) {
				langFile.set(category.toLangName(), category.toLangDefault());
				updateDone = true;
			}
		}

		// Added in version 3.0:
		if (!langFile.getKeys(false).contains("book-date")) {
			langFile.set("book-date", "Book created on DATE.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("list-back-message")) {
			langFile.set("list-back-message", "&7Back");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("week-achievement")) {
			langFile.set("week-achievement", "Weekly achievement rankings:");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("month-achievement")) {
			langFile.set("month-achievement", "Monthly achievement rankings:");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-week")) {
			langFile.set("aach-command-week", "Display weekly rankings.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-month")) {
			langFile.set("aach-command-month", "Display monthly rankings.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("not-ranked")) {
			langFile.set("not-ranked", "You are currently not ranked for this period.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-book-hover")) {
			langFile.set("aach-command-book-hover",
					"RP items you can collect and exchange with others! Time-based listing.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-stats-hover")) {
			langFile.set("aach-command-stats-hover", "Progress bar. Gotta catch 'em all!");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-list-hover")) {
			langFile.set("aach-command-list-hover",
					"Fancy GUI to get an overview of all achievements and your progress!");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-top-hover")) {
			langFile.set("aach-command-top-hover", "Who are the server's leaders and how do you compare to them?");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-give-hover")) {
			langFile.set("aach-command-give-hover", "Player must be online; only Commands achievements can be used.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-reload-hover")) {
			langFile.set("aach-command-reload-hover", "Reload most settings in config.yml and lang.yml files.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-info-hover")) {
			langFile.set("aach-command-info-hover", "Some extra info about the plugin and its awesome author!");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-check-hover")) {
			langFile.set("aach-command-check-hover", "Don't forget to add the colors defined in the config file.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-delete-hover")) {
			langFile.set("aach-command-delete-hover",
					"Player must be online; does not reset any associated statistics.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-week-hover")) {
			langFile.set("aach-command-week-hover", "Best achievement hunters since the start of the week!");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-command-month-hover")) {
			langFile.set("aach-command-month-hover", "Best achievement hunters since the start of the month!");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("aach-tip")) {
			langFile.set("aach-tip", "&lHINT &8You can &7&n&ohover &8or &7&n&oclick &8on the commands!");
			updateDone = true;
		}

		// Added in version 4.0:
		if (!langFile.getKeys(false).contains("list-achievements-in-category-singular")) {
			langFile.set("list-achievements-in-category", "AMOUNT achievement");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("list-achievements-in-category-plural")) {
			langFile.set("list-achievements-in-category-plural", "AMOUNT achievements");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("server-restart-reload")) {
			langFile.set("server-restart-reload",
					"DisabledCategories list was modified. Server must be fully reloaded or restarted for your changes to take effect.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("statistic-cooldown")) {
			langFile.set("statistic-cooldown",
					"Achievements cooldown, wait TIME seconds before this action counts again.");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("version-command-petmaster")) {
			langFile.set("version-command-petmaster", "Pet Master integration:");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("list-description")) {
			langFile.set("list-description", "Description:");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("list-goal")) {
			langFile.set("list-goal", "Goal:");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("list-reception")) {
			langFile.set("list-reception", "Reception date:");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("list-progress")) {
			langFile.set("list-progress", "Progress:");
			updateDone = true;
		}

		if (!langFile.getKeys(false).contains("book-not-received")) {
			langFile.set("book-not-received", "You have not yet received any achievements.");
			updateDone = true;
		}

		if (updateDone) {
			// Changes in the language file: save and do a fresh load.
			try {
				langFile.saveConfig();
				langFile.reloadConfig();
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "Error while saving changes to the language file: ", e);
				plugin.setSuccessfulLoad(false);
			}
		}
	}

}
