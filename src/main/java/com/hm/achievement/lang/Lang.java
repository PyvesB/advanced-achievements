package com.hm.achievement.lang;

import org.apache.commons.lang3.StringUtils;

import com.hm.mcshared.file.CommentedYamlConfiguration;

/**
 * Interface for cleaner translation implementation.
 * <p>
 * Each Lang implementation should only use keys found in language file. This means that custom language implementations
 * should add the key used to the language file.
 *
 * @author Rsl1122
 */
public interface Lang {

	/**
	 * Converts to the key in the language file.
	 *
	 * @return the language configuration key
	 */
	String toLangKey();

	/**
	 * Converts to the default message that is returned, if key is not found in the configuration.
	 *
	 * @return the language configuration default value
	 */
	String toLangDefault();

	/**
	 * Get the value in language file (with key) or default if not found.
	 *
	 * @param lang Lang implementation
	 * @param langConfig Language file configuration
	 * @return value in lang file or default value in Lang impl.
	 */
	static String get(Lang lang, CommentedYamlConfiguration langConfig) {
		return langConfig.getString(lang.toLangKey(), lang.toLangDefault());
	}

	/**
	 * Get the value in language file (with key) or default if not found, with a single instance of a String replaced.
	 *
	 * @param lang Lang implementation
	 * @param replace Replace a string in return value once
	 * @param with Replace with this string
	 * @param langConfig Language file configuration
	 * @return value in lang file or default value in Lang impl.
	 */
	static String getReplacedOnce(Lang lang, String replace, String with, CommentedYamlConfiguration langConfig) {
		return StringUtils.replaceOnce(get(lang, langConfig), replace, with);
	}

	/**
	 * Get the value in language file (with key) or default if not found, with instances of replace replaced.
	 *
	 * @param lang Lang implementation
	 * @param replace Replace strings in return value once
	 * @param with Replace with these string
	 * @param langConfig Language file configuration
	 * @return value in lang file or default value in Lang impl.
	 */
	static String getEachReplaced(Lang lang, CommentedYamlConfiguration langConfig, String[] replace, String[] with) {
		return StringUtils.replaceEach(get(lang, langConfig), replace, with);
	}

	/**
	 * Used to turn enum names into lang.yml keys.
	 * <p>
	 * Example: ENUM_VALUE_EXAMPLE -> enum-value-example
	 *
	 * @param enumName name returned by Enum#name
	 * @return config key
	 */
	static String toPath(String enumName) {
		return StringUtils.replace(enumName.toLowerCase(), "_", "-");
	}
}
