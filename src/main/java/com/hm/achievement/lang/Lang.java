package com.hm.achievement.lang;

import com.hm.achievement.AdvancedAchievements;
import org.apache.commons.lang3.StringUtils;

/**
 * Interface for cleaner translation implementation.
 * <p>
 * Each Lang implementation should only use keys found in language file.
 * This means that custom language implementations should add the key used to the language file.
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
	 * @param lang   Lang implementation
	 * @param plugin AdvancedAchievements so that lang file can be accessed.
	 * @return value in lang file or default value in Lang impl.
	 */
	static String get(Lang lang, AdvancedAchievements plugin) {
		return plugin.getPluginLang().getString(lang.toLangKey(), lang.toLangDefault());
	}

	/**
	 * Get the value in language file (with key) or default if not found with chat header in front.
	 *
	 * @param lang   Lang implementation
	 * @param plugin AdvancedAchievements so that lang file can be accessed.
	 * @return chat header + value in lang file or default value in Lang impl.
	 */
	static String getWithChatHeader(Lang lang, AdvancedAchievements plugin) {
		return plugin.getChatHeader() + get(lang, plugin);
	}

	/**
	 * Get the value in language file (with key) or default if not found, with a single instance of a String replaced.
	 *
	 * @param lang    Lang implementation
	 * @param replace Replace a string in return value once
	 * @param with    Replace with this string
	 * @param plugin  AdvancedAchievements so that lang file can be accessed.
	 * @return value in lang file or default value in Lang impl.
	 */
	static String getReplacedOnce(Lang lang, String replace, String with, AdvancedAchievements plugin) {
		return StringUtils.replaceOnce(get(lang, plugin), replace, with);
	}

	/**
	 * Get the value in language file (with key) or default if not found, with instances of replace replaced.
	 *
	 * @param lang    Lang implementation
	 * @param replace Replace strings in return value once
	 * @param with    Replace with these string
	 * @param plugin  AdvancedAchievements so that lang file can be accessed.
	 * @return value in lang file or default value in Lang impl.
	 */
	static String getEachReplaced(Lang lang, AdvancedAchievements plugin, String[] replace, String[] with) {
		return StringUtils.replaceEach(get(lang, plugin), replace, with);
	}

	/**
	 * Used to turn enum names into lang.yml keys.
	 * <p>
	 * Example:
	 * ENUM_VALUE_EXAMPLE -> enum-value-example
	 *
	 * @param enumName name returned by Enum#name
	 * @return config key
	 */
	static String toPath(String enumName) {
		return StringUtils.replace(enumName.toLowerCase(), "_", "-");
	}
}
