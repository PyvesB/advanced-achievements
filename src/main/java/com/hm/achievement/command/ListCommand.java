package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class in charge of handling the /aach list command, which displays interactive GUIs. The command displays the main
 * GUI, corresponding to all the different available categories and their names. Users will then be able to click on an
 * unlocked category to display another GUI with more specific details about the different achievements.
 * 
 * @author Pyves
 */
public class ListCommand extends AbstractCommand {

	// Pattern to delete colors if achievement not yet received.
	private static final Pattern REGEX_PATTERN = Pattern.compile("&([a-f]|[0-9]){1}");
	// Minecraft font, used to get size information in the progress bar.
	private static final MinecraftFont FONT = MinecraftFont.Font;

	// Items stacks displayed in the GUI.
	private final Map<MultipleAchievements, ItemStack> multipleAchievementCategoryItems;
	private final Map<NormalAchievements, ItemStack> normalAchievementCategoryItems;
	private ItemStack commandsCategoryItem;
	private final ItemStack backButton = new ItemStack(Material.PAPER);
	private final ItemStack barrier = new ItemStack(Material.BARRIER);

	private boolean configHideNotReceivedCategories;
	private boolean configHideNoPermissionCategories;
	private boolean configObfuscateNotReceived;
	private boolean configObfuscateProgressiveAchievements;
	private boolean configHideRewardDisplayInList;
	private boolean configEnrichedProgressBars;
	private boolean configNumberedItemsInList;
	private String configListAchievementFormat;
	private String langListGUITitle;
	private String langListCategoryNotUnlocked;
	private String langListAchievementsInCategoryPlural;
	private String langListAchievementInCategorySingular;
	private String langListAchievementReceived;
	private String langListAchievementNotReceived;
	private String langListDescription;
	private String langListReception;
	private String langListGoal;
	private String langListProgress;
	private String langListReward;

	public ListCommand(AdvancedAchievements plugin) {
		super(plugin);

		multipleAchievementCategoryItems = new EnumMap<>(MultipleAchievements.class);
		normalAchievementCategoryItems = new EnumMap<>(NormalAchievements.class);
	}

	@Override
	public void extractConfigurationParameters() {
		super.extractConfigurationParameters();

		configHideNotReceivedCategories = plugin.getPluginConfig().getBoolean("HideNotReceivedCategories", false);
		configHideNoPermissionCategories = plugin.getPluginConfig().getBoolean("HideNoPermissionCategories", false);
		configObfuscateNotReceived = plugin.getPluginConfig().getBoolean("ObfuscateNotReceived", true);
		configObfuscateProgressiveAchievements = plugin.getPluginConfig().getBoolean("ObfuscateProgressiveAchievements",
				false);
		configHideRewardDisplayInList = plugin.getPluginConfig().getBoolean("HideRewardDisplayInList", false);
		configEnrichedProgressBars = plugin.getPluginConfig().getBoolean("EnrichedListProgressBars", true);
		configNumberedItemsInList = plugin.getPluginConfig().getBoolean("NumberedItemsInList", false);
		configListAchievementFormat = "&8"
				+ plugin.getPluginConfig().getString("ListAchievementFormat", "%ICON% %NAME% %ICON%");

		langListGUITitle = ChatColor.translateAlternateColorCodes('&',
				plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List"));
		langListCategoryNotUnlocked = ChatColor.translateAlternateColorCodes('&', "&8" + plugin.getPluginLang()
				.getString("list-category-not-unlocked", "You have not yet unlocked this category."));
		langListAchievementsInCategoryPlural = plugin.getPluginLang().getString("list-achievements-in-category-plural",
				"AMOUNT achievements");
		langListAchievementInCategorySingular = plugin.getPluginLang()
				.getString("list-achievements-in-category-singular", "AMOUNT achievement");
		langListAchievementReceived = StringEscapeUtils
				.unescapeJava(plugin.getPluginLang().getString("list-achievement-received", "&a\u2714&f "));
		langListAchievementNotReceived = StringEscapeUtils
				.unescapeJava(plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2718&8 "));
		langListDescription = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-description", "Description:"));
		langListReception = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-reception", "Reception date:"));
		langListGoal = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-goal", "Goal:"));
		langListProgress = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-progress", "Progress:"));
		langListReward = ChatColor.translateAlternateColorCodes('&',
				"&7&l" + plugin.getPluginLang().getString("list-reward", "Reward(s):"));

		// Prepare item stacks displayed in the GUI for Multiple achievements.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();
			Material material = Material
					.getMaterial(plugin.getPluginGui().getString(categoryName + ".Item", "bedrock").toUpperCase());
			short metadata = (short) plugin.getPluginGui().getInt(categoryName + ".Metadata", 0);
			if (material == null) {
				material = Material.BEDROCK;
				plugin.getLogger().warning("GUI material for category " + categoryName
						+ " was not found. Have you spelt the name correctly or is it available for your Minecraft version?");
			}
			ItemStack itemStack = new ItemStack(material, 1, metadata);

			// Sum all achievements in the sub-categories of this main category.
			int totalAchievementsInCategory = 0;
			for (String section : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
				totalAchievementsInCategory += plugin.getPluginConfig()
						.getConfigurationSection(categoryName + '.' + section).getKeys(false).size();
			}
			constructItemStackLore(itemStack,
					plugin.getPluginLang().getString(category.toLangName(), category.toLangDefault()),
					totalAchievementsInCategory);
			multipleAchievementCategoryItems.put(category, itemStack);
		}

		// Prepare item stacks displayed in the GUI for Normal achievements.
		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();
			Material material = Material
					.getMaterial(plugin.getPluginGui().getString(categoryName + ".Item", "bedrock").toUpperCase());
			short metadata = (short) plugin.getPluginGui().getInt(categoryName + ".Metadata", 0);
			if (material == null) {
				material = Material.BEDROCK;
			}
			ItemStack itemStack = new ItemStack(material, 1, metadata);
			constructItemStackLore(itemStack,
					plugin.getPluginLang().getString(category.toLangName(), category.toLangDefault()),
					plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false).size());
			normalAchievementCategoryItems.put(category, itemStack);
		}

