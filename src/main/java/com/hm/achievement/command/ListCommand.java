package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import com.hm.achievement.utils.YamlManager;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MinecraftFont;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.particle.ReflectionUtils.PackageType;

/**
 * Class in charge of handling the /aach list command, which displays interactive GUIs.
 * 
 * @author Pyves
 */
public class ListCommand {

	private AdvancedAchievements plugin;
	private boolean hideNotReceivedCategories;
	private boolean obfuscateNotReceived;
	private boolean obfuscateProgressiveAchievements;
	private boolean hideRewardDisplay;
	private int listTime;
	private int version;

	// Corresponds to times at which players have entered list commands. Cooldown structure.
	private HashMap<Player, Long> players;

	// Array of category names from language file.
	private String[] normalAchievementCategoryNames;
	private String[] multipleAchievementCategoryNames;

	// Array of item stacks for items displayed in the GUI.
	private ItemStack[] multipleAchievementCategoryItems;
	private ItemStack[] normalAchievementCategoryItems;

	// Pattern to delete colors if achievement not yet received.
	private static final Pattern REGEX_PATTERN = Pattern.compile("&([a-f]|[0-9]){1}");

	// Minecraft font, used to get size information in the progress bar.
	private MinecraftFont FONT = MinecraftFont.Font;

	public ListCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		players = new HashMap<Player, Long>();
		// Load configuration parameters.
		hideNotReceivedCategories = plugin.getPluginConfig().getBoolean("HideNotReceivedCategories", false);
		obfuscateNotReceived = plugin.getPluginConfig().getBoolean("ObfuscateNotReceived", true);
		obfuscateProgressiveAchievements = plugin.getPluginConfig().getBoolean("ObfuscateProgressiveAchievements",
				false);
		hideRewardDisplay = plugin.getPluginConfig().getBoolean("HideRewardDisplayInList", false);
		listTime = plugin.getPluginConfig().getInt("TimeList", 0) * 1000;
		// Get array of category names from configuration.
		normalAchievementCategoryNames = new String[] {
				plugin.getPluginLang().getString("list-connections", "Connections"),
				plugin.getPluginLang().getString("list-deaths", "Number of Deaths"),
				plugin.getPluginLang().getString("list-arrows", "Arrows Shot"),
				plugin.getPluginLang().getString("list-snowballs", "Snowballs Thrown"),
				plugin.getPluginLang().getString("list-eggs", "Eggs Thrown"),
				plugin.getPluginLang().getString("list-fish", "Fish Caught"),
				plugin.getPluginLang().getString("list-itembreaks", "Items Broken"),
				plugin.getPluginLang().getString("list-eatenitems", "Items Eaten"),
				plugin.getPluginLang().getString("list-shear", "Sheeps Sheared"),
				plugin.getPluginLang().getString("list-milk", "Cows Milked"),
				plugin.getPluginLang().getString("list-trades", "Number of Trades"),
				plugin.getPluginLang().getString("list-anvils", "Anvils Used"),
				plugin.getPluginLang().getString("list-enchantments", "Items Enchanted"),
				plugin.getPluginLang().getString("list-beds", "Beds Entered"),
				plugin.getPluginLang().getString("list-maxlevel", "Max Level Reached"),
				plugin.getPluginLang().getString("list-potions", "Potions Consumed"),
				plugin.getPluginLang().getString("list-playedtime", "Time Played"),
				plugin.getPluginLang().getString("list-itemdrops", "Items Dropped"),
				plugin.getPluginLang().getString("list-hoeplowings", "Surface Plowed"),
				plugin.getPluginLang().getString("list-fertilising", "Plants Fertilised"),
				plugin.getPluginLang().getString("list-taming", "Animals Tamed"),
				plugin.getPluginLang().getString("list-brewing", "Potions Brewed"),
				plugin.getPluginLang().getString("list-fireworks", "Fireworks Launched"),
				plugin.getPluginLang().getString("list-musicdiscs", "Music Discs Played"),
				plugin.getPluginLang().getString("list-enderpearls", "Teleportations with Ender Pearls"),
				plugin.getPluginLang().getString("list-distance-foot", "Distance Travelled by Foot"),
				plugin.getPluginLang().getString("list-distance-pig", "Distance Travelled on a Pig"),
				plugin.getPluginLang().getString("list-distance-horse", "Distance Travelled on a Horse"),
				plugin.getPluginLang().getString("list-distance-minecart", "Distance Travelled in a Minecart"),
				plugin.getPluginLang().getString("list-distance-boat", "Distance Travelled in a Boat"),
				plugin.getPluginLang().getString("list-distance-gliding", "Distance Travelled with Elytra"),
				plugin.getPluginLang().getString("list-commands", "Other Achievements") };

