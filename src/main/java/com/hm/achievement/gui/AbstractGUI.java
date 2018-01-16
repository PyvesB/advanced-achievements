package com.hm.achievement.gui;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.lang.GuiLang;
import com.hm.achievement.lang.Lang;
import com.hm.achievement.utils.Reloadable;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * Abstract class in charge of factoring out common functionality for the GUIs.
 *
 * @author Pyves
 */
public abstract class AbstractGUI implements Reloadable {

	protected final AdvancedAchievements plugin;
	protected final Map<MultipleAchievements, ItemStack> multipleAchievementItems;
	protected final Map<NormalAchievements, ItemStack> normalAchievementItems;
	protected ItemStack commandsAchievementsItem;

	private String configListAchievementFormat;
	private String configIcon;

	private String langListAchievementsInCategoryPlural;
	private String langListAchievementInCategorySingular;

	protected AbstractGUI(AdvancedAchievements plugin) {
		this.plugin = plugin;

		multipleAchievementItems = new EnumMap<>(MultipleAchievements.class);
		normalAchievementItems = new EnumMap<>(NormalAchievements.class);
	}

	@Override
	public void extractConfigurationParameters() {
		configListAchievementFormat = "&8"
				+ plugin.getPluginConfig().getString("ListAchievementFormat", "%ICON% %NAME% %ICON%");
		configIcon = StringEscapeUtils.unescapeJava(plugin.getPluginConfig().getString("Icon", "\u2618"));

		langListAchievementsInCategoryPlural = Lang.get(GuiLang.ACHIEVEMENTS_IN_CATEGORY_PLURAL, plugin);
		langListAchievementInCategorySingular = Lang.get(GuiLang.ACHIEVEMENTS_IN_CATEGORY_SINGULAR, plugin);

		// Prepare item stacks displayed in the GUI for Multiple achievements.
		for (MultipleAchievements category : MultipleAchievements.values()) {
			String categoryName = category.toString();
			// Sum all achievements in the sub-categories of this category.
			int totalAchievements = 0;
			for (String subcategory : plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
				totalAchievements += plugin.getPluginConfig().getConfigurationSection(categoryName + '.' + subcategory)
						.getKeys(false).size();
			}
			ItemStack itemStack = createItemStack(categoryName);
			buildItemLore(itemStack, Lang.get(category, plugin), totalAchievements);
			multipleAchievementItems.put(category, itemStack);
		}

		// Prepare item stacks displayed in the GUI for Normal achievements.
		for (NormalAchievements category : NormalAchievements.values()) {
			String categoryName = category.toString();
			ItemStack itemStack = createItemStack(categoryName);
			buildItemLore(itemStack, Lang.get(category, plugin),
					plugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false).size());
			normalAchievementItems.put(category, itemStack);
		}

		// Prepare item stack displayed in the GUI for Commands achievements.
		commandsAchievementsItem = createItemStack("Commands");
		buildItemLore(commandsAchievementsItem, Lang.get(GuiLang.COMMANDS, plugin),
				plugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false).size());
	}

	/**
	 * Inventory GUIs need a number of slots that is a multiple of 9. This simple function gets the smallest multiple of
	 * 9 greater than its input value, in order for the GUI to contain all of its elements with minimum empty space.
	 *
	 * @param value
	 * @param maxPerPage
	 * @return closest multiple of 9 greater than value
	 */
	protected int nextMultipleOf9(int value, int maxPerPage) {
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
	protected ItemStack createItemStack(String categoryName) {
		Material material = Material
				.getMaterial(plugin.getPluginGui().getString(categoryName + ".Item", "bedrock").toUpperCase());
		short metadata = (short) plugin.getPluginGui().getInt(categoryName + ".Metadata", 0);
		if (material == null) {
			material = Material.BEDROCK;
			plugin.getLogger().warning("GUI material for category " + categoryName + " was not found. "
					+ "Have you spelt the name correctly and is it available for your Minecraft version?");
		}
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
			itemMeta.setDisplayName(translateColorCodes(
					StringUtils.replaceEach(configListAchievementFormat, new String[]{"%ICON%", "%NAME%"},
							new String[]{configIcon, "&l" + displayName + "&8"})));
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

	protected String translateColorCodes(String translate) {
		return ChatColor.translateAlternateColorCodes('&', translate);
	}
}
