package com.hm.achievement.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.AdvancedAchievements;

public class ListCommand {

	private AdvancedAchievements plugin;
	private boolean hideNotReceivedCategories;
	private boolean obfuscateNotReceived;
	private int listTime;

	// Corresponds to times at which players have entered list commands.
	private HashMap<Player, Long> players;

	// Messages for language file.
	private String[] normalAchievementTypesLanguage;
	private String[] multipleAchievementTypesLanguage;

	// Pattern to delete colors if achievement not yet received.
	private static final Pattern REGEX_PATTERN = Pattern.compile("&([a-f]|[0-9]){1}");

	// Get lists of item stacks for items displayed in the GUI.
	private static final ItemStack[] MULTIPLE_ACHIEVEMENT_TYPES_MATERIAL = { new ItemStack(Material.SMOOTH_BRICK),
			new ItemStack(Material.SMOOTH_BRICK, 1, (short) 2), new ItemStack(Material.BONE),
			new ItemStack(Material.WORKBENCH) };
	private static final ItemStack[] NORMAL_ACHIEVEMENT_TYPES_MATERIAL = { new ItemStack(Material.BOOK_AND_QUILL),
			new ItemStack(Material.SKULL_ITEM), new ItemStack(Material.ARROW), new ItemStack(Material.SNOW_BALL),
			new ItemStack(Material.EGG), new ItemStack(Material.RAW_FISH, 1, (short) 2), new ItemStack(Material.FLINT),
			new ItemStack(Material.MELON), new ItemStack(Material.SHEARS), new ItemStack(Material.MILK_BUCKET),
			new ItemStack(Material.EMERALD), new ItemStack(Material.ANVIL), new ItemStack(Material.ENCHANTMENT_TABLE),
			new ItemStack(Material.BED), new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.GLASS_BOTTLE),
			new ItemStack(Material.WATCH), new ItemStack(Material.HOPPER), new ItemStack(Material.GOLD_HOE),
			new ItemStack(Material.INK_SACK, 1, (short) 15), new ItemStack(Material.LEASH),
			new ItemStack(Material.BREWING_STAND_ITEM), new ItemStack(Material.FIREWORK),
			new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.CARROT_STICK), new ItemStack(Material.SADDLE),
			new ItemStack(Material.MINECART), new ItemStack(Material.BOAT), new ItemStack(Material.BOOKSHELF) };

	public ListCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		players = new HashMap<Player, Long>();
		hideNotReceivedCategories = plugin.getPluginConfig().getBoolean("HideNotReceivedCategories", false);
		obfuscateNotReceived = plugin.getPluginConfig().getBoolean("ObfuscateNotReceived", true);
		listTime = plugin.getPluginConfig().getInt("TimeList", 0) * 1000;

		normalAchievementTypesLanguage = new String[] {
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
				plugin.getPluginLang().getString("list-distance-foot", "Distance Travelled by Foot"),
				plugin.getPluginLang().getString("list-distance-pig", "Distance Travelled on a Pig"),
				plugin.getPluginLang().getString("list-distance-horse", "Distance Travelled on a Horse"),
				plugin.getPluginLang().getString("list-distance-minecart", "Distance Travelled in a Minecart"),
				plugin.getPluginLang().getString("list-distance-boat", "Distance Travelled in a Boat"),
				plugin.getPluginLang().getString("list-commands", "Other Achievements") };

		multipleAchievementTypesLanguage = new String[] {
				plugin.getPluginLang().getString("list-places", "Blocks Placed"),
				plugin.getPluginLang().getString("list-breaks", "Blocks Broken"),
				plugin.getPluginLang().getString("list-kills", "Entities Killed"),
				plugin.getPluginLang().getString("list-crafts", "Items Crafted") };

	}

	/**
	 * Check is player hasn't done a list command too recently (with
	 * "too recently" being defined in configuration file).
	 */
	private boolean timeAuthorisedList(Player player) {

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
	 * Display name of received achievements and name of missing achievements
	 * (goes through the entire config file) in a GUI. Does not display category
	 * is language file string is empty.
	 */
	public void getList(Player player) {

		if (timeAuthorisedList(player)) {

			// Create a new chest-like inventory.
			Inventory guiInv = Bukkit.createInventory(null, 36,
					plugin.getPluginLang().getString("list-gui-title", "&5§lAchievements List"));

			// Number of achievements in current category.
			int numberInCategory = 0;
			// Total number of categories displayed.
			int numberOfCategories = 0;

			// Build list of achievements with multiple sub-categories in GUI.
			for (int i = 0; i < AdvancedAchievements.MULTIPLE_ACHIEVEMENTS.length; i++)
				if (multipleAchievementTypesLanguage[i].length() != 0) {
					// Create item stack that will be displayed in the GUI.
					ItemStack connections = MULTIPLE_ACHIEVEMENT_TYPES_MATERIAL[i];
					ItemMeta connectionsMeta = connections.getItemMeta();
					ArrayList<String> lore = new ArrayList<String>();
					// Iterate through all sub-categories in achievement
					// category.
					for (String section : plugin.getPluginConfig()
							.getConfigurationSection(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i]).getKeys(false))
						// Iterate through all achievements in sub-category.
						for (String ach : plugin.getPluginConfig()
								.getConfigurationSection(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i] + '.' + section)
								.getKeys(false))
							// Check if player has received achievement and
							// build message accordingly.
							if (plugin.getDb().hasPlayerAchievement(player, plugin.getPluginConfig().getString(
									AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i] + '.' + section + '.' + ach + ".Name",
									""))) {
								numberInCategory++;
								lore.add(ChatColor.translateAlternateColorCodes('&',
										buildLoreString(
												"&f" + plugin.getPluginConfig()
														.getString(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i] + '.'
																+ section + '.' + ach + ".Name", ""),
												ach,
												plugin.getReward()
														.getRewardType(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i]
																+ '.' + section + '.' + ach),
												i)));
							} else
								lore.add(ChatColor.translateAlternateColorCodes('&',
										buildLoreString(
												plugin.getPluginConfig()
														.getString(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i] + '.'
																+ section + '.' + ach + ".Name", "")
														.replaceAll(REGEX_PATTERN.pattern(), ""),
												ach,
												plugin.getReward()
														.getRewardType(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i]
																+ '.' + section + '.' + ach),
												i)));
					// Set lore for the current category item in GUI.
					if (lore.size() > 0 && (numberInCategory != 0 || !hideNotReceivedCategories)) {
						connectionsMeta.setDisplayName(
								ChatColor.translateAlternateColorCodes('&', "&7" + plugin.getIcon() + " "
										+ multipleAchievementTypesLanguage[i] + " " + " &7" + plugin.getIcon() + " "));
						connectionsMeta.setLore(lore);
						connections.setItemMeta(connectionsMeta);

						guiInv.setItem(numberOfCategories, connections);
						numberOfCategories++;

					}
					lore.clear();
					numberInCategory = 0;
				}

			// Build list of normal achievements in GUI.
			for (int i = 0; i < AdvancedAchievements.NORMAL_ACHIEVEMENTS.length; i++)
				if (normalAchievementTypesLanguage[i].length() != 0) {
					// Create item stack that will be displayed in the GUI.
					ItemStack connections = NORMAL_ACHIEVEMENT_TYPES_MATERIAL[i];
					ItemMeta connectionsMeta = connections.getItemMeta();
					ArrayList<String> lore = new ArrayList<String>();
					// Iterate through all achievements in category.
					for (String ach : plugin.getPluginConfig()
							.getConfigurationSection(AdvancedAchievements.NORMAL_ACHIEVEMENTS[i]).getKeys(false))
						// Check if player has received achievement and
						// build message accordingly.
						if (plugin.getDb().hasPlayerAchievement(player, plugin.getPluginConfig()
								.getString(AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.' + ach + ".Name", ""))) {
							numberInCategory++;
							lore.add(
									ChatColor.translateAlternateColorCodes('&',
											buildLoreString(
													"&f" + plugin.getPluginConfig()
															.getString(AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.'
																	+ ach + ".Name", ""),
													ach,
													plugin.getReward().getRewardType(
															AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.' + ach),
													i)));
						} else
							lore.add(ChatColor.translateAlternateColorCodes('&',
									buildLoreString(
											plugin.getPluginConfig()
													.getString(AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.' + ach
															+ ".Name", "")
													.replaceAll(REGEX_PATTERN.pattern(), ""),
											ach, plugin.getReward().getRewardType(
													AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.' + ach),
											i)));
					// Set lore for the current category item in GUI.
					if (lore.size() > 0 && (numberInCategory != 0 || !hideNotReceivedCategories)) {
						connectionsMeta.setDisplayName(
								ChatColor.translateAlternateColorCodes('&', "&7" + plugin.getIcon() + " "
										+ normalAchievementTypesLanguage[i] + " " + " &7" + plugin.getIcon() + " "));
						connectionsMeta.setLore(lore);
						connections.setItemMeta(connectionsMeta);

						guiInv.setItem(numberOfCategories, connections);
						numberOfCategories++;

					}
					lore.clear();
					numberInCategory = 0;
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
	 * Create achievement line for item lore.
	 */
	public String buildLoreString(String name, String level, String reward, int number) {

		// For command achievements, no level set.
		if (number == normalAchievementTypesLanguage.length - 1) {
			// Display reward with obfuscate effect.
			if (reward.length() != 0 && obfuscateNotReceived)
				return "&8§k" + name + " |" + plugin.getPluginLang().getString("list-reward", " Reward:") + " "
						+ reward;
			// Display reward without obfuscate effect.
			else if (reward.length() != 0 && !obfuscateNotReceived)
				return "&8§o" + name + " |" + plugin.getPluginLang().getString("list-reward", " Reward:") + " "
						+ reward;
			// Display obfuscate effect without reward.
			else if (obfuscateNotReceived)
				return "&8§k" + name;
			// Display no reward and no obfuscate effect.
			else
				return "&8§o" + name;
		}

		// Display reward with obfuscate effect.
		if (reward.length() != 0 && obfuscateNotReceived)
			return "&8§k" + name + " |" + plugin.getPluginLang().getString("list-amount", " Lvl:") + " " + level + " |"
					+ plugin.getPluginLang().getString("list-reward", " Reward:") + " " + reward;
		// Display reward without obfuscate effect.
		else if (reward.length() != 0 && !obfuscateNotReceived)
			return "&8§o" + name + " |" + plugin.getPluginLang().getString("list-amount", " Lvl:") + " " + level + " |"
					+ plugin.getPluginLang().getString("list-reward", " Reward:") + " " + reward;
		// Display obfuscate effect without reward.
		else if (obfuscateNotReceived)
			return "&8§k" + name + " |" + plugin.getPluginLang().getString("list-amount", " Lvl:") + " " + level;
		// Display no reward and no obfuscate effect.
		else
			return "&8§o" + name + " |" + plugin.getPluginLang().getString("list-amount", " Lvl:") + " " + level;

	}

	public HashMap<Player, Long> getPlayers() {

		return players;
	}

}
