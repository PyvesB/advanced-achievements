package com.hm.achievement.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.exception.PluginLoadError;
import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.LangHelper;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Class providing all the items displayed in the GUIs.
 * 
 * @author Pyves
 */
@Singleton
public class GUIItems implements Reloadable {

	private final Map<OrderedCategory, ItemStack> orderedAchievementItems = new TreeMap<>();

	// Various other item stacks displayed in the GUI.
	private ItemStack previousButton;
	private ItemStack nextButton;
	private ItemStack backButton;
	private ItemStack achievementNotStarted;
	private ItemStack achievementStarted;
	private ItemStack achievementLock;
	private ItemStack categoryLock;

	private ItemStack achievementReceived;

	private final CommentedYamlConfiguration mainConfig;
	private final CommentedYamlConfiguration langConfig;

	private final CommentedYamlConfiguration guiConfig;
	private final MaterialHelper materialHelper;

	private String configListAchievementFormat;
	private String configIcon;

	private String langListAchievementsInCategoryPlural;
	private String langListAchievementInCategorySingular;

	@Inject
	public GUIItems(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("lang") CommentedYamlConfiguration langConfig,
			@Named("gui") CommentedYamlConfiguration guiConfig, MaterialHelper materialHelper) {
		this.mainConfig = mainConfig;
		this.langConfig = langConfig;
		this.guiConfig = guiConfig;
		this.materialHelper = materialHelper;
	}

	@Override
	public void extractConfigurationParameters() throws PluginLoadError {
		configListAchievementFormat = "&8" + mainConfig.getString("ListAchievementFormat", "%ICON% %NAME% %ICON%");
		configIcon = StringEscapeUtils.unescapeJava(mainConfig.getString("Icon", "\u2618"));

		langListAchievementsInCategoryPlural = LangHelper.get(GuiLang.ACHIEVEMENTS_IN_CATEGORY_PLURAL, langConfig);
		langListAchievementInCategorySingular = LangHelper.get(GuiLang.ACHIEVEMENTS_IN_CATEGORY_SINGULAR, langConfig);

		orderedAchievementItems.clear();
		// getShallowKeys returns a LinkedHashSet, preserving the ordering specified in the file.
		List<String> orderedCategories = new ArrayList<>(guiConfig.getShallowKeys(""));
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
			orderedAchievementItems.put(new OrderedCategory(orderedCategories.indexOf(categoryName), category), itemStack);
		}

		// Prepare item stacks displayed in the GUI for Normal achievements.
		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();
			ItemStack itemStack = createItemStack(categoryName);
			buildItemLore(itemStack, LangHelper.get(category, langConfig), mainConfig.getShallowKeys(categoryName).size());
			orderedAchievementItems.put(new OrderedCategory(orderedCategories.indexOf(categoryName), category), itemStack);
		}

		// Prepare item stack displayed in the GUI for Commands achievements.
		ItemStack itemStack = createItemStack(CommandAchievements.COMMANDS.toString());
		buildItemLore(itemStack, LangHelper.get(CommandAchievements.COMMANDS, langConfig),
				mainConfig.getShallowKeys(CommandAchievements.COMMANDS.toString()).size());
		orderedAchievementItems.put(new OrderedCategory(orderedCategories.indexOf(CommandAchievements.COMMANDS.toString()),
				CommandAchievements.COMMANDS), itemStack);

		achievementNotStarted = createItemStack("AchievementNotStarted");
		achievementStarted = createItemStack("AchievementStarted");
		achievementReceived = createItemStack("AchievementReceived");
		previousButton = createButton("PreviousButton", GuiLang.PREVIOUS_MESSAGE, GuiLang.PREVIOUS_LORE);
		nextButton = createButton("NextButton", GuiLang.NEXT_MESSAGE, GuiLang.NEXT_LORE);
		backButton = createButton("BackButton", GuiLang.BACK_MESSAGE, GuiLang.BACK_LORE);
		achievementLock = createButton("AchievementLock", GuiLang.ACHIEVEMENT_NOT_UNLOCKED, null);
		categoryLock = createButton("CategoryLock", GuiLang.CATEGORY_NOT_UNLOCKED, null);
	}

	/**
	 * Creates an ItemStack based on information extracted from gui.yml.
	 *
	 * @param categoryName
	 * @return the item for the category
	 */
	@SuppressWarnings("deprecation")
	private ItemStack createItemStack(String categoryName) {
		String path = categoryName + ".Item";
		Material material = materialHelper.matchMaterial(guiConfig.getString(path), Material.BEDROCK,
				"gui.yml (" + path + ")");
		short metadata = (short) guiConfig.getInt(categoryName + ".Metadata");
		return new ItemStack(material, 1, metadata);
	}

	/**
	 * Creates an ItemStack used as a button in the category GUI.
	 * 
	 * @param category
	 * @param msg
	 * @param lore
	 * @return the item stack
	 */
	private ItemStack createButton(String category, GuiLang msg, GuiLang lore) {
		ItemStack button = createItemStack(category);
		ItemMeta meta = button.getItemMeta();
		String displayName = ChatColor.translateAlternateColorCodes('&',
				StringEscapeUtils.unescapeJava(LangHelper.get(msg, langConfig)));
		meta.setDisplayName(displayName);
		if (lore != null) {
			String loreString = ChatColor.translateAlternateColorCodes('&',
					StringEscapeUtils.unescapeJava(LangHelper.get(lore, langConfig)));
			if (!loreString.isEmpty()) {
				meta.setLore(Collections.singletonList(loreString));
			}
		}
		button.setItemMeta(meta);
		return button;
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
			String formattedDisplayName = StringUtils.replaceEach(configListAchievementFormat,
					new String[] { "%ICON%", "%NAME%" }, new String[] { configIcon, "&l" + displayName + "&8" });
			itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', formattedDisplayName));
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
		itemMeta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&', "&8" + amountMessage)));
		item.setItemMeta(itemMeta);
	}

	public Map<OrderedCategory, ItemStack> getOrderedAchievementItems() {
		return orderedAchievementItems;
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

	public ItemStack getAchievementNotStarted() {
		return achievementNotStarted;
	}

	public ItemStack getAchievementStarted() {
		return achievementStarted;
	}

	public ItemStack getAchievementReceived() {
		return achievementReceived;
	}

	public ItemStack getAchievementLock() {
		return achievementLock;
	}

	public ItemStack getCategoryLock() {
		return categoryLock;
	}

}
