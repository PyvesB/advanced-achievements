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
import com.hm.achievement.language.Lang;

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
			new ItemStack(Material.EGG), new ItemStack(Material.RAW_FISH, 1, (short) 3), new ItemStack(Material.FLINT),
			new ItemStack(Material.MELON), new ItemStack(Material.SHEARS), new ItemStack(Material.MILK_BUCKET),
			new ItemStack(Material.EMERALD), new ItemStack(Material.ANVIL), new ItemStack(Material.ENCHANTMENT_TABLE),
			new ItemStack(Material.BED), new ItemStack(Material.EXP_BOTTLE), new ItemStack(Material.GLASS_BOTTLE),
			new ItemStack(Material.WATCH), new ItemStack(Material.HOPPER), new ItemStack(Material.STONE_HOE),
			new ItemStack(Material.INK_SACK, 1, (short) 15), new ItemStack(Material.LEASH),
			new ItemStack(Material.BREWING_STAND_ITEM), new ItemStack(Material.FIREWORK),
			new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.CARROT_STICK), new ItemStack(Material.SADDLE),
			new ItemStack(Material.MINECART), new ItemStack(Material.BOAT), new ItemStack(Material.BOOKSHELF) };

	public ListCommand(AdvancedAchievements plugin) {

		this.plugin = plugin;
		players = new HashMap<Player, Long>();
		hideNotReceivedCategories = plugin.getConfig().getBoolean("HideNotReceivedCategories", false);
		obfuscateNotReceived = plugin.getConfig().getBoolean("ObfuscateNotReceived", true);
		listTime = plugin.getConfig().getInt("TimeList", 0) * 1000;

		normalAchievementTypesLanguage = new String[] { Lang.LIST_CONNECTIONS.toString(), Lang.LIST_DEATHS.toString(),
				Lang.LIST_ARROWS.toString(), Lang.LIST_SNOWBALLS.toString(), Lang.LIST_EGGS.toString(),
				Lang.LIST_FISH.toString(), Lang.LIST_ITEMBREAKS.toString(), Lang.LIST_EATENITEMS.toString(),
				Lang.LIST_SHEAR.toString(), Lang.LIST_MILK.toString(), Lang.LIST_TRADES.toString(),
				Lang.LIST_ANVILS.toString(), Lang.LIST_ENCHANTMENTS.toString(), Lang.LIST_BEDS.toString(),
				Lang.LIST_MAXLEVEL.toString(), Lang.LIST_POTIONS.toString(), Lang.LIST_PLAYEDTIME.toString(),
				Lang.LIST_ITEMDROPS.toString(), Lang.LIST_HOEPLOWINGS.toString(), Lang.LIST_FERTILISING.toString(),
				Lang.LIST_TAMING.toString(), Lang.LIST_BREWING.toString(), Lang.LIST_FIREWORKS.toString(),
				Lang.LIST_DISTANCE_FOOT.toString(), Lang.LIST_DISTANCE_PIG.toString(),
				Lang.LIST_DISTANCE_HORSE.toString(), Lang.LIST_DISTANCE_MINECART.toString(),
				Lang.LIST_DISTANCE_BOAT.toString(), Lang.LIST_COMMANDS.toString() };

		multipleAchievementTypesLanguage = new String[] { Lang.LIST_PLACES.toString(), Lang.LIST_BREAKS.toString(),
				Lang.LIST_KILLS.toString(), Lang.LIST_CRAFTS.toString() };

	}

	/**
	 * Check is player hasn't done a list command too recently (with
	 * "too recently" being defined in configuration file).
	 */
	private boolean timeAuthorisedList(Player player) {

		if (player.isOp() || listTime == 0)
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
			Inventory guiInv = Bukkit.createInventory(null, 36, Lang.LIST_GUI_TITLE.toString());

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
					for (String section : plugin.getConfig()
							.getConfigurationSection(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i]).getKeys(false))
						// Iterate through all achievements in sub-category.
						for (String ach : plugin.getConfig()
								.getConfigurationSection(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i] + '.' + section)
								.getKeys(false))
							// Check if player has received achievement and
							// build message accordingly.
							if (plugin.getDb().hasPlayerAchievement(player, plugin.getConfig().getString(
									AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i] + '.' + section + '.' + ach + ".Name",
									""))) {
								numberInCategory++;
								lore.add(ChatColor.translateAlternateColorCodes('&',
										buildLoreString(
												"&f" + plugin.getConfig()
														.getString(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i] + '.'
																+ section + '.' + ach + ".Name", ""),
												ach,
												plugin.getReward()
														.getRewardType(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i]
																+ '.' + section + '.' + ach))));
							} else
								lore.add(ChatColor.translateAlternateColorCodes('&',
										buildLoreString(
												plugin.getConfig()
														.getString(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i] + '.'
																+ section + '.' + ach + ".Name", "")
												.replaceAll(REGEX_PATTERN.pattern(), ""), ach,
										plugin.getReward().getRewardType(AdvancedAchievements.MULTIPLE_ACHIEVEMENTS[i]
												+ '.' + section + '.' + ach))));
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
					for (String ach : plugin.getConfig()
							.getConfigurationSection(AdvancedAchievements.NORMAL_ACHIEVEMENTS[i]).getKeys(false))
						// Check if player has received achievement and
						// build message accordingly.
						if (plugin.getDb().hasPlayerAchievement(player, plugin.getConfig()
								.getString(AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.' + ach + ".Name", ""))) {
							numberInCategory++;
							lore.add(ChatColor.translateAlternateColorCodes('&',
									buildLoreString(
											"&f" + plugin.getConfig()
													.getString(AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.' + ach
															+ ".Name", ""),
											ach, plugin.getReward().getRewardType(
													AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.' + ach))));
						} else
							lore.add(ChatColor.translateAlternateColorCodes('&',
									buildLoreString(
											plugin.getConfig()
													.getString(AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.' + ach
															+ ".Name", "")
													.replaceAll(REGEX_PATTERN.pattern(), ""),
											ach, plugin.getReward().getRewardType(
													AdvancedAchievements.NORMAL_ACHIEVEMENTS[i] + '.' + ach))));
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
			player.sendMessage(
					plugin.getChatHeader() + Lang.LIST_DELAY.toString().replace("TIME", "" + listTime / 1000));
		}
	}

	/**
	 * Create achievement line for item lore.
	 */
	public String buildLoreString(String name, String level, String reward) {

		// Display reward with obfuscate effect.
		if (reward.length() != 0 && obfuscateNotReceived)
			return "&8§k" + name + " - " + Lang.LIST_AMOUNT + " " + level + " - " + Lang.LIST_REWARD + " " + reward;
		// Display reward without obfuscate effect.
		else if (reward.length() != 0 && !obfuscateNotReceived)
			return "&8§o" + name + " - " + Lang.LIST_AMOUNT + " " + level + " - " + Lang.LIST_REWARD + " " + reward;
		// Display obfuscate effect without reward.
		else if (obfuscateNotReceived)
			return "&8§k" + name + " - " + Lang.LIST_AMOUNT + " " + level;
		// Display no reward and no obfuscate effect.
		else
			return "&8§o" + name + " - " + Lang.LIST_AMOUNT + " " + level;

	}

	public HashMap<Player, Long> getPlayers() {

		return players;
	}

}
