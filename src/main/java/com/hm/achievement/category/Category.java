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
	 * Converts to permission name: common prefix + name of the category in lower case.
	 *
	 * @return the Bukkit permission name
	 */
	String toPermName();

	/**
	 * Converts to the key in the language file used in the list command.
	 *
	 * @return the language configuration key
	 */
	String toLangName();

	/**
	 * Converts to the default name that appears in the list command, if not found in the configuration.
	 *
	 * @return the language configuration default value
	 */
	String toLangDefault();

	/**
	 * Converts to the comment that is inserted about the category name in the configuration file.
	 *
	 * @return the configuration comment
	 */
	String toConfigComment();
}
