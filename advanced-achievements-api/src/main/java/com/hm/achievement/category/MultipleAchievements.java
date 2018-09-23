package com.hm.achievement.category;

import java.util.HashMap;
import java.util.Map;

/**
 * List of multiple achievements, ie. with sub-categories
 *
 * @author Pyves
 */
public enum MultipleAchievements implements Category {

	PLACES("Places", "Blocks Placed", "When a specific block is placed (specify material name, lower case).", "blockid"),
	BREAKS("Breaks", "Blocks Broken", "When a specific block is broken (specify material name, lower case).", "blockid"),
	KILLS("Kills", "Entities Killed", "When a specific mob is killed (specify an entity name or poweredcreeper or player, lower case).", "mobname"),
	CRAFTS("Crafts", "Items Crafted", "When a specific item is crafted (specify material name, lower case).", "item"),
	BREEDING("Breeding", "Animals Bred", "When animals breed (specify an entity name, lower case).", "mobname"),
	PLAYERCOMMANDS("PlayerCommands", "Commands Entered", "When a player enters a specific command (specify command prefixes in lower case without spaces).", "command"),
	CUSTOM("Custom", "Custom Categories", "When the command /aach add is called for this category.", "customname");

	private static final Map<String, MultipleAchievements> CATEGORY_NAMES_TO_ENUM = new HashMap<>();
	static {
		for (MultipleAchievements category : MultipleAchievements.values()) {
			CATEGORY_NAMES_TO_ENUM.put(category.categoryName, category);
		}
	}

	private final String categoryName;
	private final String langDefault;
	private final String configComment;
	private final String subcategoryDBName;

	MultipleAchievements(String categoryName, String langDefault, String configComment, String subcategoryName) {
		this.categoryName = categoryName;
		this.langDefault = langDefault;
		this.configComment = configComment;
		subcategoryDBName = subcategoryName;
	}

	/**
	 * Finds the category matching the provided name.
	 * 
	 * @param categoryName
	 * @return a category or null if not found
	 */
	public static MultipleAchievements getByName(String categoryName) {
		return CATEGORY_NAMES_TO_ENUM.get(categoryName);
	}

	@Override
	public String toString() {
		return categoryName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toDBName() {
		return name().toLowerCase();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toPermName() {
		return "achievement.count." + categoryName.toLowerCase();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toLangKey() {
		return "list-" + name().toLowerCase();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toLangDefault() {
		return langDefault;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toConfigComment() {
		return configComment;
	}

	/**
	 * Converts to the name of the column name containing the subcategory information in the database.
	 *
	 * @return the name used for the database column
	 */
	public String toSubcategoryDBName() {
		return subcategoryDBName;
	}
}
