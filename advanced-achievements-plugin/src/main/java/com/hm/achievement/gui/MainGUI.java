package com.hm.achievement.gui;

import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.NumberHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Represents the main GUI, corresponding to all the different available categories and their names.
 *
 * @author Pyves
 */
@Singleton
public class MainGUI implements Reloadable {

	private final CommentedYamlConfiguration mainConfig;
	private final CommentedYamlConfiguration langConfig;
	private final CacheManager cacheManager;
	private final Set<Category> disabledCategories;
	private final GUIItems guiItems;

	private boolean configHideNotReceivedCategories;
	private boolean configHideNoPermissionCategories;

	private String langListGUITitle;

	@Inject
	public MainGUI(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig, CacheManager cacheManager,
			Set<Category> disabledCategories, GUIItems guiItems) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.cacheManager = cacheManager;
		this.disabledCategories = disabledCategories;
		this.guiItems = guiItems;
	}

	@Override
	public void extractConfigurationParameters() {
		configHideNotReceivedCategories = mainConfig.getBoolean("HideNotReceivedCategories");
		configHideNoPermissionCategories = mainConfig.getBoolean("HideNoPermissionCategories");

		langListGUITitle = ChatColor.translateAlternateColorCodes('&', LangHelper.get(GuiLang.GUI_TITLE, langConfig));
	}

	/**
	 * Displays the main GUI to a player.
	 *
	 * @param player
	 */
	public void displayMainGUI(Player player) {
		int totalEnabledCategories = MultipleAchievements.values().length + NormalAchievements.values().length + 1
				- disabledCategories.size();
		AchievementInventoryHolder inventoryHolder = new AchievementInventoryHolder();
		int guiSize = NumberHelper.nextMultipleOf9(totalEnabledCategories);
		Inventory mainGUI = Bukkit.createInventory(inventoryHolder, guiSize, langListGUITitle);
		inventoryHolder.setInventory(mainGUI);

		int displayedSoFar = 0;
		for (Entry<OrderedCategory, ItemStack> achievementItem : guiItems.getOrderedAchievementItems().entrySet()) {
			Category category = achievementItem.getKey().getCategory();
			ItemStack item = achievementItem.getValue();
			if (shouldDisplayCategory(item, player, category)) {
				displayCategory(item, mainGUI, player, category, displayedSoFar);
				++displayedSoFar;
			}
		}

		// Display the main GUI to the player.
		player.openInventory(mainGUI);
	}

	/**
	 * Determines whether the category should be displayed in the GUI.
	 *
	 * @param item
	 * @param player
	 * @param category
	 * @return true if an item corresponding to the category should be added to the GUI
	 */
	private boolean shouldDisplayCategory(ItemStack item, Player player, Category category) {
		// Hide category if an empty name is defined for it, if it's disabled or if the player is missing permissions.
		return item.getItemMeta().getDisplayName().length() > 0 && !disabledCategories.contains(category)
				&& (!configHideNoPermissionCategories || player.hasPermission(category.toPermName()));
	}

	/**
	 * Displays an item corresponding to a category, or a barrier if the category should be hidden.
	 *
	 * @param item
	 * @param gui
	 * @param player
	 * @param category
	 * @param position
	 */
	private void displayCategory(ItemStack item, Inventory gui, Player player, Category category, int position) {
		if (category instanceof MultipleAchievements) {
			for (String subcategory : mainConfig.getShallowKeys(category.toString())) {
				if (!configHideNotReceivedCategories || hasReceivedInCategory(player, category + "." + subcategory)) {
					gui.setItem(position, item);
					return;
				}
			}
			gui.setItem(position, guiItems.getCategoryLock());
		} else if (!configHideNotReceivedCategories || hasReceivedInCategory(player, category.toString())) {
			gui.setItem(position, item);
		} else {
			gui.setItem(position, guiItems.getCategoryLock());
		}
	}

	/**
	 * Determines whether the player has received at least one achievement in the category or subcategory.
	 *
	 * @param player
	 * @param configPath
	 * @return true if the player has received at least one achievement in the category, false otherwise
	 */
	private boolean hasReceivedInCategory(Player player, String configPath) {
		for (String threshold : mainConfig.getShallowKeys(configPath)) {
			if (cacheManager.hasPlayerAchievement(player.getUniqueId(),
					mainConfig.getString(configPath + '.' + threshold + ".Name", ""))) {
				// At least one achievement was received in the current category: it is unlocked.
				return true;
			}
		}
		return false;
	}
}
