package com.hm.achievement.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
import org.bukkit.map.MinecraftFont;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.AbstractDatabaseManager;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.achievement.utils.RewardParser;
import com.hm.achievement.utils.StringHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Represents the main GUI, corresponding to more specific details about the different achievements.
 *
 * @author Pyves
 */
@Singleton
public class CategoryGUI extends AbstractGUI {

	private static final int MAX_PER_PAGE = 50;
	private static final long NO_STAT = -1L;
	private static final String NO_SUBCATEGORY = "";
	// Minecraft font, used to get size information in the progress bar.
	private static final MinecraftFont FONT = MinecraftFont.Font;

	private final AbstractDatabaseManager databaseManager;
	private final Map<String, List<Long>> sortedThresholds;
	private final RewardParser rewardParser;

	private boolean configObfuscateNotReceived;
	private boolean configObfuscateProgressiveAchievements;
	private boolean configHideRewardDisplayInList;
	private boolean configEnrichedProgressBars;
	private boolean configNumberedItemsInList;
	private ChatColor configColor;
	private ChatColor configListColorNotReceived;
	private String langListGUITitle;
	private String langListAchievementReceived;
	private String langListAchievementNotReceived;
	private String langListDescription;
	private String langListReception;
	private String langListGoal;
	private String langListProgress;
	private String langListReward;

	// Various item stacks displayed in the GUI.
	private ItemStack previousButton;
	private ItemStack nextButton;
	private ItemStack backButton;
	private ItemStack achievementNotStarted;
	private ItemStack achievementStarted;
	private ItemStack achievementReceived;

	@Inject
	public CategoryGUI(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, @Named("gui") CommentedYamlConfiguration guiConfig,
			CacheManager cacheManager, AbstractDatabaseManager databaseManager, Map<String, List<Long>> sortedThresholds,
			RewardParser rewardParser, MaterialHelper materialHelper) {
		super(mainConfig, langConfig, guiConfig, cacheManager, materialHelper);
		this.databaseManager = databaseManager;
		this.sortedThresholds = sortedThresholds;
		this.rewardParser = rewardParser;
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configObfuscateNotReceived = mainConfig.getBoolean("ObfuscateNotReceived", true);
		configObfuscateProgressiveAchievements = mainConfig.getBoolean("ObfuscateProgressiveAchievements", false);
		configHideRewardDisplayInList = mainConfig.getBoolean("HideRewardDisplayInList", false);
		configEnrichedProgressBars = mainConfig.getBoolean("EnrichedListProgressBars", true);
		configNumberedItemsInList = mainConfig.getBoolean("NumberedItemsInList", false);
		configColor = ChatColor.getByChar(mainConfig.getString("Color", "5"));
		configListColorNotReceived = ChatColor.getByChar(mainConfig.getString("ListColorNotReceived", "8"));

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

		achievementNotStarted = createItemStack("AchievementNotStarted");
		achievementStarted = createItemStack("AchievementStarted");
		achievementReceived = createItemStack("AchievementReceived");
		previousButton = createButton("PreviousButton", GuiLang.PREVIOUS_MESSAGE, GuiLang.PREVIOUS_LORE);
		nextButton = createButton("NextButton", GuiLang.NEXT_MESSAGE, GuiLang.NEXT_LORE);
		backButton = createButton("BackButton", GuiLang.BACK_MESSAGE, GuiLang.BACK_LORE);
	}

	private ItemStack createButton(String category, GuiLang msg, GuiLang lore) {
		ItemStack button = createItemStack(category);
		ItemMeta meta = button.getItemMeta();
		meta.setDisplayName(translateColorCodes(StringEscapeUtils.unescapeJava(LangHelper.get(msg, langConfig))));
		String loreString = translateColorCodes(StringEscapeUtils.unescapeJava(LangHelper.get(lore, langConfig)));
		if (!loreString.isEmpty()) {
			meta.setLore(Collections.singletonList(loreString));
		}
		button.setItemMeta(meta);
		return button;
	}

