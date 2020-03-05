package com.hm.achievement.advancement;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.advancement.AchievementAdvancement.AchievementAdvancementBuilder;
import com.hm.achievement.category.Category;
import com.hm.achievement.category.CommandAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import com.hm.achievement.lifecycle.Reloadable;
import com.hm.achievement.utils.MaterialHelper;
import com.hm.achievement.utils.StringHelper;
import com.hm.mcshared.file.CommentedYamlConfiguration;
import com.hm.mcshared.particle.ReflectionUtils.PackageType;

/**
 * Class in charge of registering achievements as advancements for servers running on Minecraft 1.12+.
 * 
 * @author Pyves
 */
@SuppressWarnings("deprecation")
@Singleton
public class AdvancementManager implements Reloadable {

	public static final String ADVANCED_ACHIEVEMENTS_PARENT = "advanced_achievements_parent";
	private static final String MINECRAFT_BOOK_KEY = "minecraft:book";
	// Pattern to produce keys for advancements.
	private static final Pattern REGEX_PATTERN_KEYS = Pattern.compile("[^A-Za-z0-9_]");
	// Strings related to Reflection.
	private static final String PACKAGE_INVENTORY = "inventory";
	private static final String CLASS_CRAFT_ITEM_STACK = "CraftItemStack";
	private static final String CLASS_ITEM = "Item";
	private static final String CLASS_ITEM_STACK = "ItemStack";
	private static final String CLASS_REGISTRY_MATERIALS = "RegistryMaterials";
	private static final String CLASS_MINECRAFT_KEY = "MinecraftKey";
	private static final String FIELD_REGISTRY = "REGISTRY";
	private static final String METHOD_AS_NMS_COPY = "asNMSCopy";
	private static final String METHOD_GET_ITEM = "getItem";
	private static final String METHOD_GET_KEY = "getKey";
	private static final String METHOD_B = "b";

	private final CommentedYamlConfiguration mainConfig;
	private final CommentedYamlConfiguration guiConfig;
	private final AdvancedAchievements advancedAchievements;
	private final Logger logger;
	private final Map<String, List<Long>> sortedThresholds;
	private final Set<Category> disabledCategories;
	private final MaterialHelper materialHelper;
	private final int serverVersion;
	private final UnsafeValues unsafeValues;

	private boolean configRegisterAdvancementDescriptions;
	private boolean configHideAdvancements;
	private String configRootAdvancementTitle;
	private String configBackgroundTexture;
	private int generatedAdvancements;

	@Inject
	public AdvancementManager(@Named("main") CommentedYamlConfiguration mainConfig,
			@Named("gui") CommentedYamlConfiguration guiConfig, AdvancedAchievements advancedAchievements, Logger logger,
			Map<String, List<Long>> sortedThresholds, Set<Category> disabledCategories, MaterialHelper materialHelper,
			int serverVersion) {
		this.mainConfig = mainConfig;
		this.guiConfig = guiConfig;
		this.advancedAchievements = advancedAchievements;
		this.logger = logger;
		this.sortedThresholds = sortedThresholds;
		this.disabledCategories = disabledCategories;
		this.materialHelper = materialHelper;
		this.serverVersion = serverVersion;
		unsafeValues = Bukkit.getUnsafe();
	}

	@Override
	public void extractConfigurationParameters() {
		configRegisterAdvancementDescriptions = mainConfig.getBoolean("RegisterAdvancementDescriptions", true);
		configHideAdvancements = mainConfig.getBoolean("HideAdvancements");
		configRootAdvancementTitle = mainConfig.getString("RootAdvancementTitle", "Advanced Achievements");
		configBackgroundTexture = parseBackgroundTexture();
	}

	public static String getKey(String achName) {
		return REGEX_PATTERN_KEYS.matcher(achName).replaceAll("").toLowerCase();
	}

	/**
	 * Registers all achievements as advancements.
	 */
	public void registerAdvancements() {
		cleanupOldAchievementAdvancements();
		registerParentAdvancement();
		registerOtherAdvancements();
	}

