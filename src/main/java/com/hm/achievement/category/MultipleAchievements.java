package com.hm.achievement.category;

/**
 * List of multiple achievements, ie. with sub-categories
 * 
 * @author Pyves
 *
 */
public enum MultipleAchievements {

	PLACES("Places"),
	BREAKS("Breaks"),
	KILLS("Kills"),
	CRAFTS("Crafts");

	private final String categoryName;

	private MultipleAchievements(String categoryName) {

		this.categoryName = categoryName;
	}

	@Override
	public String toString() {

		return categoryName;
	}

	/**
	 * Converts to database name: name of the enum in lower case.
	 * 
	 * @param type
	 * @return
	 */
	public String toDBName() {

		return name().toLowerCase();
	}

	/**
	 * Converts to permission name: common prefix + name of the category in lower case.
	 * 
	 * @param type
	 * @return
	 */
	public String toPermName() {

		return "achievement.count." + categoryName.toLowerCase();
	}
}