		multipleAchievementCategoryNames = new String[] {
				plugin.getPluginLang().getString("list-places", "Blocks Placed"),
				plugin.getPluginLang().getString("list-breaks", "Blocks Broken"),
				plugin.getPluginLang().getString("list-kills", "Entities Killed"),
				plugin.getPluginLang().getString("list-crafts", "Items Crafted") };

		// Simple and fast check to retrieve Minecraft version. Might need to be updated depending on how the
		// Minecraft versions change in the future.
		version = Integer.parseInt(PackageType.getServerVersion().split("_")[1]);

		// Get list of item stacks for items displayed in the GUI, for multiple achievements.
		multipleAchievementCategoryItems = new ItemStack[] { new ItemStack(Material.STONE, 1, (short) 6),
				new ItemStack(Material.SMOOTH_BRICK, 1, (short) 2), new ItemStack(Material.BONE),
				new ItemStack(Material.WORKBENCH) };

		// Get list of item stacks for items displayed in the GUI, for multiple achievements.
		// Elytra and Grass paths only available in Minecraft 1.9+, we construct the list depending on the game version.
		if (version >= 9)
			normalAchievementCategoryItems = new ItemStack[] { new ItemStack(Material.BOOK_AND_QUILL),
					new ItemStack(Material.SKULL_ITEM), new ItemStack(Material.ARROW),
					new ItemStack(Material.SNOW_BALL), new ItemStack(Material.EGG),
					new ItemStack(Material.RAW_FISH, 1, (short) 2), new ItemStack(Material.FLINT),
					new ItemStack(Material.MELON), new ItemStack(Material.SHEARS), new ItemStack(Material.MILK_BUCKET),
					new ItemStack(Material.EMERALD), new ItemStack(Material.ANVIL),
					new ItemStack(Material.ENCHANTMENT_TABLE), new ItemStack(Material.BED),
					new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.GLASS_BOTTLE),
					new ItemStack(Material.WATCH), new ItemStack(Material.HOPPER), new ItemStack(Material.GRASS_PATH),
					new ItemStack(Material.INK_SACK, 1, (short) 15), new ItemStack(Material.LEASH),
					new ItemStack(Material.BREWING_STAND_ITEM), new ItemStack(Material.FIREWORK),
					new ItemStack(Material.JUKEBOX), new ItemStack(Material.ENDER_PEARL),
					new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.CARROT_STICK),
					new ItemStack(Material.SADDLE), new ItemStack(Material.MINECART), new ItemStack(Material.BOAT),
					new ItemStack(Material.ELYTRA), new ItemStack(Material.BOOKSHELF) };
		else
			normalAchievementCategoryItems = new ItemStack[] { new ItemStack(Material.BOOK_AND_QUILL),
					new ItemStack(Material.SKULL_ITEM), new ItemStack(Material.ARROW),
					new ItemStack(Material.SNOW_BALL), new ItemStack(Material.EGG),
					new ItemStack(Material.RAW_FISH, 1, (short) 2), new ItemStack(Material.FLINT),
					new ItemStack(Material.MELON), new ItemStack(Material.SHEARS), new ItemStack(Material.MILK_BUCKET),
					new ItemStack(Material.EMERALD), new ItemStack(Material.ANVIL),
					new ItemStack(Material.ENCHANTMENT_TABLE), new ItemStack(Material.BED),
					new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.GLASS_BOTTLE),
					new ItemStack(Material.WATCH), new ItemStack(Material.HOPPER), new ItemStack(Material.GRASS),
					new ItemStack(Material.INK_SACK, 1, (short) 15), new ItemStack(Material.LEASH),
					new ItemStack(Material.BREWING_STAND_ITEM), new ItemStack(Material.FIREWORK),
					new ItemStack(Material.JUKEBOX), new ItemStack(Material.ENDER_PEARL),
					new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.CARROT_STICK),
					new ItemStack(Material.SADDLE), new ItemStack(Material.MINECART), new ItemStack(Material.BOAT),
					new ItemStack(Material.BEDROCK), new ItemStack(Material.BOOKSHELF) };
	}

	/**
	 * Display the main GUI, corresponding to all the different available categories and their names. Users will then be
	 * able to click on an unlocked category to display another GUI with more specific details about the different
	 * achievements.
	 * 
	 * @param player
	 */
	public void createMainGUI(Player player) {

		if (timeAuthorisedList(player)) {
			// Create a new chest-like inventory; make it as small as possible while still containing all elements.
			Inventory guiInv = Bukkit.createInventory(null,
					getClosestGreaterMultipleOf9(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS.length
							+ AdvancedAchievements.NORMAL_ACHIEVEMENTS.length - plugin.getDisabledCategorySet().size()),
					ChatColor.translateAlternateColorCodes('&',
							plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

			// Boolean corresponding to whether a player has received achievements in the current category. Used for
			// HideNotReceivedCategories config option.
			boolean hasReceivedInCategory = true;
			// Total number of categories already displayed.
			int numberOfCategories = 0;

			YamlManager config = plugin.getPluginConfig();

			// Display categories with multiple sub-categories in GUI.
			for (int i = 0; i < AdvancedAchievements.MULTIPLE_ACHIEVEMENTS.length; i++) {
				// Hide category if the user has defined an empty name for it.
				if (multipleAchievementCategoryNames[i].length() != 0) {
					// Retrieve the name of the category, defined by the user.
					String category = AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i];
					// Ignore this category if it's in the disabled list.
					if (plugin.getDisabledCategorySet().contains(category)) {
						continue;
					}

					// Hide not yet unlocked categories: we must check whether the player has received at least one
					// achievement in the category.
					if (hideNotReceivedCategories) {
						hasReceivedInCategory = false;
						// Iterate through all sub-categories.
						for (String section : config.getConfigurationSection(category).getKeys(false)) {
							// Iterate through all achievements in sub-category.
							for (String ach : config.getConfigurationSection(category + '.' + section).getKeys(false)) {
								// Check whether player has received achievement.
								if (plugin.getDb().hasPlayerAchievement(player,
										config.getString(category + '.' + section + '.' + ach + ".Name", ""))) {
									// At least one achievement was received in the current category; it is unlocked,
									// can continue processing.
									hasReceivedInCategory = true;
									break;
								}
							}
							// No need to check next sub-category, break and move to next category.
							if (hasReceivedInCategory)
								break;
						}
					}

					ItemStack categoryItem;
					ItemMeta categoryMeta;

					if (hasReceivedInCategory || !hideNotReceivedCategories) {
						// Create item stack that will be displayed in the GUI, with its category name.
						categoryItem = multipleAchievementCategoryItems[i];
						categoryMeta = categoryItem.getItemMeta();
						categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
								"&8" + config.getString("ListAchievementFormat", "%ICON% %NAME% %ICON%")
										.replaceAll("%ICON%", plugin.getIcon()).replaceAll("%NAME%",
												multipleAchievementCategoryNames[i])));
					} else {
						// The player has not unlocked any achievements in the category: display barrier item with
						// message.
						categoryItem = new ItemStack(Material.BARRIER);
						categoryMeta = categoryItem.getItemMeta();
						categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
								"&8" + plugin.getPluginLang().getString("list-category-not-unlocked",
										"You have not yet unlocked this category.")));
					}

					// Set item in the GUI.
					categoryItem.setItemMeta(categoryMeta);
					guiInv.setItem(numberOfCategories, categoryItem);
					numberOfCategories++;
				}
			}

			// Display categories with normal achievements in GUI.
			for (int i = 0; i < AdvancedAchievements.NORMAL_ACHIEVEMENTS.length; i++) {
				// Hide category if the user has defined an empty name for it.
				if (normalAchievementCategoryNames[i].length() != 0) {
					// Retrieve the name of the category, defined by the user.
					String category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[i];
					// Ignore this category if it's in the disabled list.
					if (plugin.getDisabledCategorySet().contains(category)) {
						continue;
					}

					// Hide not yet unlocked categories: we must check whether the player has received at least one
					// achievement in the category.
					if (hideNotReceivedCategories) {
						hasReceivedInCategory = false;
						// Iterate through all achievements in category.
						for (String ach : config.getConfigurationSection(category).getKeys(false)) {
							/// Check whether player has received achievement.
							if (plugin.getDb().hasPlayerAchievement(player,
									config.getString(category + '.' + ach + ".Name", ""))) {
								// At least one achievement was received in the current category; it is unlocked,
								// can continue processing.
								hasReceivedInCategory = true;
								break;
							}
						}
					}

					ItemStack categoryItem;
					ItemMeta categoryMeta;

					if (hasReceivedInCategory || !hideNotReceivedCategories) {
						// Create item stack that will be displayed in the GUI, with its category name.
						categoryItem = normalAchievementCategoryItems[i];
						categoryMeta = categoryItem.getItemMeta();
						categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
								"&8" + config.getString("ListAchievementFormat", "%ICON% %NAME% %ICON%")
										.replaceAll("%ICON%", plugin.getIcon()).replaceAll("%NAME%",
												normalAchievementCategoryNames[i])));
					} else {
						// The player has not unlocked any achievements in the category: display barrier item with
						// message.
						categoryItem = new ItemStack(Material.BARRIER);
						categoryMeta = categoryItem.getItemMeta();
						categoryMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
								"&8" + plugin.getPluginLang().getString("list-category-not-unlocked",
										"You have not yet unlocked this category.")));
					}

					// Set item in the GUI.
					categoryItem.setItemMeta(categoryMeta);
					guiInv.setItem(numberOfCategories, categoryItem);
					numberOfCategories++;
				}
			}
			// Display GUI to the player.
			player.openInventory(guiInv);
		} else {
			// The player has already done a list command recently.
			player.sendMessage(plugin.getChatHeader() + plugin.getPluginLang()
					.getString("list-delay", "You must wait TIME seconds between each list command!")
					.replace("TIME", "" + listTime / 1000));
		}
	}

	/**
	 * Check if player hasn't done a list command too recently (with "too recently" being defined in configuration
	 * file).
	 * 
	 * @param player
	 * @return whether a player is authorised to perform the list command
	 */
	private boolean timeAuthorisedList(Player player) {

		// Player bypasses cooldown if he has full plugin permissions.
		if (player.hasPermission("achievement.*") || listTime == 0)
			return true;
		long currentTime = System.currentTimeMillis();
		long lastListTime = 0;
		if (players.containsKey(player))
			lastListTime = players.get(player);
		if (currentTime - lastListTime < listTime)
			return false;
		players.put(player, currentTime);
		return true;
	}

	/**
	 * Display a category GUI, containing all the achievements from a given category. This method is used for normal
	 * achievements.
	 * 
	 * @param clickedItem
	 * @param player
	 */
	public void createCategoryGUINormal(Material clickedItem, Player player) {

		String category;
		double statistic;

		// Match the item the player clicked on with a category and its database statistic.
		switch (clickedItem) {
			case BOOK_AND_QUILL:
				statistic = plugin.getDb().getConnectionsAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[0];
				break;
			case SKULL_ITEM:
				statistic = plugin.getPoolsManager().getPlayerDeathAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[1];
				break;
			case ARROW:
				statistic = plugin.getPoolsManager().getPlayerArrowAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[2];
				break;
			case SNOW_BALL:
				statistic = plugin.getPoolsManager().getPlayerSnowballAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[3];
				break;
			case EGG:
				statistic = plugin.getPoolsManager().getPlayerEggAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[4];
				break;
			case RAW_FISH:
				statistic = plugin.getPoolsManager().getPlayerFishAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[5];
				break;
			case FLINT:
				statistic = plugin.getPoolsManager().getPlayerItemBreakAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[6];
				break;
			case MELON:
				statistic = plugin.getPoolsManager().getPlayerEatenItemAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[7];
				break;
			case SHEARS:
				statistic = plugin.getPoolsManager().getPlayerShearAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[8];
				break;
			case MILK_BUCKET:
				statistic = plugin.getPoolsManager().getPlayerMilkAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[9];
				break;
			case EMERALD:
				statistic = plugin.getPoolsManager().getPlayerTradeAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[10];
				break;
			case ANVIL:
				statistic = plugin.getPoolsManager().getPlayerAnvilAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[11];
				break;
			case ENCHANTMENT_TABLE:
				statistic = plugin.getPoolsManager().getPlayerEnchantmentAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[12];
				break;
			case BED:
				statistic = plugin.getPoolsManager().getPlayerBedAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[13];
				break;
			case EXP_BOTTLE:
				statistic = plugin.getPoolsManager().getPlayerXPAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[14];
				break;
			case GLASS_BOTTLE:
				statistic = plugin.getPoolsManager().getPlayerConsumedPotionAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[15];
				break;
			case WATCH:
				statistic = (double) (System.currentTimeMillis()
						- plugin.getConnectionListener().getJoinTime().getOrDefault(player.getUniqueId().toString(), 0L)
						+ plugin.getConnectionListener().getPlayTime().getOrDefault(player.getUniqueId().toString(),
								0L))
						/ 3600000L;
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[16];
				break;
			case HOPPER:
				statistic = plugin.getPoolsManager().getPlayerDropAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[17];
				break;
			case GRASS:
				statistic = plugin.getPoolsManager().getPlayerHoePlowingAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[18];
				break;
			case INK_SACK:
				statistic = plugin.getPoolsManager().getPlayerFertiliseAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[19];
				break;
			case LEASH:
				statistic = plugin.getPoolsManager().getPlayerTameAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[20];
				break;
			case BREWING_STAND_ITEM:
				statistic = plugin.getPoolsManager().getPlayerBrewingAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[21];
				break;
			case FIREWORK:
				statistic = plugin.getPoolsManager().getPlayerFireworkAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[22];
				break;
			case JUKEBOX:
				statistic = plugin.getPoolsManager().getPlayerMusicDiscAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[23];
				break;
			case ENDER_PEARL:
				statistic = plugin.getPoolsManager().getPlayerEnderPearlAmount(player);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[24];
				break;
			case LEATHER_BOOTS:
				statistic = plugin.getAchieveDistanceRunnable().getAchievementDistancesFoot()
						.getOrDefault(player.getUniqueId().toString(), 0);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[25];
				break;
			case CARROT_STICK:
				statistic = plugin.getAchieveDistanceRunnable().getAchievementDistancesPig()
						.getOrDefault(player.getUniqueId().toString(), 0);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[26];
				break;
			case SADDLE:
				statistic = plugin.getAchieveDistanceRunnable().getAchievementDistancesHorse()
						.getOrDefault(player.getUniqueId().toString(), 0);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[27];
				break;
			case MINECART:
				statistic = plugin.getAchieveDistanceRunnable().getAchievementDistancesMinecart()
						.getOrDefault(player.getUniqueId().toString(), 0);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[28];
				break;
			case BOAT:
				statistic = plugin.getAchieveDistanceRunnable().getAchievementDistancesBoat()
						.getOrDefault(player.getUniqueId().toString(), 0);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[29];
				break;
			case BEDROCK:
				statistic = plugin.getAchieveDistanceRunnable().getAchievementDistancesGliding()
						.getOrDefault(player.getUniqueId().toString(), 0);
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[30];
				break;
			case BOOKSHELF:
				statistic = -1;
				category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[31];
				break;
			// Default case can happen on Minecraft 1.9+.
			default:
				statistic = -1;
				category = "";
				break;
		}

		// Items used for 1.9+ version of Minecraft. This corresponds to the default case of the above switch.
		if (category.equals("") && version >= 9) {
			switch (clickedItem) {
				case ELYTRA:
					statistic = plugin.getAchieveDistanceRunnable().getAchievementDistancesGliding()
							.getOrDefault(player.getUniqueId(), 0);
					category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[30];
					break;
				case GRASS_PATH:
					statistic = plugin.getPoolsManager().getPlayerHoePlowingAmount(player);
					category = AdvancedAchievements.NORMAL_ACHIEVEMENTS[18];
					break;
				// Default case can not happen.
				default:
					statistic = -1;
					category = "";
					break;
			}
		}

		YamlManager config = plugin.getPluginConfig();

		// Create a new chest-like inventory; make it as small as possible while still containing all achievements.
		Inventory inventory = Bukkit.createInventory(null,
				getClosestGreaterMultipleOf9(config.getConfigurationSection(category).getKeys(false).size() + 1),
				ChatColor.translateAlternateColorCodes('&',
						plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

		int positionInGUI = 0;

		String previousItemDate = null;
		Integer previousItemGoal = 0;
		// Populate the GUI with all of the achievements for the category.
		for (String ach : plugin.getPluginConfig().getConfigurationSection(category).getKeys(false)) {

			// ach is the threshold for obtaining this achievement
			// Convert it to an integer
			Integer currentItemGoal = Ints.tryParse(ach);

			String achName = plugin.getPluginConfig().getString(category + '.' + ach + ".Name", "");
			String displayName = plugin.getPluginConfig().getString(category + '.' + ach + ".DisplayName", "");
			String nameToShowUser;

			if (Strings.isNullOrEmpty(displayName)) {
				// Use the achievement key name (this name is used in the achievements table in the database).
				nameToShowUser = achName;
			} else {
				// Display name is defined; use it.
				nameToShowUser = displayName;
			}

			String achMessage;
			String goal = config.getString(category + '.' + ach + ".Goal", "");
			if (Strings.isNullOrEmpty(goal)) {
				// Show the achievement message below the achievement name.
				achMessage = config.getString(category + '.' + ach + ".Message", "");
			} else {
				// Show the goal below the achievement name.
				achMessage = goal;
			}

			ArrayList<String> rewards = plugin.getReward().getRewardType(category + '.' + ach);
			String date = plugin.getDb().getPlayerAchievementDate(player, achName);

			boolean inelligibleSeriesItem;

			if (statistic == -1 || positionInGUI == 0 || date != null || previousItemDate != null) {
				// Commands achievement or
				// first achievement in the category or
				// achievement has been completed or
				// previous achievement has been completed.
				inelligibleSeriesItem = false;
			} else {
				// Check whether this achievement cannot be completed until the previous one is completed.
				if (currentItemGoal > previousItemGoal)
					inelligibleSeriesItem = true;
				else
					inelligibleSeriesItem = false;
			}

			createGUIItem(inventory, positionInGUI, ach, statistic, nameToShowUser, achMessage, rewards, date,
					inelligibleSeriesItem);
			positionInGUI++;

			previousItemDate = date;
			previousItemGoal = currentItemGoal;
		}

		// Add "back button" item.
		ItemStack achItem = new ItemStack(Material.PAPER);
		ItemMeta connectionsMeta = achItem.getItemMeta();
		connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				StringEscapeUtils.unescapeJava(plugin.getPluginLang().getString("list-back-message", "&7Back"))));
		achItem.setItemMeta(connectionsMeta);
		inventory.setItem(positionInGUI, achItem);

		// Display category GUI.
		player.openInventory(inventory);

	}

	/**
	 * Display the a category GUI, containing all the achievements from a given category. This method is used for
	 * multiple achievements, in other words those based on sub-categories.
	 * 
	 * @param item
	 * @param player
	 */
	public void createCategoryGUIMultiple(Material item, Player player) {

		String category;

		// Match the item the player clicked on with a category.
		switch (item) {
			case STONE:
				category = AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[0];
				break;
			case SMOOTH_BRICK:
				category = AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[1];
				break;
			case BONE:
				category = AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[2];
				break;
			case WORKBENCH:
				category = AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[3];
				break;
			default:
				category = "";
				break;
		}

		YamlManager config = plugin.getPluginConfig();
		ConfigurationSection categoryConfig = config.getConfigurationSection(category);

		int totalAchievementsInCategory = 0;

		// Retrieve total number of achievements in the sub-categories of the category.
		for (String section : categoryConfig.getKeys(false)) {
			ConfigurationSection subcategoryConfig = config.getConfigurationSection(category + '.' + section);
			totalAchievementsInCategory += subcategoryConfig.getKeys(false).size();
		}

		// Create a new chest-like inventory; make it as small as possible while still containing all achievements.
		Inventory inventory = Bukkit.createInventory(null,
				getClosestGreaterMultipleOf9(totalAchievementsInCategory + 1), ChatColor.translateAlternateColorCodes(
						'&', plugin.getPluginLang().getString("list-gui-title", "&5&lAchievements List")));

		int positionInGUI = 0;

		// Match the item the player clicked on with its database statistic.
		for (String section : categoryConfig.getKeys(false)) {

			int statistic;
			switch (item) {
				case STONE:
					statistic = plugin.getPoolsManager().getPlayerBlockPlaceAmount(player, section);
					break;
				case SMOOTH_BRICK:
					statistic = plugin.getPoolsManager().getPlayerBlockBreakAmount(player, section);
					break;
				case BONE:
					statistic = plugin.getPoolsManager().getPlayerKillAmount(player, section);
					break;
				case WORKBENCH:
					statistic = plugin.getPoolsManager().getPlayerCraftAmount(player, section);
					break;
				default:
					statistic = -1;
					break;
			}

			String previousItemDate = null;
			int previousItemGoal = 0;
			int subcategoryIndex = 0;

			// Populate GUI with all the achievements for the current sub-category.
			ConfigurationSection subcategoryConfig = config.getConfigurationSection(category + '.' + section);
			for (String level : subcategoryConfig.getKeys(false)) {
				// level is the threshold for obtaining this achievement
				// Convert it to an integer.
				int currentItemGoal = Ints.tryParse(level);

				String achName = config.getString(category + '.' + section + '.' + level + ".Name", "");
				String displayName = config.getString(category + '.' + section + '.' + level + ".DisplayName", "");
				String nameToShowUser;

				if (Strings.isNullOrEmpty(displayName)) {
					// Use the achievement key name (this name is used in the achievements table in the database).
					nameToShowUser = achName;
				} else {
					// Display name is defined; use it.
					nameToShowUser = displayName;
				}

				String achMessage;
				String goal = config.getString(category + '.' + section + '.' + level + ".Goal", "");
				if (Strings.isNullOrEmpty(goal)) {
					// Show the achievement message below the achievement name.
					achMessage = config.getString(category + '.' + section + '.' + level + ".Message", "");
				} else {
					// Show the goal below the achievement name.
					achMessage = goal;
				}

				ArrayList<String> rewards = plugin.getReward().getRewardType(category + '.' + section + '.' + level);
				String date = plugin.getDb().getPlayerAchievementDate(player, achName);

				boolean inelligibleSeriesItem;

				if (subcategoryIndex == 0 || date != null || previousItemDate != null) {
					// First achievement in the category or
					// achievement has been completed or
					// previous achievement has been completed.
					inelligibleSeriesItem = false;
				} else {
					// Check whether this achievement cannot be completed until the previous one is completed.
					if (currentItemGoal > previousItemGoal)
						inelligibleSeriesItem = true;
					else
						inelligibleSeriesItem = false;
				}

				createGUIItem(inventory, positionInGUI, level, statistic, nameToShowUser, achMessage, rewards, date,
						inelligibleSeriesItem);
				positionInGUI++;

				previousItemDate = date;
				previousItemGoal = currentItemGoal;
				subcategoryIndex++;
			}
		}

		// Add "back button" item.
		ItemStack achItem = new ItemStack(Material.PAPER);
		ItemMeta connectionsMeta = achItem.getItemMeta();
		connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
				StringEscapeUtils.unescapeJava(plugin.getPluginLang().getString("list-back-message", "&7Back"))));
		achItem.setItemMeta(connectionsMeta);
		inventory.setItem(positionInGUI, achItem);

		// Display category GUI.
		player.openInventory(inventory);
	}

	/**
	 * Create a GUI item for a given achievement.
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
	private void createGUIItem(Inventory inventory, int positionInGUI, String level, double statistic, String achName,
			String achMessage, ArrayList<String> rewards, String date, boolean inelligibleSeriesItem) {

		// Display a clay block in the GUI, with a color depending on whether it was received or not, or whether
		// progress was
		// started.
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
		ItemMeta connectionsMeta = achItem.getItemMeta();
		if (date != null)
			connectionsMeta
					.setDisplayName(ChatColor.translateAlternateColorCodes('&',
							StringEscapeUtils.unescapeJava(
									plugin.getPluginLang().getString("list-achievement-received", "&a\u2713&f "))
									+ achName));
		else if (obfuscateNotReceived || (obfuscateProgressiveAchievements && inelligibleSeriesItem))
			connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					StringEscapeUtils.unescapeJava(
							plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2717&8 ")) + "&k"
							+ achName.replaceAll(REGEX_PATTERN.pattern(), "")));
		else
			connectionsMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
					StringEscapeUtils.unescapeJava(
							plugin.getPluginLang().getString("list-achievement-not-received", "&4\u2717&8 ") + "&o"
									+ achName.replaceAll(REGEX_PATTERN.pattern(), ""))));

		// Build the lore of the item.
		ArrayList<String> lore = buildLoreString(achMessage, level, rewards, date, statistic, inelligibleSeriesItem);

		connectionsMeta.setLore(lore);
		achItem.setItemMeta(connectionsMeta);
		inventory.setItem(positionInGUI, achItem);
	}

	/**
	 * Create the lore for the current achievement, containing information about the progress, date of reception,
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
	private ArrayList<String> buildLoreString(String achMessage, String level, ArrayList<String> rewards, String date,
			double statistic, boolean inelligibleSeriesItem) {

		ArrayList<String> lore = new ArrayList<String>();

		// Set description of the achievement. The style depends whether it was received or not and whether the user has
		// set obfuscateNotReceived in the config.
		if (date != null)
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + achMessage));
		else if (obfuscateNotReceived || (obfuscateProgressiveAchievements && inelligibleSeriesItem))
			lore.add(ChatColor.translateAlternateColorCodes('&',
					"&8&k" + achMessage.replaceAll(REGEX_PATTERN.pattern(), "")));
		else
			lore.add(ChatColor.translateAlternateColorCodes('&',
					"&8&o" + achMessage.replaceAll(REGEX_PATTERN.pattern(), "")));
		lore.add("");

		// Display date if the achievement was received, or progress bar otherwise; achievements with statistic -1
		// correspond to
		// Commands achievements, ignore if they weren't yet received.
		if (date != null) {
			lore.add(ChatColor.translateAlternateColorCodes('&', "&r" + date.replaceAll(REGEX_PATTERN.pattern(), "")));
			lore.add("");
		} else if (!obfuscateNotReceived && statistic >= 0) {
			StringBuilder barDisplay = new StringBuilder("&7[");
			// Length of the progress bar; we make it the same size as Goal/Message.
			int textSize;
			// MinecraftFont essentially supports latin alphabet characters. If invalid characters are found just use
			// number of chars.
			if (FONT.isValid(achMessage))
				textSize = FONT.getWidth(achMessage.replaceAll(REGEX_PATTERN.pattern(), ""));
			else
				textSize = (achMessage.replaceAll(REGEX_PATTERN.pattern(), "")).length() * 3;
			for (int i = 1; i < textSize / 2; i++) {
				if (i < ((textSize / 2 - 1) * statistic) / Integer.valueOf(level)) {
					barDisplay.append(plugin.getColor()).append('|');
				} else {
					barDisplay.append("&8|");
				}
			}

			barDisplay.append("&7]");
			lore.add(ChatColor.translateAlternateColorCodes('&', barDisplay.toString()));
			lore.add("");
		}

		// Add the rewards information.
		if (!rewards.isEmpty() && !hideRewardDisplay) {
			if (date != null) {
				lore.add(ChatColor.translateAlternateColorCodes('&',
						"&r" + plugin.getPluginLang().getString("list-reward", "Reward: ")));
				for (String reward : rewards)
					lore.add(ChatColor.translateAlternateColorCodes('&', "&r- " + reward));
			} else {
				lore.add(ChatColor.translateAlternateColorCodes('&',
						"&o&8" + plugin.getPluginLang().getString("list-reward", "Reward: ")));
				for (String reward : rewards)
					lore.add(ChatColor.translateAlternateColorCodes('&', "&o&8- " + reward));
			}
		}
		return lore;
	}

	/**
	 * Inventory GUIs need a number of slots that is a multiple of 9. This simple function gets the smallest multiple of
	 * 9 greater than its input value, in order for the GUI to contain all of its elements with minimum empty space.
	 * 
	 * @param value
	 * @return closest multiple of 9 greater than value
	 */
	private int getClosestGreaterMultipleOf9(int value) {

		int multipleOfNine = 9;
		while (multipleOfNine < value && multipleOfNine <= 90)
			multipleOfNine += 9;
		return multipleOfNine;
	}

	/**
	 * Retrieve cooldown structure.
	 * 
	 * @return list cooldown structure
	 */
	public HashMap<Player, Long> getPlayers() {

		return players;
	}

}
