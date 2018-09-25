package com.hm.achievement.gui;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.db.CacheManager;
import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Abstract class in charge of factoring out common functionality for the GUIs.
 *
 * @author Pyves
 */
public abstract class AbstractGUI implements Reloadable {

	final Map<MultipleAchievements, ItemStack> multipleAchievementItems = new EnumMap<>(MultipleAchievements.class);
	final Map<NormalAchievements, ItemStack> normalAchievementItems = new EnumMap<>(NormalAchievements.class);
	final CommentedYamlConfiguration mainConfig;
	final CommentedYamlConfiguration langConfig;
	final CacheManager cacheManager;

	ItemStack commandsAchievementsItem;

	private final CommentedYamlConfiguration guiConfig;
	private final MaterialHelper materialHelper;

	private String configListAchievementFormat;
	private String configIcon;

	private String langListAchievementsInCategoryPlural;
	private String langListAchievementInCategorySingular;

	AbstractGUI(CommentedYamlConfiguration mainConfig, CommentedYamlConfiguration langConfig,
			CommentedYamlConfiguration guiConfig, CacheManager cacheManager, MaterialHelper materialHelper) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.guiConfig = guiConfig;
		this.cacheManager = cacheManager;
		this.materialHelper = materialHelper;
	}

	@Override
	public void extractConfigurationParameters() {
		configListAchievementFormat = "&8" + mainConfig.getString("ListAchievementFormat", "%ICON% %NAME% %ICON%");
		configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon", "\u2618"));

		langListAchievementsInCategoryPlural = LangHelper.get(GuiLang.ACHIEVEMENTS_IN_CATEGORY_PLURAL, langConfig);
		langListAchievementInCategorySingular = LangHelper.get(GuiLang.ACHIEVEMENTS_IN_CATEGORY_SINGULAR, langConfig);

		// Prepare item stacks displayed in the GUI for Multiple achievements.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();
			// Sum all achievements in the sub-categories of this category.
			int totalAchievements = 0;
			for (String subcategory : mainConfig.getShallowKeys(categoryName)) {
				totalAchievements += mainConfig.getShallowKeys(categoryName + '.' + subcategory).size();
			}
			ItemStack itemStack = createItemStack(categoryName);
			buildItemLore(itemStack, LangHelper.get(category, langConfig), totalAchievements);
			multipleAchievementItems.put(category, itemStack);
		}

		// Prepare item stacks displayed in the GUI for Normal achievements.
		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();
			ItemStack itemStack = createItemStack(categoryName);
			buildItemLore(itemStack, LangHelper.get(category, langConfig), mainConfig.getShallowKeys(categoryName).size());
			normalAchievementItems.put(category, itemStack);
		}

		// Prepare item stack displayed in the GUI for Commands achievements.
		commandsAchievementsItem = createItemStack(CommandAchievements.COMMANDS.toString());
		buildItemLore(commandsAchievementsItem, LangHelper.get(GuiLang.COMMANDS, langConfig),
				mainConfig.getShallowKeys(CommandAchievements.COMMANDS.toString()).size());
	}

	/**
	 * Inventory GUIs need a number of slots that is a multiple of 9. This simple function gets the smallest multiple of
	 * 9 greater than its input value, in order for the GUI to contain all of its elements with minimum empty space.
	 *
	 * @param value
	 * @param maxPerPage
	 * @return closest multiple of 9 greater than value
	 */
	int nextMultipleOf9(int value, int maxPerPage) {
		int multipleOfNine = 9;
		while (multipleOfNine < value && multipleOfNine <= maxPerPage) {
			multipleOfNine += 9;
		}
		return multipleOfNine;
	}

	/**
	 * Creates an ItemStack based on information extracted from gui.yml.
	 *
	 * @param categoryName
	 * @return the item for the category
	 */
	@SuppressWarnings("deprecation")
	ItemStack createItemStack(String categoryName) {
		String path = categoryName + ".Item";
		Material material = materialHelper.matchMaterial(guiConfig.getString(path), Material.BEDROCK,
				"gui.yml (" + path + ")");
		short metadata = (short) guiConfig.getInt(categoryName + ".Metadata", 0);
		return new ItemStack(material, 1, metadata);
	}

	/**
	 * Sets the metadata of an ItemStack representing a category in the main GUI.
	 *
	 * @param item
	 * @param displayName
	 * @param totalAchievements
	 */
	private void buildItemLore(ItemStack item, String displayName, int totalAchievements) {
		ItemMeta itemMeta = item.getItemMeta();
		// Construct title of the category item.
		if (StringUtils.isBlank(displayName)) {
			itemMeta.setDisplayName("");
		} else {
			itemMeta.setDisplayName(translateColorCodes(StringUtils.replaceEach(configListAchievementFormat,
					new String[] { "%ICON%", "%NAME%" }, new String[] { configIcon, "&l" + displayName + "&8" })));
		}

		// Construct lore of the category item.
		String amountMessage;
		if (totalAchievements > 1) {
			amountMessage = StringUtils.replaceOnce(langListAchievementsInCategoryPlural, "AMOUNT",
					Integer.toString(totalAchievements));
		} else {
			amountMessage = StringUtils.replaceOnce(langListAchievementInCategorySingular, "AMOUNT",
					Integer.toString(totalAchievements));
		}
		itemMeta.setLore(Arrays.asList(translateColorCodes("&8" + amountMessage)));
		item.setItemMeta(itemMeta);
	}

	String translateColorCodes(String translate) {
		return ChatColor.translateAlternateColorCodes('&', translate);
	}
}
