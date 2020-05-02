package com.hm.achievement.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.NumberHelper;
import com.hm.achievement.utils.RewardParser;
import com.hm.achievement.utils.StringHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Represents the main GUI, corresponding to more specific details about the different achievements.
 *
 * @author Pyves
 */
@Singleton
public class CategoryGUI implements Reloadable {

	private static final int MAX_PAGE_SIZE = 54;
	private static final int MAX_ACHIEVEMENTS_PER_PAGE = 50;
	private static final long NO_STAT = -1L;
	private static final String NO_SUBCATEGORY = "";
	private static final int PROGRESS_BAR_SIZE = 90;

	private final CommentedYamlConfiguration mainConfig;
	private final CommentedYamlConfiguration langConfig;
	private final CacheManager cacheManager;
	private final AbstractDatabaseManager databaseManager;
	private final Map<String, List<Long>> sortedThresholds;
	private final RewardParser rewardParser;
	private final GUIItems guiItems;

	private boolean configObfuscateNotReceived;
	private boolean configObfuscateProgressiveAchievements;
	private boolean configHideProgressiveAchievements;
	private boolean configHideRewardDisplayInList;
	private boolean configEnrichedProgressBars;
	private boolean configNumberedItemsInList;
	private ChatColor configColor;
	private ChatColor configListColorNotReceived;
	private String configFormatNotReceived;
	private String langListGUITitle;
	private String langListAchievementReceived;
	private String langListAchievementNotReceived;
	private String langListDescription;
	private String langListReception;
	private String langListGoal;
	private String langListProgress;
	private String langListReward;
	private String langListRewards;

