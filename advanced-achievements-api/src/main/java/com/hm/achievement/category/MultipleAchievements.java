package com.hm.achievement.category;

import java.util.HashMap;
import java.util.Map;

/**
 * List of multiple achievements, ie. with sub-categories
 *
 * @author Pyves
 */
public enum MultipleAchievements implements Category {

	PLACES("Places", "blockid"),
	BREAKS("Breaks", "blockid"),
	KILLS("Kills", "mobname"),
	TARGETSSHOT("TargetsShot", "targetname"),
	CRAFTS("Crafts", "item"),
	BREEDING("Breeding", "mobname"),
	PLAYERCOMMANDS("PlayerCommands", "command"),
	CUSTOM("Custom", "customname"),
	JOBSREBORN("JobsReborn", "jobname"),
	EFFECTSHELD("EffectsHeld", "effect");

	private static final Map<String, MultipleAchievements> CATEGORY_NAMES_TO_ENUM = new HashMap<>();
	static {
		for (MultipleAchievements category : MultipleAchievements.values()) {
			CATEGORY_NAMES_TO_ENUM.put(category.categoryName, category);
		}
	}

	private final String categoryName;
	private final String subcategoryDBName;
	private final String dbName;
	private final String permNamePrefix;
	private final String permName;

	MultipleAchievements(String categoryName, String subcategoryDBName) {
		this.categoryName = categoryName;
		this.subcategoryDBName = subcategoryDBName;
		this.dbName = name().toLowerCase();
		this.permNamePrefix = "achievement.count." + categoryName.toLowerCase() + '.';
		this.permName = permNamePrefix + '*';
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
		return dbName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toPermName() {
		return permName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toChildPermName(String child) {
		return permNamePrefix + child;
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
