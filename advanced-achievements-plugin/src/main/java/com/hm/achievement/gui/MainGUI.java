package com.hm.achievement.gui;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.category.Category;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.config.AchievementMap;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.NumberHelper;

/**
 * Represents the main GUI, corresponding to all the different available categories and their names.
 *
 * @author Pyves
 */
@Singleton
public class MainGUI implements Reloadable {

	private final YamlConfiguration mainConfig;
	private final YamlConfiguration langConfig;
	private final CacheManager cacheManager;
	private final Set<Category> disabledCategories;
	private final GUIItems guiItems;
	private final AchievementMap achievementMap;

	private boolean configHideNotReceivedCategories;
	private boolean configHideNoPermissionCategories;

	private String langListGUITitle;
	private String langListAchievementsInCategoryPlural;
	private String langListAchievementInCategorySingular;

	@Inject
	public MainGUI(@Named("main") YamlConfiguration mainConfig, @Named("lang") YamlConfiguration langConfig,
			CacheManager cacheManager, Set<Category> disabledCategories, GUIItems guiItems, AchievementMap achievementMap) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.cacheManager = cacheManager;
		this.disabledCategories = disabledCategories;
		this.guiItems = guiItems;
		this.achievementMap = achievementMap;
	}

	@Override
	public void extractConfigurationParameters() {
		configHideNotReceivedCategories = mainConfig.getBoolean("HideNotReceivedCategories");
		configHideNoPermissionCategories = mainConfig.getBoolean("HideNoPermissionCategories");

		langListGUITitle = ChatColor.translateAlternateColorCodes('&', langConfig.getString("list-gui-title"));
		langListAchievementsInCategoryPlural = langConfig.getString("list-achievements-in-category-plural");
		langListAchievementInCategorySingular = langConfig.getString("list-achievements-in-category-singular");
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
		long receivedAmount = achievementMap.getForCategory(category).stream()
				.filter(a -> cacheManager.hasPlayerAchievement(player.getUniqueId(), a.getName()))
				.count();
		if (!configHideNotReceivedCategories || receivedAmount > 0) {
			int totalAmount = achievementMap.getForCategory(category).size();
			String message = totalAmount > 1 ? langListAchievementsInCategoryPlural : langListAchievementInCategorySingular;
			if (message.isEmpty()) {
				gui.setItem(position, item);
			} else {
				ItemStack itemWithLore = item.clone();
				ItemMeta itemMetaWithLore = itemWithLore.getItemMeta();
				String amountMessage = StringUtils.replaceOnce(message, "AMOUNT", receivedAmount + "/" + totalAmount);
				itemMetaWithLore.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&8" + amountMessage)));
				itemWithLore.setItemMeta(itemMetaWithLore);
				gui.setItem(position, itemWithLore);
			}
		} else {
			gui.setItem(position, guiItems.getCategoryLock());
		}
	}
}