	@Inject
	public CategoryGUI(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, CacheManager cacheManager,
			AbstractDatabaseManager databaseManager, Map<String, List<Long>> sortedThresholds, RewardParser rewardParser,
			GUIItems guiItems) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.cacheManager = cacheManager;
		this.databaseManager = databaseManager;
		this.sortedThresholds = sortedThresholds;
		this.rewardParser = rewardParser;
		this.guiItems = guiItems;
	}

	@Override
	public void extractConfigurationParameters() {
		configObfuscateNotReceived = mainConfig.getBoolean("ObfuscateNotReceived", true);
		configObfuscateProgressiveAchievements = mainConfig.getBoolean("ObfuscateProgressiveAchievements");
		configHideProgressiveAchievements = mainConfig.getBoolean("HideProgressiveAchievements");
		configHideRewardDisplayInList = mainConfig.getBoolean("HideRewardDisplayInList");
		configEnrichedProgressBars = mainConfig.getBoolean("EnrichedListProgressBars", true);
		configNumberedItemsInList = mainConfig.getBoolean("NumberedItemsInList");
		configColor = ChatColor.getByChar(mainConfig.getString("Color", "5"));
		configListColorNotReceived = ChatColor.getByChar(mainConfig.getString("ListColorNotReceived", "8"));
		configFormatNotReceived = mainConfig.getBoolean("ListItaliciseNotReceived", true) ? "&o" : "";

		langListGUITitle = translateColorCodes(LangHelper.get(GuiLang.GUI_TITLE, langConfig));
		langListAchievementReceived = StringEscapeUtils
				.unescapeJava(LangHelper.get(GuiLang.ACHIEVEMENT_RECEIVED, langConfig));
		langListAchievementNotReceived = StringEscapeUtils
				.unescapeJava(LangHelper.get(GuiLang.ACHIEVEMENT_NOT_RECEIVED, langConfig) + configListColorNotReceived);
		langListDescription = translateColorCodes("&7&l" + LangHelper.get(GuiLang.DESCRIPTION, langConfig));
		langListReception = translateColorCodes("&7&l" + LangHelper.get(GuiLang.RECEPTION, langConfig));
		langListGoal = translateColorCodes("&7&l" + LangHelper.get(GuiLang.GOAL, langConfig));
		langListProgress = translateColorCodes("&7&l" + LangHelper.get(GuiLang.PROGRESS, langConfig));
		langListReward = translateColorCodes("&7&l" + LangHelper.get(GuiLang.REWARD, langConfig));
		langListRewards = translateColorCodes("&7&l" + LangHelper.get(GuiLang.REWARDS, langConfig));
	}

	/**
	 * Displays a category GUI, containing all the achievements from a given category.
	 *
	 * @param item
	 * @param player
	 * @param requestedPage
	 */
	public void displayCategoryGUI(ItemStack item, Player player, int requestedPage) {
		for (Entry<OrderedCategory, ItemStack> achievementItem : guiItems.getOrderedAchievementItems().entrySet()) {
			if (achievementItem.getValue().isSimilar(item)) {
				Category category = achievementItem.getKey().getCategory();
				List<String> achievementPaths;
				Map<String, Long> subcategoriesToStatistics;
				if (category instanceof MultipleAchievements) {
					achievementPaths = getSortedMultipleAchievementPaths(category.toString());
					subcategoriesToStatistics = getMultipleStatisticsMapping((MultipleAchievements) category, player);
				} else if (category instanceof NormalAchievements) {
					achievementPaths = getSortedNormalAchievementThresholds(category.toString());
					long statistic = getNormalStatistic((NormalAchievements) category, player);
					subcategoriesToStatistics = Collections.singletonMap(NO_SUBCATEGORY, statistic);
				} else {
					achievementPaths = new ArrayList<>(mainConfig.getShallowKeys(CommandAchievements.COMMANDS.toString()));
					subcategoriesToStatistics = Collections.singletonMap(NO_SUBCATEGORY, NO_STAT);
				}
				displayPage(category.toString(), player, subcategoriesToStatistics, requestedPage, item, achievementPaths);
				return;
			}
		}
	}

	/**
	 * Displays a category GUI page, containing up to MAX_PER_PAGE achievements from a given category.
	 *
	 * @param categoryName
	 * @param player
	 * @param subcategoriesToStatistics
	 * @param requestedIndex
	 * @param clickedItem
	 * @param achievementPaths
	 */
	private void displayPage(String categoryName, Player player, Map<String, Long> subcategoriesToStatistics,
			int requestedIndex, ItemStack clickedItem, List<String> achievementPaths) {
		int pageIndex = getPageIndex(requestedIndex, achievementPaths.size());
		int pageStart = MAX_ACHIEVEMENTS_PER_PAGE * pageIndex;
		int pageEnd = Math.min(MAX_ACHIEVEMENTS_PER_PAGE * (pageIndex + 1), achievementPaths.size());
		int navigationItems = achievementPaths.size() > MAX_ACHIEVEMENTS_PER_PAGE ? 3 : 1;

		// Create a new chest-like inventory as small as possible whilst still containing the category item, all page
		// achievements and the navigation items.
		int guiSize = Math.min(NumberHelper.nextMultipleOf9(achievementPaths.size() + navigationItems + 1), MAX_PAGE_SIZE);
		AchievementInventoryHolder inventoryHolder = new AchievementInventoryHolder(pageIndex);
		Inventory inventory = Bukkit.createInventory(inventoryHolder, guiSize, langListGUITitle);
		inventoryHolder.setInventory(inventory);
		// Persist clicked item (ie. category's item in the main GUI) as first item in the category GUI.
		inventory.setItem(0, clickedItem);

		String previousItemDate = null;
		String previousSubcategory = NO_SUBCATEGORY;
		int seriesStart = 0;
		if (pageStart > 0) {
			String previousAchievement = achievementPaths.get(pageStart - 1);
			String achName = mainConfig.getString(categoryName + '.' + previousAchievement + ".Name", "");
			previousItemDate = databaseManager.getPlayerAchievementDate(player.getUniqueId(), achName);
			previousSubcategory = extractSubcategory(previousAchievement);
			String currentSubcategory = extractSubcategory(achievementPaths.get(pageStart));
			if (currentSubcategory != NO_SUBCATEGORY) {
				seriesStart = IntStream.range(0, achievementPaths.size())
						.filter(i -> achievementPaths.get(i).startsWith(currentSubcategory + "."))
						.findFirst()
						.getAsInt();
			}
		}
		// Populate the current GUI page with all of the achievements for the category.
		for (int index = pageStart; index < pageEnd; ++index) {
			// Path can either be a threshold (eg '10', or a subcategory and threshold (eg 'skeleton.10').
			String path = achievementPaths.get(index);
			String subcategory = extractSubcategory(path);
			long statistic = subcategoriesToStatistics.get(subcategory);
			String achName = mainConfig.getString(categoryName + '.' + path + ".Name", "");
			String receptionDate = databaseManager.getPlayerAchievementDate(player.getUniqueId(), achName);

			boolean differentSubcategory = !previousSubcategory.equals(subcategory);
			if (differentSubcategory) {
				seriesStart = index;
			}
			boolean ineligibleSeriesItem = true;
			if (statistic == NO_STAT || receptionDate != null || previousItemDate != null
					|| index == pageStart && pageStart == 0 || differentSubcategory) {
				// Commands achievement OR achievement has been completed OR previous achievement has been completed OR
				// first achievement in the category OR different subcategory.
				ineligibleSeriesItem = false;
			}

			if (configHideProgressiveAchievements && ineligibleSeriesItem) {
				inventory.setItem(index - pageStart + 1, guiItems.getAchievementLock());
			} else {
				String nameToDisplay = getNameToDisplay(categoryName, path, achName);
				List<String> descriptions = getDescriptionsToDisplay(categoryName, path, receptionDate != null);
				List<String> lore = buildLore(categoryName, descriptions, path, receptionDate, statistic,
						ineligibleSeriesItem, player);
				insertAchievement(inventory, index - pageStart + 1, statistic, nameToDisplay, receptionDate,
						ineligibleSeriesItem, index - seriesStart + 1, lore);
			}

			previousItemDate = receptionDate;
			previousSubcategory = subcategory;
		}
		// Add navigation items.
		if (navigationItems > 1) {
			inventory.setItem(pageEnd - pageStart + 1, guiItems.getPreviousButton());
			inventory.setItem(pageEnd - pageStart + 2, guiItems.getNextButton());
			inventory.setItem(pageEnd - pageStart + 3, guiItems.getBackButton());
		} else {
			inventory.setItem(pageEnd - pageStart + 1, guiItems.getBackButton());
		}

		// Display page.
		player.openInventory(inventory);
	}

	private String extractSubcategory(String path) {
		return path.contains(".") ? StringUtils.substringBefore(path, ".") : NO_SUBCATEGORY;
	}

	/**
	 * Creates a GUI item for a given achievement.
	 *
	 * @param gui
	 * @param position
	 * @param statistic
	 * @param name
	 * @param date
	 * @param ineligibleSeriesItem
	 * @param seriesNumber
	 * @param lore
	 */
	private void insertAchievement(Inventory gui, int position, long statistic, String name, String date,
			boolean ineligibleSeriesItem, int seriesNumber, List<String> lore) {
		// Display an item depending on whether the achievement was received or not, or whether progress was started.
		// Clone in order to work with an independent set of metadata.
		ItemStack achItem;
		if (date != null) {
			achItem = guiItems.getAchievementReceived().clone();
		} else if (statistic > 0) {
			achItem = guiItems.getAchievementStarted().clone();
		} else {
			achItem = guiItems.getAchievementNotStarted().clone();
		}

		String displayName = date == null ? langListAchievementNotReceived + notReceivedStyle(name, ineligibleSeriesItem)
				: langListAchievementReceived + name;
		ItemMeta itemMeta = achItem.getItemMeta();
		itemMeta.setDisplayName(translateColorCodes(displayName));
		itemMeta.setLore(lore);
		achItem.setItemMeta(itemMeta);
		if (configNumberedItemsInList) {
			achItem.setAmount(seriesNumber);
		}
		gui.setItem(position, achItem);
	}

	/**
	 * Gets a sorted list of paths (subcategory + threshold) for a Multiple category.
	 *
	 * @param categoryName
	 * @return the list of paths for the Multiple category
	 */
	public List<String> getSortedMultipleAchievementPaths(String categoryName) {
		List<String> paths = new ArrayList<>();
		// Populate the achievements from all the sub-categories in the category.
		for (String subcategory : mainConfig.getShallowKeys(categoryName)) {
			List<String> subcategoryAchievements = new ArrayList<>();
			for (long threshold : sortedThresholds.get(categoryName + "." + subcategory)) {
				subcategoryAchievements.add(subcategory + "." + threshold);
			}
			paths.addAll(subcategoryAchievements);
		}
		return paths;
	}

	/**
	 * Gets a sorted list of thresholds for a Normal category.
	 *
	 * @param categoryName
	 * @return the list of paths for the Normal category
	 */
	public List<String> getSortedNormalAchievementThresholds(String categoryName) {
		return sortedThresholds.get(categoryName).stream().map(String::valueOf).collect(Collectors.toList());
	}

	/**
	 * Gets the player's statistics for each subcategory in the Multiple category.
	 *
	 * @param category
	 * @param player
	 * @return the mapping from subcategory to player's statistics
	 */
	public Map<String, Long> getMultipleStatisticsMapping(MultipleAchievements category, Player player) {
		Map<String, Long> subcategoriesToStatistics = new HashMap<>();
		for (String subcategory : mainConfig.getShallowKeys(category.toString())) {
			long statistic = cacheManager.getAndIncrementStatisticAmount(category, subcategory, player.getUniqueId(), 0);
			subcategoriesToStatistics.put(subcategory, statistic);
		}
		return subcategoriesToStatistics;
	}

	/**
	 * Gets the player's statistic for a Normal category.
	 *
	 * @param category
	 * @param player
	 * @return the player's statistic for the category
	 */
	public long getNormalStatistic(NormalAchievements category, Player player) {
		if (category == NormalAchievements.CONNECTIONS) {
			return databaseManager.getConnectionsAmount(player.getUniqueId());
		}
		return cacheManager.getAndIncrementStatisticAmount(category, player.getUniqueId(), 0);
	}

	/**
	 * Extracts the achievement name that should be shown in the item lore.
	 *
	 * @param category
	 * @param path
	 * @param achName
	 * @return the name to display in the GUI
	 */
	private String getNameToDisplay(String category, String path, String achName) {
		String displayName = mainConfig.getString(category + '.' + path + ".DisplayName", "");
		if (StringUtils.isNotBlank(displayName)) {
			// Display name is defined; use it.
			return displayName;
		}
		return achName;
	}

	/**
	 * Extracts the achievement message/goals that should be shown in the item lore.
	 *
	 * @param category
	 * @param path
	 * @param completed
	 * @return the description to display in the GUI
	 */
	private List<String> getDescriptionsToDisplay(String category, String path, boolean completed) {
		String goal = mainConfig.getString(category + '.' + path + ".Goal", "");
		if (StringUtils.isNotBlank(goal) && !completed) {
			return Arrays.asList(StringUtils.splitByWholeSeparator(goal, "\\n"));
		}
		return Collections.singletonList(mainConfig.getString(category + '.' + path + ".Message", ""));
	}

	/**
	 * Computes the page index to display given a requested page and the total number of achievements in the category.
	 * The returned page index must be within a meaningful range, such that there are achievements to display on the
	 * page.
	 *
	 * @param requestedPage Index of requested page
	 * @param totalAchievements Number of achievements in the category
	 * @return the page index to display (start index is 0)
	 */
	private int getPageIndex(int requestedPage, int totalAchievements) {
		if (requestedPage <= 0) {
			return 0;
		} else if (totalAchievements <= MAX_ACHIEVEMENTS_PER_PAGE * requestedPage) {
			return requestedPage - 1;
		}
		return requestedPage;
	}

	/**
	 * Creates the lore for the current achievement, containing information about the progress, date of reception,
	 * description, rewards.
	 *
	 * @param categoryName
	 * @param descriptions
	 * @param path
	 * @param date
	 * @param statistic
	 * @param ineligibleSeriesItem
	 * @param player
	 * @return the list representing the lore of a category item
	 */
	private List<String> buildLore(String categoryName, List<String> descriptions, String path, String date, long statistic,
			boolean ineligibleSeriesItem, Player player) {
		List<String> lore = new ArrayList<>();
		lore.add("");

		if (date != null) {
			lore.add(langListDescription);
			descriptions.forEach(d -> lore.add(translateColorCodes("&r" + d)));
			lore.add("");
			lore.add(langListReception);
			lore.add(translateColorCodes("&r" + date));
			lore.add("");
		} else {
			lore.add(langListGoal);
			descriptions.forEach(d -> lore.add(translateColorCodes(notReceivedStyle(d, ineligibleSeriesItem))));
			lore.add("");
			// Display progress if not Commands category.
			if (!configObfuscateNotReceived && statistic != NO_STAT) {
				String threshold = StringUtils.defaultIfEmpty(StringUtils.substringAfter(path, "."), path);
				boolean timeStat = NormalAchievements.PLAYEDTIME.toString().equals(categoryName);
				lore.add(langListProgress);
				lore.add(translateColorCodes(constructProgressBar(threshold, statistic, timeStat)));
				lore.add("");
			}
		}

		List<String> rewards = rewardParser.getRewardListing(categoryName + '.' + path + ".Reward", player);
		// Add the rewards information.
		if (!rewards.isEmpty() && !configHideRewardDisplayInList) {
			lore.add(rewards.size() == 1 ? langListReward : langListRewards);
			String dot;
			if (date != null) {
				dot = StringEscapeUtils.unescapeJava("&r\u25CF ");
			} else {
				dot = StringEscapeUtils.unescapeJava(configListColorNotReceived + "\u25CF " + configFormatNotReceived);
			}
			for (String reward : rewards) {
				lore.add(translateColorCodes(dot + reward));
			}
		}
		return lore;
	}

	/**
	 * Constructs the progress bar to be displayed in an achievement's item lore.
	 *
	 * @param threshold
	 * @param statistic
	 * @param time
	 * @return progress bar
	 */
	private String constructProgressBar(String threshold, long statistic, boolean time) {
		String middleText;
		double statisticDouble;
		if (time) {
			statisticDouble = statistic / 3600000.0; // Convert from millis to hours.
			// Display one floating digit in the progress bar.
			middleText = String.format(" %s%s%.1f/%s ", configListColorNotReceived,
					configFormatNotReceived, statisticDouble, threshold);
		} else {
			statisticDouble = statistic; // Cast to double.
			middleText = String.format(" %s%s%s/%s ", configListColorNotReceived, configFormatNotReceived,
					statistic, threshold);
		}

		StringBuilder barDisplay = new StringBuilder().append(configListColorNotReceived).append("[");
		long numericalThreshold = Long.parseLong(threshold);
		// Approximation: colours chars account for no size, spaces ~2 vertical bars, other chars ~3 vertical bars.
		int middleTextSize = configEnrichedProgressBars ? (middleText.length() - 6) * 3 + 4 : 0;
		boolean hasDisplayedMiddleText = false;
		int i = 0;
		while (++i < PROGRESS_BAR_SIZE) {
			if (configEnrichedProgressBars && !hasDisplayedMiddleText && i >= (PROGRESS_BAR_SIZE - middleTextSize) / 2) {
				// Middle reached: append enriched statistic information.
				barDisplay.append(middleText);
				// Do not display middleText again.
				hasDisplayedMiddleText = true;
				// Iterate a number of times equal to the number of iterations so far to have the same number of
				// vertical bars left and right from the middle text.
				i = PROGRESS_BAR_SIZE - i;
			} else if (i < PROGRESS_BAR_SIZE * statisticDouble / numericalThreshold) {
				// Standard color: progress by user.
				barDisplay.append(configColor).append("|");
			} else {
				// Not received color: amount not yet reached by user.
				barDisplay.append(configListColorNotReceived).append("|");
			}
		}
		return barDisplay.append(configListColorNotReceived).append("]").toString();
	}

	/**
	 * Randomises the contents of a string; preserves spaces.
	 * 
	 * @param text
	 * @return a string with randomised alphabetic characters
	 */
	private String randomiseParts(String text) {
		if (text.isEmpty()) {
			return "";
		}
		StringBuilder randomisedText = new StringBuilder();
		for (String part : StringUtils.split(text)) {
			randomisedText.append(RandomStringUtils.randomAlphabetic(part.length())).append(' ');
		}
		return randomisedText.substring(0, randomisedText.length() - 1);
	}

	private String notReceivedStyle(String input, boolean ineligibleSeriesItem) {
		if (configObfuscateNotReceived || (configObfuscateProgressiveAchievements && ineligibleSeriesItem)) {
			return configListColorNotReceived + "&k" + randomiseParts(StringHelper.removeFormattingCodes(input));
		} else {
			return configListColorNotReceived + configFormatNotReceived + StringHelper.removeFormattingCodes(input);
		}
	}

	private String translateColorCodes(String translate) {
		return ChatColor.translateAlternateColorCodes('&', translate);
	}

}