	/**
	 * Displays a category GUI, containing all the achievements from a given category.
	 *
	 * @param item
	 * @param player
	 * @param requestedPage
	 */
	public void displayCategoryGUI(ItemStack item, Player player, int requestedPage) {
		for (Entry<MultipleAchievements, ItemStack> entry : multipleAchievementItems.entrySet()) {
			if (entry.getValue().isSimilar(item)) {
				String categoryName = entry.getKey().toString();
				List<String> achievementPaths = getSortedMultipleAchievementPaths(categoryName);
				Map<String, Long> subcategoriesToStatistics = getMultipleStatisticsMapping(entry.getKey(), player);
				displayPage(categoryName, player, subcategoriesToStatistics, requestedPage, item, achievementPaths);
				return;
			}
		}
		for (Entry<NormalAchievements, ItemStack> entry : normalAchievementItems.entrySet()) {
			if (entry.getValue().isSimilar(item)) {
				String categoryName = entry.getKey().toString();
				List<String> achievementThresholds = getSortedNormalAchievementThresholds(categoryName);
				long statistic = getNormalStatistic(entry.getKey(), player);
				displayPage(categoryName, player, Collections.singletonMap(NO_SUBCATEGORY, statistic), requestedPage, item,
						achievementThresholds);
				return;
			}
		}
		List<String> achievementPaths = new ArrayList<>(mainConfig.getShallowKeys(CommandAchievements.COMMANDS.toString()));
		displayPage(CommandAchievements.COMMANDS.toString(), player, Collections.singletonMap(NO_SUBCATEGORY, NO_STAT),
				requestedPage, item, achievementPaths);
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
		int pageStart = MAX_PER_PAGE * pageIndex;
		int pageEnd = Math.min(MAX_PER_PAGE * (pageIndex + 1), achievementPaths.size());
		int navigationItems = achievementPaths.size() > MAX_PER_PAGE ? 3 : 1;

		// Create a new chest-like inventory as small as possible whilst still containing the category item, all page
		// achievements and the navigation items.
		int guiSize = nextMultipleOf9(achievementPaths.size() + navigationItems + 1, MAX_PER_PAGE);
		AchievementInventoryHolder inventoryHolder = new AchievementInventoryHolder(pageIndex);
		Inventory inventory = Bukkit.createInventory(inventoryHolder, guiSize, langListGUITitle);
		inventoryHolder.setInventory(inventory);
		// Persist clicked item (ie. category's item in the main GUI) as first item in the category GUI.
		inventory.setItem(0, clickedItem);

		String previousItemDate = null;
		String previousSubcategory = NO_SUBCATEGORY;
		if (pageStart > 0) {
			String previousAchievement = achievementPaths.get(pageStart - 1);
			String achName = mainConfig.getString(categoryName + '.' + previousAchievement + ".Name", "");
			previousItemDate = databaseManager.getPlayerAchievementDate(player.getUniqueId(), achName);
			if (previousAchievement.contains(".")) {
				previousSubcategory = StringUtils.substringBefore(previousAchievement, ".");
			}
		}
		// Populate the current GUI page with all of the achievements for the category.
		for (int index = pageStart; index < pageEnd; ++index) {
			// Path can either be a threshold (eg '10', or a subcategory and threshold (eg 'skeleton.10').
			String path = achievementPaths.get(index);
			String subcategory = path.contains(".") ? StringUtils.substringBefore(path, ".") : NO_SUBCATEGORY;
			long statistic = subcategoriesToStatistics.get(subcategory);
			String achName = mainConfig.getString(categoryName + '.' + path + ".Name", "");
			String receptionDate = databaseManager.getPlayerAchievementDate(player.getUniqueId(), achName);

			boolean ineligibleSeriesItem = true;
			if (statistic == NO_STAT || receptionDate != null || previousItemDate != null
					|| index == pageStart && pageStart == 0 || !previousSubcategory.equals(subcategory)) {
				// Commands achievement OR achievement has been completed OR previous achievement has been completed OR
				// first achievement in the category OR different subcategory.
				ineligibleSeriesItem = false;
			}

			String nameToDisplay = getNameToDisplay(categoryName, path, achName);
			String descriptionToDisplay = getDescriptionToDisplay(categoryName, path, receptionDate != null);
			List<String> lore = buildLore(categoryName, descriptionToDisplay, path, receptionDate, statistic,
					ineligibleSeriesItem);
			insertAchievement(inventory, index - pageStart + 1, statistic, nameToDisplay, receptionDate,
					ineligibleSeriesItem, lore);

			previousItemDate = receptionDate;
			previousSubcategory = subcategory;
		}
		// Add navigation items.
		if (navigationItems > 1) {
			inventory.setItem(pageEnd - pageStart + 1, previousButton);
			inventory.setItem(pageEnd - pageStart + 2, nextButton);
			inventory.setItem(pageEnd - pageStart + 3, backButton);
		} else {
			inventory.setItem(pageEnd - pageStart + 1, backButton);
		}

		// Display page.
		player.openInventory(inventory);
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
	 * @param lore
	 */
	private void insertAchievement(Inventory gui, int position, long statistic, String name, String date,
			boolean ineligibleSeriesItem, List<String> lore) {
		// Display an item depending on whether the achievement was received or not, or whether progress was started.
		// Clone in order to work with an independent set of metadata.
		ItemStack achItem;
		if (date != null) {
			achItem = achievementReceived.clone();
		} else if (statistic > 0) {
			achItem = achievementStarted.clone();
		} else {
			achItem = achievementNotStarted.clone();
		}

		// Set name of the achievement. The style depends whether it was received or not and whether the user has set
		// obfuscateNotReceived and/or obfuscateProgressiveAchievements in the config.
		ItemMeta itemMeta = achItem.getItemMeta();
		if (date != null) {
			itemMeta.setDisplayName(translateColorCodes(langListAchievementReceived + name));
		} else if (configObfuscateNotReceived || (configObfuscateProgressiveAchievements && ineligibleSeriesItem)) {
			itemMeta.setDisplayName(translateColorCodes(langListAchievementNotReceived
					+ "&k" + StringHelper.removeFormattingCodes(name)));
		} else {
			itemMeta.setDisplayName(translateColorCodes(StringEscapeUtils.unescapeJava(langListAchievementNotReceived
					+ "&o" + StringHelper.removeFormattingCodes(name))));
		}

		itemMeta.setLore(lore);
		achItem.setItemMeta(itemMeta);
		if (configNumberedItemsInList) {
			achItem.setAmount(position);
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
	 * Extracts the achievement message that should be shown in the item lore.
	 *
	 * @param category
	 * @param path
	 * @param completed
	 * @return the description to display in the GUI
	 */
	private String getDescriptionToDisplay(String category, String path, boolean completed) {
		String goal = mainConfig.getString(category + '.' + path + ".Goal", "");
		if (StringUtils.isNotBlank(goal) && !completed) {
			// Show the goal below the achievement name.
			return goal;
		}
		// Show the achievement message below the achievement name.
		return mainConfig.getString(category + '.' + path + ".Message", "");
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
		} else if (totalAchievements <= MAX_PER_PAGE * requestedPage) {
			return requestedPage - 1;
		}
		return requestedPage;
	}

	/**
	 * Creates the lore for the current achievement, containing information about the progress, date of reception,
	 * description, rewards.
	 *
	 * @param categoryName
	 * @param description
	 * @param path
	 * @param date
	 * @param statistic
	 * @param ineligibleSeriesItem
	 * @return the list representing the lore of a category item
	 */
	private List<String> buildLore(String categoryName, String description, String path, String date, long statistic,
			boolean ineligibleSeriesItem) {
		List<String> lore = new ArrayList<>();
		lore.add("");

		if (date != null) {
			lore.add(langListDescription);
			lore.add(translateColorCodes("&r" + description));
			lore.add("");
			lore.add(langListReception);
			lore.add(translateColorCodes("&r" + date));
			lore.add("");
		} else {
			lore.add(langListGoal);
			String strippedAchMessage = StringHelper.removeFormattingCodes(description);
			if (configObfuscateNotReceived || (configObfuscateProgressiveAchievements && ineligibleSeriesItem)) {
				lore.add(translateColorCodes(configListColorNotReceived + "&k" + randomiseParts(strippedAchMessage)));
			} else {
				lore.add(translateColorCodes(configListColorNotReceived + "&o" + strippedAchMessage));
			}
			lore.add("");
			// Display progress if not Commands category.
			if (!configObfuscateNotReceived && statistic != NO_STAT) {
				String threshold = StringUtils.defaultIfEmpty(StringUtils.substringAfter(path, "."), path);
				boolean timeStat = NormalAchievements.PLAYEDTIME.toString().equals(categoryName);
				lore.add(langListProgress);
				lore.add(translateColorCodes(constructProgressBar(strippedAchMessage, threshold, statistic, timeStat)));
				lore.add("");
			}
		}

		List<String> rewards = rewardParser.getRewardListing(categoryName + '.' + path + ".Reward");
		// Add the rewards information.
		if (!rewards.isEmpty() && !configHideRewardDisplayInList) {
			lore.add(langListReward);
			String dot;
			if (date != null) {
				dot = StringEscapeUtils.unescapeJava("&r\u25CF ");
			} else {
				dot = StringEscapeUtils.unescapeJava(configListColorNotReceived + "\u25CF &o");
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
	 * @param achMessage
	 * @param level
	 * @param statistic
	 * @param time
	 * @return progress bar
	 */
	private String constructProgressBar(String achMessage, String level, long statistic, boolean time) {
		StringBuilder barDisplay = new StringBuilder(configListColorNotReceived.toString()).append("[");
		// Length of the progress bar; we make it the same size as Goal/Message.
		int textSize;
		// MinecraftFont essentially supports latin alphabet characters. If invalid characters are found just use
		// number of chars.
		if (FONT.isValid(achMessage)) {
			textSize = FONT.getWidth(StringHelper.removeFormattingCodes(achMessage));
		} else {
			textSize = achMessage.length() * 3;
		}

		long levelInt = Long.parseLong(level);
		String middleText;
		double statisticDouble;
		if (time) {
			// Convert from millis to hours.
			statisticDouble = statistic / 3600000.0;
			// Display one floating digit in the progress bar.
			middleText = " " + String.format("%.1f", statisticDouble) + "/" + levelInt + " ";
		} else {
			middleText = " " + statistic + "/" + levelInt + " ";
			// Cast to double.
			statisticDouble = statistic;
		}
		int middleTextSize = FONT.getWidth(middleText);
		middleText = configListColorNotReceived + "&o" + middleText;

		boolean hasDisplayedMiddleText = false;
		int i = 1;
		while (i < textSize / 2) {
			if (configEnrichedProgressBars && !hasDisplayedMiddleText && i >= (textSize - middleTextSize) / 4) {
				// Middle reached: append enriched statistic information.
				barDisplay.append(middleText);
				// Do not display middleText again.
				hasDisplayedMiddleText = true;
				// Iterate a number of times equal to the number of iterations so far to have the same number of
				// vertical bars left and right from the middle text.
				i = textSize / 2 + 1 - i;
			} else {
				if (i < (((double) textSize / 2 - 1) * statisticDouble) / levelInt) {
					// Standard color: progress by user.
					barDisplay.append(configColor);
				} else {
					// Not received color: amount not yet reached by user.
					barDisplay.append(configListColorNotReceived);
				}
				barDisplay.append("|");
				i++;
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

	public ItemStack getPreviousButton() {
		return previousButton;
	}

	public ItemStack getNextButton() {
		return nextButton;
	}

	public ItemStack getBackButton() {
		return backButton;
	}
}
