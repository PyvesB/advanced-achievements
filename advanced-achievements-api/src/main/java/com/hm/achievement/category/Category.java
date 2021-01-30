package com.hm.achievement.category;

/**
 * Interface for Achievement Category Enums.
 */
public interface Category {

	/**
	 * Converts to database name: name of the enum in lower case.
	 *
	 * @return the name used for the database table
	 */
	String toDBName();

	/**
	 * Converts to permission name.
	 *
	 * @return the Bukkit permission name
	 */
	String toPermName();

	/**
	 * Converts to a child's permission name if this category supports children, else returns {@link #toPermName()}.
	 * 
	 * @param child the child node
	 *
	 * @return the Bukkit child permission name
	 */
	default String toChildPermName(String child) {
		return toPermName();
	}
}