	/**
	 * Parses the background texture to insure maximum compatibility across Minecraft versions.
	 * 
	 * @return the background texture path
	 */
	private String parseBackgroundTexture() {
		String configTexture = mainConfig.getString("AdvancementsBackground", "minecraft:textures/item/book.png");
		if (serverVersion == 12) {
			return StringUtils.replace(configTexture, "/item/", "/items/");
		}
		return StringUtils.replaceEach(configTexture, new String[] { "/items/", "book_enchanted.png" },
				new String[] { "/item/", "enchanted_book.png" });
	}

	/**
	 * Removes all advancements previously generated by the plugin.
	 */
	private void cleanupOldAchievementAdvancements() {
		int achievementsCleaned = 0;
		Iterator<Advancement> advancements = Bukkit.advancementIterator();
		while (advancements.hasNext()) {
			NamespacedKey namespacedKey = advancements.next().getKey();
			if ("advancedachievements".equals(namespacedKey.getNamespace())) {
				++achievementsCleaned;
				unsafeValues.removeAdvancement(namespacedKey);
			}
		}
		Bukkit.reloadData();
		logger.info("Cleaned " + achievementsCleaned + " old advancements.");
	}

	/**
	 * Registers an "Advanced Achievements" advancement, which will be used as the parent of all advancements generated
	 * by Advanced Achievements.
	 */
	private void registerParentAdvancement() {
		NamespacedKey namespacedKey = new NamespacedKey(advancedAchievements, ADVANCED_ACHIEVEMENTS_PARENT);
		if (Bukkit.getAdvancement(namespacedKey) == null) {
			if (configHideAdvancements) {
				unsafeValues.loadAdvancement(namespacedKey, AdvancementJsonHelper.toHiddenJson(configBackgroundTexture));
			} else {
				AchievementAdvancementBuilder builder = new AchievementAdvancementBuilder()
						.iconItem(MINECRAFT_BOOK_KEY)
						.title(configRootAdvancementTitle)
						.description("")
						.background(configBackgroundTexture)
						.type(AdvancementType.GOAL);

				AchievementAdvancement aa = (serverVersion == 12 ? builder.iconData(Integer.toString(0)) : builder).build();
				unsafeValues.loadAdvancement(namespacedKey, AdvancementJsonHelper.toJson(aa));
			}
		}
	}

	/**
	 * Registers all non parent advancements.
	 */
	private void registerOtherAdvancements() {
		generatedAdvancements = 1; // Already generated 1 for parent.
		if (!disabledCategories.contains(CommandAchievements.COMMANDS)) {
			String parentKey = ADVANCED_ACHIEVEMENTS_PARENT;
			for (String ach : mainConfig.getShallowKeys(CommandAchievements.COMMANDS.toString())) {
				parentKey = registerAdvancement(CommandAchievements.COMMANDS, CommandAchievements.COMMANDS + "." + ach,
						parentKey, true);
			}
		}

		for (NormalAchievements category : NormalAchievements.values()) {
			registerCategoryAdvancements(category, "");
		}

		for (MultipleAchievements category : MultipleAchievements.values()) {
			for (String section : mainConfig.getShallowKeys(category.toString())) {
				registerCategoryAdvancements(category, "." + section);
			}
		}
		Bukkit.reloadData();
		logger.info("Generated " + generatedAdvancements + " new advancements.");
	}

	/**
	 * Registers all advancements for a given category or subcategory.
	 * 
	 * @param category
	 * @param subcategory
	 */
	private void registerCategoryAdvancements(Category category, String subcategory) {
		if (disabledCategories.contains(category)) {
			// Ignore this type.
			return;
		}

		List<Long> orderedThresholds = subcategory.isEmpty() ? sortedThresholds.get(category.toString())
				: sortedThresholds.get(category + subcategory);
		String parentKey = ADVANCED_ACHIEVEMENTS_PARENT;
		// Advancements are registered as a branch with increasing threshold values.
		for (int i = 0; i < orderedThresholds.size(); ++i) {
			boolean last = (i == orderedThresholds.size() - 1);
			parentKey = registerAdvancement(category, category + subcategory + "." + orderedThresholds.get(i), parentKey,
					last);
		}
	}