		// Prepare item stack displayed in the GUI for Commands achievements.
		Material material = Material
				.getMaterial(plugin.getPluginGui().getString("Commands.Item", "bedrock").toUpperCase());
		short metadata = (short) plugin.getPluginGui().getInt("Commands.Metadata", 0);
		if (material == null) {
			material = Material.BEDROCK;
		}
		commandsCategoryItem = new ItemStack(material, 1, metadata);
		constructItemStackLore(commandsCategoryItem,
				plugin.getPluginLang().getString("list-commands", "Other Achievements"),
				plugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false).size());

		ItemMeta backMeta = backButton.getItemMeta();
		backMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				StringEscapeUtils.unescapeJava(plugin.getPluginLang().getString("list-back-message", "&7Back"))));
		backButton.setItemMeta(backMeta);

		ItemMeta barrierMeta = barrier.getItemMeta();
		barrierMeta.setDisplayName(langListCategoryNotUnlocked);
	}

	/**
	 * Sets the metadata of an ItemStack.
	 * 
	 * @param itemStacks
	 * @param displayName
	 * @param indexInItemStacksArray
	 * @param i
	 * @return
	 */
	private void constructItemStackLore(ItemStack categoryItem, String displayName, int totalAchievementsInCategory) {
		ItemMeta categoryMeta = categoryItem.getItemMeta();
		// Create item stack that will be displayed in the GUI, with its category name.
		if (StringUtils.isBlank(displayName)) {
			categoryMeta.setDisplayName("");
		} else {
			categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					StringUtils.replaceEach(configListAchievementFormat, new String[] { "%ICON%", "%NAME%" },
							new String[] { configIcon, "&l" + displayName + "&8" })));
		}

		String loreAmountMessage;
		if (totalAchievementsInCategory > 1) {
			loreAmountMessage = langListAchievementsInCategoryPlural;

		} else {
			loreAmountMessage = langListAchievementInCategorySingular;
		}
		// Set item lore.
		categoryMeta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&8" + StringUtils
				.replaceOnce(loreAmountMessage, "AMOUNT", Integer.toString(totalAchievementsInCategory)))));
		// Set item meta.
		categoryItem.setItemMeta(categoryMeta);
	}

	@Override
	protected void executeCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return;
		}

		Player player = (Player) sender;

		Inventory guiInv = Bukkit.createInventory(
				null, getClosestGreaterMultipleOf9(MultipleAchievements.values().length
						+ NormalAchievements.values().length - plugin.getDisabledCategorySet().size() + 1),
				langListGUITitle);

		// Total number of categories already displayed.
		int numberOfCategories = 0;
		numberOfCategories += populateMainGUIWithMultipleAchievements(player, guiInv);
		numberOfCategories += populateMainGUIWithNormalAchievements(player, guiInv, numberOfCategories);
		populateMainGUIWithCommandsAchievements(player, guiInv, numberOfCategories);

		// Display GUI to the player.
		player.openInventory(guiInv);
	}

	/**
	 * Displays the a category GUI, containing all the achievements from a given category.
	 * 
	 * @param item
	 * @param player
	 */
	public void createCategoryGUI(ItemStack item, Player player) {
		for (Entry<MultipleAchievements, ItemStack> entry : multipleAchievementCategoryItems.entrySet()) {
			if (entry.getValue().getType() == item.getType()
					&& entry.getValue().getDurability() == item.getDurability()) {
				createCategoryGUIMultiple(entry.getKey(), player);
				return;
			}
		}
		for (Entry<NormalAchievements, ItemStack> entry : normalAchievementCategoryItems.entrySet()) {
			if (entry.getValue().getType() == item.getType()
					&& entry.getValue().getDurability() == item.getDurability()) {
				createCategoryGUINormal(entry.getKey(), player);
				return;
			}
		}
		// Commands achievements.
		createCategoryGUINormal(null, player);
	}

	/**
	 * Displays the a category GUI, containing all the achievements from a given category. This method is used for
	 * multiple achievements, in other words those based on sub-categories.
	 * 
	 * @param category
	 * @param player
	 */
	public void createCategoryGUIMultiple(MultipleAchievements category, Player player) {
		String categoryName = category.toString();

		CommentedYamlConfiguration config = plugin.getPluginConfig();
		ConfigurationSection categoryConfig = config.getConfigurationSection(categoryName);

		int totalAchievementsInCategory = 0;

		// Retrieve total number of achievements in the sub-categories of the category.
		for (String section : categoryConfig.getKeys(false)) {
			totalAchievementsInCategory += config.getConfigurationSection(categoryName + '.' + section).getKeys(false)
					.size();
		}

		// Used to make the GUI as small as possible while still containing all achievements.
		int inventorySize = getClosestGreaterMultipleOf9(totalAchievementsInCategory + 1);

		// Create a new chest-like inventory; make it as small as possible while still containing all achievements.
		Inventory inventory = Bukkit.createInventory(null, inventorySize, langListGUITitle);

		int positionInGUI = 0;

		// Match the item the player clicked on with its database statistic.
		for (String section : categoryConfig.getKeys(false)) {
			// Retrieve statistic from category and subcategory.
			long statistic = plugin.getCacheManager().getAndIncrementStatisticAmount(category, section,
					player.getUniqueId(), 0);

			String previousItemDate = null;
			long previousItemGoal = 0;
			int subcategoryIndex = 0;

			// Populate GUI with all the achievements for the current sub-category.
			ConfigurationSection subcategoryConfig = config.getConfigurationSection(categoryName + '.' + section);
			for (String level : subcategoryConfig.getKeys(false)) {
				// If the user specifies more than 98 achievements in the category they will not be displayed.
				if (positionInGUI >= inventorySize - 1) {
					break;
				}

				// Convert threshold to an integer.
				long currentItemGoal = Long.parseLong(level);

				String achName = config.getString(categoryName + '.' + section + '.' + level + ".Name", "");
				String nameToShowUser = getAchievementNameToDisplay(categoryName, '.' + section, achName, level);
				List<String> rewards = plugin.getRewardParser()
						.getRewardListing(categoryName + '.' + section + '.' + level);
				String date = plugin.getDatabaseManager().getPlayerAchievementDate(player.getUniqueId(), achName);
				String achMessage = getAchievementMessageToDisplay(categoryName, '.' + section, level, date != null);

				boolean inelligibleSeriesItem;
				if (subcategoryIndex == 0 || date != null || previousItemDate != null) {
					// First achievement in the category OR achievement has been completed OR previous achievement has
					// been completed.
					inelligibleSeriesItem = false;
				} else {
					// Check whether this achievement cannot be completed until the previous one is completed.
					if (currentItemGoal > previousItemGoal) {
						inelligibleSeriesItem = true;
					} else {
						inelligibleSeriesItem = false;
					}
				}

				createCategoryGUIItem(inventory, positionInGUI, level, statistic, nameToShowUser, achMessage, rewards,
						date, inelligibleSeriesItem, false);
				positionInGUI++;

				previousItemDate = date;
				previousItemGoal = currentItemGoal;
				subcategoryIndex++;
			}
		}

		// Add "back button" item.
		inventory.setItem(positionInGUI, backButton);

		// Display category GUI.
		player.openInventory(inventory);
	}

	/**
	 * Displays a category GUI, containing all the achievements from a given category. This method is used for normal
	 * achievements.
	 * 
	 * @param category
	 * @param player
	 */
	private void createCategoryGUINormal(NormalAchievements category, Player player) {
		long statistic = 0L;

		String categoryName;
		if (category == null) {
			categoryName = "Commands";
			statistic = -1L;
		} else {
			categoryName = category.toString();
		}

		if (category == NormalAchievements.CONNECTIONS) {
			statistic = plugin.getDatabaseManager().getConnectionsAmount(player.getUniqueId());
		} else if (category != null) {
			statistic = plugin.getCacheManager().getAndIncrementStatisticAmount(category, player.getUniqueId(), 0);
		}

		// Used to make the GUI as small as possible while still containing all achievements.
		int inventorySize = getClosestGreaterMultipleOf9(
				plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false).size() + 1);

		// Create a new chest-like inventory.
		Inventory inventory = Bukkit.createInventory(null, inventorySize, langListGUITitle);

		int positionInGUI = 0;

		String previousItemDate = null;
		Long previousItemGoal = 0L;
		// Populate the GUI with all of the achievements for the category.
		for (String level : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
			// If the user specifies more than 98 achievements in the category they will not be displayed.
			if (positionInGUI >= inventorySize - 1) {
				break;
			}

			// Convert threshold to an integer.
			Long currentItemGoal = NumberUtils.toLong(level, 0);

			String achName = plugin.getPluginConfig().getString(categoryName + '.' + level + ".Name", "");
			String nameToShowUser = getAchievementNameToDisplay(categoryName, "", achName, level);
			List<String> rewards = plugin.getRewardParser().getRewardListing(categoryName + '.' + level);
			String date = plugin.getDatabaseManager().getPlayerAchievementDate(player.getUniqueId(), achName);
			String achMessage = getAchievementMessageToDisplay(categoryName, "", level, date != null);

			boolean inelligibleSeriesItem;
			if (statistic == -1L || positionInGUI == 0 || date != null || previousItemDate != null) {
				// Commands achievement OR first achievement in the category OR achievement has been completed OR
				// previous achievement has been completed.
				inelligibleSeriesItem = false;
			} else {
				// Check whether this achievement cannot be completed until the previous one is completed.
				if (currentItemGoal > previousItemGoal) {
					inelligibleSeriesItem = true;
				} else {
					inelligibleSeriesItem = false;
				}
			}

			boolean playedTime = false;
			if (category == NormalAchievements.PLAYEDTIME) {
				playedTime = true;
			}

			createCategoryGUIItem(inventory, positionInGUI, level, statistic, nameToShowUser, achMessage, rewards, date,
					inelligibleSeriesItem, playedTime);
			positionInGUI++;

			previousItemDate = date;
			previousItemGoal = currentItemGoal;
		}

		// Add "back button" item.
		inventory.setItem(positionInGUI, backButton);

		// Display category GUI.
		player.openInventory(inventory);
	}

	/**
	 * Extracts the achievement name that should be shown in the item lore.
	 * 
	 * @param categoryName
	 * @param subcategory
	 * @param achName
	 * @param level
	 * @return
	 */
	private String getAchievementNameToDisplay(String categoryName, String subcategory, String achName, String level) {
		String nameToShowUser;
		String displayName = plugin.getPluginConfig()
				.getString(categoryName + subcategory + '.' + level + ".DisplayName", "");
		if (StringUtils.isNotBlank(displayName)) {
			// Display name is defined; use it.
			nameToShowUser = displayName;
		} else {
			// Use the achievement key name (this name is used in the achievements table in the database).
			nameToShowUser = achName;
		}
		return nameToShowUser;
	}

	/**
	 * Extracts the achievement message that should be shown in the item lore.
	 * 
	 * @param categoryName
	 * @param subcategory
	 * @param level
	 * @param completed
	 * @return
	 */
	private String getAchievementMessageToDisplay(String categoryName, String subcategory, String level,
			boolean completed) {
		String achMessage;
		String goal = plugin.getPluginConfig().getString(categoryName + subcategory + '.' + level + ".Goal", "");
		if (StringUtils.isNotBlank(goal) && !completed) {
			// Show the goal below the achievement name.
			achMessage = goal;
		} else {
			// Show the achievement message below the achievement name.
			achMessage = plugin.getPluginConfig().getString(categoryName + subcategory + '.' + level + ".Message", "");
		}
		return achMessage;
	}

	/**
	 * Populates the main GUI with MultipleAchievement categories.
	 * 
	 * @param player
	 * @param guiInv
	 * @return
	 */
	private int populateMainGUIWithMultipleAchievements(Player player, Inventory guiInv) {
		int categoriesProcessed = 0;

		// Display categories with multiple sub-categories in GUI.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			// Boolean corresponding to whether a player has received achievements in the current category. Used for
			// HideNotReceivedCategories config option.
			boolean hasReceivedInCategory = !configHideNotReceivedCategories;

			ItemStack itemStack = multipleAchievementCategoryItems.get(category);
			String categoryName = category.toString();
			// Hide category if the user has defined an empty name for it or if it's in the disabled list.
			if (itemStack.getItemMeta().getDisplayName().length() == 0
					|| plugin.getDisabledCategorySet().contains(categoryName)
					|| configHideNoPermissionCategories && !player.hasPermission(category.toPermName())) {
				continue;
			}

			// Iterate through all sub-categories.
			for (String section : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
				Set<String> configSubcategory = plugin.getPluginConfig()
						.getConfigurationSection(categoryName + '.' + section).getKeys(false);
				// No need to check next sub-category, continue and move to next sub-category.
				if (hasReceivedInCategory) {
					continue;
				}
				// Hide not yet unlocked categories: we must check whether the player has received at least one
				// achievement in the category. Iterate through all achievements in sub-category.
				for (String level : configSubcategory) {
					// Check whether player has received achievement.
					if (plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(), plugin.getPluginConfig()
							.getString(categoryName + '.' + section + '.' + level + ".Name", ""))) {
						// At least one achievement was received in the current category; it is unlocked, can
						// continue processing.
						hasReceivedInCategory = true;
						break;
					}
				}
			}

			if (hasReceivedInCategory) {
				guiInv.setItem(categoriesProcessed, itemStack);
			} else {
				guiInv.setItem(categoriesProcessed, barrier);
			}
			categoriesProcessed++;
		}
		return categoriesProcessed;
	}

	/**
	 * Populates the main GUI with NormalAchievement categories.
	 * 
	 * @param player
	 * @param guiInv
	 * @return
	 */
	private int populateMainGUIWithNormalAchievements(Player player, Inventory guiInv,
			int categoriesPreviouslyProcessed) {
		int categoriesProcessed = 0;

		// Display categories with normal achievements in GUI.
		for (NormalAchievements category : NormalAchievements.values()) {
			ItemStack itemStack = normalAchievementCategoryItems.get(category);
			String categoryName = category.toString();
			// Hide category if the user has defined an empty name for it or if it's in the disabled list.
			if (itemStack.getItemMeta().getDisplayName().length() == 0
					|| plugin.getDisabledCategorySet().contains(categoryName)
					|| configHideNoPermissionCategories && !player.hasPermission(category.toPermName())) {
				continue;
			}

			// Boolean corresponding to whether a player has received achievements in the current category. Used for
			// HideNotReceivedCategories config option.
			boolean hasReceivedInCategory = true;

			Set<String> configCategory = plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false);

			// Hide not yet unlocked categories: we must check whether the player has received at least one achievement
			// in the category.
			if (configHideNotReceivedCategories) {
				hasReceivedInCategory = false;
				// Iterate through all achievements in category.

				for (String level : configCategory) {
					/// Check whether player has received achievement.
					if (plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(),
							plugin.getPluginConfig().getString(categoryName + '.' + level + ".Name", ""))) {
						// At least one achievement was received in the current category; it is unlocked, can continue
						// processing.
						hasReceivedInCategory = true;
						break;
					}
				}
			}

			if (hasReceivedInCategory) {
				guiInv.setItem(categoriesPreviouslyProcessed + categoriesProcessed, itemStack);
			} else {
				guiInv.setItem(categoriesPreviouslyProcessed + categoriesProcessed, barrier);
			}
			categoriesProcessed++;
		}
		return categoriesProcessed;
	}

	/**
	 * Populates the main GUI with Commands category.
	 * 
	 * @param player
	 * @param guiInv
	 * @return
	 */
	private void populateMainGUIWithCommandsAchievements(Player player, Inventory guiInv,
			int categoriesPreviouslyProcessed) {
		// Hide Commands category if the user has defined an empty name for it or if it was disabled.
		if (commandsCategoryItem.getItemMeta().getDisplayName().length() == 0
				|| plugin.getDisabledCategorySet().contains("Commands")) {
			return;
		}
		// Boolean corresponding to whether a player has received achievements in the current category. Used for
		// HideNotReceivedCategories config option.
		boolean hasReceivedInCategory = true;

		Set<String> configCategory = plugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false);

		// Hide not yet unlocked categories: we must check whether the player has received at least one achievement in
		// the category.
		if (configHideNotReceivedCategories) {
			hasReceivedInCategory = false;
			// Iterate through all achievements in category.
			for (String ach : configCategory) {
				/// Check whether player has received achievement.
				if (plugin.getCacheManager().hasPlayerAchievement(player.getUniqueId(),
						plugin.getPluginConfig().getString("Commands." + ach + ".Name", ""))) {
					// At least one achievement was received in the current category; it is unlocked, can continue
					// processing.
					hasReceivedInCategory = true;
					break;
				}
			}
		}

		if (hasReceivedInCategory) {
			guiInv.setItem(categoriesPreviouslyProcessed, commandsCategoryItem);
		} else {
			guiInv.setItem(categoriesPreviouslyProcessed, barrier);
		}
	}

	/**
	 * Creates a GUI item for a given achievement.
	 * 
	 * @param inventory
	 * @param positionInGUI
	 * @param level
	 * @param statistic
	 * @param achName
	 * @param achMessage
	 * @param rewards
	 * @param date
	 * @param inelligibleSeriesItem
	 */
	private void createCategoryGUIItem(Inventory inventory, int positionInGUI, String level, long statistic,
			String achName, String achMessage, List<String> rewards, String date, boolean inelligibleSeriesItem,
			boolean playedTime) {
		// Display a clay block in the GUI, with a color depending on whether it was received or not, or whether
		// progress was started.
		ItemStack achItem;
		if (date != null) {
			// Achievement has been received.
			achItem = new ItemStack(Material.STAINED_CLAY, 1, (short) 5);
		} else if (statistic > 0) {
			// Player is making progress toward the achievement.
			achItem = new ItemStack(Material.STAINED_CLAY, 1, (short) 4);
		} else {
			// Player has not started progress.
			achItem = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
		}

		// Set name of the achievement. The style depends whether it was received or not and whether the user has set
		// obfuscateNotReceived and/or obfuscateProgressiveAchievements in the config.
		ItemMeta itemMeta = achItem.getItemMeta();
		if (date != null) {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', langListAchievementReceived + achName));
		} else if (configObfuscateNotReceived || (configObfuscateProgressiveAchievements && inelligibleSeriesItem)) {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					langListAchievementNotReceived + "&k" + REGEX_PATTERN.matcher(achName).replaceAll("")));
		} else {
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(
					langListAchievementNotReceived + "&o" + REGEX_PATTERN.matcher(achName).replaceAll(""))));
		}

		// Build the lore of the item.
		List<String> lore = buildLoreString(achMessage, level, rewards, date, statistic, inelligibleSeriesItem,
				playedTime);

		itemMeta.setLore(lore);
		achItem.setItemMeta(itemMeta);
		if (configNumberedItemsInList) {
			achItem.setAmount(positionInGUI % 64 + 1);
		}
		inventory.setItem(positionInGUI, achItem);
	}

	/**
	 * Creates the lore for the current achievement, containing information about the progress, date of reception,
	 * description, rewards.
	 * 
	 * @param achMessage
	 * @param level
	 * @param rewards
	 * @param date
	 * @param statistic
	 * @param inelligibleSeriesItem
	 * @return
	 */
	private List<String> buildLoreString(String achMessage, String level, List<String> rewards, String date,
			long statistic, boolean inelligibleSeriesItem, boolean playedTime) {
		List<String> lore = new ArrayList<>();
		lore.add("");

		if (date != null) {
			lore.add(langListDescription);
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + achMessage));
			lore.add("");
			lore.add(langListReception);
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + date));
			lore.add("");
		} else {
			lore.add(langListGoal);
			String strippedAchMessage = REGEX_PATTERN.matcher(achMessage).replaceAll("");
			if (configObfuscateNotReceived || (configObfuscateProgressiveAchievements && inelligibleSeriesItem)) {
				lore.add(ChatColor.translateAlternateColorCodes('&', "&8&k" + strippedAchMessage));
			} else {
				lore.add(ChatColor.translateAlternateColorCodes('&', "&8&o" + strippedAchMessage));
			}
			lore.add("");
			// Display progress bar. Achievements with statistic -1 correspond to Commands achievements, ignore.
			if (!configObfuscateNotReceived && statistic >= 0) {
				lore.add(langListProgress);
				lore.add(ChatColor.translateAlternateColorCodes('&',
						constructProgressBar(strippedAchMessage, level, statistic, playedTime)));
				lore.add("");
			}
		}

		// Add the rewards information.
		if (!rewards.isEmpty() && !configHideRewardDisplayInList) {
			lore.add(langListReward);
			String dot;
			if (date != null) {
				dot = StringEscapeUtils.unescapeJava("&r\u25CF ");
			} else {
				dot = StringEscapeUtils.unescapeJava("&8\u25CF &o");
			}
			for (String reward : rewards) {
				lore.add(ChatColor.translateAlternateColorCodes('&', dot + reward));
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
	 * @param playedTime
	 * @return progress bar
	 */
	private String constructProgressBar(String achMessage, String level, long statistic, boolean playedTime) {
		StringBuilder barDisplay = new StringBuilder("&8[");
		// Length of the progress bar; we make it the same size as Goal/Message.
		int textSize;
		// MinecraftFont essentially supports latin alphabet characters. If invalid characters are found just use
		// number of chars.
		if (FONT.isValid(achMessage)) {
			textSize = FONT.getWidth(REGEX_PATTERN.matcher(achMessage).replaceAll(""));
		} else {
			textSize = achMessage.length() * 3;
		}

		long levelInt = Long.parseLong(level);
		String middleText;
		double statisticDouble;
		if (playedTime) {
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
		middleText = "&8&o" + middleText;

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
			} else if (i < (((double) textSize / 2 - 1) * statisticDouble) / levelInt) {
				// Color: progress by user.
				barDisplay.append(configColor).append('|');
				i++;
			} else {
				// Grey: amount not yet reached by user.
				barDisplay.append("&8|");
				i++;
			}
		}

		barDisplay.append("&8]");
		return barDisplay.toString();
	}

	/**
	 * Inventory GUIs need a number of slots that is a multiple of 9. This simple function gets the smallest multiple of
	 * 9 greater than its input value, in order for the GUI to contain all of its elements with minimum empty space.
	 * Limit to 99, above the GUI is really messed up.
	 * 
	 * @param value
	 * @return closest multiple of 9 greater than value
	 */
	private int getClosestGreaterMultipleOf9(int value) {
		int multipleOfNine = 9;
		while (multipleOfNine < value && multipleOfNine <= 99) {
			multipleOfNine += 9;
		}
		return multipleOfNine;
	}
}
