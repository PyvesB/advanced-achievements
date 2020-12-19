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

	/**
	 * Converts to the comment that is inserted about the category name in the configuration file.
	 * 
	 * @deprecated category comments should be extracted from the default configuration files instead.
	 * @return the configuration comment
	 */
	@Deprecated
	default String toConfigComment() {
		return "";
	}

	/**
	 * Converts to the key in the language file.
	 *
	 * @deprecated this is effectively an internal implementation detail.
	 * @return the language configuration key
	 */
	@Deprecated
	default String toLangKey() {
		return "";
	}

	/**
	 * Converts to the default message that is returned, if key is not found in the configuration.
	 *
	 * @deprecated language default values should be extracted from the default configuration files instead.
	 * @return the language configuration default value
	 */
	@Deprecated
	default String toLangDefault() {
		return "";
	}
}
