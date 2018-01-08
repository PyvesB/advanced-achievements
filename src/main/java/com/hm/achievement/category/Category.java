package com.hm.achievement.category;

import com.hm.achievement.lang.Lang;

/**
 * Interface for Achievement Category Enums.
 */
public interface Category extends Lang {

	/**
	 * Converts to database name: name of the enum in lower case.
	 *
	 * @return the name used for the database table
	 */
	String toDBName();

	/**
	 * Converts to permission name: common prefix + name of the category in lower case.
	 *
	 * @return the Bukkit permission name
	 */
	String toPermName();

	/**
	 * Converts to the comment that is inserted about the category name in the configuration file.
	 *
	 * @return the configuration comment
	 */
	String toConfigComment();
}