	/**
	 * Registers an individual advancement.
	 * 
	 * @param category
	 * @param configAchievement
	 * @param parentKey
	 * @param lastAchievement
	 * @return the key of the registered achievement
	 */
	private String registerAdvancement(Category category, String configAchievement, String parentKey,
			boolean lastAchievement) {
		String achName = mainConfig.getString(configAchievement + ".Name", "");
		String achDisplayName = mainConfig.getString(configAchievement + ".DisplayName", "");
		if (StringUtils.isEmpty(achDisplayName)) {
			achDisplayName = achName;
		}
		// Strip colours as the advancements interface does not support them.
		achDisplayName = StringHelper.removeFormattingCodes(achDisplayName);

		String achKey = getKey(achName);
		NamespacedKey namespacedKey = new NamespacedKey(advancedAchievements, achKey);
		int metadata = guiConfig.getInt(category + ".Metadata");
		String description = "";
		if (configRegisterAdvancementDescriptions) {
			// Give priority to the goal to stick with Vanilla naming of advancements. Advancement descriptions do not
			// support multiline goals.
			description = StringUtils.replace(mainConfig.getString(configAchievement + ".Goal", ""), "\\n", " ");
			if (!StringUtils.isNotBlank(description)) {
				description = mainConfig.getString(configAchievement + ".Message", "");
			}
			description = StringHelper.removeFormattingCodes(description);
		}

		String path = category + ".Item";
		Material material = materialHelper.matchMaterial(guiConfig.getString(path), Material.BOOK, "gui.yml (" + path + ")");
		String icon = serverVersion == 12 ? getInternalName(new ItemStack(material, 1, (short) metadata))
				: material.name().toLowerCase();
		AchievementAdvancementBuilder builder = new AchievementAdvancementBuilder()
				.iconItem(icon)
				.title(achDisplayName)
				.description(description)
				.parent("advancedachievements:" + parentKey)
				.type(lastAchievement ? AdvancementType.CHALLENGE : AdvancementType.TASK);

		AchievementAdvancement aa = (serverVersion == 12 ? builder.iconData(Integer.toString(metadata)) : builder).build();
		unsafeValues.loadAdvancement(namespacedKey, AdvancementJsonHelper.toJson(aa));
		++generatedAdvancements;
		return achKey;
	}

	/**
	 * Gets the internal item used by Vanilla Minecraft. These are the only names supported by advancements. Material
	 * and internal names can differ quite significantly (for instance: book_and_quill vs. writable_book).
	 * 
	 * @param item
	 * @return the internal Minecraft name, prefixed with "minecraft:"
	 */
	private String getInternalName(ItemStack item) {
		try {
			Object nmsItemStack = PackageType.CRAFTBUKKIT.getClass(PACKAGE_INVENTORY + "." + CLASS_CRAFT_ITEM_STACK)
					.getMethod(METHOD_AS_NMS_COPY, ItemStack.class).invoke(null, item);
			Object nmsItem = PackageType.MINECRAFT_SERVER.getClass(CLASS_ITEM_STACK).getMethod(METHOD_GET_ITEM)
					.invoke(nmsItemStack);
			Object registry = PackageType.MINECRAFT_SERVER.getClass(CLASS_ITEM).getField(FIELD_REGISTRY).get(null);
			Object minecraftKey = PackageType.MINECRAFT_SERVER.getClass(CLASS_REGISTRY_MATERIALS)
					.getMethod(METHOD_B, Object.class).invoke(registry, nmsItem);
			return "minecraft:" + PackageType.MINECRAFT_SERVER.getClass(CLASS_MINECRAFT_KEY).getMethod(METHOD_GET_KEY)
					.invoke(minecraftKey);
		} catch (Exception e) {
			logger.warning("Failed to get internal " + item.getType().name().toLowerCase() + " name for advancement icon."
					+ "Using book instead.");
			return MINECRAFT_BOOK_KEY;
		}
	}

}
